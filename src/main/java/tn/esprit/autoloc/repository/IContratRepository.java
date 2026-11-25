package tn.esprit.autoloc.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.web.dto.ChiffreAffaireAgenceDTO;

public interface IContratRepository extends JpaRepository<Contrat, Long> {

    // ---------- Atelier 8 — Exercice 9.1 : agrégation SUM + projection DTO ----------

    /**
     * Chiffre d'affaires par agence = somme des montantTotal des contrats valides,
     * l'agence étant celle du véhicule de la réservation liée au contrat.
     * Contrat -> Reservation (côté inverse) -> Vehicule -> Agence.
     */
    @Query("""
            SELECT new tn.esprit.autoloc.web.dto.ChiffreAffaireAgenceDTO(a.idAgence, a.nom, SUM(c.montantTotal))
            FROM Reservation r
            JOIN r.contrat c
            JOIN r.vehicule v
            JOIN v.agence a
            WHERE c.valide = true
            GROUP BY a.idAgence, a.nom
            ORDER BY SUM(c.montantTotal) DESC
            """)
    List<ChiffreAffaireAgenceDTO> chiffreAffaireParAgence();

    // ---------- Atelier 8 — Exercice 9.2 ----------

    /**
     * Contrat n'a pas de date d'échéance : on retient la date de fin de la réservation
     * couverte par le contrat (fin de la location). Contrats valides dont l'échéance
     * est comprise entre aujourdhui et dateLimite (bornes incluses).
     */
    @Query("""
            SELECT c FROM Contrat c
            JOIN c.reservation r
            WHERE c.valide = true
              AND r.dateFin BETWEEN :aujourdhui AND :dateLimite
            ORDER BY r.dateFin ASC
            """)
    List<Contrat> trouverContratsAEcheanceProche(@Param("aujourdhui") LocalDate aujourdhui,
                                                 @Param("dateLimite") LocalDate dateLimite);
}
