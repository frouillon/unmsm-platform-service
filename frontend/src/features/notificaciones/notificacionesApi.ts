import { apiFetch } from '../../lib/api'
import type { Notificacion } from './types'

export function fetchNotificaciones() {
  return apiFetch<Notificacion[]>('/api/notificaciones')
}

export function fetchNoLeidas() {
  return apiFetch<{ cantidad: number }>('/api/notificaciones/no-leidas')
}

export function marcarLeida(id: number) {
  return apiFetch<{ ok: boolean }>(`/api/notificaciones/${id}/leer`, { method: 'POST' })
}

export function marcarTodasLeidas() {
  return apiFetch<{ actualizadas: number }>('/api/notificaciones/leer-todas', { method: 'POST' })
}
