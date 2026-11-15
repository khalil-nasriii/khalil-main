# Atelier 5 — API REST, Postman et Swagger

Le préfixe `/api` est défini une seule fois (`server.servlet.context-path=/api`) ; les contrôleurs
utilisent `@RequestMapping("/agences")`, etc. Application sur **http://localhost:8082** (port configuré dans
`application.properties` ; l'énoncé utilise 8080 — adapter `baseUrl` si vous changez le port).

- Swagger UI : http://localhost:8082/api/swagger-ui.html
- OpenAPI JSON : http://localhost:8082/api/v3/api-docs
- Collection Postman : `postman/collections/AutoLoc.postman_collection.json`
  + environnement `postman/environments/local.postman_environment.json` (variable `baseUrl`).

Depuis l'Atelier 7, les contrôleurs reçoivent et renvoient des **DTO** (plus aucune entité JPA exposée).

## A. Endpoints

| Contrôleur | Verbe et route | Service appelé | Succès | Erreurs |
|---|---|---|---|---|
| AgenceController | `GET /api/agences` | `agenceService.findAll()` | 200 | — |
| AgenceController | `GET /api/agences/{id}` | `agenceService.findById(id)` | 200 | 404 |
| AgenceController | `POST /api/agences` | `agenceService.create(dto)` | 201 | 400 |
| AgenceController | `PUT /api/agences/{id}` | `agenceService.update(id, dto)` | 200 | 400, 404 |
| AgenceController | `DELETE /api/agences/{id}` | `agenceService.deleteById(id)` | 204 | 404 (409 si encore référencé) |
| ClientController | `GET /api/clients` | `clientService.findAll()` | 200 | — |
| ClientController | `GET /api/clients/{id}` | `clientService.findById(id)` | 200 | 404 |
| ClientController | `POST /api/clients` | `clientService.create(dto)` | 201 | 400 |
| ClientController | `PUT /api/clients/{id}` | `clientService.update(id, dto)` | 200 | 400, 404 |
| ClientController | `DELETE /api/clients/{id}` | `clientService.deleteById(id)` | 204 | 404 (409 si encore référencé) |
| VehiculeController | `GET /api/vehicules` | `vehiculeService.findAll()` | 200 | — |
| VehiculeController | `GET /api/vehicules/{id}` | `vehiculeService.findById(id)` | 200 | 404 |
| VehiculeController | `POST /api/vehicules` | `vehiculeService.create(dto)` | 201 | 400 |
| VehiculeController | `PUT /api/vehicules/{id}` | `vehiculeService.update(id, dto)` | 200 | 400, 404 |
| VehiculeController | `DELETE /api/vehicules/{id}` | `vehiculeService.deleteById(id)` | 204 | 404 (409 si encore référencé) |
| VehiculeController | `PUT /api/vehicules/{vehiculeId}/agence/{agenceId}` | `vehiculeService.affecterVehiculeAAgence` | 200 | 404 |
| VehiculeController | `PUT /api/vehicules/{vehiculeId}/desaffectation` | `vehiculeService.desaffecterVehiculeDeAgence` | 200 | 404, 409 |
| EmployeController | `GET /api/employes` | `employeService.findAll()` | 200 | — |
| EmployeController | `GET /api/employes/{id}` | `employeService.findById(id)` | 200 | 404 |
| EmployeController | `POST /api/employes` | `employeService.create(dto)` | 201 | 400 |
| EmployeController | `PUT /api/employes/{id}` | `employeService.update(id, dto)` | 200 | 400, 404 |
| EmployeController | `DELETE /api/employes/{id}` | `employeService.deleteById(id)` | 204 | 404 (409 si encore référencé) |
| EmployeController | `PUT /api/employes/{employeId}/agence/{agenceId}` | `employeService.affecterEmployeAAgence` | 200 | 404 |
| EmployeController | `PUT /api/employes/{employeId}/desaffectation` | `employeService.desaffecterEmployeDeAgence` | 200 | 404, 409 |
| EquipementController | `GET /api/equipements` | `equipementService.findAll()` | 200 | — |
| EquipementController | `GET /api/equipements/{id}` | `equipementService.findById(id)` | 200 | 404 |
| EquipementController | `POST /api/equipements` | `equipementService.create(dto)` | 201 | 400 |
| EquipementController | `PUT /api/equipements/{id}` | `equipementService.update(id, dto)` | 200 | 400, 404 |
| EquipementController | `DELETE /api/equipements/{id}` | `equipementService.deleteById(id)` | 204 | 404 (409 si encore référencé) |
| MaintenanceController | `GET /api/maintenances` | `maintenanceService.findAll()` | 200 | — |
| MaintenanceController | `GET /api/maintenances/{id}` | `maintenanceService.findById(id)` | 200 | 404 |
| MaintenanceController | `POST /api/maintenances` | `maintenanceService.create(dto)` | 201 | 400 |
| MaintenanceController | `PUT /api/maintenances/{id}` | `maintenanceService.update(id, dto)` | 200 | 400, 404 |
| MaintenanceController | `DELETE /api/maintenances/{id}` | `maintenanceService.deleteById(id)` | 204 | 404 (409 si encore référencé) |
| ContratController | `GET /api/contrats` | `contratService.findAll()` | 200 | — |
| ContratController | `GET /api/contrats/{id}` | `contratService.findById(id)` | 200 | 404 |
| ContratController | `POST /api/contrats` | `contratService.create(dto)` | 201 | 400 |
| ContratController | `PUT /api/contrats/{id}` | `contratService.update(id, dto)` | 200 | 400, 404 |
| ContratController | `DELETE /api/contrats/{id}` | `contratService.deleteById(id)` | 204 | 404 (409 si encore référencé) |
| ContratController | `POST /api/contrats/{id}/paiements` | `contratService.ajouterPaiement(id, dto)` | 201 | 400, 404 |
| ContratController | `DELETE /api/contrats/{id}/paiements/{paiementId}` | `contratService.retirerPaiement` | 204 | 404 |
| PaiementController | `GET /api/paiements` | `paiementService.findAll()` | 200 | — |
| PaiementController | `GET /api/paiements/{id}` | `paiementService.findById(id)` | 200 | 404 |
| PaiementController | `POST/PUT/DELETE /api/paiements…` | `— (aucune méthode)` | — | 405 |
| ReservationController | `GET /api/reservations` | `reservationService.findAll()` | 200 | — |
| ReservationController | `GET /api/reservations/{id}` | `reservationService.findById(id)` | 200 | 404 |
| ReservationController | `POST /api/reservations` | `reservationService.creer(dto)` | 201 | 400, 404 |
| ReservationController | `PUT /api/reservations/{id}` | `reservationService.update(id, dto)` | 200 | 400, 404 |
| ReservationController | `PUT /api/reservations/{id}/annulation` | `reservationService.annuler(id)` | 200 | 404, 409 |
| ReservationController | `DELETE /api/reservations/{id}` | `reservationService.deleteById(id)` | 204 | 404 |

