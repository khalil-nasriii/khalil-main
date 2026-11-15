# Atelier 6 — Patrons GoF et principes SOLID

## Partie 1 — Analyse de AutoLoc v0 (`legacy/AutoLocManager`)

> Le code de l'énoncé ne compile pas tel quel (paramètre `typeVehicule` mais test sur `typeReservation`,
> méthode `enregistrerPaiement()` absente). La version conservée dans
> `src/main/java/tn/esprit/autoloc/legacy/AutoLocManager.java` corrige ces deux points sans rien changer
> à sa conception, pour pouvoir l'analyser. Elle est marquée `@Deprecated` et n'est utilisée nulle part.

**Question 1 — Combien de responsabilités ?** Au moins cinq :
- calculer le prix de la location (choix du tarif selon le type de réservation) ;
- créer le contrat ;
- notifier le client (email, et SMS prévu) ;
- enregistrer le paiement ;
- sauvegarder le contrat (persistance) — plus l'affichage console.

**Question 2 — Principe SOLID non respecté :**
- **SRP** (Single Responsibility) : la classe a plusieurs raisons de changer (nouvelle grille tarifaire,
  nouveau canal de notification, nouveau mode de stockage…).
- **DIP** (Dependency Inversion) : elle dépend de détails concrets (`System.out`, méthodes privées
  d'envoi et de sauvegarde) au lieu d'abstractions injectées ; impossible de la tester isolément.

**Question 3 — Nouveau type « PleineSaison » :**
- il faut **modifier** `louerVehicule` et ajouter un `else if (typeReservation.equals("PleineSaison"))` ;
- chaque nouveau tarif allonge la cascade, recompile et peut casser les tarifs existants (une faute de
  frappe sur la chaîne donne silencieusement un prix de 0).

**Donc violation de :**
- **OCP** (Open/Closed) : la classe n'est pas fermée à la modification ; on ne peut pas étendre le calcul
  sans modifier le code existant.

## Refactoring

| Problème v0 | Patron / principe | Code |
|---|---|---|
| Cascade de `if` sur le type de tarif | **Strategy** + OCP | `service.strategy.ITarificationStrategy` ; `TarifStandardStrategy` (tarif × jours), `TarifWeekendStrategy` (× 0,90), `TarifLongueDureeStrategy` (× 0,80). Le calcul commun est dans `DureeLocation`. « PleineSaison » = une nouvelle classe `@Component`, aucune ligne existante modifiée. |
| Le client fait `new TarifXxxStrategy()` | **Factory** + DIP | `service.factory.TarificationStrategyFactory` reçoit **toutes** les stratégies injectées par Spring (`List<ITarificationStrategy>` → `Map<code, stratégie>`) et choisit selon la réservation : ≥ 7 jours → `LONGUE_DUREE` ; ≤ 3 jours commençant vendredi/samedi/dimanche → `WEEKEND` ; sinon `STANDARD`. |
| Canaux de notification codés en dur | **Factory Method** | `service.factory.NotificationFactory.creerObservateur("EMAIL" \| "SMS")` renvoie un `IReservationObserver` sans exposer les classes concrètes. |
| Notification couplée à la réservation | **Observer** + SRP | `service.observer.ReservationSubject` maintient la liste des `IReservationObserver` (injectés par Spring) et les notifie ; `EmailNotificationObserver` et `SmsNotificationObserver` journalisent `[EMAIL] …` / `[SMS] …`. Ajouter « administrateur » ou « statistiques » = un nouvel observateur. |

Intégration dans l'application :
- `ReservationServiceImpl.creer(dto)` persiste une réservation `CONFIRMEE`, appelle
  `reservationSubject.notifierConfirmation(...)` et renvoie un `ReservationResponseDTO` dont
  `tarifEstime` vient de `tarificationStrategyFactory.resoudre(r).calculerTarif(vehicule, r)`.
- `ReservationServiceImpl.annuler(id)` → `annulerReservation(r)` → `notifierAnnulation(...)`.
- Endpoints : `POST /api/reservations`, `PUT /api/reservations/{id}/annulation`.
- Les méthodes de l'énoncé `creerReservation(Client)` et `annulerReservation(Reservation)` sont dans
  `IReservationService` (`creerReservation` ne persiste pas, comme dans l'énoncé).

## Tests

| Classe | Contenu |
|---|---|
| `ReservationServiceTest` | `creerReservation` → logs `[EMAIL] Confirmation envoyee au client …` et `[SMS] Confirmation envoyee au …` ; `annulerReservation` → `[EMAIL] Annulation notifiee …` / `[SMS] Annulation notifiee …`. Les observateurs sont des `@MockitoSpyBean` : le vrai code s'exécute (logs visibles) et `verify(...)` prouve l'appel. Un 3e test crée une réservation persistée de 10 jours : tarif `LONGUE_DUREE` = 10 × 100 × 0,80 = 800. |
| `TarificationStrategyFactoryTest` | Les 4 cas de l'énoncé (7 j, borne 7 j, 2 j vendredi, 4 j lundi) + court séjour en semaine, réservation sans dates, calcul des montants. Test unitaire pur (`new`, sans Spring). |
| `NotificationFactoryTest` | `EMAIL`, `email`, `SMS`, canal inconnu, canal `null`. |

| Test | Durée | Jour début | Branche testée | Résultat |
|---|---|---|---|---|
| 1 | 7 j (01/01 → 08/01/2024) | lundi | jours ≥ 7 | LONGUE_DUREE |
| 2 | 7 j (01/05 → 08/05/2024) | mercredi | jours ≥ 7 (limite) | LONGUE_DUREE |
| 3 | 2 j (08/03 → 10/03/2024) | vendredi | jours ≤ 3 et week-end | WEEKEND |
| 4 | 4 j (04/03 → 08/03/2024) | lundi | repli | STANDARD |

(Le tableau de l'énoncé indique « mercredi » pour le test 1 ; le 1er janvier 2024 est en réalité un lundi —
sans incidence, la branche testée est la durée.)
