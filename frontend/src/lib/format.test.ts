import { describe, expect, it } from 'vitest'
import { formatBytes, formatFecha, haceTiempo, plazoTexto } from './format'

const KB = 1024
const MB = 1024 * 1024

/*
 * Textos de apoyo que se muestran en las bandejas y en el detalle.
 * Técnicas: análisis de valores límite (cambios de unidad)
 * y tabla de decisión (cantidad × tipo de día).
 */
describe('formatBytes — análisis de valores límite', () => {
  it('cambia de unidad en 1 KB y en 1 MB', () => {
    expect(formatBytes(0)).toBe('0 B')
    expect(formatBytes(KB - 1)).toBe('1023 B')
    expect(formatBytes(KB)).toBe('1 KB')
    expect(formatBytes(MB)).toBe('1.0 MB')
    expect(formatBytes(5 * MB)).toBe('5.0 MB')
  })
})

describe('plazoTexto — tabla de decisión', () => {
  it('combina singular o plural con días hábiles o calendario', () => {
    expect(plazoTexto(1, true)).toBe('1 día hábil')
    expect(plazoTexto(3, true)).toBe('3 días hábiles')
    expect(plazoTexto(1, false)).toBe('1 día calendario')
    expect(plazoTexto(7, false)).toBe('7 días calendario')
  })
})

describe('haceTiempo — análisis de valores límite', () => {
  const MINUTO = 60
  const HORA = 60 * MINUTO
  const DIA = 24 * HORA

  /** Momento fijo para la prueba: martes 20 de octubre de 2026, 10:00. */
  const AHORA = new Date(2026, 9, 20, 10, 0, 0)

  /** Fecha ISO de un instante ocurrido `segundos` antes de AHORA. */
  const hace = (segundos: number) => new Date(AHORA.getTime() - segundos * 1000).toISOString()

  it('pasa de «hace un momento» a minutos, horas, días y, tras 30 días, a la fecha', () => {
    expect(haceTiempo(hace(59), AHORA)).toBe('hace un momento')
    expect(haceTiempo(hace(MINUTO), AHORA)).toBe('hace 1 minuto')
    expect(haceTiempo(hace(HORA), AHORA)).toBe('hace 1 hora')
    expect(haceTiempo(hace(DIA), AHORA)).toBe('ayer')
    expect(haceTiempo(hace(29 * DIA), AHORA)).toBe('hace 29 días')

    const haceUnMes = hace(30 * DIA)
    expect(haceTiempo(haceUnMes, AHORA)).toBe(`el ${formatFecha(haceUnMes)}`)
  })
})
