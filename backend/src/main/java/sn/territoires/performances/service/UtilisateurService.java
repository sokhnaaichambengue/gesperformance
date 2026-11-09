package sn.territoires.performances.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.territoires.performances.dto.Dtos.UtilisateurRequest;
import sn.territoires.performances.dto.Dtos.UtilisateurView;
import sn.territoires.performances.entity.Utilisateur;
import sn.territoires.performances.repository.RoleRepository;
import sn.territoires.performances.repository.UtilisateurRepository;

@Service
public class UtilisateurService {
    private final UtilisateurRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;

    public UtilisateurService(UtilisateurRepository users, RoleRepository roles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }

    static UtilisateurView vue(Utilisateur u) {
        return new UtilisateurView(u.getId(), u.getNom(), u.getEmail(), u.getRole().getCode(), u.isActif());
    }

    @Transactional(readOnly = true)
    public List<UtilisateurView> lister() {
        return users.findAll(Sort.by("nom")).stream().map(UtilisateurService::vue).toList();
    }

    @Transactional
    public UtilisateurView creer(UtilisateurRequest req) {
        if (req.motDePasse() == null || req.motDePasse().length() < 8) {
            throw Erreurs.invalide("Le mot de passe est obligatoire (8 caractères minimum)");
        }
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw Erreurs.conflit("Un compte existe déjà avec cet email");
        Utilisateur u = new Utilisateur();
        remplir(u, req, email);
        u.setMotDePasse(encoder.encode(req.motDePasse()));
        return vue(users.saveAndFlush(u));
    }

    @Transactional
    public UtilisateurView modifier(Long id, UtilisateurRequest req, String emailCourant) {
        Utilisateur u = users.findById(id).orElseThrow(() -> Erreurs.introuvable("Utilisateur"));
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailAndIdNot(email, id)) throw Erreurs.conflit("Un compte existe déjà avec cet email");
        boolean soiMeme = u.getEmail().equalsIgnoreCase(emailCourant);
        if (soiMeme && Boolean.FALSE.equals(req.actif())) {
            throw Erreurs.invalide("Vous ne pouvez pas désactiver votre propre compte");
        }
        if (soiMeme && !u.getRole().getCode().equals(req.roleCode())) {
            throw Erreurs.invalide("Vous ne pouvez pas changer votre propre rôle");
        }
        remplir(u, req, email);
        if (req.motDePasse() != null && !req.motDePasse().isBlank()) {
            if (req.motDePasse().length() < 8) throw Erreurs.invalide("Mot de passe : 8 caractères minimum");
            u.setMotDePasse(encoder.encode(req.motDePasse()));
        }
        return vue(users.saveAndFlush(u));
    }

    @Transactional
    public void supprimer(Long id, String emailCourant) {
        Utilisateur u = users.findById(id).orElseThrow(() -> Erreurs.introuvable("Utilisateur"));
        if (u.getEmail().equalsIgnoreCase(emailCourant)) {
            throw Erreurs.invalide("Vous ne pouvez pas supprimer votre propre compte");
        }
        users.delete(u);
        users.flush();
    }

    private void remplir(Utilisateur u, UtilisateurRequest req, String email) {
        u.setNom(req.nom().trim());
        u.setEmail(email);
        u.setRole(roles.findByCode(req.roleCode()).orElseThrow(() -> Erreurs.invalide("Rôle inconnu")));
        u.setActif(req.actif() == null || req.actif());
    }
}
