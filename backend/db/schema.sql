-- Gestion des performances des territoires : schéma MySQL 8
CREATE DATABASE IF NOT EXISTS performances_territoires
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE performances_territoires;

CREATE TABLE roles (
  id TINYINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL UNIQUE,   -- ADMIN, RESPONSABLE_TERRITORIAL, EVALUATEUR, LECTEUR
  libelle VARCHAR(80) NOT NULL
);

CREATE TABLE utilisateurs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  mot_de_passe VARCHAR(255) NOT NULL,
  role_id TINYINT NOT NULL,
  actif BOOLEAN NOT NULL DEFAULT TRUE,
  cree_le TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE regions (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE collectivites (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(150) NOT NULL,
  type ENUM('COMMUNE','DEPARTEMENT','VILLE','POLE_TERRITORIAL') NOT NULL,
  region_id INT NOT NULL,
  responsable VARCHAR(150),
  adresse VARCHAR(255),
  telephone VARCHAR(30),
  email VARCHAR(150),
  UNIQUE KEY uk_collectivite (nom, type, region_id),
  FOREIGN KEY (region_id) REFERENCES regions(id)
);

CREATE TABLE categories_indicateurs (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE indicateurs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(150) NOT NULL,
  description TEXT,
  categorie_id INT NOT NULL,
  unite VARCHAR(30) NOT NULL,
  periodicite ENUM('MENSUELLE','TRIMESTRIELLE','SEMESTRIELLE','ANNUELLE') NOT NULL,
  sens ENUM('HAUSSE_FAVORABLE','BAISSE_FAVORABLE') NOT NULL,
  -- Règle si l'objectif vaut 0 : APPLIQUER (100 si objectif respecté, sinon 0) ou IGNORER (indicateur exclu du score)
  regle_objectif_zero ENUM('APPLIQUER','IGNORER') NOT NULL DEFAULT 'APPLIQUER',
  UNIQUE KEY uk_indicateur (nom, categorie_id),
  FOREIGN KEY (categorie_id) REFERENCES categories_indicateurs(id)
);

-- Association indicateur / collectivité avec objectif et pondération
CREATE TABLE objectifs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  collectivite_id BIGINT NOT NULL,
  indicateur_id BIGINT NOT NULL,
  annee SMALLINT NOT NULL,
  valeur_cible DECIMAL(18,4) NOT NULL,
  ponderation DECIMAL(6,2) NOT NULL CHECK (ponderation > 0),
  UNIQUE KEY uk_objectif (collectivite_id, indicateur_id, annee),
  FOREIGN KEY (collectivite_id) REFERENCES collectivites(id) ON DELETE CASCADE,
  FOREIGN KEY (indicateur_id) REFERENCES indicateurs(id)
);

CREATE TABLE realisations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  objectif_id BIGINT NOT NULL,
  periode VARCHAR(10) NOT NULL,        -- ex. 2026, 2026-T1, 2026-03
  valeur DECIMAL(18,4) NOT NULL,
  statut ENUM('SAISIE','VALIDEE','REJETEE') NOT NULL DEFAULT 'SAISIE',
  saisi_par BIGINT NOT NULL,
  valide_par BIGINT NULL,
  saisi_le TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  valide_le TIMESTAMP NULL,
  UNIQUE KEY uk_realisation (objectif_id, periode),
  FOREIGN KEY (objectif_id) REFERENCES objectifs(id) ON DELETE CASCADE,
  FOREIGN KEY (saisi_par) REFERENCES utilisateurs(id),
  FOREIGN KEY (valide_par) REFERENCES utilisateurs(id)
);

-- Traçabilité des calculs de score
CREATE TABLE evaluations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  realisation_id BIGINT NOT NULL,
  score DECIMAL(5,2) NULL,             -- NULL = indicateur non valide (exclu du global)
  formule VARCHAR(60) NOT NULL,
  detail VARCHAR(255) NOT NULL,
  calcule_le TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (realisation_id) REFERENCES realisations(id) ON DELETE CASCADE
);

CREATE TABLE domaines_competence (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE projets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(200) NOT NULL,
  collectivite_id BIGINT NOT NULL,
  domaine_id INT NOT NULL,
  budget_prevu DECIMAL(18,2) NOT NULL CHECK (budget_prevu >= 0),
  budget_execute DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (budget_execute >= 0),
  avancement TINYINT NOT NULL DEFAULT 0 CHECK (avancement BETWEEN 0 AND 100),
  statut ENUM('PLANIFIE','EN_COURS','TERMINE','SUSPENDU') NOT NULL DEFAULT 'PLANIFIE',
  FOREIGN KEY (collectivite_id) REFERENCES collectivites(id),
  FOREIGN KEY (domaine_id) REFERENCES domaines_competence(id)
);

CREATE TABLE investissements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  projet_id BIGINT NOT NULL,
  date_operation DATE NOT NULL,
  montant DECIMAL(18,2) NOT NULL CHECK (montant > 0),
  source_financement VARCHAR(120),
  FOREIGN KEY (projet_id) REFERENCES projets(id) ON DELETE CASCADE
);

CREATE TABLE rapports (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  titre VARCHAR(200) NOT NULL,
  format ENUM('PDF','EXCEL') NOT NULL,
  parametres JSON,
  genere_par BIGINT NOT NULL,
  genere_le TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (genere_par) REFERENCES utilisateurs(id)
);

INSERT INTO roles (code, libelle) VALUES
  ('ADMIN','Administrateur'),
  ('RESPONSABLE_TERRITORIAL','Responsable territorial'),
  ('EVALUATEUR','Évaluateur'),
  ('LECTEUR','Lecteur');

-- Données de référence (modifiables depuis l'application)
INSERT INTO regions (nom) VALUES
  ('Dakar'),('Diourbel'),('Fatick'),('Kaffrine'),('Kaolack'),('Kédougou'),('Kolda'),
  ('Louga'),('Matam'),('Saint-Louis'),('Sédhiou'),('Tambacounda'),('Thiès'),('Ziguinchor');

INSERT INTO domaines_competence (nom) VALUES
  ('Éducation et formation'),('Santé et action sociale'),('Environnement et ressources naturelles'),
  ('Urbanisme et habitat'),('Planification et aménagement du territoire'),
  ('Jeunesse, sports et loisirs'),('Culture'),('Gestion du domaine');

INSERT INTO categories_indicateurs (nom) VALUES
  ('Gouvernance'),('Finances locales'),('Services de base'),('Développement local');
