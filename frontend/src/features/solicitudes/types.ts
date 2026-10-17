import type { Estado, Prioridad } from '../../lib/domain'

export type Categoria = {
  id: number
  nombre: string
  area: string
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
