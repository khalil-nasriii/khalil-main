-- =====================================================================
--  AutoLoc - base de données autoloc_db (MySQL / MariaDB - XAMPP)
--  Import : phpMyAdmin > Importer > ce fichier, ou
--           mysql -u root < docs/database/autoloc_db.sql
--
--  Le schéma est identique à celui généré par Hibernate
--  (spring.jpa.hibernate.ddl-auto=update) à partir du package domain.
--  ATTENTION : le script SUPPRIME puis recrée les tables (données remises à zéro).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS autoloc_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE autoloc_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS paiement, reservation, contrat, maintenance,
                     vehicule_equipements, equipement, employe, vehicule,
                     client, agence;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------
CREATE TABLE agence (
  id_agence  BIGINT NOT NULL AUTO_INCREMENT,
  adresse    VARCHAR(255),
  nom        VARCHAR(255),
  telephone  VARCHAR(255),
  ville      VARCHAR(255),
  PRIMARY KEY (id_agence)
) ENGINE=InnoDB;

CREATE TABLE client (
  id_client        BIGINT NOT NULL AUTO_INCREMENT,
  date_inscription DATE,
  email            VARCHAR(255),
  nom              VARCHAR(255),
  num_permis       VARCHAR(255),
  prenom           VARCHAR(255),
  telephone        VARCHAR(255),
  PRIMARY KEY (id_client)
) ENGINE=InnoDB;

CREATE TABLE vehicule (
  id_vehicule      BIGINT NOT NULL AUTO_INCREMENT,
  categorie        ENUM('CITADINE','BERLINE','SUV','UTILITAIRE'),
  immatriculation  VARCHAR(255),
  marque           VARCHAR(255),
  modele           VARCHAR(255),
  statut           ENUM('DISPONIBLE','LOUE','MAINTENANCE'),
  tarif_journalier DECIMAL(38,2),
  agence_id        BIGINT,
  PRIMARY KEY (id_vehicule),
  CONSTRAINT fk_vehicule_agence FOREIGN KEY (agence_id) REFERENCES agence (id_agence)
) ENGINE=InnoDB;

CREATE TABLE employe (
  id_employe BIGINT NOT NULL AUTO_INCREMENT,
  nom        VARCHAR(255),
  prenom     VARCHAR(255),
  role       ENUM('AGENT','MANAGER'),
  agence_id  BIGINT,
  PRIMARY KEY (id_employe),
  CONSTRAINT fk_employe_agence FOREIGN KEY (agence_id) REFERENCES agence (id_agence)
) ENGINE=InnoDB;

CREATE TABLE equipement (
  id_equipement BIGINT NOT NULL AUTO_INCREMENT,
  libelle       VARCHAR(255),
  PRIMARY KEY (id_equipement)
) ENGINE=InnoDB;

-- table de jointure du @ManyToMany Vehicule.equipements
CREATE TABLE vehicule_equipements (
  vehicule_id   BIGINT NOT NULL,
  equipement_id BIGINT NOT NULL,
  CONSTRAINT fk_ve_vehicule   FOREIGN KEY (vehicule_id)   REFERENCES vehicule (id_vehicule),
  CONSTRAINT fk_ve_equipement FOREIGN KEY (equipement_id) REFERENCES equipement (id_equipement)
) ENGINE=InnoDB;

CREATE TABLE maintenance (
  id_maintenance BIGINT NOT NULL AUTO_INCREMENT,
  date_debut     DATE,
  date_fin       DATE,
  description    VARCHAR(255),
  vehicule_id    BIGINT,
  PRIMARY KEY (id_maintenance),
  CONSTRAINT fk_maintenance_vehicule FOREIGN KEY (vehicule_id) REFERENCES vehicule (id_vehicule)
) ENGINE=InnoDB;

CREATE TABLE contrat (
  id_contrat     BIGINT NOT NULL AUTO_INCREMENT,
  date_signature DATE,
  montant_total  DECIMAL(38,2),
  valide         BIT(1) NOT NULL,
  PRIMARY KEY (id_contrat)
) ENGINE=InnoDB;

-- composition Contrat -> Paiement (cascade ALL + orphanRemoval côté JPA)
CREATE TABLE paiement (
  id_paiement   BIGINT NOT NULL AUTO_INCREMENT,
  date_paiement DATE,
  mode_paiement ENUM('CARTE','ESPECES','VIREMENT'),
  montant       DECIMAL(38,2),
  contrat_id    BIGINT,
  PRIMARY KEY (id_paiement),
  CONSTRAINT fk_paiement_contrat FOREIGN KEY (contrat_id) REFERENCES contrat (id_contrat)
) ENGINE=InnoDB;

