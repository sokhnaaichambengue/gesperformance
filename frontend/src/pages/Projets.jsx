import { useState } from 'react';
import api, { getErrorMessage, nettoyer } from '../api/axios.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useLoad } from '../hooks/useLoad.js';
import { Alert, Badge, Chargement, Field, Modal, PageHeader } from '../components/ui.jsx';
import { STATUTS_PROJET, canWrite, fmt, fmtDate, fmtMoney } from '../labels.js';

const VIDE = { nom: '', collectiviteId: '', domaineId: '', budgetPrevu: '', budgetExecute: '0', avancement: '0', statut: 'PLANIFIE' };

function Investissements({ projet, ecriture, onClose, onChange }) {
  const liste = useLoad(() => api.get(`/projets/${projet.id}/investissements`));
  const [f, setF] = useState({ dateOperation: new Date().toISOString().slice(0, 10), montant: '', sourceFinancement: '' });
  const [erreur, setErreur] = useState('');

  async function ajouter(e) {
    e.preventDefault();
    setErreur('');
    if (!f.dateOperation) return setErreur('La date est obligatoire.');
    if (!(Number(f.montant) > 0)) return setErreur('Le montant doit être > 0.');
    try {
      await api.post(`/projets/${projet.id}/investissements`, { ...f, montant: Number(f.montant) });
      setF({ ...f, montant: '', sourceFinancement: '' });
      liste.reload();
      onChange();
    } catch (err) {
      setErreur(getErrorMessage(err));
    }
  }

  async function supprimer(i) {
    if (!window.confirm('Supprimer cet investissement ?')) return;
    try {
      await api.delete(`/investissements/${i.id}`);
      liste.reload();
      onChange();
    } catch (err) {
      setErreur(getErrorMessage(err));
    }
  }

  return (
    <Modal title={`Investissements — ${projet.nom}`} onClose={onClose}>
      <p className="muted">Chaque investissement ajouté s'ajoute au budget exécuté du projet.</p>
      <Alert>{erreur || liste.error}</Alert>
      <table>
        <thead><tr><th>Date</th><th>Montant</th><th>Financement</th>{ecriture && <th></th>}</tr></thead>
        <tbody>
          {(liste.data || []).map((i) => (
            <tr key={i.id}>
              <td>{fmtDate(i.dateOperation)}</td>
              <td>{fmtMoney(i.montant)}</td>
              <td>{i.sourceFinancement || '—'}</td>
              {ecriture && <td><button type="button" className="small danger" onClick={() => supprimer(i)}>Supprimer</button></td>}
            </tr>
          ))}
          {liste.data && liste.data.length === 0 && <tr><td colSpan={4} className="muted">Aucun investissement.</td></tr>}
        </tbody>
      </table>
      {ecriture && (
        <form onSubmit={ajouter}>
          <div className="row2">
            <Field label="Date *"><input type="date" value={f.dateOperation} onChange={(e) => setF({ ...f, dateOperation: e.target.value })} /></Field>
            <Field label="Montant (FCFA) *"><input type="number" step="any" min="0" value={f.montant} onChange={(e) => setF({ ...f, montant: e.target.value })} /></Field>
          </div>
          <Field label="Source de financement"><input value={f.sourceFinancement} onChange={(e) => setF({ ...f, sourceFinancement: e.target.value })} /></Field>
          <div className="form-actions"><button type="submit">Ajouter l'investissement</button></div>
        </form>
      )}
    </Modal>
  );
}

