package tn.esprit.autoloc.service.strategy;

import java.math.BigDecimal;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.Vehicule;

/**
 * Patron GoF : STRATEGY.
 * Chaque implémentation encapsule une façon de calculer le prix d'une location.
 * Ajouter un nouveau tarif = ajouter une classe, sans modifier le code existant (OCP).
 */
public interface ITarificationStrategy {

    BigDecimal calculerTarif(Vehicule vehicule, Reservation reservation);

    /** Code utilisé pour sélectionner dynamiquement la stratégie. */
    String getCode();
}
