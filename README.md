# AutoLoc API — Gestion de location de véhicules

Projet Spring Boot des ateliers ASI 26-27 (Ateliers 3 à 8) : Spring Data JPA, couche Service (IoC/DI),
API REST + Swagger, patrons GoF (Strategy, Factory, Observer), DTO + MapStruct, validation, Query Methods et JPQL.

## Démarrage avec XAMPP

1. **Lancer MySQL** dans le panneau de contrôle XAMPP (bouton *Start* sur la ligne MySQL).
2. **Créer la base** (deux options) :
   - *Automatique* : rien à faire, l'URL JDBC contient `createDatabaseIfNotExist=true` et Hibernate crée
     les tables (`ddl-auto=update`).
   - *Avec données de démonstration* : phpMyAdmin (http://localhost/phpmyadmin) → onglet **Importer** →
     choisir `docs/database/autoloc_db.sql` → *Exécuter*.
     En ligne de commande : `C:\xampp\mysql\bin\mysql -u root < docs\database\autoloc_db.sql`.
     ⚠️ Le script **supprime et recrée** les tables d'`autoloc_db`.
3. Identifiants par défaut XAMPP : utilisateur `root`, mot de passe vide (voir
   `src/main/resources/application.properties`).
4. **Lancer l'application** : `AutolocApiApplication` dans IntelliJ, ou `./mvnw spring-boot:run`
   (Windows : `mvnw.cmd spring-boot:run`).

| | URL |
|---|---|
| API | http://localhost:8082/api/… (ex. `/api/agences`) |
| Swagger UI | http://localhost:8082/api/swagger-ui.html |
| OpenAPI JSON | http://localhost:8082/api/v3/api-docs |

## Tests

```
./mvnw test
```

MySQL doit être démarré pour les tests `@SpringBootTest` (`AutolocRepositoryTests`, `ServiceTests`,
`AffectationTests`, `ReservationServiceTest`, `RequetesTests`). `TarificationStrategyFactoryTest`, `NotificationFactoryTest`
et `ValidationTests` sont des tests unitaires sans base.

Postman : importer `postman/collections/AutoLoc.postman_collection.json` et
`postman/environments/local.postman_environment.json`, sélectionner l'environnement **local**, puis
*Run collection* (les requêtes s'enchaînent dans l'ordre et nettoient leurs données à la fin).

## Notes des ateliers

| Atelier | Note |
|---|---|
| 3 — Repository | [docs/repository-notes.md](docs/repository-notes.md) |
| 4 — IoC / Service | [docs/service-notes.md](docs/service-notes.md) |
| 5 — API REST / Swagger | [docs/api-notes.md](docs/api-notes.md) |
| 6 — GoF / SOLID | [docs/gof-solid-notes.md](docs/gof-solid-notes.md) |
| 7 — Affectation / DTO / Validation | [docs/atelier7-notes.md](docs/atelier7-notes.md) |
| 8 — Query Methods / JPQL | [docs/requetes-notes.md](docs/requetes-notes.md) |
