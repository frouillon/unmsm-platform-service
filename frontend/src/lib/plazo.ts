import type { Estado } from './domain'

export type TonoPlazo = 'ok' | 'bad' | 'warn' | 'muted'
export type Plazo = { tono: TonoPlazo; texto: string }

const ATENDIDAS: readonly Estado[] = ['Resuelto', 'Cerrado']
const DIA = 24 * 60 * 60 * 1000

export const estaAtendida = (estado: Estado) => ATENDIDAS.includes(estado)

const inicioDelDia = (fecha: Date) => new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate()).getTime()

const hora = new Intl.DateTimeFormat('es-PE', { hour: '2-digit', minute: '2-digit' })

/** Etiqueta de vencimiento: los días se cuentan por fecha calendario, no por bloques de 24 h. */
export function calcularPlazo(fechaVencimiento: string, estado: Estado, ahora = new Date()): Plazo {
  if (estaAtendida(estado)) return { tono: 'ok', texto: 'Atendida' }
  const vence = new Date(fechaVencimiento)
  const dias = Math.round((inicioDelDia(vence) - inicioDelDia(ahora)) / DIA)

  if (vence.getTime() < ahora.getTime()) {
    const atraso = -dias
    return { tono: 'bad', texto: atraso <= 0 ? 'Vencida hoy' : atraso === 1 ? 'Vencida ayer' : `Vencida hace ${atraso} días` }
  }
  if (dias === 0) return { tono: 'warn', texto: `Vence hoy, ${hora.format(vence)}` }
  if (dias === 1) return { tono: 'warn', texto: 'Vence mañana' }
  return { tono: dias <= 2 ? 'warn' : 'muted', texto: `Vence en ${dias} días` }
}
