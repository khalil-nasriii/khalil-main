package tn.esprit.autoloc.service.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.Vehicule;

/** Court séjour de week-end : remise de 10 %. */
@Component
public class TarifWeekendStrategy implements ITarificationStrategy {

    public static final String CODE = "WEEKEND";
    private static final BigDecimal REMISE_WEEKEND = new BigDecimal("0.90");

    @Override
    public BigDecimal calculerTarif(Vehicule vehicule, Reservation reservation) {
        return DureeLocation.prixDeBase(vehicule, reservation)
                .multiply(REMISE_WEEKEND)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getCode() {
        return CODE;
    }
}
