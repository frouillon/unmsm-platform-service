import { apiBlob, apiFetch } from '../../lib/api'
import type {
  Categoria,
  NuevaSolicitud,
  PrioridadCatalogo,
  SolicitudDetalle,
  SolicitudRegistrada,
  SolicitudResumen,
  TecnicoOpcion,
} from './types'

export function fetchMisSolicitudes(): Promise<SolicitudResumen[]> {
  return apiFetch('/api/solicitudes/mias')
}

export function fetchSolicitudesArea(): Promise<SolicitudResumen[]> {
  return apiFetch('/api/solicitudes/area')
}

export function fetchAsignadas(): Promise<SolicitudResumen[]> {
  return apiFetch('/api/solicitudes/asignadas')
}

export function fetchTodas(): Promise<SolicitudResumen[]> {
  return apiFetch('/api/solicitudes/todas')
}

export function fetchTecnicos(codigo: string): Promise<TecnicoOpcion[]> {
  return apiFetch(`/api/solicitudes/${encodeURIComponent(codigo)}/tecnicos`)
}

export function avanzarSolicitud(
  codigo: string,
  estado: string,
  tecnicoId: number | null,
  nota: string,
): Promise<SolicitudDetalle> {
  return apiFetch(`/api/solicitudes/${encodeURIComponent(codigo)}/estado`, {
    method: 'POST',
    body: { estado, tecnicoId, nota: nota.trim() || null },
  })
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
