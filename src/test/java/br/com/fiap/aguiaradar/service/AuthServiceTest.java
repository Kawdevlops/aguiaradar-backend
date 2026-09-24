package br.com.fiap.aguiaradar.service;

import br.com.fiap.aguiaradar.dto.LoginRequest;
import br.com.fiap.aguiaradar.dto.LoginResponse;
import br.com.fiap.aguiaradar.exception.CredenciaisInvalidasException;
import br.com.fiap.aguiaradar.model.Usuario;
import br.com.fiap.aguiaradar.model.enums.Perfil;
import br.com.fiap.aguiaradar.repository.UsuarioRepository;
import br.com.fiap.aguiaradar.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios das regras de login do AuthService: credenciais validas,
 * senha incorreta, usuario inexistente e usuario inativo. Repositorio, encoder
 * e JwtService sao mockados - nenhum MongoDB real e necessario.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioGestor;

    @BeforeEach
    void setUp() {
        usuarioGestor = Usuario.builder()
                .id("1")
                .nome("Gestora Teste")
                .email("gestor@aguiaradar.com")
                .senhaHash("hash-simulado")
                .perfil(Perfil.GESTOR)
                .ativo(true)
                .build();
    }

    @Test
    void deveAutenticarQuandoCredenciaisSaoValidas() {
        LoginRequest request = new LoginRequest();
        request.setEmail("gestor@aguiaradar.com");
        request.setSenha("123456");

        when(usuarioRepository.findByEmailIgnoreCase("gestor@aguiaradar.com"))
                .thenReturn(Optional.of(usuarioGestor));
        when(passwordEncoder.matches("123456", "hash-simulado")).thenReturn(true);
        when(jwtService.gerarToken("1", "gestor@aguiaradar.com", "GESTOR")).thenReturn("token-fake");
        when(jwtService.getExpirationMs()).thenReturn(86_400_000L);

        LoginResponse resposta = authService.login(request);

        assertThat(resposta.getToken()).isEqualTo("token-fake");
        assertThat(resposta.getPerfil()).isEqualTo(Perfil.GESTOR);
        assertThat(resposta.getEmail()).isEqualTo("gestor@aguiaradar.com");
        assertThat(resposta.getTipo()).isEqualTo("Bearer");
    }

    @Test
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        LoginRequest request = new LoginRequest();
        request.setEmail("gestor@aguiaradar.com");
        request.setSenha("senha-errada");

        when(usuarioRepository.findByEmailIgnoreCase("gestor@aguiaradar.com"))
                .thenReturn(Optional.of(usuarioGestor));
        when(passwordEncoder.matches("senha-errada", "hash-simulado")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        LoginRequest request = new LoginRequest();
        request.setEmail("naoexiste@aguiaradar.com");
        request.setSenha("123456");

        when(usuarioRepository.findByEmailIgnoreCase("naoexiste@aguiaradar.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioEstaInativo() {
        usuarioGestor.setAtivo(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("gestor@aguiaradar.com");
        request.setSenha("123456");

        when(usuarioRepository.findByEmailIgnoreCase("gestor@aguiaradar.com"))
                .thenReturn(Optional.of(usuarioGestor));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}
