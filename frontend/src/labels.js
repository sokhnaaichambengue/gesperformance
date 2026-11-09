export const TYPES = { COMMUNE: 'Commune', DEPARTEMENT: 'Département', VILLE: 'Ville', POLE_TERRITORIAL: 'Pôle territorial' };
export const SENS = { HAUSSE_FAVORABLE: 'Une valeur élevée est favorable', BAISSE_FAVORABLE: 'Une valeur faible est favorable' };
export const PERIODICITES = { MENSUELLE: 'Mensuelle', TRIMESTRIELLE: 'Trimestrielle', SEMESTRIELLE: 'Semestrielle', ANNUELLE: 'Annuelle' };
export const REGLES = { APPLIQUER: 'Appliquer (100 si objectif respecté, sinon 0)', IGNORER: 'Ignorer l\'indicateur' };
export const STATUTS_REAL = { SAISIE: 'En attente', VALIDEE: 'Validée', REJETEE: 'Rejetée' };
export const STATUTS_PROJET = { PLANIFIE: 'Planifié', EN_COURS: 'En cours', TERMINE: 'Terminé', SUSPENDU: 'Suspendu' };
export const ROLES = {
  ADMIN: 'Administrateur',
  RESPONSABLE_TERRITORIAL: 'Responsable territorial',
  EVALUATEUR: 'Évaluateur',
  LECTEUR: 'Lecteur',
};

export const canWrite = (role) => role === 'ADMIN' || role === 'RESPONSABLE_TERRITORIAL';
export const canEnter = (role) => canWrite(role) || role === 'EVALUATEUR';

export function fmt(n, d = 2) {
  if (n === null || n === undefined) return '—';
  return Number(n).toLocaleString('fr-FR', { maximumFractionDigits: d });
}
export const fmtMoney = (n) => (n === null || n === undefined ? '—' : `${fmt(n, 0)} FCFA`);
export const fmtDate = (iso) => (iso ? new Date(iso).toLocaleDateString('fr-FR') : '—');
