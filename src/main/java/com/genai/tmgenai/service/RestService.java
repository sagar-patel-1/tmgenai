package com.genai.tmgenai.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
public class RestService {

    //insurernotice, brochure

    private final RestTemplate restTemplate;

    RestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RestService() {
        this(new RestTemplate());
    }
    public <T> ResponseEntity<T> postForEntity(URI uri, HttpEntity request, Class<T> responseType) {
        try {
            return restTemplate.postForEntity(uri, request, responseType);
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
