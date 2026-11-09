import { useState } from 'react';
import api, { nettoyer } from '../api/axios.js';
import { useLoad } from '../hooks/useLoad.js';
import { Alert, Chargement, Field, PageHeader } from '../components/ui.jsx';
import { BarList, LineChart } from '../components/charts.jsx';
import { TYPES, fmt, fmtMoney } from '../labels.js';

export default function Dashboard() {
  const [annee, setAnnee] = useState('');
  const [regionId, setRegionId] = useState('');
  const [type, setType] = useState('');
  const [domaineId, setDomaineId] = useState('');

  const regions = useLoad(() => api.get('/referentiels/regions'));
  const domaines = useLoad(() => api.get('/referentiels/domaines'));
  const d = useLoad(
    () => api.get('/dashboard', { params: nettoyer({ annee, regionId, type, domaineId }) }),
    [annee, regionId, type, domaineId]
  );
  const data = d.data;

  return (
    <>
      <PageHeader title="Tableau de bord">
        <button type="button" className="secondary" onClick={d.reload}>Actualiser</button>
      </PageHeader>

      <div className="filters">
        <Field label="Période (année)">
          <select value={annee} onChange={(e) => setAnnee(e.target.value)}>
            <option value="">{data ? `${data.annee} (dernière)` : 'Dernière année'}</option>
            {(data?.annees || []).map((a) => <option key={a} value={a}>{a}</option>)}
          </select>
        </Field>
        <Field label="Région">
          <select value={regionId} onChange={(e) => setRegionId(e.target.value)}>
            <option value="">Toutes</option>
            {(regions.data || []).map((r) => <option key={r.id} value={r.id}>{r.nom}</option>)}
          </select>
        </Field>
        <Field label="Type de collectivité">
          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="">Tous</option>
            {Object.entries(TYPES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
        </Field>
        <Field label="Domaine (projets)">
          <select value={domaineId} onChange={(e) => setDomaineId(e.target.value)}>
            <option value="">Tous</option>
            {(domaines.data || []).map((x) => <option key={x.id} value={x.id}>{x.nom}</option>)}
          </select>
        </Field>
      </div>

      <Alert>{d.error}</Alert>
      <Chargement loading={d.loading && !data} />

      {data && (
        <>
          <div className="cards">
            <div className="stat"><span>Collectivités</span><b>{data.nbCollectivites}</b></div>
            <div className="stat"><span>Indicateurs</span><b>{data.nbIndicateurs}</b></div>
            <div className="stat"><span>Projets</span><b>{data.nbProjets}</b></div>
            <div className="stat vert"><span>Score moyen global</span><b>{fmt(data.scoreMoyen)}</b></div>
            <div className="stat vert">
              <span>Taux d'exécution des projets</span>
              <b>{data.tauxExecution === null ? '—' : `${fmt(data.tauxExecution, 1)} %`}</b>
            </div>
          </div>
          <p className="muted">
            Budget exécuté : {fmtMoney(data.budgetExecute)} sur {fmtMoney(data.budgetPrevu)} prévu.
            Les scores utilisent uniquement les réalisations validées.
          </p>

          <div className="grid2">
            <section className="card">
              <h2>Score moyen par région ({data.annee})</h2>
              <BarList items={data.scoreParRegion.map((r) => ({ label: r.region, value: Number(r.score) }))} />
            </section>
            <section className="card">
              <h2>Évolution du score moyen</h2>
              <LineChart points={data.evolution.map((p) => ({ label: String(p.annee), value: Number(p.score) }))} />
            </section>
          </div>

          <section className="card">
            <h2>Classement des collectivités ({data.annee})</h2>
            {data.classement.length === 0 ? (
              <p className="muted">Aucune collectivité pour ces filtres.</p>
            ) : (
              <table>
                <thead>
                  <tr><th>Rang</th><th>Collectivité</th><th>Type</th><th>Région</th><th>Score</th><th>Indicateurs calculés</th></tr>
                </thead>
                <tbody>
                  {data.classement.map((l) => (
                    <tr key={l.collectiviteId}>
                      <td>{l.rang ?? '—'}</td>
                      <td>{l.nom}</td>
                      <td>{TYPES[l.type]}</td>
                      <td>{l.region}</td>
                      <td><strong>{fmt(l.score)}</strong></td>
                      <td>{l.nbValides} / {l.nbTotal}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>
        </>
      )}
    </>
  );
}
