package br.com.fiap.aguiaradar.security;

import br.com.fiap.aguiaradar.model.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Pequeno helper para recuperar o Usuario autenticado a partir do contexto de seguranca. */
@Component
public class AutenticacaoUtil {

    public Usuario usuarioLogado() {
        AguiaRadarUserDetails details =
                (AguiaRadarUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return details.getUsuario();
    }
}
