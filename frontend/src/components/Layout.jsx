import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { ROLES } from '../labels.js';

const NAV = [
  ['/', 'Tableau de bord'],
  ['/collectivites', 'Collectivités'],
  ['/indicateurs', 'Indicateurs'],
  ['/objectifs', 'Objectifs'],
  ['/realisations', 'Réalisations'],
  ['/resultats', 'Résultats'],
  ['/projets', 'Projets'],
  ['/rapports', 'Rapports'],
  ['/referentiels', 'Référentiels'],
  ['/utilisateurs', 'Utilisateurs', ['ADMIN']],
];

export default function Layout() {
  const { user, logout } = useAuth();
  const [ouvert, setOuvert] = useState(false);

  return (
    <div className="layout">
      <aside className={`sidebar ${ouvert ? 'ouvert' : ''}`}>
        <h2>Territoires</h2>
        <nav>
          {NAV.filter(([, , roles]) => !roles || roles.includes(user.role)).map(([to, label]) => (
            <NavLink key={to} to={to} end={to === '/'} onClick={() => setOuvert(false)}>
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <main className="content">
        <header className="topbar">
          <button type="button" className="menu-btn" onClick={() => setOuvert(!ouvert)} aria-label="Menu">☰</button>
          <span className="who">
            {user.nom} — <span className="muted">{ROLES[user.role] || user.role}</span>
          </span>
          <button type="button" className="secondary" onClick={logout}>Déconnexion</button>
        </header>
        <Outlet />
      </main>
    </div>
  );
}
