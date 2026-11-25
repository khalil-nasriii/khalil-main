# Atelier 8 — Spring Data JPA : Query Methods et JPQL

## Adaptation au modèle AutoLoc

Dans le modèle, les identifiants s'appellent `idAgence`, `idVehicule`, `idClient`, `idContrat`… et non `id`.
Spring Data construit la requête à partir des **noms de propriétés** : une méthode `findByStatutAndAgence_Id`
ferait échouer le démarrage (*No property 'id' found for type 'Agence'*). Les signatures du support sont donc
reprises avec les vrais noms :

| Signature du support | Signature dans le projet |
|---|---|
| `findByStatutAndAgence_Id` | `findByStatutAndAgence_IdAgence` |
| `existsByIdAndStatut` | `existsByIdVehiculeAndStatut` |
| `findByVehicule_IdAndStatutNot` | `findByVehicule_IdVehiculeAndStatutNot` |
| `findByStatutAndVehiculeAgenceId` | `findByStatutAndVehiculeAgenceIdAgence` |
| `existsByVehicule_IdAndStatut` | `existsByVehicule_IdVehiculeAndStatut` |
| `findByAgence_IdAndRole` | `findByAgence_IdAgenceAndRole` |
| `findByContrat_Id` | `findByContrat_IdContrat` |
| `Optional<Agence> findByVille` | `Optional<Agence> findFirstByVilleOrderByIdAgenceAsc` + `List<Agence> findByVille` |
| `Optional<Agence> findByNomContaining` | `List<Agence> findByNomContaining` |

**Pourquoi changer les deux méthodes d'Agence ?** La ville et le nom d'une agence ne sont pas uniques
(la base de démo a déjà plusieurs agences dans la même ville possible). Un `Optional` impose *au plus un*
résultat : dès que deux lignes correspondent, Spring lève `IncorrectResultSizeDataAccessException` (erreur 500).
`findFirstBy…OrderByIdAgenceAsc` garde le contrat « une agence ou aucune » de manière sûre ; les listes
servent à la recherche. À l'inverse, l'email d'un client **est** rendu unique : `ClientServiceImpl` refuse
désormais un email déjà utilisé (`existsByEmail` → 409 Conflict), ce qui garantit `Optional<Client> findByEmail`.

Le `_` (ex. `Agence_Ville`) lève l'ambiguïté du découpage : il indique explicitement que `Ville` est une
propriété de l'association `agence`. Sans `_` (`findByAgenceVille`), Spring trouve lui-même le découpage.

## Partie I — Méthodes dérivées

| Repository | Méthodes | Keywords |
|---|---|---|
| `IVehiculeRepository` | `findByAgenceVille`, `findByStatutAndAgence_IdAgence`, `findByStatut(…, Pageable)`, `findByAgence_VilleAndCategorie`, `existsByIdVehiculeAndStatut`, `countByStatut`, `findByTarifJournalierLessThan`, `findByTarifJournalierGreaterThan`, `findAllByOrderByTarifJournalierAsc` | navigation, And, Page, exists, count, LessThan/GreaterThan (stricts), OrderBy |
| `IClientRepository` | `findByEmail`, `findByNumPermis`, `findByNomContaining`, `existsByEmail` | Optional, Containing (`LIKE %x%`) |
| `IReservationRepository` | `findByClientIdClient`, `findByVehicule_IdVehiculeAndStatutNot`, `findByStatut`, `findByDateDebutBetween`, `findByDateFinAfter`, `findByStatutAndVehiculeAgenceIdAgence`, `existsByVehicule_IdVehiculeAndStatut` | Not, Between (bornes incluses), After (strict), navigation sur 2 niveaux |
| `IEmployeRepository` | `findByAgenceNom`, `findByAgence_IdAgenceAndRole`, `findByNomContaining` | |
| `IAgenceRepository` | `findFirstByVilleOrderByIdAgenceAsc`, `findByVille`, `findByNomContaining` | First, OrderBy |
| `IPaiementRepository` | `findByContrat_IdContrat`, `findByDatePaiementBetween` | |

## Partie II — JPQL (`@Query` + `@Param`)

