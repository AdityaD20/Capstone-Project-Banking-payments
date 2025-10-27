package com.aurionpro.app.service.impl;

import com.aurionpro.app.dto.CaptchaResponseDto;
import com.aurionpro.app.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    @Override
    public boolean validateCaptcha(String captchaResponse) {
        if (captchaResponse == null || captchaResponse.isEmpty()) {
            return false;
        }

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", recaptchaSecret);
        requestMap.add("response", captchaResponse);

        try {
            CaptchaResponseDto apiResponse = restTemplate.postForObject(GOOGLE_RECAPTCHA_VERIFY_URL, requestMap, CaptchaResponseDto.class);
            if (apiResponse == null) {
                log.error("Captcha validation returned null response from Google.");
                return false;
            }
            return apiResponse.isSuccess();
        } catch (Exception e) {
            log.error("Exception occurred during Captcha validation: {}", e.getMessage());
            return false;
        }
    }
}