package br.com.fiap.aguiaradar.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Responsavel por gerar e validar os tokens JWT utilizados na autenticacao
 * das requisicoes (mecanismo de seguranca exigido pelo desafio).
 */
@Component
public class JwtService {

    @Value("${aguiaradar.jwt.secret}")
    private String secret;

    @Value("${aguiaradar.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secret));
    }

    public String gerarToken(String usuarioId, String email, String perfil) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("perfil", perfil);
        claims.put("email", email);

        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(usuarioId)
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public String extrairUsuarioId(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    public String extrairPerfil(String token) {
        return extrairClaim(token, claims -> claims.get("perfil", String.class));
    }

    public String extrairEmail(String token) {
        return extrairClaim(token, claims -> claims.get("email", String.class));
    }

    public boolean tokenValido(String token) {
        try {
            Date expiracao = extrairClaim(token, Claims::getExpiration);
            return expiracao.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extrairClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