Les codes d'erreur sont produits par `GlobalExceptionHandler` (corps JSON : `statut`, `message`,
`horodatage`, `details`) : `ResourceNotFoundException` → 404, `IllegalArgumentException` et
`@Valid` → 400, `IllegalStateException` → 409, contrainte de clé étrangère → 409.

## B. Annotations Jackson

| Association | Annotation retenue | Justification |
|---|---|---|
| `Contrat.paiements` / `Paiement.contrat` | `@JsonManagedReference("contrat-paiements")` / `@JsonBackReference("contrat-paiements")` | Composition : les paiements sont créés dans le JSON de leur contrat ; la paire coupe la boucle tout en reliant chaque paiement à son contrat à la désérialisation. |
| `Agence.vehicules`, `Agence.employes` | `@JsonIgnore` (côté inverse) | Enfant créé seul, qui désigne son parent par un identifiant ; la liste côté agence n'a pas à être sérialisée. |
| `Client.reservations`, `Vehicule.reservations`, `Vehicule.maintenances`, `Equipement.vehicules`, `Contrat.reservation` | `@JsonIgnore` | Côtés inverses : évitent la boucle et les chargements LAZY. |
| `Reservation.client`, `Reservation.vehicule` | aucune | Côté propriétaire : doit rester lisible à la désérialisation (un `@JsonBackReference` ferait ignorer le client au POST). |

