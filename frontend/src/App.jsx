import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './auth/ProtectedRoute.jsx';
import Layout from './components/Layout.jsx';
import Login from './pages/Login.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Collectivites from './pages/Collectivites.jsx';
import Indicateurs from './pages/Indicateurs.jsx';
import Objectifs from './pages/Objectifs.jsx';
import Realisations from './pages/Realisations.jsx';
import Resultats from './pages/Resultats.jsx';
import Projets from './pages/Projets.jsx';
import Rapports from './pages/Rapports.jsx';
import Referentiels from './pages/Referentiels.jsx';
import Utilisateurs from './pages/Utilisateurs.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<Dashboard />} />
        <Route path="/collectivites" element={<Collectivites />} />
        <Route path="/indicateurs" element={<Indicateurs />} />
        <Route path="/objectifs" element={<Objectifs />} />
        <Route path="/realisations" element={<Realisations />} />
        <Route path="/resultats" element={<Resultats />} />
        <Route path="/projets" element={<Projets />} />
        <Route path="/rapports" element={<Rapports />} />
        <Route path="/referentiels" element={<Referentiels />} />
        <Route
          path="/utilisateurs"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <Utilisateurs />
            </ProtectedRoute>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
