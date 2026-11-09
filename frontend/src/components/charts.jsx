/** Barres horizontales : items = [{ label, value }] (valeurs de 0 à 100). */
export function BarList({ items }) {
  if (!items.length) return <p className="muted">Aucune donnée.</p>;
  return (
    <div>
      {items.map((it) => (
        <div className="bar" key={it.label}>
          <i title={it.label}>{it.label}</i>
          <u>
            <s style={{ width: `${Math.max(0, Math.min(100, it.value))}%` }} />
          </u>
          <em>{Math.round(it.value)}</em>
        </div>
      ))}
    </div>
  );
}

/** Courbe : points = [{ label, value }] (valeurs de 0 à 100). */
export function LineChart({ points }) {
  if (!points.length) return <p className="muted">Aucune donnée.</p>;
  const W = 400;
  const H = 180;
  const x0 = 40;
  const x1 = 380;
  const y0 = 150;
  const y1 = 20;
  const x = (i) => (points.length === 1 ? (x0 + x1) / 2 : x0 + (i * (x1 - x0)) / (points.length - 1));
  const y = (v) => y0 - (Math.max(0, Math.min(100, v)) * (y0 - y1)) / 100;
  const ligne = points.map((p, i) => `${x(i)},${y(p.value)}`).join(' ');
  return (
    <svg viewBox={`0 0 ${W} ${H}`} width="100%" role="img" aria-label="Évolution du score dans le temps">
      {[0, 50, 100].map((g) => (
        <g key={g}>
          <line x1={x0} y1={y(g)} x2={x1} y2={y(g)} stroke="#e1e6ed" />
          <text x="4" y={y(g) + 4} fontSize="11" fill="#5a6675">{g}</text>
        </g>
      ))}
      <polyline points={ligne} fill="none" stroke="#1f9d55" strokeWidth="3" />
      {points.map((p, i) => (
        <g key={p.label}>
          <circle cx={x(i)} cy={y(p.value)} r="4" fill="#0b2a4a" />
          <text x={x(i)} y={H - 6} fontSize="12" textAnchor="middle" fill="#5a6675">{p.label}</text>
        </g>
      ))}
    </svg>
  );
}
