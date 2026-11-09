import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
});

// Ajoute le jeton JWT à chaque requête
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Session expirée ou jeton invalide : retour à la page de connexion
api.interceptors.response.use(
  (res) => res,
  (error) => {
    const isLogin = error.config?.url?.includes('/auth/login');
    if (error.response?.status === 401 && !isLogin) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/** Message d'erreur lisible pour l'utilisateur. */
export function getErrorMessage(error) {
  if (!error.response) return 'Impossible de joindre le serveur. Vérifiez que le backend est démarré.';
  if (error.response.status === 403) return error.response.data?.message || 'Droits insuffisants pour cette action.';
  return error.response.data?.message || `Erreur du serveur (${error.response.status}).`;
}

/** Retire les paramètres vides avant l'envoi. */
export function nettoyer(params) {
  const res = {};
  Object.entries(params).forEach(([k, v]) => {
    if (v !== '' && v !== null && v !== undefined) res[k] = v;
  });
  return res;
}

/** Télécharge un fichier (rapport PDF ou Excel) avec le jeton JWT. */
export async function telecharger(url, params) {
  try {
    const res = await api.get(url, { params: nettoyer(params), responseType: 'blob' });
    const cd = res.headers['content-disposition'] || '';
    const m = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(cd);
    const nom = m ? decodeURIComponent(m[1]) : 'rapport';
    const lien = document.createElement('a');
    lien.href = URL.createObjectURL(res.data);
    lien.download = nom;
    document.body.appendChild(lien);
    lien.click();
    lien.remove();
    URL.revokeObjectURL(lien.href);
  } catch (err) {
    // En cas d'erreur, le corps est un Blob : on le relit pour récupérer le message du serveur
    if (err.response?.data instanceof Blob) {
      let data = {};
      try {
        data = JSON.parse(await err.response.data.text());
      } catch (e) {
        data = {};
      }
      throw { response: { status: err.response.status, data } };
    }
    throw err;
  }
}

export default api;
