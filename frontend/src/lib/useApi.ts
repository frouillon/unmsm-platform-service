import { useCallback, useEffect, useState } from 'react'
import { ApiError } from './api'

type Estado<T> =
  | { status: 'loading'; data?: undefined; error?: undefined }
  | { status: 'success'; data: T; error?: undefined }
  | { status: 'error'; data?: undefined; error: ApiError }

/** Carga datos al montar y cuando cambia `key`; `reload` repite la petición. */
export function useApi<T>(fetcher: () => Promise<T>, key: string) {
  const [state, setState] = useState<Estado<T> & { key: string; intento: number }>({
    status: 'loading',
    key,
    intento: 0,
  })

  const [intento, setIntento] = useState(0)
  if (state.key !== key || state.intento !== intento) {
    setState({ status: 'loading', key, intento })
  }

  useEffect(() => {
    let vigente = true
    fetcher()
      .then((data) => vigente && setState({ status: 'success', data, key, intento }))
      .catch((error: unknown) => {
        const apiError = error instanceof ApiError ? error : new ApiError(0, 'Ocurrió un error inesperado.')
        if (vigente) setState({ status: 'error', error: apiError, key, intento })
      })
    return () => {
      vigente = false
    }
    // `fetcher` se identifica por `key`: así no hace falta memorizarlo en cada página.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key, intento])

  const reload = useCallback(() => setIntento((n) => n + 1), [])
  return { ...state, reload }
}
