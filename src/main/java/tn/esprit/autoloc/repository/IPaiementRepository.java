package tn.esprit.autoloc.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.domain.Paiement;

public interface IPaiementRepository extends JpaRepository<Paiement, Long> {

    // ---------- Atelier 8 — Exercice 6 ----------

    List<Paiement> findByContrat_IdContrat(Long contratId);

    List<Paiement> findByDatePaiementBetween(LocalDate debut, LocalDate fin);
}