**`@JsonBackReference` vs `@JsonIgnore`** : `@JsonBackReference` fonctionne en paire avec
`@JsonManagedReference` — le champ n'est pas écrit en sortie, mais Jackson le **renseigne** à la lecture à
partir du parent (le paiement reçoit son contrat). `@JsonIgnore` exclut complètement le champ, en lecture
comme en écriture. Avec les DTO de l'Atelier 7, ces annotations ne servent plus que de garde-fou : les
entités ne sont plus sérialisées.

## C. Postman et Swagger

- **Postman** teste : requêtes rejouables dans l'ordre (Run collection), variables (`{{baseUrl}}`,
  `{{contratId}}` mémorisé après le POST), assertions (`pm.response.to.have.status(201)`). Exemple : le
  scénario de cascade — créer un contrat, lui ajouter deux paiements, supprimer le contrat, puis constater
  que `GET /api/paiements/{{paiement2Id}}` renvoie 404 (cascade REMOVE) ; ou `POST /api/paiements` → 405.
- **Swagger UI** documente et explore : le contrat OpenAPI est généré depuis le code (`@Tag`, `@Operation`,
  `@ApiResponse`, `@Parameter`) et reste synchronisé. Exemple : la section *Schemas* montre `VehiculeDTO`
  avec ses contraintes (`immatriculation` obligatoire, `tarifJournalier` > 0) ; « Try it out » sur
  `GET /api/contrats/{id}` avec 9999 affiche directement le 404 documenté.

## D. Anomalies SonarQube for IDE (couche web)

| Règle | Explication | Correction |
|---|---|---|
| **java:S2208** | `import org.springframework.web.bind.annotation.*;` (imports joker). | Imports explicites. |
| Routes non REST | `/api/agences/api/add`, `/getByID/{id}`, `/getAll`, `/update/{id}` : le verbe est dans l'URL et `/api` était dupliqué (`/api/api/agences/...` avec le context-path). | Routes au pluriel, verbe porté par HTTP (`GET/POST/PUT/DELETE /api/agences[/{id}]`). |
| Entité exposée (couplage API/base) | Les contrôleurs renvoyaient `Agence`, `Contrat`… (champs internes visibles, boucles Jackson). | DTO + MapStruct (Atelier 7). |
| **java:S1452** | `ResponseEntity<?>` (type joker en retour) dans l'exemple de l'énoncé. | `ResponseEntity<Object>` dans `ReservationController`. |
| Annotation mal placée | `@Operation` séparée de sa méthode par des lignes vides, `@Operation(description=…)` sans `summary`. | `@Operation(summary = …)` + `@ApiResponses` sur chaque méthode. |

## E. Questions

1. **Composant d'entrée** : le `DispatcherServlet` reçoit toutes les requêtes ; il interroge les
   `HandlerMapping` (construits au démarrage à partir de `@RequestMapping`/`@GetMapping`…) pour trouver la
   méthode correspondant au verbe et au chemin, puis la `HandlerAdapter` l'invoque (conversion JSON via
   `HttpMessageConverter`, `@PathVariable`, `@RequestBody`, `@Valid`).
2. **`ContratController` → `IContratService`** : le contrôleur reste mince (HTTP uniquement) ; la logique
   métier et les transactions sont dans le service ; dépendre de l'interface (DIP) permet de changer
   l'implémentation ou de la simuler en test. Accéder au repository contournerait les règles métier.
3. **201 / 204 / 404** : 201 *Created* signale qu'une ressource a été créée (corps = ressource avec son
   id) ; 204 *No Content* : suppression réussie, rien à renvoyer. Une ressource absente est une erreur du
   client (mauvais identifiant) → 404 ; 500 signifierait une panne du serveur.
4. **`PaiementController` en lecture seule** : composition — un paiement n'existe qu'à travers son contrat
   (`POST /api/contrats/{id}/paiements`). Un `POST /api/paiements` reçoit **405 Method Not Allowed**.
5. **Risques de l'exposition des entités** : couplage API/schéma de base, fuite de champs internes,
   boucles de sérialisation, `LazyInitializationException`, sur-affectation (un client peut envoyer un id ou
   une association non prévue). `Contrat.paiements` utilise `@JsonManagedReference` car les paiements sont
   créés dans le JSON du contrat (composition) ; `Agence.vehicules` utilise `@JsonIgnore` car un véhicule
   est créé seul et désigne son agence par identifiant.
