import { useState } from 'react';
import api from '../api/axios.js';
import { useLoad } from '../hooks/useLoad.js';
import { Alert, Badge, Chargement, Field, PageHeader } from '../components/ui.jsx';
import { BarList, LineChart } from '../components/charts.jsx';
import { fmt } from '../labels.js';

const ANNEE = new Date().getFullYear();
const STATUT = { VALIDE: ['Calculé', 'vert'], MANQUANT: ['Manquant', 'orange'], IGNORE: ['Ignoré', 'gris'] };

function Detail({ collectivites }) {
  const [id, setId] = useState('');
  const [annee, setAnnee] = useState(String(ANNEE));
  const score = useLoad(
    () => (id ? api.get(`/scores/collectivite/${id}`, { params: { annee } }) : Promise.resolve({ data: null })),
    [id, annee]
  );
  const evo = useLoad(
    () => (id ? api.get(`/scores/evolution/${id}`) : Promise.resolve({ data: [] })),
    [id]
  );
  const s = score.data;

  return (
    <section className="card">
      <h2>Résultats d'une collectivité</h2>
      <div className="filters">
        <Field label="Collectivité">
          <select value={id} onChange={(e) => setId(e.target.value)}>
            <option value="">Choisir…</option>
            {collectivites.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}
          </select>
        </Field>
        <Field label="Année"><input type="number" value={annee} onChange={(e) => setAnnee(e.target.value)} /></Field>
      </div>
      <Alert>{score.error}</Alert>
      <Chargement loading={score.loading && !!id} />
      {s && (
        <>
          <div className="cards">
            <div className="stat vert"><span>Score global {s.annee}</span><b>{fmt(s.scoreGlobal)}</b></div>
            <div className="stat"><span>Indicateurs calculés</span><b>{s.nbValides}</b></div>
            <div className="stat"><span>Manquants (exclus du score)</span><b>{s.nbManquants}</b></div>
            <div className="stat"><span>Ignorés (règle objectif 0)</span><b>{s.nbIgnores}</b></div>
          </div>
          <table>
            <thead>
              <tr><th>Indicateur</th><th>Objectif</th><th>Réalisé</th><th>Période</th><th>Pond.</th><th>Score</th><th>Statut</th><th>Calcul</th></tr>
            </thead>
            <tbody>
              {s.indicateurs.map((i) => (
                <tr key={i.objectifId}>
                  <td>{i.indicateur}</td>
                  <td>{fmt(i.cible)} {i.unite}</td>
                  <td>{i.valeur === null ? '—' : `${fmt(i.valeur)} ${i.unite}`}</td>
                  <td>{i.periode || '—'}</td>
                  <td>{fmt(i.ponderation)}</td>
                  <td><strong>{fmt(i.score)}</strong></td>
                  <td><Badge tone={STATUT[i.statut][1]}>{STATUT[i.statut][0]}</Badge></td>
                  <td className="muted">{i.detail}</td>
                </tr>
              ))}
              {s.indicateurs.length === 0 && <tr><td colSpan={8} className="muted">Aucun objectif défini pour cette année.</td></tr>}
            </tbody>
          </table>
          <h2 style={{ marginTop: '1.25rem' }}>Évolution dans le temps</h2>
          <LineChart points={(evo.data || []).map((p) => ({ label: String(p.annee), value: Number(p.score) }))} />
        </>
      )}
    </section>
  );
}

function Comparaison({ collectivites }) {
  const [ids, setIds] = useState([]);
  const [annee, setAnnee] = useState(String(ANNEE));
  const cmp = useLoad(
    () => (ids.length >= 2 ? api.get('/scores/comparaison', { params: { ids: ids.join(','), annee } }) : Promise.resolve({ data: [] })),
    [ids.join(','), annee]
  );
  const bascule = (id) => setIds(ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id]);
  const liste = cmp.data || [];
  const noms = [...new Set(liste.flatMap((c) => c.indicateurs.map((i) => i.indicateur)))].sort();

  return (
    <section className="card">
      <h2>Comparer plusieurs collectivités</h2>
      <Field label="Année"><input type="number" value={annee} onChange={(e) => setAnnee(e.target.value)} style={{ maxWidth: 160 }} /></Field>
      <Field label="Collectivités (au moins deux)">
        <div className="checks">
          {collectivites.map((c) => (
            <label key={c.id}>
              <input type="checkbox" checked={ids.includes(c.id)} onChange={() => bascule(c.id)} />
              {c.nom}
            </label>
          ))}
        </div>
      </Field>
      <Alert>{cmp.error}</Alert>
      {ids.length < 2 && <p className="muted">Cochez au moins deux collectivités.</p>}
      {liste.length > 0 && (
        <>
          <h2 style={{ marginTop: '1rem' }}>Score global {annee}</h2>
          <BarList items={liste.map((c) => ({ label: c.nom, value: Number(c.scoreGlobal ?? 0) }))} />
          <p className="muted">Une collectivité sans score calculé apparaît à 0 sur ce graphique ; voir le tableau ci-dessous.</p>
          <table>
            <thead>
              <tr><th>Collectivité</th>{noms.map((n) => <th key={n}>{n}</th>)}<th>Global</th></tr>
            </thead>
            <tbody>
              {liste.map((c) => (
                <tr key={c.collectiviteId}>
                  <td><strong>{c.nom}</strong></td>
                  {noms.map((n) => {
                    const ind = c.indicateurs.find((i) => i.indicateur === n);
                    return <td key={n}>{ind ? fmt(ind.score) : '—'}</td>;
                  })}
                  <td><strong>{fmt(c.scoreGlobal)}</strong></td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </section>
  );
}

export default function Resultats() {
  const cs = useLoad(() => api.get('/collectivites'));
  return (
    <>
      <PageHeader title="Résultats et comparaisons" />
      <Alert>{cs.error}</Alert>
      <Chargement loading={cs.loading && !cs.data} />
      {cs.data && (
        <>
          <Detail collectivites={cs.data} />
          <Comparaison collectivites={cs.data} />
        </>
      )}
    </>
  );
}