-- Reservation est propriétaire du @OneToOne vers Contrat (contrat_id unique)
CREATE TABLE reservation (
  id_reservation BIGINT NOT NULL AUTO_INCREMENT,
  date_debut     DATE,
  date_fin       DATE,
  statut         ENUM('EN_ATTENTE','CONFIRMEE','ANNULEE','TERMINEE'),
  client_id      BIGINT,
  contrat_id     BIGINT,
  vehicule_id    BIGINT,
  PRIMARY KEY (id_reservation),
  CONSTRAINT uk_reservation_contrat UNIQUE (contrat_id),
  CONSTRAINT fk_reservation_client   FOREIGN KEY (client_id)   REFERENCES client (id_client),
  CONSTRAINT fk_reservation_contrat  FOREIGN KEY (contrat_id)  REFERENCES contrat (id_contrat),
  CONSTRAINT fk_reservation_vehicule FOREIGN KEY (vehicule_id) REFERENCES vehicule (id_vehicule)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Jeu de données de démonstration
-- ---------------------------------------------------------------------
INSERT INTO agence (id_agence, nom, ville, adresse, telephone) VALUES
  (1, 'AutoLoc Tunis Centre', 'Tunis',  '12 Avenue Habib Bourguiba', '71123456'),
  (2, 'AutoLoc Sousse',       'Sousse', '5 Boulevard du 14 Janvier', '73222333'),
  (3, 'AutoLoc Sfax',         'Sfax',   '20 Rue de la Liberté',      '74987654');

INSERT INTO client (id_client, nom, prenom, email, telephone, num_permis, date_inscription) VALUES
  (1, 'Ben Ali',  'Ahmed', 'ahmed.benali@mail.tn',  '20111222', 'P-100234', '2026-01-15'),
  (2, 'Trabelsi', 'Sarra', 'sarra.trabelsi@mail.tn','55333444', 'P-200567', '2026-03-02'),
  (3, 'Mansour',  'Yassine','yassine.mansour@mail.tn','98555666','P-300891', '2026-06-20');

INSERT INTO vehicule (id_vehicule, immatriculation, marque, modele, categorie, tarif_journalier, statut, agence_id) VALUES
  (1, '200 TUN 4567', 'Peugeot', '208',     'CITADINE',   80.00,  'DISPONIBLE',  1),
  (2, '215 TUN 1020', 'Renault', 'Megane',  'BERLINE',    110.00, 'LOUE',        1),
  (3, '198 TUN 7788', 'Hyundai', 'Tucson',  'SUV',        150.00, 'DISPONIBLE',  2),
  (4, '220 TUN 3344', 'Renault', 'Kangoo',  'UTILITAIRE', 120.00, 'MAINTENANCE', 3),
  (5, '225 TUN 5566', 'Kia',     'Picanto', 'CITADINE',   70.00,  'DISPONIBLE',  NULL);

INSERT INTO employe (id_employe, nom, prenom, role, agence_id) VALUES
  (1, 'Gharbi',  'Karim', 'MANAGER', 1),
  (2, 'Jebali',  'Amira', 'AGENT',   1),
  (3, 'Hammami', 'Sami',  'AGENT',   2),
  (4, 'Ayari',   'Nour',  'AGENT',   NULL);

INSERT INTO equipement (id_equipement, libelle) VALUES
  (1, 'GPS'), (2, 'Siège bébé'), (3, 'Chaînes neige'), (4, 'Wi-Fi embarqué');

INSERT INTO vehicule_equipements (vehicule_id, equipement_id) VALUES
  (1, 1), (2, 1), (2, 4), (3, 1), (3, 2);

INSERT INTO maintenance (id_maintenance, date_debut, date_fin, description, vehicule_id) VALUES
  (1, '2026-10-01', '2026-10-03', 'Vidange et contrôle des freins', 1),
  (2, '2026-11-10', NULL,         'Remplacement embrayage',         4);

INSERT INTO contrat (id_contrat, date_signature, montant_total, valide) VALUES
  (1, '2026-11-02', 480.00, b'1'),
  (2, '2026-11-05', 750.00, b'1');

INSERT INTO paiement (id_paiement, montant, date_paiement, mode_paiement, contrat_id) VALUES
  (1, 150.00, '2026-11-02', 'CARTE',    1),
  (2, 330.00, '2026-11-06', 'VIREMENT', 1),
  (3, 750.00, '2026-11-05', 'ESPECES',  2);

INSERT INTO reservation (id_reservation, date_debut, date_fin, statut, client_id, vehicule_id, contrat_id) VALUES
  (1, '2026-11-03', '2026-11-07', 'TERMINEE',   1, 2, 1),
  (2, '2026-11-06', '2026-11-11', 'CONFIRMEE',  2, 3, 2),
  (3, '2026-12-19', '2026-12-21', 'EN_ATTENTE', 3, 1, NULL);
