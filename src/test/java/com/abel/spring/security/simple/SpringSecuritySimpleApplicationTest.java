package com.abel.spring.security.simple;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author Alex Belikov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {SpringSecuritySimpleApplication.class, SpringSecuritySimpleApplicationTest.SpringSecuritySimpleConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringSecuritySimpleApplicationTest {

    @Configuration
    public static class SpringSecuritySimpleConfiguration {
        private String user = "alex", password = "good";

        @Bean
        public RestTemplate auth() {

            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);


            HttpClientBuilder builder = HttpClientBuilder.create();
            builder.setDefaultCredentialsProvider(credentialsProvider);

            CloseableHttpClient client = builder.build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(factory);

            return restTemplate;
        }
    }

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private RestTemplate restTemplate;


    @Test
    public void testHi() {

        ResponseEntity<Map> entity = restTemplate.getForEntity("http://localhost:" + port + "/hi", Map.class);

        String answer = (String) entity.getBody().get("content");

        Assert.assertEquals("Hi, there!", answer);
    }
}
