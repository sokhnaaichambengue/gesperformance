package sn.territoires.performances.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Fabrique d'erreurs métier renvoyées au frontend sous la forme {"message": "..."}. */
public final class Erreurs {
    private Erreurs() {
    }

    public static ResponseStatusException introuvable(String quoi) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, quoi + " introuvable");
    }

    public static ResponseStatusException invalide(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public static ResponseStatusException conflit(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
