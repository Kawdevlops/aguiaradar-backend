package br.com.fiap.aguiaradar.service;

import br.com.fiap.aguiaradar.dto.LoginRequest;
import br.com.fiap.aguiaradar.dto.LoginResponse;
import br.com.fiap.aguiaradar.dto.RegistroUsuarioRequest;
import br.com.fiap.aguiaradar.exception.CredenciaisInvalidasException;
import br.com.fiap.aguiaradar.exception.RegraDeNegocioException;
import br.com.fiap.aguiaradar.model.Usuario;
import br.com.fiap.aguiaradar.repository.UsuarioRepository;
import br.com.fiap.aguiaradar.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha invalidos."));

        if (!usuario.isAtivo()) {
            throw new CredenciaisInvalidasException("Usuario inativo. Procure a lideranca.");
        }

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException("E-mail ou senha invalidos.");
        }

        String token = jwtService.gerarToken(usuario.getId(), usuario.getEmail(), usuario.getPerfil().name());

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .perfil(usuario.getPerfil())
                .expiraEmMs(jwtService.getExpirationMs())
                .build();
    }

    public Usuario registrar(RegistroUsuarioRequest request) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new RegraDeNegocioException("Ja existe um usuario cadastrado com este e-mail.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .perfil(request.getPerfil())
                .filialId(request.getFilialId())
                .ativo(true)
                .dataCriacao(LocalDateTime.now())
                .build();

        return usuarioRepository.save(usuario);
    }
}
