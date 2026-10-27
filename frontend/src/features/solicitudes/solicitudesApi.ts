import { apiBlob, apiFetch } from '../../lib/api'
import type {
  Categoria,
  NuevaSolicitud,
  PrioridadCatalogo,
  SolicitudDetalle,
  SolicitudRegistrada,
  SolicitudResumen,
} from './types'

export function fetchMisSolicitudes(): Promise<SolicitudResumen[]> {
  return apiFetch('/api/solicitudes/mias')
}

export function fetchSolicitud(codigo: string): Promise<SolicitudDetalle> {
  return apiFetch(`/api/solicitudes/${encodeURIComponent(codigo)}`)
}

export function fetchEvidencia(codigo: string, evidenciaId: number): Promise<Blob> {
  return apiBlob(`/api/solicitudes/${encodeURIComponent(codigo)}/evidencias/${evidenciaId}`)
}

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

import type { AsignarSolicitudRequest, CambiarEstadoRequest, SolicitudGestionResumen } from './types'

export function fetchAsignadas(): Promise<SolicitudGestionResumen[]> {
  return apiFetch('/api/solicitudes/asignadas')
}

export function fetchGestion(): Promise<SolicitudGestionResumen[]> {
  return apiFetch('/api/solicitudes/gestion')
}

export function asignarSolicitud(codigo: string, datos: AsignarSolicitudRequest): Promise<void> {
  return apiFetch(`/api/solicitudes/${encodeURIComponent(codigo)}/asignar`, { method: 'PATCH', body: datos })
}

export function cambiarEstadoSolicitud(codigo: string, datos: CambiarEstadoRequest): Promise<void> {
  return apiFetch(`/api/solicitudes/${encodeURIComponent(codigo)}/estado`, { method: 'PATCH', body: datos })
}
