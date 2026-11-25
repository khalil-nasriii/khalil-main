package tn.esprit.autoloc.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.domain.Agence;

public interface IAgenceRepository extends JpaRepository<Agence, Long> {

    // ---------- Atelier 8 — Exercice 5 ----------
    // La ville et le nom ne sont pas uniques dans le modèle : un Optional<Agence> findByVille
    // lèverait IncorrectResultSizeDataAccessException dès que deux agences partagent la ville.
    // On garde un Optional (première agence, par identifiant) et une List pour la recherche par nom.

    Optional<Agence> findFirstByVilleOrderByIdAgenceAsc(String ville);

    List<Agence> findByVille(String ville);

    List<Agence> findByNomContaining(String nom);
}