Les requêtes portent sur les **entités et propriétés Java** (`Vehicule v`, `v.agence.ville`), jamais sur les
tables (`vehicule`, `agence_id`). Les constantes d'énumération sont écrites avec leur nom complet
(`tn.esprit.autoloc.domain.StatutReservation.CONFIRMEE`).

### 7.1 `rechercherVehiculesDisponibles(ville, categorie, dateDebut, dateFin)`
- ville de l'agence et catégorie du véhicule ;
- **statut** : un véhicule en `MAINTENANCE` est exclu. `LOUE` décrit l'état actuel et n'empêche pas une location
  future : la disponibilité sur la période est déterminée par les réservations ;
- **réservations bloquantes** : statut `EN_ATTENTE` ou `CONFIRMEE` (une réservation `ANNULEE` ou `TERMINEE`
  ne bloque pas) ;
- **chevauchement** : `r.dateDebut < :dateFin AND r.dateFin > :dateDebut` (`NOT EXISTS`). Deux périodes qui
  *se touchent* (retour le 15, nouveau départ le 15) ne se chevauchent pas : le véhicule est rendu le jour même ;
- résultat trié par tarif croissant.

### 7.2 `trouverVehiculesAReleaser(aujourdhui)`
Véhicules ayant une réservation encore `CONFIRMEE` dont `dateFin <= aujourdhui` : la location est arrivée à son
terme mais n'a pas été clôturée (à passer en `TERMINEE` et à remettre `DISPONIBLE`). `DISTINCT` évite les
doublons si un véhicule a plusieurs réservations concernées.

### 8.1 `topClients(Pageable)` → `TopClientDTO(clientId, nom, prenom, nombreReservations)`
`SELECT new …TopClientDTO(c.idClient, c.nom, c.prenom, COUNT(r)) FROM Reservation r JOIN r.client c
GROUP BY c.idClient, c.nom, c.prenom ORDER BY COUNT(r) DESC, c.idClient ASC`.
Toutes les réservations comptent (quel que soit leur statut). Le `Pageable` (`PageRequest.of(0, 5)`) limite le
résultat : JPQL n'a pas de `LIMIT`. Le second critère de tri rend l'ordre déterministe en cas d'égalité.

