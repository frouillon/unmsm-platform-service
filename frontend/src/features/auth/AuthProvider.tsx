import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { setUnauthorizedHandler } from '../../lib/api'
import { clearToken, getToken, setToken } from '../../lib/session'
import * as authApi from './authApi'
import { AuthContext, type AuthContextValue, type AuthStatus } from './authContext'
import type { UsuarioActual } from './types'

type AuthState = {
  status: AuthStatus
  usuario: UsuarioActual | null
  sessionExpired: boolean
}

const anonymous: AuthState = { status: 'anonymous', usuario: null, sessionExpired: false }

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>(() =>
    getToken() ? { status: 'loading', usuario: null, sessionExpired: false } : anonymous,
  )

  useEffect(() => {
    setUnauthorizedHandler(() => {
      clearToken()
      setState({ status: 'anonymous', usuario: null, sessionExpired: true })
    })
    return () => setUnauthorizedHandler(null)
  }, [])

  useEffect(() => {
    if (!getToken()) return
    authApi
      .fetchUsuarioActual()
      .then((usuario) => setState({ status: 'authenticated', usuario, sessionExpired: false }))
      .catch(() => {
        clearToken()
        setState((prev) => (prev.status === 'loading' ? anonymous : prev))
      })
  }, [])

  const login = useCallback(async (correo: string, password: string) => {
    const response = await authApi.login(correo, password)
    setToken(response.token)
    setState({ status: 'authenticated', usuario: response.usuario, sessionExpired: false })
    return response.usuario
  }, [])

  const logout = useCallback(() => {
    clearToken()
    setState(anonymous)
  }, [])

  const value = useMemo<AuthContextValue>(() => ({ ...state, login, logout }), [state, login, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
