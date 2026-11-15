# Atelier 3 — Couche Repository (Spring Data JPA)

## 1. Choix d'interface

Les neuf interfaces sont dans `tn.esprit.autoloc.repository`, préfixées `I`, sans `@Repository`
(Spring Data génère le proxy au démarrage : *Found 9 JPA repository interfaces*).

| Interface | Étend | Justification |
|---|---|---|
| `IContratRepository` | `JpaRepository<Contrat, Long>` | CRUD complet, `findAll` renvoie une `List`, `saveAndFlush`/`flush` disponibles (utilisé pour obtenir l'id d'un paiement ajouté par cascade). Suppression via `delete` (pas en lot) pour que cascade REMOVE et orphanRemoval s'appliquent aux paiements. |
| `IPaiementRepository` | `JpaRepository<Paiement, Long>` | Lecture des paiements (`findAll`, `findById`). La création/suppression passe par le Contrat (composition). |
| `IAgenceRepository` | `JpaRepository<Agence, Long>` | CRUD + listes ; tri/pagination disponibles pour une future liste d'agences paginée. |
| `IEmployeRepository` | `JpaRepository<Employe, Long>` | CRUD + listes ; utilisé pour l'affectation/désaffectation à une agence (Atelier 7). |
| `IVehiculeRepository` | `JpaRepository<Vehicule, Long>` | CRUD + listes ; `save` sur une entité existante = merge (modification du tarif, de l'agence). |
| `IEquipementRepository` | `JpaRepository<Equipement, Long>` | CRUD simple + listes. |
| `IClientRepository` | `JpaRepository<Client, Long>` | CRUD + listes ; `existsById` utilisé par le service avant suppression. |
| `IReservationRepository` | `JpaRepository<Reservation, Long>` | CRUD + listes ; tri par date possible avec `findAll(Sort)`. |
| `IMaintenanceRepository` | `JpaRepository<Maintenance, Long>` | CRUD + listes. |

**Pourquoi `JpaRepository` partout ?** Il réunit `ListCrudRepository` (CRUD + `List`) et
`ListPagingAndSortingRepository` (tri, pagination) et ajoute `flush`, `saveAndFlush`,
`getReferenceById`. Depuis Spring Data 3, `PagingAndSortingRepository` n'étend plus `CrudRepository` :
`JpaRepository` évite de déclarer les deux.

**À éviter :** `deleteAllInBatch` / `deleteAllByIdInBatch` sur `Contrat` — ces suppressions en une requête
contournent le contexte de persistance : pas de cascade, pas d'orphanRemoval → paiements orphelins ou
violation de clé étrangère.

## 2. Comportement observé (logs SQL)

| Appel | SQL observé |
|---|---|
| `save(c)` avec `id == null` | `insert into contrat ...` puis `insert into paiement ...` (cascade PERSIST) |
| `save(v)` avec `id != null` | `select ... from vehicule where id_vehicule=?` puis `update vehicule ...` (merge) |
| `findById(id)` | `select ...` → `Optional` vide si absent (jamais `null`) |
| `deleteById(id)` inexistant | `select` seulement, **aucune exception** (Spring Data 3) → le service lève `ResourceNotFoundException` |
| `deleteById(contrat)` | `delete from paiement ...` puis `delete from contrat ...` (cascade REMOVE) |

Tests : `src/test/java/tn/esprit/autoloc/AutolocRepositoryTests.java` (Agence, Client, Véhicule guidés ;
Employé, Équipement, Contrat en exercice autonome).

## 3. Anomalies SonarQube for IDE

| Anomalie | Règle / explication | Correction apportée |
|---|---|---|
| `import jakarta.persistence.*;` et `import lombok.*;` dans les entités | **java:S2208** — les imports « joker » masquent l'origine des types et peuvent créer des conflits de noms. | Imports explicites dans les 9 entités. |
| En-têtes « Source code recreated from a .class file by IntelliJ IDEA » dans les énumérations | **java:S125** / commentaire trompeur : le fichier n'est pas décompilé, le commentaire est du bruit. | Commentaires supprimés. |
| `;` superflu après la dernière constante d'énumération sans corps | **java:S1116** — instruction vide inutile. | Supprimé dans les 5 énumérations. |
| `@ToString.Exclude` sans `@ToString` sur la classe | Code mort : l'annotation n'a aucun effet (Lombok `@ToString` absent, `@Data` interdit). | Annotations retirées ; les entités n'utilisent que `@Getter @Setter @NoArgsConstructor` (Lombok ciblé, sans `@Data`). |
