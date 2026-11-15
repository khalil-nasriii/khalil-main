# Atelier 4 — IoC, injection de dépendances et couche Service

Organisation : interfaces `I…Service` dans `tn.esprit.autoloc.service`, implémentations `…ServiceImpl`
dans `tn.esprit.autoloc.service.impl`, exception métier dans `tn.esprit.autoloc.exception`.
Depuis l'Atelier 7, les services reçoivent et renvoient des DTO (voir `atelier7-notes.md`).

## A. Modes d'injection comparés

| Critère | Constructeur (`final` + `@RequiredArgsConstructor`) | Attribut (`@Autowired private …`) |
|---|---|---|
| Champ `final` possible ? | Oui | Non (le champ est affecté par réflexion après la construction) |
| Dépendance visible dans la signature ? | Oui, dans le constructeur | Non, cachée dans les attributs |
| Utilisable avec `new` hors conteneur sans dépendance `null` ? | Oui : impossible de créer l'objet sans lui fournir ses dépendances | Non : `new ContratServiceImpl()` laisse `contratRepository == null` → `NullPointerException` |
| Signalé par SonarQube for IDE ? | Non | Oui — **java:S6813** « Field dependency injection should be avoided » |

Convention AutoLoc : injection par constructeur, champs `final`, dépendances typées par une interface.

## B. Services

| Service | Dépendances injectées | Mode | Justification |
|---|---|---|---|
| `ContratServiceImpl` | `IContratRepository`, `IReservationRepository`, `ContratMapper`, `PaiementMapper` | Constructeur | Le contrat porte ses paiements (composition) ; la réservation est nécessaire pour rattacher le contrat (côté propriétaire `reservation.contrat_id`). |
| `PaiementServiceImpl` | `IPaiementRepository`, `PaiementMapper` | Constructeur | Lecture seule ; aucune écriture directe d'un enfant de composition. |
| `AgenceServiceImpl` | `IAgenceRepository`, `AgenceMapper` | Constructeur | CRUD ; règle : nom et ville obligatoires. |
| `EmployeServiceImpl` | `IEmployeRepository`, `IAgenceRepository`, `EmployeMapper` | Constructeur | CRUD + affectation/désaffectation à une agence ; règle : nom, prénom, rôle obligatoires. |
| `VehiculeServiceImpl` | `IVehiculeRepository`, `IAgenceRepository`, `VehiculeMapper` | Constructeur | CRUD + affectation/désaffectation ; règle : tarif journalier non négatif. |
| `EquipementServiceImpl` | `IEquipementRepository`, `EquipementMapper` | Constructeur | CRUD ; règle : libellé obligatoire (le modèle n'a pas d'attribut quantité). |
| `ClientServiceImpl` | `IClientRepository`, `ClientMapper` | Constructeur | CRUD ; règle : nom et e-mail obligatoires. |
| `ReservationServiceImpl` | `IReservationRepository`, `IClientRepository`, `IVehiculeRepository`, `ReservationMapper`, `TarificationStrategyFactory`, `ReservationSubject` | Constructeur | Règle : date de fin non antérieure à la date de début ; calcul du tarif (Strategy) et notifications (Observer) — Atelier 6. |
| `MaintenanceServiceImpl` | `IMaintenanceRepository`, `IVehiculeRepository`, `MaintenanceMapper` | Constructeur | Règle : date de début obligatoire, date de fin non antérieure (le modèle n'a pas d'attribut coût). |

Tous les services sont `@Service`, `@Transactional`, sans état (aucun attribut modifiable), sans `new`
de repository, sans injection par attribut. Les règles communes (`exigerNouveau`, `exigerNonVide`,
`exigerFinApresDebut`…) sont factorisées dans `service.impl.Regles`.

## C. Messages d'erreur du conteneur

**Message 1** — `NoSuchBeanDefinitionException` (enveloppée dans `UnsatisfiedDependencyException`) :
`ContratController` attend un bean `IContratService` introuvable. Causes probables côté
`ContratServiceImpl` : (1) l'annotation `@Service` a été oubliée ; (2) la classe est placée hors du package
racine `tn.esprit.autoloc` (le scan ne la voit pas) — ou elle n'implémente pas `IContratService`.
Correction : ajouter `@Service`, déplacer la classe sous `tn.esprit.autoloc`, vérifier `implements`.

