package ru.basil.llm.llmview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Конфигурация Spring Security для страницы авторизации /authz.
 * Учётные данные берутся из application.properties: authz.login / authz.pwd.
 */
@Configuration
public class SecurityConfig {

    @Value("${authz.login}")
    private String login;

    @Value("${authz.pwd}")
    private String pwd;

    /**
     * Пользователь в памяти: логин/пароль из authz.login / authz.pwd.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername(login)
                        .password(pwd)
                        .roles("USER")
                        .build());
    }

    /**
     * Пароли сравниваются как есть (без хеширования), чтобы значение
     * authz.pwd из application.properties совпадало с введённым на форме.
     */
    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * Цепочка фильтров безопасности:
     * - /main и /index доступны только авторизованным;
     * - /authz доступна всем (это страница входа);
     * - при попытке открыть защищённую страницу по прямой ссылке без
     *   авторизации возвращается ошибка 403 Forbidden;
     * - после успешной авторизации открывается страница /main;
     * - при неудачной авторизации возврат на /authz с флагом ошибки
     *   (показывается всплывающее окно «авторизация не удалась»);
     * - выход по кнопке logout завершает сессию и ведёт обратно на /authz.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/authz", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/main", "/index").authenticated()
                        .anyRequest().authenticated())
                // вместо редиректа на страницу входа — отдаём 403 Forbidden
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                        .accessDeniedHandler((request, response, deniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")))
                .formLogin(form -> form
                        .loginPage("/authz")
                        .loginProcessingUrl("/authz")
                        .successHandler((request, response, authentication) ->
                                response.sendRedirect(request.getContextPath() + "/main"))
                        .failureHandler((request, response, exception) ->
                                response.sendRedirect(request.getContextPath() + "/authz?error"))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/authz?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
