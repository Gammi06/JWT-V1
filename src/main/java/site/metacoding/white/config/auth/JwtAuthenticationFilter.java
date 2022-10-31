package site.metacoding.white.config.auth;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.metacoding.white.domain.User;
import site.metacoding.white.domain.UserRepository;
import site.metacoding.white.dto.ResponseDto;
import site.metacoding.white.dto.SessionUser;
import site.metacoding.white.dto.UserReqDto.LoginReqDto;
import site.metacoding.white.util.SHA256;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements Filter {

    private final UserRepository userRepository; // DI(FilterConfig 주입받음)

    // /login 요청시
    // post 요청시
    // username, password(json)
    // db 확인
    // 토큰 생성
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // ServletRequest -> httpRequest로 다운캐스팅
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // post요청이 아닌 것을 거부함
        if (!req.getMethod().equals("POST")) {
            customResponse("로그인시에는 post요청을 해야 합니다.", resp);
            return;
        }

        // Body 값 받기
        ObjectMapper om = new ObjectMapper();
        LoginReqDto loginReqDto = om.readValue(req.getInputStream(), LoginReqDto.class);
        log.debug("디버그 : " + loginReqDto.getUsername());
        log.debug("디버그 : " + loginReqDto.getPassword());

        // DB에 이 유저가 존재하는지 부터 먼저 확인함
        Optional<User> userOP = userRepository.findByUsername(loginReqDto.getUsername());
        User userPS = userOP.get();
        if (userPS == null) {
            customResponse("ID가 다릅니다.", resp);
            return;
        }

        // 패스워드 체크함
        SHA256 sh = new SHA256();
        String encPassword = sh.encrypt(loginReqDto.getPassword());
        if (!userPS.getPassword().equals(encPassword)) {
            customResponse("비밀번호가 다릅니다.", resp);
            return;
        }

        // JWT 토큰 만들기

        // 1초 = 1/1000
        Date expire = new Date(System.currentTimeMillis() + (1000 * 60 * 60));

        String jwtToken = JWT.create()
                .withSubject("metacoding") // 토큰의 이름
                .withExpiresAt(expire) // 만료시간
                .withClaim("id", userPS.getId()) // 페이로드(바디)에 들어가는거 (복사가능)
                .withClaim("username", userPS.getUsername()) // 페이로드(바디)에 들어가는거 (복사가능)
                .sign(Algorithm.HMAC512("뺑소니")); // 어떤 알고리즘을 사용하였는지

        log.debug("디버그 : " + jwtToken);

        // JWT 응답하기
        customJwtResponse(jwtToken, userPS, resp);

        // chain.doFilter(req, res); // 필터를 탔다가 끝나면 디스패처 서블릿에 전달됨 (미등록시 통신 끝나버림)
    }

    private void customJwtResponse(String token, User userPS, HttpServletResponse resp)
            throws IOException, JsonProcessingException {
        resp.setContentType("application/json; charset=utf-8");
        resp.setHeader("Authorization", "Bearer " + token);
        PrintWriter out = resp.getWriter();
        resp.setStatus(200);
        ResponseDto<?> responseDto = new ResponseDto<>(1, "로그인 성공", new SessionUser(userPS));
        ObjectMapper om = new ObjectMapper();
        String body = om.writeValueAsString(responseDto);
        out.println(body);
        out.flush();
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
