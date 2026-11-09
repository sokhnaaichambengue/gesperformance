import { useState } from 'react';
import api, { getErrorMessage } from '../api/axios.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useLoad } from '../hooks/useLoad.js';
import { Alert, Badge, Chargement, Field, Modal, PageHeader } from '../components/ui.jsx';
import { ROLES } from '../labels.js';

const VIDE = { nom: '', email: '', motDePasse: '', roleCode: 'LECTEUR', actif: true };

export default function Utilisateurs() {
  const { user } = useAuth();
  const liste = useLoad(() => api.get('/utilisateurs'));
  const [form, setForm] = useState(null);
  const [erreur, setErreur] = useState('');
  const [message, setMessage] = useState('');
  const [envoi, setEnvoi] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function enregistrer(e) {
    e.preventDefault();
    setErreur('');
    if (!form.nom.trim()) return setErreur('Le nom est obligatoire.');
    if (!/^\S+@\S+\.\S+$/.test(form.email)) return setErreur('Email invalide.');
    if (!form.id && form.motDePasse.length < 8) return setErreur('Le mot de passe doit avoir 8 caractères minimum.');
    if (form.id && form.motDePasse && form.motDePasse.length < 8) return setErreur('Mot de passe : 8 caractères minimum.');
    const payload = { nom: form.nom.trim(), email: form.email.trim(), motDePasse: form.motDePasse, roleCode: form.roleCode, actif: form.actif };
    setEnvoi(true);
    try {
      if (form.id) await api.put(`/utilisateurs/${form.id}`, payload);
      else await api.post('/utilisateurs', payload);
      setForm(null);
      liste.reload();
    } catch (err) {
      setErreur(getErrorMessage(err));
    } finally {
      setEnvoi(false);
    }
  }

  async function supprimer(u) {
    if (!window.confirm(`Supprimer le compte « ${u.email} » ?`)) return;
    setMessage('');
    try {
      await api.delete(`/utilisateurs/${u.id}`);
      liste.reload();
    } catch (err) {
      setMessage(getErrorMessage(err));
    }
  }

  const ouvrir = (u) => {
    setErreur('');
    setForm(u ? { id: u.id, nom: u.nom, email: u.email, motDePasse: '', roleCode: u.role, actif: u.actif } : { ...VIDE });
  };

  return (
    <>
      <PageHeader title="Utilisateurs">
        <button type="button" onClick={() => ouvrir(null)}>+ Ajouter</button>
      </PageHeader>
      <Alert>{message || liste.error}</Alert>
      <Chargement loading={liste.loading && !liste.data} />
      <section className="card">
        <table>
          <thead><tr><th>Nom</th><th>Email</th><th>Rôle</th><th>Compte</th><th></th></tr></thead>
          <tbody>
            {(liste.data || []).map((u) => (
              <tr key={u.id}>
                <td><strong>{u.nom}</strong></td>
                <td>{u.email}</td>
                <td>{ROLES[u.role]}</td>
                <td><Badge tone={u.actif ? 'vert' : 'rouge'}>{u.actif ? 'Actif' : 'Désactivé'}</Badge></td>
                <td className="actions-cell">
                  <button type="button" className="small secondary" onClick={() => ouvrir(u)}>Modifier</button>
                  {u.email !== user.email && (
                    <button type="button" className="small danger" onClick={() => supprimer(u)}>Supprimer</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {form && (
        <Modal title={form.id ? "Modifier l'utilisateur" : 'Nouvel utilisateur'} onClose={() => setForm(null)}>
          <form onSubmit={enregistrer}>
            <Field label="Nom *"><input value={form.nom} onChange={set('nom')} /></Field>
            <Field label="Email *"><input type="email" value={form.email} onChange={set('email')} /></Field>
            <Field label={form.id ? 'Nouveau mot de passe (laisser vide pour ne pas changer)' : 'Mot de passe * (8 caractères minimum)'}>
              <input type="password" value={form.motDePasse} onChange={set('motDePasse')} autoComplete="new-password" />
            </Field>
            <Field label="Rôle *">
              <select value={form.roleCode} onChange={set('roleCode')}>
                {Object.entries(ROLES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
              </select>
            </Field>
            <label className="field" style={{ display: 'flex', gap: '.5rem', alignItems: 'center' }}>
              <input type="checkbox" checked={form.actif} onChange={(e) => setForm({ ...form, actif: e.target.checked })} />
              <span style={{ margin: 0 }}>Compte actif</span>
            </label>
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
