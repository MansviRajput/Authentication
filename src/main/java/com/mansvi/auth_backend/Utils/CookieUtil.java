package com.mansvi.auth_backend.Utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {

    public static void addCookie(HttpServletResponse response,String name,String value,String path,long maxAgeSecond){
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .path(path)
                .sameSite("Strict")
                .maxAge(maxAgeSecond)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void clearCookie(HttpServletResponse response,String name, String path){
        ResponseCookie cookie = ResponseCookie.from(name,"")
                .httpOnly(true)
                .secure(false)
                .path(path)
                .sameSite("Strict")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static Optional<String> readCookie(HttpServletRequest request, String name){
        if(request.getCookies() == null){
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst();
    }
}
