import { apiFetch } from '../../lib/api'
import type { LoginResponse, UsuarioActual } from './types'

export function login(correo: string, password: string): Promise<LoginResponse> {
  return apiFetch<LoginResponse>('/api/auth/login', { method: 'POST', body: { correo, password } })
}

export function fetchUsuarioActual(): Promise<UsuarioActual> {
  return apiFetch<UsuarioActual>('/api/auth/me')
}
