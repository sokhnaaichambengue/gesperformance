export function Modal({ title, onClose, children }) {
  return (
    <div className="overlay" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={title}>
        <div className="modal-head">
          <h2>{title}</h2>
          <button type="button" className="ghost" onClick={onClose} aria-label="Fermer">✕</button>
        </div>
        {children}
      </div>
    </div>
  );
}

export function Field({ label, children }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
    </label>
  );
}

export function Alert({ children, type = 'error' }) {
  if (!children) return null;
  return <div className={type === 'error' ? 'error' : 'info'}>{children}</div>;
}

export function PageHeader({ title, children }) {
  return (
    <div className="page-head">
      <h1>{title}</h1>
      <div className="actions">{children}</div>
    </div>
  );
}

export function Badge({ children, tone = 'gris' }) {
  return <span className={`badge ${tone}`}>{children}</span>;
}

export function Chargement({ loading }) {
  return loading ? <p className="muted">Chargement…</p> : null;
}
