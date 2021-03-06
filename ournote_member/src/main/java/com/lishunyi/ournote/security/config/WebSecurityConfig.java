package com.lishunyi.ournote.security.config;

import com.lishunyi.ournote.exception.SecurityAccessDeniedHandler;
import com.lishunyi.ournote.exception.SecurityAuthenticationEntryPoint;
import com.lishunyi.ournote.member.service.MemberDetailsService;
import com.lishunyi.ournote.security.handler.TokenAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author 李顺仪
 * @version 1.0
 * @since 2020/9/11 13:17
 **/
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(IgnoreConfig.class)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private IgnoreConfig ignoreConfig;

    @Autowired
    private SecurityAccessDeniedHandler securityAccessDeniedHandler;

    @Autowired
    private SecurityAuthenticationEntryPoint securityAuthenticationEntryPoint;

    @Autowired
    private TokenAuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MemberDetailsService memberDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(memberDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().successHandler(authenticationSuccessHandler).and()
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                // 自定义异常处理
                .exceptionHandling().accessDeniedHandler(securityAccessDeniedHandler).authenticationEntryPoint(securityAuthenticationEntryPoint);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        WebSecurity and = web.ignoring().and();
        and.ignoring().antMatchers("/swagger-resources/**")
                .antMatchers("/v2/**")
                .antMatchers("/doc.html")
                .antMatchers("/webjars/**")
                .antMatchers("classpath*:/META-INF/resources/")
                .antMatchers("classpath*:/META-INF/resources/webjars/")
                .antMatchers("/favicon.ico");
        ignoreConfig.getGet().forEach(url -> and.ignoring().antMatchers(HttpMethod.GET, url));
        ignoreConfig.getPost().forEach(url -> and.ignoring().antMatchers(HttpMethod.POST, url));
        ignoreConfig.getDelete().forEach(url -> and.ignoring().antMatchers(HttpMethod.DELETE, url));
        ignoreConfig.getPut().forEach(url -> and.ignoring().antMatchers(HttpMethod.PUT, url));
        ignoreConfig.getHead().forEach(url -> and.ignoring().antMatchers(HttpMethod.HEAD, url));
        ignoreConfig.getPatch().forEach(url -> and.ignoring().antMatchers(HttpMethod.PATCH, url));
        ignoreConfig.getOptions().forEach(url -> and.ignoring().antMatchers(HttpMethod.OPTIONS, url));
        ignoreConfig.getTrace().forEach(url -> and.ignoring().antMatchers(HttpMethod.TRACE, url));
        ignoreConfig.getPattern().forEach(url -> and.ignoring().antMatchers(url));
    }
}
