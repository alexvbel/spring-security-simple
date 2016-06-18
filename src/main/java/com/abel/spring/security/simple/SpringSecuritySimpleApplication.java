package com.abel.spring.security.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@SpringBootApplication
public class SpringSecuritySimpleApplication {

    @Bean
    public UserDetailsService userDetailsService(JdbcTemplate jdbcTemplate) {
        RowMapper<User> userRowMapper = (ResultSet rs, int i) ->
                new User(rs.getString("ACCOUNT_NAME"), rs.getString("PASSWORD"),
                        rs.getBoolean("ENABLED"),
                        rs.getBoolean("ENABLED"),
                        rs.getBoolean("ENABLED"),
                        rs.getBoolean("ENABLED"),
                        AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")
                );
        return username -> jdbcTemplate.queryForObject("select * from account where account_name = ?", userRowMapper, username);
    }

    @Configuration
    @EnableGlobalAuthentication
    @Profile("digest")
    static class DigestWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        protected UserDetailsService userDetailsService;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.exceptionHandling().authenticationEntryPoint(digestAuthenticationEntryPoint()).and().authorizeRequests().anyRequest().authenticated().and()
                    .addFilterAfter(digestAuthenticationFilter(), BasicAuthenticationFilter.class).userDetailsService(userDetailsService);
        }

        @Bean
        public DigestAuthenticationFilter digestAuthenticationFilter() {
            DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
            digestAuthenticationFilter.setUserDetailsService(userDetailsService);
            digestAuthenticationFilter.setAuthenticationEntryPoint(digestAuthenticationEntryPoint());
            return digestAuthenticationFilter;
        }

        @Bean
        public DigestAuthenticationEntryPoint digestAuthenticationEntryPoint() {
            DigestAuthenticationEntryPoint digestAuthenticationEntryPoint = new DigestAuthenticationEntryPoint();
            digestAuthenticationEntryPoint.setRealmName("realName");
            digestAuthenticationEntryPoint.setKey("acegi");
            return digestAuthenticationEntryPoint;
        }

    }

    @Configuration
    @EnableGlobalAuthentication
    @Profile("basic-digest")
    static class BasicDigestWebSecurityConfigurerAdapter extends DigestWebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().authenticated().and().httpBasic().authenticationEntryPoint(digestAuthenticationEntryPoint())
                    .and()
                    .addFilterAfter(digestAuthenticationFilter(), BasicAuthenticationFilter.class)
                    .userDetailsService(userDetailsService);
        }

    }

    @Configuration
    @EnableGlobalAuthentication
    @Profile("basic")
    static class WebSecurityConfigurerAdapterConfiguration extends WebSecurityConfigurerAdapter {

        @Autowired
        private UserDetailsService userDetailsService;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().authenticated().and().httpBasic()
                    .and().userDetailsService(userDetailsService);
        }
    }

    @Configuration
    @EnableGlobalAuthentication
    @Profile("default")
    static class UserDetailsServiceGlobalAuthManagerConfig extends GlobalAuthenticationConfigurerAdapter {

        @Autowired
        private UserDetailsService userDetailsService;

        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userDetailsService);
        }

    }

    @RequestMapping("/hi")
    public Map<String, Object> hello() {
        Map<String, Object> answer = new HashMap<>();
        answer.put("id", UUID.randomUUID().toString());
        answer.put("content", "Hi, there!");
        return answer;
    }


    public static void main(String[] args) {
        SpringApplication.run(SpringSecuritySimpleApplication.class, args);
    }
}
