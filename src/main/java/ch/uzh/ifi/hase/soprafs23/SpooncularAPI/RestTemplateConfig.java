package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean("spoonacularRestTemplate")
    public RestTemplate restTemplate() {
        // Configure your RestTemplate as needed
        RestTemplate restTemplate = new RestTemplate();
        // Add any additional configurations or customizations
        return restTemplate;
    }
}

