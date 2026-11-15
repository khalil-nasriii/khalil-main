# Atelier 7 — Affectation, DTO, MapStruct, SonarLint et validation

Arborescence obtenue (sous `tn.esprit.autoloc`) :

```
domain/        entités JPA + énumérations
repository/    I…Repository (JpaRepository)
service/       I…Service
  impl/        …ServiceImpl + Regles
  strategy/    ITarificationStrategy + 3 stratégies   (Atelier 6)
  factory/     TarificationStrategyFactory, NotificationFactory
  observer/    ReservationSubject + observateurs Email / SMS
web/
  controller/  contrôleurs REST + GlobalExceptionHandler
  dto/         DTO + ApiError
  mapper/      interfaces MapStruct
  validator/   ReservationRequestValidator
config/        OpenApiConfig
exception/     ResourceNotFoundException
```

## Partie 1 — Affectation / désaffectation

**Côté propriétaire** : `Vehicule.agence` et `Employe.agence` (`@ManyToOne` + `@JoinColumn(name = "agence_id")`)
portent la clé étrangère ; `Agence.vehicules` / `Agence.employes` sont les côtés inverses (`mappedBy`).
Seule une modification du côté propriétaire est écrite en base.

| Opération (service) | Endpoint | Comportement |
|---|---|---|
| `affecterVehiculeAAgence(vehiculeId, agenceId)` | `PUT /api/vehicules/{vehiculeId}/agence/{agenceId}` | 404 si le véhicule ou l'agence n'existe pas ; sinon `vehicule.setAgence(agence)` puis `save`. Sert aussi au **transfert** d'une agence à une autre. |
| `desaffecterVehiculeDeAgence(vehiculeId)` | `PUT /api/vehicules/{vehiculeId}/desaffectation` | 404 si le véhicule n'existe pas ; **409 Conflict** s'il n'est rattaché à aucune agence (comportement choisi : signaler l'état incohérent plutôt que réussir silencieusement) ; sinon `agence_id = NULL`. |
| `affecterEmployeAAgence(employeId, agenceId)` | `PUT /api/employes/{employeId}/agence/{agenceId}` | idem véhicule. |
| `desaffecterEmployeDeAgence(employeId)` | `PUT /api/employes/{employeId}/desaffectation` | idem véhicule. |

Les services sont `@Transactional` : si l'agence n'existe pas, l'exception est levée **avant** toute
modification et la transaction est annulée — la base reste cohérente (vérifié dans `AffectationTests`).
Les réponses sont des `VehiculeDTO` / `EmployeDTO` (aucune entité exposée).

Tests (`AffectationTests`) : affectation à une agence existante, véhicule inexistant, agence inexistante,
transfert, désaffectation avec vérification de `agence_id` relu en base, double désaffectation (409), mêmes
tests pour l'employé. Vérification SQL possible : `SELECT id_vehicule, agence_id FROM vehicule;`.

## Partie 2 — DTO et MapStruct

DTO créés (Lombok `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`) : `AgenceDTO`,
`ClientDTO`, `ContratDTO`, `PaiementDTO`, `ReservationRequestDTO`, `ReservationResponseDTO`, `VehiculeDTO`,
et pour que **aucun** contrôleur ne renvoie d'entité : `EmployeDTO`, `EquipementDTO`, `MaintenanceDTO`.

