package tn.esprit.autoloc.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;

public interface IVehiculeRepository extends JpaRepository<Vehicule, Long> {

    // ---------- Atelier 8 — Partie I : méthodes dérivées (Exercice 1) ----------
    // L'identifiant s'appelle idAgence / idVehicule dans le modèle : Agence_Id devient Agence_IdAgence.

    List<Vehicule> findByAgenceVille(String ville);

    List<Vehicule> findByStatutAndAgence_IdAgence(StatutVehicule statut, Long agenceId);

    Page<Vehicule> findByStatut(StatutVehicule statut, Pageable pageable);

    List<Vehicule> findByAgence_VilleAndCategorie(String ville, CategorieVehicule categorie);

    boolean existsByIdVehiculeAndStatut(Long vehiculeId, StatutVehicule statut);

    long countByStatut(StatutVehicule statut);

    List<Vehicule> findByTarifJournalierLessThan(BigDecimal tarif);

    List<Vehicule> findByTarifJournalierGreaterThan(BigDecimal tarif);

    List<Vehicule> findAllByOrderByTarifJournalierAsc();

    // ---------- Atelier 8 — Partie II : JPQL (Exercice 7) ----------

    /**
     * 7.1 — Véhicules d'une ville et d'une catégorie, hors maintenance, sans réservation
     * active (EN_ATTENTE ou CONFIRMEE) qui chevauche [dateDebut, dateFin].
     * Deux périodes se chevauchent si r.dateDebut < :dateFin ET r.dateFin > :dateDebut :
     * une réservation qui se termine le jour où la nouvelle commence (bornes qui se touchent)
     * ne bloque donc pas le véhicule.
     */
    @Query("""
            SELECT v FROM Vehicule v
            WHERE v.agence.ville = :ville
              AND v.categorie = :categorie
              AND v.statut <> tn.esprit.autoloc.domain.StatutVehicule.MAINTENANCE
              AND NOT EXISTS (
                  SELECT r FROM Reservation r
                  WHERE r.vehicule = v
                    AND r.statut IN (tn.esprit.autoloc.domain.StatutReservation.EN_ATTENTE,
                                     tn.esprit.autoloc.domain.StatutReservation.CONFIRMEE)
                    AND r.dateDebut < :dateFin
                    AND r.dateFin > :dateDebut)
            ORDER BY v.tarifJournalier ASC
            """)
    List<Vehicule> rechercherVehiculesDisponibles(@Param("ville") String ville,
                                                  @Param("categorie") CategorieVehicule categorie,
                                                  @Param("dateDebut") LocalDate dateDebut,
                                                  @Param("dateFin") LocalDate dateFin);

    /**
     * 7.2 — Véhicules à libérer : ils ont une réservation encore CONFIRMEE dont la date de fin
     * est atteinte ou dépassée à la date donnée (la location est arrivée à son terme).
     */
    @Query("""
            SELECT DISTINCT v FROM Reservation r
            JOIN r.vehicule v
            WHERE r.statut = tn.esprit.autoloc.domain.StatutReservation.CONFIRMEE
              AND r.dateFin <= :aujourdhui
            """)
    List<Vehicule> trouverVehiculesAReleaser(@Param("aujourdhui") LocalDate aujourdhui);
}
