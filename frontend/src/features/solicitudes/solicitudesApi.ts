import { apiFetch } from '../../lib/api'
import type { Categoria, NuevaSolicitud, PrioridadCatalogo, SolicitudRegistrada } from './types'

export function fetchCategorias(): Promise<Categoria[]> {
  return apiFetch('/api/catalogos/categorias')
}

export function fetchPrioridades(): Promise<PrioridadCatalogo[]> {
  return apiFetch('/api/catalogos/prioridades')
}

export function registrarSolicitud(datos: NuevaSolicitud, evidencias: File[]): Promise<SolicitudRegistrada> {
  const form = new FormData()
  form.append('solicitud', new Blob([JSON.stringify(datos)], { type: 'application/json' }))
  evidencias.forEach((archivo) => form.append('evidencias', archivo))
  return apiFetch('/api/solicitudes', { method: 'POST', body: form })
}
