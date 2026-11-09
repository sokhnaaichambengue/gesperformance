package sn.territoires.performances.security;

import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sn.territoires.performances.entity.Utilisateur;
import sn.territoires.performances.repository.UtilisateurRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UtilisateurRepository repo;

    public CustomUserDetailsService(UtilisateurRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        return new User(u.getEmail(), u.getMotDePasse(), u.isActif(), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().getCode())));
    }
}
