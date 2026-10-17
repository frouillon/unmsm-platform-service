import { createContext } from 'react'
import type { UsuarioActual } from './types'

export type AuthStatus = 'loading' | 'authenticated' | 'anonymous'

export type AuthContextValue = {
  status: AuthStatus
  usuario: UsuarioActual | null
  sessionExpired: boolean
  login: (correo: string, password: string) => Promise<UsuarioActual>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
