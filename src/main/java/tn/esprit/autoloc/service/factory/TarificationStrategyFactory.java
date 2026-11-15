package tn.esprit.autoloc.service.factory;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.service.strategy.ITarificationStrategy;
import tn.esprit.autoloc.service.strategy.TarifLongueDureeStrategy;
import tn.esprit.autoloc.service.strategy.TarifStandardStrategy;
import tn.esprit.autoloc.service.strategy.TarifWeekendStrategy;

/**
 * Sélectionne dynamiquement la ITarificationStrategy adaptée à une réservation
 * (durée du séjour). Toutes les implémentations Spring de ITarificationStrategy
 * sont injectées automatiquement puis rangées dans une Map(code, stratégie).
 *
 * Règles :
 *  - 7 jours ou plus                               : LONGUE_DUREE
 *  - 3 jours ou moins, début vendredi/samedi/dimanche : WEEKEND
 *  - sinon                                          : STANDARD
 */
@Component
public class TarificationStrategyFactory {

    static final long SEUIL_LONGUE_DUREE = 7;
    static final long SEUIL_COURT_SEJOUR = 3;

    private final Map<String, ITarificationStrategy> strategies;

    public TarificationStrategyFactory(List<ITarificationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ITarificationStrategy::getCode, Function.identity()));
    }

    public ITarificationStrategy resoudre(Reservation reservation) {
        if (reservation == null || reservation.getDateDebut() == null || reservation.getDateFin() == null) {
            throw new IllegalArgumentException("Les dates de la réservation sont obligatoires");
        }
        long jours = ChronoUnit.DAYS.between(reservation.getDateDebut(), reservation.getDateFin());

        if (jours >= SEUIL_LONGUE_DUREE) {
            return parCode(TarifLongueDureeStrategy.CODE);
        }
        if (jours <= SEUIL_COURT_SEJOUR && estWeekend(reservation)) {
            return parCode(TarifWeekendStrategy.CODE);
        }
        return parCode(TarifStandardStrategy.CODE);
    }

    private boolean estWeekend(Reservation reservation) {
        DayOfWeek jour = reservation.getDateDebut().getDayOfWeek();
        return jour == DayOfWeek.FRIDAY || jour == DayOfWeek.SATURDAY || jour == DayOfWeek.SUNDAY;
    }

    private ITarificationStrategy parCode(String code) {
        ITarificationStrategy strategy = strategies.get(code);
        if (strategy == null) {
            throw new IllegalStateException("Aucune stratégie de tarification enregistrée pour le code " + code);
        }
        return strategy;
    }
}