export default function Projets() {
  const { user } = useAuth();
  const ecriture = canWrite(user.role);
  const [collectiviteId, setCollectiviteId] = useState('');
  const [domaineId, setDomaineId] = useState('');
  const [statut, setStatut] = useState('');
  const collectivites = useLoad(() => api.get('/collectivites'));
  const domaines = useLoad(() => api.get('/referentiels/domaines'));
  const liste = useLoad(
    () => api.get('/projets', { params: nettoyer({ collectiviteId, domaineId, statut }) }),
    [collectiviteId, domaineId, statut]
  );
  const [form, setForm] = useState(null);
  const [invest, setInvest] = useState(null);
  const [erreur, setErreur] = useState('');
  const [message, setMessage] = useState('');
  const [envoi, setEnvoi] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function enregistrer(e) {
    e.preventDefault();
    setErreur('');
    if (!form.nom.trim()) return setErreur('Le nom est obligatoire.');
    if (!form.collectiviteId) return setErreur('La collectivité est obligatoire.');
    if (!form.domaineId) return setErreur('Le domaine est obligatoire.');
    if (form.budgetPrevu === '' || Number(form.budgetPrevu) < 0) return setErreur('Le budget prévu doit être ≥ 0.');
    if (Number(form.budgetExecute || 0) < 0) return setErreur('Le budget exécuté doit être ≥ 0.');
    const av = Number(form.avancement);
    if (Number.isNaN(av) || av < 0 || av > 100) return setErreur("L'avancement doit être entre 0 et 100.");
    const payload = {
      nom: form.nom.trim(), collectiviteId: Number(form.collectiviteId), domaineId: Number(form.domaineId),
      budgetPrevu: Number(form.budgetPrevu), budgetExecute: Number(form.budgetExecute || 0), avancement: av, statut: form.statut,
    };
    setEnvoi(true);
    try {
      if (form.id) await api.put(`/projets/${form.id}`, payload);
      else await api.post('/projets', payload);
      setForm(null);
      liste.reload();
    } catch (err) {
      setErreur(getErrorMessage(err));
    } finally {
      setEnvoi(false);
    }
  }

  async function supprimer(p) {
    if (!window.confirm(`Supprimer le projet « ${p.nom} » ?`)) return;
    setMessage('');
    try {
      await api.delete(`/projets/${p.id}`);
      liste.reload();
    } catch (err) {
      setMessage(getErrorMessage(err));
    }
  }

  const ouvrir = (p) => {
    setErreur('');
    setForm(p ? {
      id: p.id, nom: p.nom, collectiviteId: String(p.collectiviteId), domaineId: String(p.domaineId),
      budgetPrevu: String(p.budgetPrevu), budgetExecute: String(p.budgetExecute), avancement: String(p.avancement), statut: p.statut,
    } : { ...VIDE });
  };

  return (
    <>
      <PageHeader title="Projets et investissements">
        {ecriture && <button type="button" onClick={() => ouvrir(null)}>+ Ajouter un projet</button>}
      </PageHeader>
      <div className="filters">
        <Field label="Collectivité">
          <select value={collectiviteId} onChange={(e) => setCollectiviteId(e.target.value)}>
            <option value="">Toutes</option>
            {(collectivites.data || []).map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
          </select>
        </Field>
        <Field label="Domaine">
          <select value={domaineId} onChange={(e) => setDomaineId(e.target.value)}>
            <option value="">Tous</option>
            {(domaines.data || []).map((d) => <option key={d.id} value={d.id}>{d.nom}</option>)}
          </select>
        </Field>
        <Field label="Statut">
          <select value={statut} onChange={(e) => setStatut(e.target.value)}>
            <option value="">Tous</option>
            {Object.entries(STATUTS_PROJET).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </Field>
      </div>
      <Alert>{message || liste.error}</Alert>
      <Chargement loading={liste.loading && !liste.data} />
      <section className="card">
        <table>
          <thead>
            <tr><th>Projet</th><th>Collectivité</th><th>Domaine</th><th>Budget prévu</th><th>Budget exécuté</th><th>Exécution</th><th>Avancement</th><th>Statut</th><th></th></tr>
          </thead>
          <tbody>
            {(liste.data || []).map((p) => (
              <tr key={p.id}>
                <td><strong>{p.nom}</strong></td>
                <td>{p.collectiviteNom}</td>
                <td>{p.domaineNom}</td>
                <td>{fmtMoney(p.budgetPrevu)}</td>
                <td>{fmtMoney(p.budgetExecute)}</td>
                <td>{p.tauxExecution === null ? '—' : `${fmt(p.tauxExecution, 1)} %`}</td>
                <td>{p.avancement} %</td>
                <td><Badge tone={p.statut === 'TERMINE' ? 'vert' : p.statut === 'SUSPENDU' ? 'rouge' : 'gris'}>{STATUTS_PROJET[p.statut]}</Badge></td>
                <td className="actions-cell">
                  <button type="button" className="small secondary" onClick={() => setInvest(p)}>Investissements</button>
                  {ecriture && (
                    <>
                      <button type="button" className="small secondary" onClick={() => ouvrir(p)}>Modifier</button>
                      <button type="button" className="small danger" onClick={() => supprimer(p)}>Supprimer</button>
                    </>
                  )}
                </td>
              </tr>
            ))}
            {liste.data && liste.data.length === 0 && <tr><td colSpan={9} className="muted">Aucun projet pour ces filtres.</td></tr>}
          </tbody>
        </table>
      </section>

      {invest && <Investissements projet={invest} ecriture={ecriture} onClose={() => setInvest(null)} onChange={liste.reload} />}

      {form && (
        <Modal title={form.id ? 'Modifier le projet' : 'Nouveau projet'} onClose={() => setForm(null)}>
          <form onSubmit={enregistrer}>
            <Field label="Nom du projet *"><input value={form.nom} onChange={set('nom')} maxLength={200} /></Field>
            <div className="row2">
              <Field label="Collectivité *">
                <select value={form.collectiviteId} onChange={set('collectiviteId')}>
                  <option value="">Choisir…</option>
                  {(collectivites.data || []).map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
                </select>
              </Field>
              <Field label="Domaine de compétence *">
                <select value={form.domaineId} onChange={set('domaineId')}>
                  <option value="">Choisir…</option>
                  {(domaines.data || []).map((d) => <option key={d.id} value={d.id}>{d.nom}</option>)}
                </select>
              </Field>
            </div>
            <div className="row2">
              <Field label="Budget prévu (FCFA) *"><input type="number" step="any" min="0" value={form.budgetPrevu} onChange={set('budgetPrevu')} /></Field>
              <Field label="Budget exécuté (FCFA)"><input type="number" step="any" min="0" value={form.budgetExecute} onChange={set('budgetExecute')} /></Field>
            </div>
            <div className="row2">
              <Field label="Avancement (%) *"><input type="number" min="0" max="100" value={form.avancement} onChange={set('avancement')} /></Field>
              <Field label="Statut *">
                <select value={form.statut} onChange={set('statut')}>
                  {Object.entries(STATUTS_PROJET).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                </select>
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
