import { useState } from 'react';
import api, { getErrorMessage } from '../api/axios.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { useLoad } from '../hooks/useLoad.js';
import { Alert, PageHeader } from '../components/ui.jsx';
import { canWrite } from '../labels.js';

function Bloc({ titre, type, ecriture }) {
  const liste = useLoad(() => api.get(`/referentiels/${type}`));
  const [nom, setNom] = useState('');
  const [erreur, setErreur] = useState('');

  async function ajouter(e) {
    e.preventDefault();
    setErreur('');
    if (!nom.trim()) return setErreur('Le nom est obligatoire.');
    try {
      await api.post(`/referentiels/${type}`, { nom: nom.trim() });
      setNom('');
      liste.reload();
    } catch (err) {
      setErreur(getErrorMessage(err));
    }
  }

  async function supprimer(x) {
    if (!window.confirm(`Supprimer « ${x.nom} » ?`)) return;
    setErreur('');
    try {
      await api.delete(`/referentiels/${type}/${x.id}`);
      liste.reload();
    } catch (err) {
      setErreur(getErrorMessage(err));
    }
  }

  return (
    <section className="card">
      <h2>{titre}</h2>
      {ecriture && (
        <form className="actions" onSubmit={ajouter}>
          <input style={{ flex: 1, minWidth: 160 }} placeholder="Nouveau nom" value={nom} onChange={(e) => setNom(e.target.value)} />
          <button type="submit">Ajouter</button>
        </form>
      )}
      <Alert>{erreur || liste.error}</Alert>
      <table>
        <tbody>
          {(liste.data || []).map((x) => (
            <tr key={x.id}>
              <td>{x.nom}</td>
              {ecriture && (
                <td className="actions-cell">
                  <button type="button" className="small danger" onClick={() => supprimer(x)}>Supprimer</button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

export default function Referentiels() {
  const { user } = useAuth();
  const ecriture = canWrite(user.role);
  return (
    <>
      <PageHeader title="Référentiels" />
      <div className="grid2">
        <Bloc titre="Régions" type="regions" ecriture={ecriture} />
        <Bloc titre="Catégories d'indicateurs" type="categories" ecriture={ecriture} />
        <Bloc titre="Domaines de compétence" type="domaines" ecriture={ecriture} />
      </div>
    </>
  );
}
