package sn.territoires.performances.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sn.territoires.performances.entity.Utilisateur;
import sn.territoires.performances.repository.RoleRepository;
import sn.territoires.performances.repository.UtilisateurRepository;

/** Crée un administrateur de DÉMONSTRATION au premier démarrage. À changer ou supprimer en production. */
@Component
public class DemoDataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final UtilisateurRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public DemoDataInitializer(UtilisateurRepository users, RoleRepository roles, PasswordEncoder encoder,
                               @Value("${app.demo-admin.email}") String email,
                               @Value("${app.demo-admin.password}") String password) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;
        var role = roles.findByCode("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Rôle ADMIN absent : exécuter db/schema.sql"));
        Utilisateur u = new Utilisateur();
        u.setNom("Administrateur (DÉMO)");
        u.setEmail(email);
        u.setMotDePasse(encoder.encode(password));
        u.setRole(role);
        users.save(u);
        log.warn("COMPTE DE DÉMONSTRATION créé : {} — changer ce mot de passe.", email);
    }
}
