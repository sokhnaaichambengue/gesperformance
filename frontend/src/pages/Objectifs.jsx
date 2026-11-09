import { useState } from 'react';
import api, { getErrorMessage, nettoyer } from '../api/axios.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useLoad } from '../hooks/useLoad.js';
import { Alert, Chargement, Field, Modal, PageHeader } from '../components/ui.jsx';
import { canWrite, fmt } from '../labels.js';

const ANNEE = new Date().getFullYear();

export default function Objectifs() {
  const { user } = useAuth();
  const ecriture = canWrite(user.role);
  const [annee, setAnnee] = useState(String(ANNEE));
  const [collectiviteId, setCollectiviteId] = useState('');
  const collectivites = useLoad(() => api.get('/collectivites'));
  const indicateurs = useLoad(() => api.get('/indicateurs'));
  const liste = useLoad(
    () => api.get('/objectifs', { params: nettoyer({ annee, collectiviteId }) }),
    [annee, collectiviteId]
  );
  const [form, setForm] = useState(null);
  const [erreur, setErreur] = useState('');
  const [message, setMessage] = useState('');
  const [envoi, setEnvoi] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function enregistrer(e) {
    e.preventDefault();
    setErreur('');
    if (!form.collectiviteId) return setErreur('La collectivité est obligatoire.');
    if (!form.indicateurId) return setErreur("L'indicateur est obligatoire.");
    const an = Number(form.annee);
    if (!an || an < 2000 || an > 2100) return setErreur('Année invalide (2000 à 2100).');
    if (form.valeurCible === '' || Number(form.valeurCible) < 0) return setErreur("L'objectif doit être un nombre ≥ 0.");
    if (!(Number(form.ponderation) > 0)) return setErreur('La pondération doit être > 0.');
    const payload = {
      collectiviteId: Number(form.collectiviteId), indicateurId: Number(form.indicateurId), annee: an,
      valeurCible: Number(form.valeurCible), ponderation: Number(form.ponderation),
    };
    setEnvoi(true);
    try {
      if (form.id) await api.put(`/objectifs/${form.id}`, payload);
      else await api.post('/objectifs', payload);
      setForm(null);
      liste.reload();
    } catch (err) {
      setErreur(getErrorMessage(err));
    } finally {
      setEnvoi(false);
    }
  }

  async function supprimer(o) {
    if (!window.confirm('Supprimer cet objectif ?')) return;
    setMessage('');
    try {
      await api.delete(`/objectifs/${o.id}`);
      liste.reload();
    } catch (err) {
      setMessage(getErrorMessage(err));
    }
  }

  const ouvrir = (o) => {
    setErreur('');
    setForm(o ? {
      id: o.id, collectiviteId: String(o.collectiviteId), indicateurId: String(o.indicateurId), annee: String(o.annee),
      valeurCible: String(o.valeurCible), ponderation: String(o.ponderation),
    } : { collectiviteId, indicateurId: '', annee, valeurCible: '', ponderation: '1' });
  };

  return (
    <>
      <PageHeader title="Objectifs et pondérations">
        {ecriture && <button type="button" onClick={() => ouvrir(null)}>+ Associer un indicateur</button>}
      </PageHeader>
      <p className="muted">
        Un objectif associe un indicateur à une collectivité pour une année, avec sa valeur cible et son poids dans le score global.
      </p>
      <div className="filters">
        <Field label="Année">
          <input type="number" value={annee} onChange={(e) => setAnnee(e.target.value)} />
        </Field>
        <Field label="Collectivité">
          <select value={collectiviteId} onChange={(e) => setCollectiviteId(e.target.value)}>
            <option value="">Toutes</option>
            {(collectivites.data || []).map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
          </select>
        </Field>
      </div>
      <Alert>{message || liste.error}</Alert>
      <Chargement loading={liste.loading && !liste.data} />
      <section className="card">
        <table>
          <thead>
            <tr><th>Collectivité</th><th>Indicateur</th><th>Année</th><th>Objectif</th><th>Pondération</th>{ecriture && <th></th>}</tr>
          </thead>
          <tbody>
            {(liste.data || []).map((o) => (
              <tr key={o.id}>
                <td>{o.collectiviteNom}</td>
                <td>{o.indicateurNom}</td>
                <td>{o.annee}</td>
                <td>{fmt(o.valeurCible)} {o.unite}</td>
                <td>{fmt(o.ponderation)}</td>
                {ecriture && (
                  <td className="actions-cell">
                    <button type="button" className="small secondary" onClick={() => ouvrir(o)}>Modifier</button>
                    <button type="button" className="small danger" onClick={() => supprimer(o)}>Supprimer</button>
                  </td>
                )}
              </tr>
            ))}
            {liste.data && liste.data.length === 0 && (
              <tr><td colSpan={6} className="muted">Aucun objectif pour ces filtres.</td></tr>
            )}
          </tbody>
        </table>
      </section>

      {form && (
        <Modal title={form.id ? "Modifier l'objectif" : 'Nouvel objectif'} onClose={() => setForm(null)}>
          <form onSubmit={enregistrer}>
            <Field label="Collectivité *">
              <select value={form.collectiviteId} onChange={set('collectiviteId')}>
                <option value="">Choisir…</option>
                {(collectivites.data || []).map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
              </select>
            </Field>
            <Field label="Indicateur *">
              <select value={form.indicateurId} onChange={set('indicateurId')}>
                <option value="">Choisir…</option>
                {(indicateurs.data || []).map((i) => <option key={i.id} value={i.id}>{i.nom} ({i.unite})</option>)}
              </select>
            </Field>
            <div className="row2">
              <Field label="Année *"><input type="number" value={form.annee} onChange={set('annee')} /></Field>
              <Field label="Objectif (valeur cible) *">
                <input type="number" step="any" min="0" value={form.valeurCible} onChange={set('valeurCible')} />
              </Field>
            </div>
            <Field label="Pondération * (poids dans le score global)">
              <input type="number" step="any" min="0" value={form.ponderation} onChange={set('ponderation')} />
            </Field>
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
