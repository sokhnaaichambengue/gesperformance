# Gestion des performances des territoires

Application de suivi des performances des collectivités territoriales du Sénégal (communes, départements, villes, pôles territoriaux).

```
performances-territoires/
├── backend/    Spring Boot 3.3 (Java 17) · Spring Security + JWT · JPA · MySQL 8
│   ├── db/schema.sql                 schéma + données de référence (14 régions, domaines, catégories)
│   └── src/main/java/.../            controller · service · repository · entity · dto · scoring · security · config
└── frontend/   React 18 (Vite) · Axios · React Router
    └── src/                          api · auth · components · hooks · pages
```

## État de livraison (honnêteté)
Toutes les étapes sont écrites : socle/JWT, collectivités, indicateurs/objectifs, réalisations, moteur de scores,
projets/investissements, tableau de bord, rapports PDF/Excel.

| Vérification | Résultat |
|---|---|
| Moteur de calcul des scores (`ScoreCalculator`) | **Testé** : 14 cas exécutés (plafond 100, objectif 0, valeur 0, indicateurs manquants, score global pondéré) — tous passent. Les mêmes cas sont dans `ScoreCalculatorTest` (JUnit, lancé par `mvn test`). |
| Syntaxe des 61 fichiers Java | Vérifiée (aucune erreur de syntaxe) |
| Syntaxe des 21 fichiers React et résolution des imports | Vérifiée |
| **Compilation Maven, démarrage Spring, MySQL réel, échanges frontend ↔ backend** | **Non testés** (pas de Maven, de MySQL ni de réseau dans l'environnement de rédaction) |

La première exécution chez vous est donc le vrai test. Suivez le scénario de la section 4 ; en cas d'erreur de compilation ou
de démarrage, copiez le message tel quel pour correction.

## Prérequis
Java 17, Maven 3.9+, Node 20+, MySQL 8.

## 1. Base de données
```
mysql -u root -p < backend/db/schema.sql
```

## 2. Backend (http://localhost:8080)
```
cd backend
export DB_USER=root DB_PASSWORD=votre_mot_de_passe          # PowerShell : $env:DB_USER="root" ...
export JWT_SECRET="une-longue-phrase-secrete-d-au-moins-32-caracteres"
mvn test              # tests du moteur de scores
mvn spring-boot:run
```
Au premier démarrage, un compte **de démonstration** est créé : `admin@demo.local` / `Admin@2026`
(propriétés `app.demo-admin.*` ; à changer ou supprimer en production). CORS : `CORS_ORIGINS` (défaut `http://localhost:5173`).

## 3. Frontend (http://localhost:5173)
```
cd frontend
npm install
npm run dev
```
URL de l'API : `frontend/.env` (`VITE_API_URL=http://localhost:8080/api`).

## 4. Scénario de test de bout en bout
1. Connexion avec le compte de démonstration.
2. **Collectivités** : ajouter « Commune X » (région au choix) → elle apparaît dans le tableau ; vérifier dans MySQL : `SELECT * FROM collectivites;`.
3. **Indicateurs** : créer deux indicateurs (un « hausse favorable », un « baisse favorable »).
4. **Objectifs** : associer les deux indicateurs à la collectivité pour l'année en cours (objectif et pondération).
5. **Réalisations** : saisir une valeur pour chaque indicateur, puis **Valider**.
6. **Résultats** : le score par indicateur, le score global pondéré et la trace du calcul s'affichent.
7. **Tableau de bord** (bouton Actualiser) : compteurs, score moyen, classement. **Projets** : créer un projet, ajouter un investissement → le budget exécuté augmente.
8. **Rapports** : télécharger un PDF et un Excel.
9. **Utilisateurs** : créer un compte « Lecteur », s'y connecter : aucun bouton de modification n'apparaît, et l'API répond 403 aux écritures.

## Rôles
| Rôle | Droits |
|---|---|
| Administrateur | Tout, y compris les comptes utilisateurs |
| Responsable territorial | Gère collectivités, indicateurs, objectifs, projets, référentiels ; saisit, **valide** ou rejette les réalisations |
| Évaluateur | Consulte ; saisit des réalisations (qu'un responsable valide) |
| Lecteur | Consultation, résultats et rapports uniquement |

## Règles de calcul des scores (0 à 100, jamais plus de 100)
- **Hausse favorable** : (réalisé / objectif) × 100, plafonné à 100 ; réalisé ≤ 0 → 0.
- **Baisse favorable** : 100 si réalisé ≤ objectif, sinon (objectif / réalisé) × 100.
- **Objectif = 0** (aucune division par zéro) : règle configurable par indicateur — *Appliquer* (100 si l'objectif est respecté, sinon 0) ou *Ignorer* (indicateur exclu).
- **Score global** = Σ(score × pondération) / Σ(pondérations) sur les indicateurs **valides** uniquement.
- Un indicateur sans réalisation validée est **« Manquant » : exclu du global, jamais compté comme 0**.
- Pour chaque objectif d'une année, la réalisation **validée** de la période la plus récente est retenue.
- **Traçabilité** : chaque validation (et chaque changement d'objectif ou d'indicateur) enregistre un calcul dans la table `evaluations` (score, formule, détail), consultable via le bouton « Calculs ».

## Choix de conception à connaître
- Budget exécuté d'un projet : saisissable à la création/modification ; chaque **investissement** ajouté (ou supprimé) l'augmente (ou le diminue).
- Tableau de bord : les filtres région/type s'appliquent aux collectivités, scores et projets ; le filtre **domaine** s'applique aux projets (les indicateurs ont des catégories, pas de domaine).
- Période d'une réalisation : `2026`, `2026-T1`, `2026-S2` ou `2026-03`, et elle doit appartenir à l'année de l'objectif.
- Une réalisation validée n'est ni modifiable ni supprimable : il faut d'abord la rejeter.
- Suppression : refusée si la donnée est utilisée ailleurs (collectivité avec objectifs/projets, objectif avec réalisations…), avec un message clair.
- La table `rapports` du schéma n'est pas alimentée : les rapports sont générés à la demande, sans historique.

## Endpoints REST
Toutes les routes sauf `/api/auth/login` exigent `Authorization: Bearer <jeton>`. Erreurs : `{"message": "..."}` (400 validation, 401 non connecté, 403 droits, 404, 409 conflit).
Lecture : tout utilisateur connecté. Écriture : voir colonne « Écriture ».

| Méthode | URL | Écriture | Description |
|---|---|---|---|
| POST | `/api/auth/login` | public | `{email, motDePasse}` → `{token, utilisateur}` |
| GET | `/api/auth/me` | — | Utilisateur connecté |
| GET/POST/PUT/DELETE | `/api/utilisateurs[/{id}]` | Admin (tout) | Comptes ; corps `{nom, email, motDePasse, roleCode, actif}` |
| GET/POST/DELETE | `/api/referentiels/{regions\|categories\|domaines}[/{id}]` | Admin, Responsable | `{nom}` |
| GET | `/api/collectivites?type=&regionId=` | — | Liste filtrée |
| GET/POST/PUT/DELETE | `/api/collectivites[/{id}]` | Admin, Responsable | `{nom, type, regionId, responsable, adresse, telephone, email}` |
| GET/POST/PUT/DELETE | `/api/indicateurs[/{id}]` | Admin, Responsable | `{nom, description, categorieId, unite, periodicite, sens, regleObjectifZero}` |
| GET | `/api/objectifs?collectiviteId=&indicateurId=&annee=` | — | Liste filtrée |
| POST/PUT/DELETE | `/api/objectifs[/{id}]` | Admin, Responsable | `{collectiviteId, indicateurId, annee, valeurCible, ponderation}` |
| GET | `/api/realisations?collectiviteId=&annee=&statut=` | — | Historique des réalisations |
| POST/PUT/DELETE | `/api/realisations[/{id}]` | Admin, Responsable, Évaluateur | `{objectifId, periode, valeur}` |
| POST | `/api/realisations/{id}/valider` · `/rejeter` | Admin, Responsable | Validation des données |
| GET | `/api/realisations/{id}/evaluations` | — | Trace des calculs |
| GET | `/api/scores/collectivite/{id}?annee=` | — | Scores par indicateur + score global |
| GET | `/api/scores/classement?annee=&regionId=&type=` | — | Classement |
| GET | `/api/scores/comparaison?ids=1,2,3&annee=` | — | Comparaison de collectivités |
| GET | `/api/scores/evolution/{id}` | — | Score global par année |
| GET/POST/PUT/DELETE | `/api/projets[/{id}]` (`?collectiviteId=&domaineId=&statut=`) | Admin, Responsable | `{nom, collectiviteId, domaineId, budgetPrevu, budgetExecute, avancement, statut}` |
| GET/POST | `/api/projets/{id}/investissements` | Admin, Responsable | `{dateOperation, montant, sourceFinancement}` |
| DELETE | `/api/investissements/{id}` | Admin, Responsable | |
| GET | `/api/dashboard?annee=&regionId=&type=&domaineId=` | — | Compteurs, scores, classement, évolution, taux d'exécution |
| GET | `/api/rapports/{collectivites\|collectivite\|comparaison\|evolution}?format=pdf\|excel&annee=&regionId=&typeCollectivite=&collectiviteId=&ids=` | — | Téléchargement du rapport |
"# gesperformance" 
