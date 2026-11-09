import { useState } from 'react';
import api, { getErrorMessage, telecharger } from '../api/axios.js';
import { useLoad } from '../hooks/useLoad.js';
import { Alert, Field, PageHeader } from '../components/ui.jsx';
import { TYPES } from '../labels.js';

const ANNEE = new Date().getFullYear();
const RAPPORTS = {
  collectivites: 'Résultats par collectivité (classement)',
  collectivite: "Scores par indicateur d'une collectivité",
  comparaison: 'Comparaison de plusieurs collectivités',
  evolution: 'Évolution des performances dans le temps',
};

export default function Rapports() {
  const [rapport, setRapport] = useState('collectivites');
  const [format, setFormat] = useState('pdf');
  const [annee, setAnnee] = useState(String(ANNEE));
  const [regionId, setRegionId] = useState('');
  const [typeCollectivite, setTypeCollectivite] = useState('');
  const [collectiviteId, setCollectiviteId] = useState('');
  const [ids, setIds] = useState([]);
  const [erreur, setErreur] = useState('');
  const [envoi, setEnvoi] = useState(false);
  const regions = useLoad(() => api.get('/referentiels/regions'));
  const collectivites = useLoad(() => api.get('/collectivites'));
  const cs = collectivites.data || [];

  async function generer(e) {
    e.preventDefault();
    setErreur('');
    if (rapport === 'collectivite' && !collectiviteId) return setErreur('Choisissez une collectivité.');
    if (rapport === 'comparaison' && ids.length < 2) return setErreur('Cochez au moins deux collectivités.');
    setEnvoi(true);
    try {
      await telecharger(`/rapports/${rapport}`, {
        format, annee,
        regionId: rapport === 'collectivites' ? regionId : '',
        typeCollectivite: rapport === 'collectivites' ? typeCollectivite : '',
        collectiviteId: rapport === 'collectivite' || rapport === 'evolution' ? collectiviteId : '',
        ids: rapport === 'comparaison' ? ids.join(',') : '',
      });
    } catch (err) {
      setErreur(getErrorMessage(err));
    } finally {
      setEnvoi(false);
    }
  }

  const bascule = (id) => setIds(ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id]);

  return (
    <>
      <PageHeader title="Rapports" />
      <form className="card" onSubmit={generer} style={{ maxWidth: 640 }}>
        <Field label="Type de rapport">
          <select value={rapport} onChange={(e) => setRapport(e.target.value)}>
            {Object.entries(RAPPORTS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </Field>
        <div className="row2">
          <Field label="Format">
            <select value={format} onChange={(e) => setFormat(e.target.value)}>
              <option value="pdf">PDF</option>
              <option value="excel">Excel (.xlsx)</option>
            </select>
          </Field>
          {rapport !== 'evolution' && (
            <Field label="Année"><input type="number" value={annee} onChange={(e) => setAnnee(e.target.value)} /></Field>
          )}
        </div>

        {rapport === 'collectivites' && (
          <div className="row2">
            <Field label="Région">
              <select value={regionId} onChange={(e) => setRegionId(e.target.value)}>
                <option value="">Toutes</option>
                {(regions.data || []).map((r) => <option key={r.id} value={r.id}>{r.nom}</option>)}
              </select>
            </Field>
            <Field label="Type">
              <select value={typeCollectivite} onChange={(e) => setTypeCollectivite(e.target.value)}>
                <option value="">Tous</option>
                {Object.entries(TYPES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
              </select>
            </Field>
          </div>
        )}

        {(rapport === 'collectivite' || rapport === 'evolution') && (
          <Field label={rapport === 'evolution' ? 'Collectivité (vide = toutes)' : 'Collectivité *'}>
            <select value={collectiviteId} onChange={(e) => setCollectiviteId(e.target.value)}>
              <option value="">{rapport === 'evolution' ? 'Toutes' : 'Choisir…'}</option>
              {cs.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
            </select>
          </Field>
        )}

        {rapport === 'comparaison' && (
          <Field label="Collectivités à comparer * (au moins deux)">
            <div className="checks">
              {cs.map((c) => (
                <label key={c.id}>
                  <input type="checkbox" checked={ids.includes(c.id)} onChange={() => bascule(c.id)} />
                  {c.nom}
                </label>
              ))}
            </div>
          </Field>
        )}

        <Alert>{erreur || collectivites.error}</Alert>
        <div className="form-actions">
          <button type="submit" disabled={envoi}>{envoi ? 'Génération…' : 'Télécharger le rapport'}</button>
        </div>
      </form>
    </>
  );
}
