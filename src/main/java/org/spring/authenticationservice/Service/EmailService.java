package org.spring.authenticationservice.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailService {

    @Autowired
    RestTemplate restTemplate;

    @Value("${EMAIL_SERVICE_NAME}")
    private String emailServiceName;


    public String ActivationEmail(Map<String, String> emailPayload){

        System.out.println(emailServiceName);
        String url = "http://" +emailServiceName+ "/api/activation";



        ResponseEntity<String> response = restTemplate.postForEntity(url, emailPayload, String.class);

        return response.getBody();

    }


}