### 9.1 `chiffreAffaireParAgence()` → `ChiffreAffaireAgenceDTO(agenceId, nomAgence, chiffreAffaires)`
**Règle de calcul retenue :** chiffre d'affaires d'une agence = **somme des `montantTotal` des contrats valides**
(`valide = true`) rattachés à une réservation dont le **véhicule** appartient à l'agence.
Chemin : `Reservation r JOIN r.contrat c JOIN r.vehicule v JOIN v.agence a`, `GROUP BY a.idAgence, a.nom`,
trié par montant décroissant. On part de `Reservation` car c'est elle qui porte la clé étrangère `contrat_id`.
Un contrat non valide est exclu ; une agence sans contrat valide n'apparaît pas. On s'appuie sur le montant
contractuel et non sur les paiements encaissés (qui mesureraient la trésorerie, pas le chiffre d'affaires).

### 9.2 `trouverContratsAEcheanceProche(aujourdhui, dateLimite)`
`Contrat` n'a pas de date d'échéance. **Propriété métier retenue :** la date de fin de la réservation couverte
par le contrat (`c.reservation.dateFin`) — le contrat de location prend fin au retour du véhicule. Aucun
changement du modèle ni de la base n'est nécessaire. Contrats **valides** dont l'échéance est
`BETWEEN :aujourdhui AND :dateLimite` (bornes incluses), triés par échéance.

## Endpoints (préfixe `/api`)

| Partie | Endpoint | Méthode de repository |
|---|---|---|
| I | `GET /vehicules/ville?ville=Tunis` | `findByAgenceVille` |
| I | `GET /vehicules/statut?statut=DISPONIBLE&page=0&size=5` (aussi `/vehicules/recherche/statut`) | `findByStatut(…, Pageable)` |
| I | `GET /vehicules/recherche?ville=Tunis&categorie=BERLINE` | `findByAgence_VilleAndCategorie` |
| I | `GET /vehicules/agence/{agenceId}?statut=DISPONIBLE` | `findByStatutAndAgence_IdAgence` |
| I | `GET /vehicules/{id}/a-le-statut?statut=…` · `GET /vehicules/compteur?statut=…` | `existsBy…` · `countByStatut` |
| I | `GET /vehicules/tarif-inferieur?tarif=100` · `/tarif-superieur?tarif=100` · `/tries-par-tarif` | LessThan · GreaterThan · OrderBy |
| I | `GET /clients/email?email=…` · `/clients/permis?numPermis=…` · `/clients/recherche?nom=…` · `/clients/existe?email=…` | Exercice 2 |
| I | `GET /agences/ville?ville=…` · `/agences/ville/toutes?ville=…` · `/agences/recherche?nom=…` | Exercice 5 |
| I | `GET /employes/agence?nom=…` · `/employes/agence/{agenceId}?role=AGENT` · `/employes/recherche?nom=…` | Exercice 4 |
| I | `GET /reservations/client/{id}` · `/reservations/vehicule/{id}?statutExclu=ANNULEE` · `/reservations/vehicule/{id}/existe?statut=…` · `/reservations/statut?statut=…` · `/reservations/periode?debut=…&fin=…` · `/reservations/fin-apres?date=…` · `/reservations/agence/{id}?statut=…` | Exercice 3 |
| I | `GET /paiements/contrat/{id}` · `/paiements/periode?debut=…&fin=…` | Exercice 6 |
| II | `GET /vehicules/disponibles?ville=…&categorie=…&dateDebut=…&dateFin=…` (aussi `/jpql/vehicules/disponibles`) | 7.1 |
| II | `GET /vehicules/a-liberer?aujourdhui=…` (aussi `/jpql/vehicules/a-liberer`) | 7.2 |
| II | `GET /reservations/top-clients?limit=5` | 8.1 |
| II | `GET /reservations/chiffre-affaires-agences` (aussi `/reservations/chiffre-affaires/agences`) | 9.1 |
| II | `GET /contrats/echeance?aujourdhui=…&dateLimite=…` | 9.2 |

Les contrôleurs délèguent au service, qui renvoie des DTO (jamais d'entité). Erreurs : paramètre manquant ou
valeur invalide (statut inconnu, date mal formée) → 400 ; période dont la fin précède le début → 400 ;
recherche « un seul résultat » sans correspondance (email, permis, ville) → 404 ; email en double → 409.
Une `Page` est sérialisée en `{ "content": [...], "page": { "size", "number", "totalElements", "totalPages" } }`
(`WebConfig`, `PageSerializationMode.VIA_DTO`).

## Vérifications (Exercice 10)

**Tests JUnit — `RequetesTests`** (jeu de données propre à chaque test, annulé par `@Transactional`) :
- Partie I : cas nominal et résultat vide pour chaque méthode ; navigation sur une et deux associations ;
  pagination (taille de page, total = `countByStatut`) ; tri croissant ; `LessThan`/`GreaterThan` stricts
  (150 n'est ni < 150 ni > 150) ; `Between` bornes incluses, `After` strict ; `existsBy…` vrai/faux.
- 7.1 : chevauchement, chevauchement d'un seul jour, bornes qui se touchent (avant et après), réservation
  annulée ignorée, véhicule en maintenance exclu, aucune réservation, aucun résultat.
- 7.2 : la veille de la fin → absent ; le jour de la fin → présent ; réservation annulée → absent.
- 8.1 : ordre décroissant, client à 3 réservations avant client à 2, mapping complet du record, limite à 1 ligne.
- 9.1 : agence A = 300 + 200 = 500, agence B = 50 (le contrat invalide de 1000 est exclu), agence sans contrat
  absente, nom de l'agence correct.
- 9.2 : intervalle, bornes incluses, intervalle vide, contrat invalide exclu.

**Postman** — dossiers « Atelier 8 — Partie I » et « Atelier 8 — Partie II » de la collection : ils créent leurs
propres données (agence dans une ville unique, 2 berlines, un client, une réservation, un contrat de 400 et
un paiement), vérifient le code HTTP, le JSON et les cas sans résultat, puis suppriment tout.
