package br.com.fiap.aguiaradar.dto;

import br.com.fiap.aguiaradar.model.enums.Perfil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tipo; // "Bearer"
    private String id;
    private String nome;
    private String email;
    private Perfil perfil;
    private long expiraEmMs;
}
