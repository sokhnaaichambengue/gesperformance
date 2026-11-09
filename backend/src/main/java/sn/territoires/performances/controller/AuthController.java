package sn.territoires.performances.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.territoires.performances.dto.LoginRequest;
import sn.territoires.performances.dto.LoginResponse;
import sn.territoires.performances.dto.UserInfo;
import sn.territoires.performances.entity.Utilisateur;
import sn.territoires.performances.repository.UtilisateurRepository;
import sn.territoires.performances.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final UtilisateurRepository users;
    private final JwtService jwt;

    public AuthController(AuthenticationManager authManager, UtilisateurRepository users, JwtService jwt) {
        this.authManager = authManager;
        this.users = users;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.motDePasse()));
        Utilisateur u = users.findByEmail(req.email()).orElseThrow();
        return new LoginResponse(jwt.generate(u.getEmail(), u.getRole().getCode()), toInfo(u));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfo> me(Authentication auth) {
        return users.findByEmail(auth.getName())
                .map(u -> ResponseEntity.ok(toInfo(u)))
                .orElse(ResponseEntity.status(401).build());
    }

    private UserInfo toInfo(Utilisateur u) {
        return new UserInfo(u.getId(), u.getNom(), u.getEmail(), u.getRole().getCode());
    }
}
