package site.metacoding.white.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.metacoding.white.config.auth.JwtAuthenticationFilter;
import site.metacoding.white.config.auth.JwtAuthorizationFilter;
import site.metacoding.white.domain.UserRepository;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final UserRepository userRepository;

    @Bean // ioc컨테이너에 등록하기 위해서 실행시키기 << bean이 있으면 강제로 메서드를 실행시킴
    // 로그인 인증 필터
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegister() {
        log.debug("디버그 : 인증 필터 등록");
        FilterRegistrationBean<JwtAuthenticationFilter> bean = new FilterRegistrationBean<>(
                new JwtAuthenticationFilter(userRepository));
        bean.addUrlPatterns("/login"); // 이 주소에서만 실행됨
        bean.setOrder(1); // 낮은 순서부터 실행됨
        return bean;
    }

    @Bean // ioc컨테이너에 등록하기 위해서 실행시키기 << bean이 있으면 강제로 메서드를 실행시킴
    // 인가 필터
    public FilterRegistrationBean<JwtAuthorizationFilter> jwtAuthorizationRegister() {
        log.debug("디버그 : 인가 필터 등록");
        FilterRegistrationBean<JwtAuthorizationFilter> bean = new FilterRegistrationBean<>(
                new JwtAuthorizationFilter());
        bean.addUrlPatterns("/s/*"); // 이 주소에서 실행됨, 원래 **인데 얘만 *
        bean.setOrder(2); // 제일 마지막 순서의 필터가 실행되면 바로 디스패처 서블릿으로 감
        return bean;
    }
}