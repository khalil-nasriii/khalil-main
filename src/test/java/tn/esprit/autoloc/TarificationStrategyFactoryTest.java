package tn.esprit.autoloc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.service.factory.TarificationStrategyFactory;
import tn.esprit.autoloc.service.strategy.ITarificationStrategy;
import tn.esprit.autoloc.service.strategy.TarifLongueDureeStrategy;
import tn.esprit.autoloc.service.strategy.TarifStandardStrategy;
import tn.esprit.autoloc.service.strategy.TarifWeekendStrategy;

/** Atelier 6 : test unitaire de la Factory et des Strategy (sans Spring ni base de données). */
class TarificationStrategyFactoryTest {

    private TarificationStrategyFactory factory;
    private TarifStandardStrategy standard;
    private TarifWeekendStrategy weekend;
    private TarifLongueDureeStrategy longueDuree;

    @BeforeEach
    void setUp() {
        standard = new TarifStandardStrategy();
        weekend = new TarifWeekendStrategy();
        longueDuree = new TarifLongueDureeStrategy();
        factory = new TarificationStrategyFactory(List.of(standard, weekend, longueDuree));
    }

    private static Reservation reservation(String debut, String fin) {
        return Reservation.builder()
                .dateDebut(LocalDate.parse(debut))
                .dateFin(LocalDate.parse(fin))
                .build();
    }

    @Test
    void doitRetournerLongueDureePour7JoursOuPlus() {
        ITarificationStrategy resultat = factory.resoudre(reservation("2024-01-01", "2024-01-08"));

        assertThat(resultat).isInstanceOf(TarifLongueDureeStrategy.class);
        assertThat(resultat.getCode()).isEqualTo("LONGUE_DUREE");
    }

    @Test
    void doitRetournerLongueDureePourExactement7Jours() {
        ITarificationStrategy resultat = factory.resoudre(reservation("2024-05-01", "2024-05-08"));

        assertThat(resultat.getCode()).isEqualTo("LONGUE_DUREE");
    }

    @Test
    void doitRetournerWeekendPourCourtSejourVendredi() {
        Reservation reservation = reservation("2024-03-08", "2024-03-10");
        assertThat(reservation.getDateDebut().getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);

        ITarificationStrategy resultat = factory.resoudre(reservation);

        assertThat(resultat).isInstanceOf(TarifWeekendStrategy.class);
        assertThat(resultat.getCode()).isEqualTo("WEEKEND");
    }

    @Test
    void doitRetournerStandardPourMoyenneDuree() {
        ITarificationStrategy resultat = factory.resoudre(reservation("2024-03-04", "2024-03-08"));

        assertThat(resultat.getCode()).isEqualTo("STANDARD");
    }

    @Test
    void doitRetournerStandardPourCourtSejourEnSemaine() {
        // 2 jours mais début un lundi : pas de tarif week-end
        assertThat(factory.resoudre(reservation("2024-03-04", "2024-03-06")).getCode()).isEqualTo("STANDARD");
    }

    @Test
    void doitRefuserUneReservationSansDates() {
        Reservation sansDates = new Reservation();
        assertThatThrownBy(() -> factory.resoudre(sansDates)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void calculDesTarifs() {
        Vehicule vehicule = new Vehicule();
        vehicule.setTarifJournalier(new BigDecimal("100.00"));

        // 4 jours x 100 = 400
        assertThat(standard.calculerTarif(vehicule, reservation("2024-03-04", "2024-03-08")))
                .isEqualByComparingTo("400.00");
        // 2 jours x 100 x 0.90 = 180
        assertThat(weekend.calculerTarif(vehicule, reservation("2024-03-08", "2024-03-10")))
                .isEqualByComparingTo("180.00");
        // 7 jours x 100 x 0.80 = 560
        assertThat(longueDuree.calculerTarif(vehicule, reservation("2024-05-01", "2024-05-08")))
                .isEqualByComparingTo("560.00");
        // location le jour même : 1 jour facturé
        assertThat(standard.calculerTarif(vehicule, reservation("2024-03-04", "2024-03-04")))
                .isEqualByComparingTo("100.00");
    }
}
