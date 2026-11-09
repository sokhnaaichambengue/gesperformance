import { useState } from 'react';
import api, { getErrorMessage } from '../api/axios.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useLoad } from '../hooks/useLoad.js';
import { Alert, Chargement, Field, Modal, PageHeader } from '../components/ui.jsx';
import { PERIODICITES, REGLES, SENS, canWrite } from '../labels.js';

const VIDE = {
  nom: '', description: '', categorieId: '', unite: '', periodicite: 'ANNUELLE',
  sens: 'HAUSSE_FAVORABLE', regleObjectifZero: 'APPLIQUER',
};

export default function Indicateurs() {
  const { user } = useAuth();
  const ecriture = canWrite(user.role);
  const categories = useLoad(() => api.get('/referentiels/categories'));
  const liste = useLoad(() => api.get('/indicateurs'));
  const [form, setForm] = useState(null);
  const [erreur, setErreur] = useState('');
  const [message, setMessage] = useState('');
  const [envoi, setEnvoi] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function enregistrer(e) {
    e.preventDefault();
    setErreur('');
    if (!form.nom.trim()) return setErreur('Le nom est obligatoire.');
    if (!form.categorieId) return setErreur('La catégorie est obligatoire.');
    if (!form.unite.trim()) return setErreur("L'unité est obligatoire.");
    const payload = {
      nom: form.nom.trim(), description: form.description, categorieId: Number(form.categorieId),
      unite: form.unite.trim(), periodicite: form.periodicite, sens: form.sens, regleObjectifZero: form.regleObjectifZero,
    };
    setEnvoi(true);
    try {
      if (form.id) await api.put(`/indicateurs/${form.id}`, payload);
      else await api.post('/indicateurs', payload);
      setForm(null);
      liste.reload();
    } catch (err) {
      setErreur(getErrorMessage(err));
    } finally {
      setEnvoi(false);
    }
  }

  async function supprimer(i) {
    if (!window.confirm(`Supprimer l'indicateur « ${i.nom} » ?`)) return;
    setMessage('');
    try {
      await api.delete(`/indicateurs/${i.id}`);
      liste.reload();
    } catch (err) {
      setMessage(getErrorMessage(err));
    }
  }

  const ouvrir = (i) => {
    setErreur('');
    setForm(i ? {
      id: i.id, nom: i.nom, description: i.description || '', categorieId: String(i.categorieId), unite: i.unite,
      periodicite: i.periodicite, sens: i.sens, regleObjectifZero: i.regleObjectifZero,
    } : { ...VIDE });
  };

  return (
    <>
      <PageHeader title="Indicateurs">
        {ecriture && <button type="button" onClick={() => ouvrir(null)}>+ Ajouter</button>}
      </PageHeader>
      <Alert>{message || liste.error}</Alert>
      <Chargement loading={liste.loading && !liste.data} />
      <section className="card">
        <table>
          <thead>
            <tr><th>Nom</th><th>Catégorie</th><th>Unité</th><th>Périodicité</th><th>Sens</th>{ecriture && <th></th>}</tr>
          </thead>
          <tbody>
            {(liste.data || []).map((i) => (
              <tr key={i.id}>
                <td><strong>{i.nom}</strong>{i.description && <div className="muted">{i.description}</div>}</td>
                <td>{i.categorieNom}</td>
                <td>{i.unite}</td>
                <td>{PERIODICITES[i.periodicite]}</td>
                <td>{i.sens === 'HAUSSE_FAVORABLE' ? 'Hausse favorable' : 'Baisse favorable'}</td>
                {ecriture && (
                  <td className="actions-cell">
                    <button type="button" className="small secondary" onClick={() => ouvrir(i)}>Modifier</button>
                    <button type="button" className="small danger" onClick={() => supprimer(i)}>Supprimer</button>
                  </td>
                )}
              </tr>
            ))}
            {liste.data && liste.data.length === 0 && (
              <tr><td colSpan={6} className="muted">Aucun indicateur enregistré.</td></tr>
            )}
          </tbody>
        </table>
      </section>

      {form && (
        <Modal title={form.id ? "Modifier l'indicateur" : 'Nouvel indicateur'} onClose={() => setForm(null)}>
          <form onSubmit={enregistrer}>
            <Field label="Nom *"><input value={form.nom} onChange={set('nom')} maxLength={150} /></Field>
            <Field label="Description"><textarea rows={2} value={form.description} onChange={set('description')} /></Field>
            <div className="row2">
              <Field label="Catégorie *">
                <select value={form.categorieId} onChange={set('categorieId')}>
                  <option value="">Choisir…</option>
                  {(categories.data || []).map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
                </select>
              </Field>
              <Field label="Unité * (%, FCFA, nombre…)"><input value={form.unite} onChange={set('unite')} maxLength={30} /></Field>
            </div>
            <Field label="Périodicité *">
              <select value={form.periodicite} onChange={set('periodicite')}>
                {Object.entries(PERIODICITES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
              </select>
            </Field>
            <Field label="Nature *">
              <select value={form.sens} onChange={set('sens')}>
                {Object.entries(SENS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
              </select>
            </Field>
            <Field label="Règle si l'objectif vaut 0">
              <select value={form.regleObjectifZero} onChange={set('regleObjectifZero')}>
                {Object.entries(REGLES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
              </select>
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