MapStruct 1.5.5.Final (`pom.xml` : dépendance `mapstruct` + `mapstruct-processor` et
`lombok-mapstruct-binding` dans `annotationProcessorPaths`, Lombok d'abord). Chaque mapper est
`@Mapper(componentModel = "spring")` :
- les identifiants du modèle s'appellent `idAgence`, `idClient`… → `@Mapping(target = "id", source = "idAgence")` ;
- les associations sont aplaties en identifiants (`agenceId ← agence.idAgence`, `contratId ← contrat.idContrat`) ;
- `toEntity` ignore les associations (le service les charge depuis la base à partir des identifiants) ;
- `updateEntity(dto, @MappingTarget entite)` réalise le « charger puis recopier » de l'Atelier 4 ;
- `ReservationMapper` : `clientNomComplet` via une méthode `@Named("nomComplet")` (sûre si le client est
  `null`, contrairement à l'`expression` de l'énoncé), `tarifEstime` ignoré puis calculé par le service.

## Partie 3 — Services et contrôleurs en DTO

Tous les services reçoivent/renvoient des DTO et sont `@Transactional` (`readOnly = true` pour les
lectures). Tous les contrôleurs renvoient `ResponseEntity<…DTO>`. Choix particuliers :
- **Paiement** : composition → création par `POST /api/contrats/{id}/paiements` (le `contratId` du corps est
  obligatoire et doit correspondre au chemin), suppression par
  `DELETE /api/contrats/{id}/paiements/{paiementId}` (orphanRemoval). `PaiementController` reste en lecture
  seule (POST → 405).
- **Contrat** : `reservationId` rattache le contrat à une réservation (côté propriétaire
  `reservation.contrat_id`) ; la suppression d'un contrat libère d'abord cette clé étrangère.
- **Réservation** : création finalisée dans le service (`creer`) avec la `TarificationStrategyFactory` et le
  `ReservationSubject` de l'Atelier 6 (le support ne fournit pas de `ReservationFactory`).

## Partie 4 — Corrections SonarLint (règles appliquées)

> À faire dans IntelliJ pour le compte rendu : *Analyze with SonarQube for IDE* sur le projet et faire les
> captures. Les corrections ci-dessous ont été appliquées en suivant les règles correspondantes.

| Constat | Règle | Correction |
|---|---|---|
| Imports joker (`jakarta.persistence.*`, `lombok.*`, `org.springframework.web.bind.annotation.*`) | java:S2208 | Imports explicites partout. |
| Condition à 6 opérateurs dans `AgenceServiceImpl.valider` et équivalents | java:S1067 | Règles extraites dans `Regles` (une condition par attribut). |
| Littéraux dupliqués (`"Agence"`, `"Vehicule"`…) | java:S1192 | Constante `RESSOURCE` par service. |
| Code dupliqué (chargement + 404 répété, calcul jours × tarif dans chaque stratégie) | duplication | Méthodes privées `charger(...)`, classe utilitaire `DureeLocation`. |
| Mauvaise exception (`IllegalArgumentException("Client introuvable")` → 400) | gestion des exceptions | `ResourceNotFoundException` (404) ; `IllegalStateException` (409) pour les états incohérents. |
| `ResponseEntity<?>` | java:S1452 | `ResponseEntity<Object>`. |
| `expression = "java(reservation.getClient().getPrenom() ...)"` | gestion imprécise des `null` | Méthode `@Named` null-safe. |
| BOM UTF-8 en tête de `application.properties` (clé lue `﻿spring.application.name`) | fichier mal encodé | BOM supprimé. |
| Commentaires de décompilation, `;` vides, `@ToString.Exclude` sans effet | java:S125, S1116, code mort | Supprimés. |

## Partie 5 — Validation

Contraintes (messages explicites) :

| DTO | Contraintes |
|---|---|
| `AgenceDTO` | `nom`, `ville` : `@NotBlank` |
| `ClientDTO` | `nom`, `prenom` : `@NotBlank` ; `email` : `@NotBlank @Email` |
| `ContratDTO` | `montantTotal` : `@NotNull @PositiveOrZero` |
| `PaiementDTO` | `montant` : `@NotNull @Positive` ; `modePaiement`, `contratId` : `@NotNull` |
| `ReservationRequestDTO` | `clientId`, `vehiculeId` : `@NotNull` ; `dateDebut` : `@NotNull @FutureOrPresent` ; `dateFin` : `@NotNull @Future` |
| `VehiculeDTO` | `immatriculation`, `marque`, `modele` : `@NotBlank` ; `categorie`, `agenceId` : `@NotNull` ; `tarifJournalier` : `@NotNull @Positive` |
| `EmployeDTO`, `EquipementDTO`, `MaintenanceDTO` | champs obligatoires (`@NotBlank` / `@NotNull`) |

`dateDebut` utilise `@FutureOrPresent` (et non `@Future`) pour rester cohérent avec la règle du validateur
« aujourd'hui ou dans le futur ».

`@Valid` est posé sur chaque `@RequestBody`. Les erreurs sont renvoyées en 400 par `GlobalExceptionHandler`
(`details` : `"email : L'adresse email est invalide"`…).

**Validateur personnalisé** `web.validator.ReservationRequestValidator` (`org.springframework.validation.Validator`) :
date de début ≥ aujourd'hui, date de fin strictement postérieure à la date de début. Il est injecté dans
`ReservationController` et appelé explicitement après `@Valid` (`BindingResult` placé juste après le
paramètre `@Valid`) ; en cas d'erreur : 400 avec la liste `champ : message`.

Tests (`ValidationTests`, unitaires) : email invalide, véhicule sans immatriculation, paiement à 0 et
négatif, réservation sans dates, date de fin antérieure **et** égale à la date de début, date de début
passée, dates cohérentes acceptées. Les mêmes cas existent en HTTP dans la collection Postman.
