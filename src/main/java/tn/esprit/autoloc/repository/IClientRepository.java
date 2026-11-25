package tn.esprit.autoloc.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.domain.Client;

public interface IClientRepository extends JpaRepository<Client, Long> {

    // ---------- Atelier 8 — Exercice 2 ----------

    Optional<Client> findByEmail(String email);

    Optional<Client> findByNumPermis(String numPermis);

    List<Client> findByNomContaining(String nom);

    boolean existsByEmail(String email);
}
