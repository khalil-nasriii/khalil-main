package tn.esprit.autoloc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.autoloc.service.factory.NotificationFactory;
import tn.esprit.autoloc.service.observer.EmailNotificationObserver;
import tn.esprit.autoloc.service.observer.SmsNotificationObserver;

/** Atelier 6 : Factory Method appliqué aux canaux de notification. */
class NotificationFactoryTest {

    private NotificationFactory factory;
    private EmailNotificationObserver email;
    private SmsNotificationObserver sms;

    @BeforeEach
    void setUp() {
        email = new EmailNotificationObserver();
        sms = new SmsNotificationObserver();
        factory = new NotificationFactory(email, sms);
    }

    @Test
    void doitRetournerLeCanalEmail() {
        assertThat(factory.creerObservateur("EMAIL")).isSameAs(email);
        assertThat(factory.creerObservateur("email")).isSameAs(email);
    }

    @Test
    void doitRetournerLeCanalSms() {
        assertThat(factory.creerObservateur("SMS")).isSameAs(sms);
    }

    @Test
    void doitRefuserUnCanalInconnu() {
        assertThatThrownBy(() -> factory.creerObservateur("PIGEON"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> factory.creerObservateur(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
