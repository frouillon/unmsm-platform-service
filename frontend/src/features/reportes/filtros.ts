import type { Cumplimiento, FiltrosReporte } from './types'

/** Fecha local en formato AAAA-MM-DD (el backend trabaja en hora de Lima). */
export function isoLocal(fecha: Date): string {
  const y = fecha.getFullYear()
  const m = String(fecha.getMonth() + 1).padStart(2, '0')
  const d = String(fecha.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function menosDias(fecha: Date, dias: number): Date {
  const copia = new Date(fecha)
  copia.setDate(copia.getDate() - dias)
  return copia
}

export const RANGOS = {
  '7d': 'Últimos 7 días',
  '30d': 'Últimos 30 días',
  mes: 'Este mes',
  'mes-anterior': 'Mes anterior',
} as const
export type Rango = keyof typeof RANGOS

/** Rangos rápidos, ambos extremos inclusive. */
export function rangoRapido(rango: Rango, hoy = new Date()): { desde: string; hasta: string } {
  switch (rango) {
    case '7d':
      return { desde: isoLocal(menosDias(hoy, 6)), hasta: isoLocal(hoy) }
    case '30d':
      return { desde: isoLocal(menosDias(hoy, 29)), hasta: isoLocal(hoy) }
    case 'mes':
      return { desde: isoLocal(new Date(hoy.getFullYear(), hoy.getMonth(), 1)), hasta: isoLocal(hoy) }
    case 'mes-anterior':
      return {
        desde: isoLocal(new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1)),
        hasta: isoLocal(new Date(hoy.getFullYear(), hoy.getMonth(), 0)),
      }
  }
}

/** Mismo rango que valida el backend: inicio ≤ fin y como máximo un año. */
export function validarRango(desde: string, hasta: string): string | null {
  if (!desde || !hasta) return 'Indica ambas fechas.'
  if (desde > hasta) return 'La fecha inicial no puede ser posterior a la final.'
  const dias = (Date.parse(hasta) - Date.parse(desde)) / 86_400_000 + 1
  if (dias > 366) return 'El rango no puede superar un año.'
  return null
}

export function consultaReporte({ desde, hasta, areaId }: FiltrosReporte): string {
  const params = new URLSearchParams({ desde, hasta })
  if (areaId) params.set('areaId', areaId)
  return params.toString()
}

export function nombreCsv({ desde, hasta }: FiltrosReporte): string {
  return `reporte-solicitudes_${desde}_${hasta}.csv`
}

export const TONO_CUMPLIMIENTO: Record<Cumplimiento, string> = {
  'A tiempo': 'bg-ok-soft text-ok',
  Atendida: 'bg-ok-soft text-ok',
  'En plazo': 'bg-subtle-soft text-subtle',
  'Fuera de plazo': 'bg-warn-soft text-warn',
  Vencida: 'bg-bad-soft text-bad',
}

/** "93.3 %" o "—" si todavía no hay datos. */
export function porcentaje(valor: number | null): string {
  return valor === null ? '—' : `${valor.toLocaleString('es-PE', { maximumFractionDigits: 1 })} %`
}

/** "2.4 días", "1 día" o "—". */
export function dias(valor: number | null): string {
  if (valor === null) return '—'
  const texto = valor.toLocaleString('es-PE', { maximumFractionDigits: 1 })
  return `${texto} ${valor === 1 ? 'día' : 'días'}`
}
