package br.com.fiap.aguiaradar.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitarios do JwtService: geracao, leitura das claims e validacao de expiracao.
 * Nao sobem contexto Spring nem MongoDB - rodam rapido e sem dependencias externas.
 */
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET_BASE64 =
            "c3VwZXItc2VjcmV0LWtleS1hZ3VpYS1yYWRhci1kZXNhZmlvLWZpYXAtMjAyNi1jaGF2ZS1zZWd1cmE=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET_BASE64);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86_400_000L); // 24h
    }

    @Test
    void deveGerarTokenEExtrairAsClaimsCorretamente() {
        String token = jwtService.gerarToken("user-123", "gestor@aguiaradar.com", "GESTOR");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extrairUsuarioId(token)).isEqualTo("user-123");
        assertThat(jwtService.extrairEmail(token)).isEqualTo("gestor@aguiaradar.com");
        assertThat(jwtService.extrairPerfil(token)).isEqualTo("GESTOR");
    }

    @Test
    void tokenRecemGeradoDeveSerValido() {
        String token = jwtService.gerarToken("user-1", "operador@aguiaradar.com", "OPERADOR");

        assertThat(jwtService.tokenValido(token)).isTrue();
    }

    @Test
    void tokenComExpiracaoNoPassadoDeveSerInvalido() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String tokenExpirado = jwtService.gerarToken("user-1", "a@a.com", "OPERADOR");

        assertThat(jwtService.tokenValido(tokenExpirado)).isFalse();
    }

    @Test
    void tokenMalFormadoDeveSerInvalido() {
        assertThat(jwtService.tokenValido("isto-nao-e-um-jwt-valido")).isFalse();
    }
}
