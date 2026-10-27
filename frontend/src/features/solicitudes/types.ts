import type { Estado, Prioridad } from '../../lib/domain'

export type Categoria = {
  id: number
  nombre: string
  area: string
  requiereUbicacion: boolean
}

export type PrioridadCatalogo = {
  id: number
  nombre: Prioridad
  slaDias: number
  diasHabiles: boolean
}

export type NuevaSolicitud = {
  categoriaId: number | null
  prioridadId: number | null
  asunto: string
  descripcion: string
  pabellon: string
  nivel: string
  ambiente: string
}

export type SolicitudResumen = {
  id: number
  codigo: string
  asunto: string
  categoria: string
  area: string
  prioridad: Prioridad
  estado: Estado
  tecnico: string | null
  fechaCreacion: string
  fechaVencimiento: string
  ultimaActualizacion: string
}

export type EvidenciaDetalle = {
  id: number
  nombre: string
  tipoMime: string
  tamanoBytes: number
}

export type Movimiento = {
  estadoOrigen: Estado | null
  estadoDestino: Estado
  usuario: string
  rol: string
  nota: string | null
  fecha: string
}

export type SolicitudDetalle = Omit<SolicitudResumen, 'ultimaActualizacion'> & {
  descripcion: string
  solicitante: string
  pabellon: string | null
  nivel: string | null
  ambiente: string | null
  evidencias: EvidenciaDetalle[]
  historial: Movimiento[]
}

export type SolicitudRegistrada = {
  id: number
  codigo: string
  asunto: string
  categoria: string
  area: string
  prioridad: Prioridad
  estado: Estado
  fechaCreacion: string
  fechaVencimiento: string
  evidencias: number
}

export type SolicitudGestionResumen = {
  id: number
  codigo: string
  asunto: string
  categoria: string
  area: string
  prioridad: string
  estado: string
  solicitante: string
  tecnico: string | null
  fechaCreacion: string
  fechaVencimiento: string
  ultimoMovimiento: string | null
}

export type AsignarSolicitudRequest = {
  tecnicoId: number
  notaTecnica?: string | null
}

export type CambiarEstadoRequest = {
  estadoDestino: string
  notaTecnica?: string | null
}
