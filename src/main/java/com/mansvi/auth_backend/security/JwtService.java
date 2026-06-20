package com.mansvi.auth_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    public String generateAccessToken(CustomUserDetail userDetail) {
        return buildToken(userDetail,accessTokenExpiryMs,"access");
    }

    public String generateRefreshToken(CustomUserDetail userDetail) {
        return buildToken(userDetail,refreshTokenExpiryMs,"refresh");
    }

    private String buildToken(CustomUserDetail userDetail, long expiryMs,String type) {
        List<String> roles = userDetail.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(userDetail.getUsername())   // email is subject
                .claim("userId",userDetail.getUserId().toString())      //payload
                .claim("roles",roles)
                .claim("type",type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)        //algorithm
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        return parseClaims(token).get("type",String.class);
    }

    public boolean isTokenValid(String token) {
        try{
            parseClaims(token);         // expired / tempered exception throw
            return true;
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    private Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public long getAccessTokenExpiryMs() { return accessTokenExpiryMs; }
    public long getRefreshTokenExpiryMs() { return refreshTokenExpiryMs; }
}
