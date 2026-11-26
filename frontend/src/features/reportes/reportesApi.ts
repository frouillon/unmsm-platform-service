import { apiBlob, apiFetch } from '../../lib/api'
import { consultaReporte } from './filtros'
import type { AreaOpcion, FiltrosReporte, ReporteSolicitudes } from './types'

export function fetchReporte(filtros: FiltrosReporte): Promise<ReporteSolicitudes> {
  return apiFetch(`/api/reportes/solicitudes?${consultaReporte(filtros)}`)
}

export function fetchReporteCsv(filtros: FiltrosReporte): Promise<Blob> {
  return apiBlob(`/api/reportes/solicitudes/csv?${consultaReporte(filtros)}`)
}

export function fetchAreasReporte(): Promise<AreaOpcion[]> {
  return apiFetch('/api/reportes/areas')
}
