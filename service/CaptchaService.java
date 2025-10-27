package com.aurionpro.app.service;

public interface CaptchaService {
    boolean validateCaptcha(String captchaResponse);
}