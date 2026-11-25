package tn.esprit.autoloc.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.RoleEmploye;

public interface IEmployeRepository extends JpaRepository<Employe, Long> {

    // ---------- Atelier 8 — Exercice 4 ----------

    List<Employe> findByAgenceNom(String nomAgence);

    List<Employe> findByAgence_IdAgenceAndRole(Long agenceId, RoleEmploye role);

    List<Employe> findByNomContaining(String nom);
}
