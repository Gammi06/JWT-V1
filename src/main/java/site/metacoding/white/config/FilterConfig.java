package site.metacoding.white.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FilterConfig {

    @Bean // ioc컨테이너에 등록하기 위해서 실행시키기
    // 로그인 인증 필터
    public FilterRegistrationBean jwtAuthenticationFilter() {
        log.debug("디버그: 인증 필터 등록");
        FilterRegistrationBean<HelloFilter> bean = new FilterRegistrationBean<>(new HelloFilter());
        bean.addUrlPatterns("/hello"); // 이 주소에서 실행됨
        return bean;
    }
}

@Slf4j
// 톰캣 영역에서 실행됨
class HelloFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (req.getMethod().equals("POST")) {
            log.debug("디버그 : HelloFilter 실행됨");
        } else {
            log.debug("디버그 : POST요청이 아니어서 실핼할 수 없습니다.");
        }

        log.debug("디버그 : HelloFilter 실행됨");
        // chain.doFilter(req, res); // 필터를 탔다가 끝나면 디스패처 서블릿에 전달됨 (미등록시 통신 끝나버림)
    }

}