**Message 2** — `NoUniqueBeanDefinitionException` : deux beans du même type (`emailNotificateur`,
`smsNotificateur`). Corrections : (a) `@Primary` **sur la classe** du bean à utiliser par défaut ;
(b) `@Qualifier("smsNotificateur")` **sur le paramètre du constructeur** de `NotificationController` pour
choisir explicitement. (Autre option : injecter `List<Notificateur>` pour recevoir les deux — c'est ce que
fait `ReservationSubject` à l'Atelier 6.)

**Message 3** — dépendance circulaire (`BeanCurrentlyInCreationException`) : `clientServiceImpl` ↔
`reservationServiceImpl`. Avec l'injection par constructeur, pour construire A il faut B déjà construit, et
inversement : aucun ordre de création n'est possible, Spring ne peut pas fournir de référence partielle.
Piste : supprimer le cycle en faisant dépendre les deux services des **repositories** (c'est le cas dans
AutoLoc : `ReservationServiceImpl` utilise `IClientRepository`, pas `IClientService`), ou extraire la
responsabilité commune dans un troisième composant, ou passer par des événements (Observer).

## D. Anomalies SonarQube for IDE

| Règle | Explication | Correction |
|---|---|---|
| **java:S6813** (étape 2) | Injection par attribut `@Autowired private IContratRepository` : champ non `final`, dépendance cachée. | Retour à `private final` + `@RequiredArgsConstructor`. |
| **java:S112** (expérience étape 4) | `throw new RuntimeException(...)` : exception générique, l'appelant ne peut pas la distinguer. | `ResourceNotFoundException` (exception métier, traduite en 404). |
| **java:S1067** | `AgenceServiceImpl.valider` : expression booléenne à 6 opérateurs `\|\|` (seuil 3), illisible. | Règles extraites dans `Regles.exigerNonVide(...)`, une ligne par attribut. |
| **java:S1192** | Littéraux répétés (`"Agence"`, `"Contrat"`… 3 fois et plus par classe). | Constante `private static final String RESSOURCE`. |
| Sémantique d'exception | `ReservationServiceImpl` levait `IllegalArgumentException("Client introuvable")` → 400 au lieu de 404. | `ResourceNotFoundException("Client", id)`. |
| Code mort | Constructeur et imports inutilisés après le passage aux DTO. | Supprimés. |

## E. Questions de compréhension

1. **Où est le `new` de `ContratServiceImpl` ?** Nulle part dans notre code : c'est le conteneur Spring
   (`ApplicationContext` créé par `SpringApplication.run`) qui détecte `@Service` lors du scan et appelle
   le constructeur par réflexion, en lui passant les beans requis.
2. **Pourquoi le contrôleur dépend de `IContratService` ?** Pour dépendre d'une abstraction (DIP) :
   l'implémentation peut changer (ou être remplacée par un mock en test) sans toucher au contrôleur, et
   Spring peut fournir un proxy (transactions) qui implémente l'interface.
3. **Pourquoi un singleton sans état ?** Une seule instance est partagée par tous les threads/requêtes. Un
   attribut « contrat courant » serait écrasé par une requête concurrente : l'utilisateur A modifierait le
   contrat de l'utilisateur B.
4. **Pourquoi `update` charge avant `save` ?** Passer l'objet reçu à `save` déclenche un merge qui écrase
   **toutes** les colonnes (les champs absents deviennent `null`) et, avec `orphanRemoval`, remplacerait la
   collection de paiements par une liste vide → suppression des paiements. Charger puis recopier les seuls
   attributs modifiables protège l'identifiant et les associations.
5. **`@Component` vs `@Bean`** : `@Component` se pose sur une classe que l'on écrit (détectée par le scan) ;
   `@Bean` se pose sur une méthode d'une classe `@Configuration` pour déclarer un objet que l'on ne peut pas
   annoter (classe de bibliothèque, ex. `OpenAPI` dans `OpenApiConfig`). **`@Primary` vs `@Qualifier`** :
   `@Primary` (sur la classe) désigne le candidat par défaut ; `@Qualifier` (au point d'injection) choisit
   explicitement un bean par son nom. **`IPaiementService` en lecture seule** : Paiement est l'enfant d'une
   composition ; il est créé et supprimé à travers son Contrat (`ajouterPaiement`, cascade, orphanRemoval),
   ce qui garantit qu'aucun paiement n'existe sans contrat.
