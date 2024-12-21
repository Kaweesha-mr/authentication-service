package org.spring.authenticationservice.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailService {

    @Autowired
    RestTemplate restTemplate;


    public String ActivationEmail(Map<String, String> emailPayload){

        String url = "http://NODEJS-MAIL-SERVICE/api/activation";

        ResponseEntity<String> response = restTemplate.postForEntity(url, emailPayload, String.class);

        return response.getBody();

    }


}
