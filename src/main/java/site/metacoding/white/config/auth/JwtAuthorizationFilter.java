package site.metacoding.white.config.auth;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.metacoding.white.domain.User;
import site.metacoding.white.domain.UserRepository;
import site.metacoding.white.dto.ResponseDto;
import site.metacoding.white.dto.SessionUser;

// /board (post) - 인증
// /board/{id} (put) - 인증, 권한 체크
// /board/{id} (delete) - 인증, 권한 체크
// /comment (post) - 인증
// /comment/{id} (delete) - 인증, 권한 체크

// 인증이 필요한 모든 곳
@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // ServletRequest -> httpRequest로 다운캐스팅
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 헤더 Authorization 키 값에 Bearer로 적힌 값이 있는지 체크
        String jwtToken = req.getHeader("Authorization");
        log.debug("디버그 : " + jwtToken);
        if (jwtToken == null) {
            customResponse("Jwt토큰이 없어서 인가할 수 없습니다.", resp);
            return;
        }

        // 토큰 검증 (토큰은 공백이 생기면 다른 토큰이 된다)
        jwtToken = jwtToken.replace("Bearer ", ""); // Bearer << 다음의 공백 지키기
        jwtToken = jwtToken.trim(); // 앞 뒤 공백 삭제
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512("뺑소니")).build().verify(jwtToken);
            Long userId = decodedJWT.getClaim("id").asLong();
            String username = decodedJWT.getClaim("username").asString();
            SessionUser sessionUser = new SessionUser(User.builder().id(userId).username(username).build());
            HttpSession session = req.getSession();
            session.setAttribute("sessionUser", sessionUser);
        } catch (Exception e) {
            customResponse("토큰 검증 실패", resp);
        }

        // 디스패처 서블릿으로 입장 혹은 Filter체인 타기
        chain.doFilter(req, resp);
    }

    private void customResponse(String msg, HttpServletResponse resp) throws IOException, JsonProcessingException {
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();
        resp.setStatus(400);
        ResponseDto<?> responseDto = new ResponseDto<>(-1, msg, null);
        ObjectMapper om = new ObjectMapper();
        String body = om.writeValueAsString(responseDto);
        out.println(body);
        out.flush();
    }

}