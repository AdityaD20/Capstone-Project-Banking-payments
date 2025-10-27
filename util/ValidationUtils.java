package com.aurionpro.app.util;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final String PASSWORD_PATTERN = 
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isPasswordStrong(String password) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }
}