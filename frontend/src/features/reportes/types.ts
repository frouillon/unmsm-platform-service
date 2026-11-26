import type { ConteoItem } from '../dashboard/types'
import type { Estado, Prioridad } from '../../lib/domain'

export type Cumplimiento = 'A tiempo' | 'Fuera de plazo' | 'En plazo' | 'Vencida' | 'Atendida'

export type FilaReporte = {
  codigo: string
  fechaCreacion: string
  categoria: string
  area: string
  prioridad: Prioridad
  estado: Estado
  tecnico: string | null
  fechaVencimiento: string
  fechaResolucion: string | null
  diasAtencion: number | null
  cumplimiento: Cumplimiento
}

export type CargaTecnico = {
  tecnico: string
  asignadas: number
  enProceso: number
  resueltas: number
}

export type ResumenReporte = {
  total: number
  atendidas: number
  abiertas: number
  vencidas: number
  atendidasFueraDePlazo: number
  cumplimientoSla: number | null
  mttrDias: number | null
  porEstado: ConteoItem[]
  porCategoria: ConteoItem[]
  porPrioridad: ConteoItem[]
}

export type ReporteSolicitudes = {
  desde: string
  hasta: string
  alcance: string
  generadoEn: string
  resumen: ResumenReporte
  cargaTecnica: CargaTecnico[]
  filas: FilaReporte[]
}

export type AreaOpcion = {
  id: number
  nombre: string
}

export type FiltrosReporte = {
  desde: string
  hasta: string
  /** Vacío = todas las áreas (solo administrador). */
  areaId: string
}
