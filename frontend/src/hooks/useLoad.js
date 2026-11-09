import { useEffect, useState } from 'react';
import { getErrorMessage } from '../api/axios.js';

/** Charge des données depuis l'API : fn() doit renvoyer une promesse Axios. */
export function useLoad(fn, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tick, setTick] = useState(0);

  useEffect(() => {
    let annule = false;
    setLoading(true);
    setError('');
    fn()
      .then((res) => {
        if (!annule) setData(res.data);
      })
      .catch((err) => {
        if (!annule) setError(getErrorMessage(err));
      })
      .finally(() => {
        if (!annule) setLoading(false);
      });
    return () => {
      annule = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, tick]);

  return { data, loading, error, reload: () => setTick((t) => t + 1) };
}
