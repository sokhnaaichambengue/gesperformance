import { useState } from 'react';
import api, { getErrorMessage, nettoyer } from '../api/axios.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useLoad } from '../hooks/useLoad.js';
import { Alert, Badge, Chargement, Field, Modal, PageHeader } from '../components/ui.jsx';
import { STATUTS_REAL, canEnter, canWrite, fmt, fmtDate } from '../labels.js';

const ANNEE = new Date().getFullYear();
const TON = { SAISIE: 'orange', VALIDEE: 'vert', REJETEE: 'rouge' };

function Calculs({ realisation, onClose }) {
  const ev = useLoad(() => api.get(`/realisations/${realisation.id}/evaluations`));
  return (
    <Modal title="Traçabilité des calculs" onClose={onClose}>
      <p className="muted">{realisation.collectiviteNom} — {realisation.indicateurNom} ({realisation.periode})</p>
      <Alert>{ev.error}</Alert>
      <Chargement loading={ev.loading} />
      <table>
        <thead><tr><th>Date</th><th>Score</th><th>Formule</th><th>Détail</th></tr></thead>
        <tbody>
          {(ev.data || []).map((e) => (
            <tr key={e.id}>
              <td>{fmtDate(e.calculeLe)}</td>
              <td><strong>{fmt(e.score)}</strong></td>
              <td>{e.formule}</td>
              <td>{e.detail}</td>
            </tr>
          ))}
          {ev.data && ev.data.length === 0 && <tr><td colSpan={4} className="muted">Aucun calcul enregistré.</td></tr>}
        </tbody>
      </table>
    </Modal>
  );
}

