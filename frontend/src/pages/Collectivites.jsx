import { useState } from 'react';
import api, { getErrorMessage, nettoyer } from '../api/axios.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useLoad } from '../hooks/useLoad.js';
import { Alert, Chargement, Field, Modal, PageHeader } from '../components/ui.jsx';
import { TYPES, canWrite } from '../labels.js';

const VIDE = { nom: '', type: 'COMMUNE', regionId: '', responsable: '', adresse: '', telephone: '', email: '' };

export default function Collectivites() {
  const { user } = useAuth();
  const ecriture = canWrite(user.role);
  const [type, setType] = useState('');
  const [regionId, setRegionId] = useState('');
  const regions = useLoad(() => api.get('/referentiels/regions'));
  const liste = useLoad(() => api.get('/collectivites', { params: nettoyer({ type, regionId }) }), [type, regionId]);
  const [form, setForm] = useState(null);
  const [erreur, setErreur] = useState('');
  const [message, setMessage] = useState('');
  const [envoi, setEnvoi] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function enregistrer(e) {
    e.preventDefault();
    setErreur('');
    if (!form.nom.trim()) return setErreur('Le nom est obligatoire.');
    if (!form.regionId) return setErreur('La région est obligatoire.');
    const payload = {
      nom: form.nom.trim(), type: form.type, regionId: Number(form.regionId),
      responsable: form.responsable, adresse: form.adresse, telephone: form.telephone, email: form.email,
    };
    setEnvoi(true);
    try {
      if (form.id) await api.put(`/collectivites/${form.id}`, payload);
      else await api.post('/collectivites', payload);
      setForm(null);
      liste.reload();
    } catch (err) {
      setErreur(getErrorMessage(err));
    } finally {
      setEnvoi(false);
    }
  }

  async function supprimer(c) {
    if (!window.confirm(`Supprimer « ${c.nom} » ?`)) return;
    setMessage('');
    try {
      await api.delete(`/collectivites/${c.id}`);
      liste.reload();
    } catch (err) {
      setMessage(getErrorMessage(err));
    }
  }

  const ouvrir = (c) => {
    setErreur('');
    setForm(c ? {
      id: c.id, nom: c.nom, type: c.type, regionId: String(c.regionId), responsable: c.responsable || '',
      adresse: c.adresse || '', telephone: c.telephone || '', email: c.email || '',
    } : { ...VIDE });
  };

  return (
    <>
      <PageHeader title="Collectivités">
        {ecriture && <button type="button" onClick={() => ouvrir(null)}>+ Ajouter</button>}
      </PageHeader>
      <div className="filters">
        <Field label="Type">
          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="">Tous</option>
            {Object.entries(TYPES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </Field>
        <Field label="Région">
          <select value={regionId} onChange={(e) => setRegionId(e.target.value)}>
            <option value="">Toutes</option>
            {(regions.data || []).map((r) => <option key={r.id} value={r.id}>{r.nom}</option>)}
          </select>
        </Field>
      </div>
      <Alert>{message || liste.error}</Alert>
      <Chargement loading={liste.loading && !liste.data} />
      <section className="card">
        <table>
          <thead>
            <tr><th>Nom</th><th>Type</th><th>Région</th><th>Responsable</th><th>Contact</th>{ecriture && <th></th>}</tr>
          </thead>
          <tbody>
            {(liste.data || []).map((c) => (
              <tr key={c.id}>
                <td><strong>{c.nom}</strong></td>
                <td>{TYPES[c.type]}</td>
                <td>{c.regionNom}</td>
                <td>{c.responsable || '—'}</td>
                <td>{[c.telephone, c.email].filter(Boolean).join(' · ') || '—'}</td>
                {ecriture && (
                  <td className="actions-cell">
                    <button type="button" className="small secondary" onClick={() => ouvrir(c)}>Modifier</button>
                    <button type="button" className="small danger" onClick={() => supprimer(c)}>Supprimer</button>
                  </td>
                )}
              </tr>
            ))}
            {liste.data && liste.data.length === 0 && (
              <tr><td colSpan={6} className="muted">Aucune collectivité enregistrée.</td></tr>
            )}
          </tbody>
        </table>
      </section>

      {form && (
        <Modal title={form.id ? 'Modifier la collectivité' : 'Nouvelle collectivité'} onClose={() => setForm(null)}>
          <form onSubmit={enregistrer}>
            <Field label="Nom *"><input value={form.nom} onChange={set('nom')} maxLength={150} /></Field>
            <div className="row2">
              <Field label="Type *">
                <select value={form.type} onChange={set('type')}>
                  {Object.entries(TYPES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                </select>
              </Field>
              <Field label="Région *">
                <select value={form.regionId} onChange={set('regionId')}>
                  <option value="">Choisir…</option>
                  {(regions.data || []).map((r) => <option key={r.id} value={r.id}>{r.nom}</option>)}
                </select>
              </Field>
            </div>
            <Field label="Responsable"><input value={form.responsable} onChange={set('responsable')} /></Field>
            <Field label="Adresse"><input value={form.adresse} onChange={set('adresse')} /></Field>
            <div className="row2">
              <Field label="Téléphone"><input value={form.telephone} onChange={set('telephone')} /></Field>
              <Field label="Email"><input type="email" value={form.email} onChange={set('email')} /></Field>
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
