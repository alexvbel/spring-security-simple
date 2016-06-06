package com.abel.spring.security.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
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
