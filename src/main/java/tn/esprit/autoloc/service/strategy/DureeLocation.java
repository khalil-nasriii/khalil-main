package tn.esprit.autoloc.service.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.Vehicule;

/**
 * Outils communs aux stratégies : nombre de jours facturés et prix de base.
 * Évite de dupliquer ce calcul dans chaque stratégie.
 */
public final class DureeLocation {

    private DureeLocation() {
    }

    /** Nombre de jours entre dateDebut et dateFin ; une location le jour même compte 1 jour. */
    public static long nombreJours(Reservation reservation) {
        if (reservation == null || reservation.getDateDebut() == null || reservation.getDateFin() == null) {
            throw new IllegalArgumentException("Les dates de la réservation sont obligatoires");
        }
        long jours = ChronoUnit.DAYS.between(reservation.getDateDebut(), reservation.getDateFin());
        return Math.max(1, jours);
    }

    /** tarifJournalier x nombre de jours, arrondi au centime. */
    public static BigDecimal prixDeBase(Vehicule vehicule, Reservation reservation) {
        if (vehicule == null || vehicule.getTarifJournalier() == null) {
            throw new IllegalArgumentException("Le véhicule et son tarif journalier sont obligatoires");
        }
        return vehicule.getTarifJournalier()
                .multiply(BigDecimal.valueOf(nombreJours(reservation)))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
