package br.com.fiap.aguiaradar.exception;

import br.com.fiap.aguiaradar.dto.ApiErro;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Rota que nao existe (ex: /actuator/health sem a dependencia actuator,
     * ou qualquer URL digitada errada). Sem este handler, cai no
     * handleGenerico abaixo e retorna 500 - o que e enganoso, pois o
     * problema real e que a rota nao existe (404), nao um erro interno.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErro> handleRotaNaoEncontrada(NoResourceFoundException ex, HttpServletRequest req) {
        return construirResposta(HttpStatus.NOT_FOUND, "Rota nao encontrada: " + req.getRequestURI(), req);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErro> handleNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return construirResposta(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ApiErro> handleRegraNegocio(RegraDeNegocioException ex, HttpServletRequest req) {
        return construirResposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ApiErro> handleCredenciais(CredenciaisInvalidasException ex, HttpServletRequest req) {
        return construirResposta(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErro> handleAcessoNegado(AccessDeniedException ex, HttpServletRequest req) {
        return construirResposta(HttpStatus.FORBIDDEN, "Acesso negado para o perfil atual.", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErro> handleValidacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construirResposta(HttpStatus.BAD_REQUEST, mensagem, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErro> handleGenerico(Exception ex, HttpServletRequest req) {
        return construirResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno: " + ex.getMessage(), req);
    }

    private ResponseEntity<ApiErro> construirResposta(HttpStatus status, String mensagem, HttpServletRequest req) {
        ApiErro erro = ApiErro.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .erro(status.getReasonPhrase())
                .mensagem(mensagem)
                .caminho(req.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(erro);
    }
}