export default function Realisations() {
  const { user } = useAuth();
  const saisie = canEnter(user.role);
  const validation = canWrite(user.role);
  const [collectiviteId, setCollectiviteId] = useState('');
  const [annee, setAnnee] = useState(String(ANNEE));
  const [statut, setStatut] = useState('');
  const collectivites = useLoad(() => api.get('/collectivites'));
  const liste = useLoad(
    () => api.get('/realisations', { params: nettoyer({ collectiviteId, annee, statut }) }),
    [collectiviteId, annee, statut]
  );
  const [form, setForm] = useState(null);
  const [erreur, setErreur] = useState('');
  const [message, setMessage] = useState('');
  const [envoi, setEnvoi] = useState(false);
  const [calculs, setCalculs] = useState(null);

  // Objectifs proposés dans le formulaire (selon collectivité et année choisies)
  const objectifs = useLoad(
    () => (form && form.collectiviteId && form.annee
      ? api.get('/objectifs', { params: { collectiviteId: form.collectiviteId, annee: form.annee } })
      : Promise.resolve({ data: [] })),
    [form?.collectiviteId, form?.annee]
  );
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function enregistrer(e) {
    e.preventDefault();
    setErreur('');
    if (!form.objectifId) return setErreur("Choisissez l'indicateur (objectif).");
    if (!/^\d{4}(-(T[1-4]|S[1-2]|0[1-9]|1[0-2]))?$/.test(form.periode)) {
      return setErreur('Période invalide (ex. 2026, 2026-T1, 2026-S2, 2026-03).');
    }
    if (form.valeur === '' || Number.isNaN(Number(form.valeur))) return setErreur('La valeur est obligatoire.');
    const payload = { objectifId: Number(form.objectifId), periode: form.periode, valeur: Number(form.valeur) };
    setEnvoi(true);
    try {
      if (form.id) await api.put(`/realisations/${form.id}`, payload);
      else await api.post('/realisations', payload);
      setForm(null);
      liste.reload();
    } catch (err) {
      setErreur(getErrorMessage(err));
    } finally {
      setEnvoi(false);
    }
  }

  async function action(r, chemin, confirmation) {
    if (confirmation && !window.confirm(confirmation)) return;
    setMessage('');
    try {
      if (chemin === 'supprimer') await api.delete(`/realisations/${r.id}`);
      else await api.post(`/realisations/${r.id}/${chemin}`);
      liste.reload();
    } catch (err) {
      setMessage(getErrorMessage(err));
    }
  }

  const ouvrir = (r) => {
    setErreur('');
    setForm(r ? {
      id: r.id, collectiviteId: String(r.collectiviteId), annee: String(r.annee), objectifId: String(r.objectifId),
      periode: r.periode, valeur: String(r.valeur),
    } : { collectiviteId, annee, objectifId: '', periode: annee, valeur: '' });
  };

  return (
    <>
      <PageHeader title="Réalisations">
        {saisie && <button type="button" onClick={() => ouvrir(null)}>+ Saisir une réalisation</button>}
      </PageHeader>
      <p className="muted">Seules les réalisations validées sont utilisées dans le calcul des scores.</p>
      <div className="filters">
        <Field label="Collectivité">
          <select value={collectiviteId} onChange={(e) => setCollectiviteId(e.target.value)}>
            <option value="">Toutes</option>
            {(collectivites.data || []).map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
          </select>
        </Field>
        <Field label="Année"><input type="number" value={annee} onChange={(e) => setAnnee(e.target.value)} /></Field>
        <Field label="Statut">
          <select value={statut} onChange={(e) => setStatut(e.target.value)}>
            <option value="">Tous</option>
            {Object.entries(STATUTS_REAL).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </Field>
      </div>
      <Alert>{message || liste.error}</Alert>
      <Chargement loading={liste.loading && !liste.data} />
      <section className="card">
        <table>
          <thead>
            <tr><th>Collectivité</th><th>Indicateur</th><th>Période</th><th>Réalisé</th><th>Objectif</th><th>Statut</th><th>Saisi par</th><th></th></tr>
          </thead>
          <tbody>
            {(liste.data || []).map((r) => (
              <tr key={r.id}>
                <td>{r.collectiviteNom}</td>
                <td>{r.indicateurNom}</td>
                <td>{r.periode}</td>
                <td><strong>{fmt(r.valeur)}</strong> {r.unite}</td>
                <td>{fmt(r.valeurCible)}</td>
                <td><Badge tone={TON[r.statut]}>{STATUTS_REAL[r.statut]}</Badge></td>
                <td>{r.saisiPar}<div className="muted">{fmtDate(r.saisiLe)}</div></td>
                <td className="actions-cell">
                  {validation && r.statut === 'SAISIE' && (
                    <button type="button" className="small" onClick={() => action(r, 'valider')}>Valider</button>
                  )}
                  {validation && r.statut !== 'REJETEE' && (
                    <button type="button" className="small danger" onClick={() => action(r, 'rejeter', 'Rejeter cette réalisation ? Elle sera retirée du calcul des scores.')}>Rejeter</button>
                  )}
                  {r.statut === 'VALIDEE' && (
                    <button type="button" className="small secondary" onClick={() => setCalculs(r)}>Calculs</button>
                  )}
                  {saisie && r.statut !== 'VALIDEE' && (
                    <>
                      <button type="button" className="small secondary" onClick={() => ouvrir(r)}>Modifier</button>
                      <button type="button" className="small danger" onClick={() => action(r, 'supprimer', 'Supprimer cette réalisation ?')}>Supprimer</button>
                    </>
                  )}
                </td>
              </tr>
            ))}
            {liste.data && liste.data.length === 0 && (
              <tr><td colSpan={8} className="muted">Aucune réalisation pour ces filtres.</td></tr>
            )}
          </tbody>
        </table>
      </section>

      {calculs && <Calculs realisation={calculs} onClose={() => setCalculs(null)} />}

      {form && (
        <Modal title={form.id ? 'Modifier la réalisation' : 'Nouvelle réalisation'} onClose={() => setForm(null)}>
          <form onSubmit={enregistrer}>
            <div className="row2">
              <Field label="Collectivité *">
                <select value={form.collectiviteId} onChange={(e) => setForm({ ...form, collectiviteId: e.target.value, objectifId: '' })} disabled={!!form.id}>
                  <option value="">Choisir…</option>
                  {(collectivites.data || []).map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
                </select>
              </Field>
              <Field label="Année *">
                <input type="number" value={form.annee} onChange={(e) => setForm({ ...form, annee: e.target.value, objectifId: '' })} disabled={!!form.id} />
              </Field>
            </div>
            <Field label="Indicateur (objectif) *">
              <select value={form.objectifId} onChange={set('objectifId')} disabled={!!form.id}>
                <option value="">{(objectifs.data || []).length ? 'Choisir…' : 'Aucun objectif pour cette collectivité et cette année'}</option>
                {(objectifs.data || []).map((o) => (
                  <option key={o.id} value={o.id}>{o.indicateurNom} — objectif {fmt(o.valeurCible)} {o.unite}</option>
                ))}
              </select>
            </Field>
            <div className="row2">
              <Field label="Période * (2026, 2026-T1, 2026-S2, 2026-03)">
                <input value={form.periode} onChange={set('periode')} />
              </Field>
              <Field label="Valeur réalisée *">
                <input type="number" step="any" value={form.valeur} onChange={set('valeur')} />
              </Field>
            </div>
            <Alert>{erreur}</Alert>
            <div className="form-actions">
              <button type="button" className="secondary" onClick={() => setForm(null)}>Annuler</button>
              <button type="submit" disabled={envoi}>{envoi ? 'Enregistrement…' : 'Enregistrer'}</button>
            </div>
          </form>
        </Modal>
      )}
    </>
  );
}
