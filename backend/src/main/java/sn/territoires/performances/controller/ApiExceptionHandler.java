package sn.territoires.performances.controller;

import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private static ResponseEntity<Map<String, String>> reponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Map<String, String>> auth(AuthenticationException e) {
        return reponse(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect, ou compte désactivé");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, String>> interdit(AccessDeniedException e) {
        return reponse(HttpStatus.FORBIDDEN, "Droits insuffisants pour cette action");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage()).collect(Collectors.joining(" ; "));
        return reponse(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<Map<String, String>> requeteIllisible(Exception e) {
        return reponse(HttpStatus.BAD_REQUEST, "Requête invalide : une valeur n'a pas le bon format");
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> metier(ResponseStatusException e) {
        return reponse(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> integrite(DataIntegrityViolationException e) {
        log.warn("Contrainte de base de données : {}", e.getMostSpecificCause().getMessage());
        return reponse(HttpStatus.CONFLICT,
                "Opération refusée : cet enregistrement existe déjà ou il est utilisé par d'autres données");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> inattendu(Exception e) {
        log.error("Erreur inattendue", e);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur");
    }
}
