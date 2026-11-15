package tn.esprit.autoloc.service.strategy;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.Vehicule;

/** Tarif plein : tarif journalier x nombre de jours. */
@Component
public class TarifStandardStrategy implements ITarificationStrategy {

    public static final String CODE = "STANDARD";

    @Override
    public BigDecimal calculerTarif(Vehicule vehicule, Reservation reservation) {
        return DureeLocation.prixDeBase(vehicule, reservation);
    }

    @Override
    public String getCode() {
        return CODE;
    }
}
