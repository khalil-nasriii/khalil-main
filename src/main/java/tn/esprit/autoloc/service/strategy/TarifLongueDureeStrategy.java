package tn.esprit.autoloc.service.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.Vehicule;

/** Location de 7 jours ou plus : remise de 20 %. */
@Component
public class TarifLongueDureeStrategy implements ITarificationStrategy {

    public static final String CODE = "LONGUE_DUREE";
    private static final BigDecimal REMISE_LONGUE_DUREE = new BigDecimal("0.80");

    @Override
    public BigDecimal calculerTarif(Vehicule vehicule, Reservation reservation) {
        return DureeLocation.prixDeBase(vehicule, reservation)
                .multiply(REMISE_LONGUE_DUREE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getCode() {
        return CODE;
    }
}
