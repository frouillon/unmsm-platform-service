import { describe, expect, it } from 'vitest'
import { calcularPlazo } from './plazo'

/** Momento fijo para todas las pruebas: martes 20 de octubre de 2026, 10:00. */
const AHORA = new Date(2026, 9, 20, 10, 0)

/*
 * HU-16 · RF-28 — Etiqueta del plazo de una solicitud.
 * Técnicas: partición de equivalencia (solicitud atendida / sin atender)
 * y análisis de valores límite sobre los días que faltan o que pasaron.
 * Los días se cuentan por fecha calendario, no por bloques de 24 horas.
 */
describe('calcularPlazo', () => {
  describe('partición de equivalencia', () => {
    it('una solicitud Resuelta o Cerrada figura como «Atendida» aunque su plazo haya vencido', () => {
      const vencida = '2026-10-01T09:00:00'

      expect(calcularPlazo(vencida, 'Resuelto', AHORA)).toEqual({ tono: 'ok', texto: 'Atendida' })
      expect(calcularPlazo(vencida, 'Cerrado', AHORA)).toEqual({ tono: 'ok', texto: 'Atendida' })
    })
  })

  describe('análisis de valores límite: plazo vigente', () => {
    it('0 días: «Vence hoy» con la hora, en tono de advertencia', () => {
      const plazo = calcularPlazo('2026-10-20T18:00:00', 'Pendiente', AHORA)

      expect(plazo.tono).toBe('warn')
      expect(plazo.texto).toMatch(/^Vence hoy, /)
    })

    it('1 día: «Vence mañana», aunque falten menos de 24 horas', () => {
      const plazo = calcularPlazo('2026-10-21T09:00:00', 'Pendiente', AHORA)

      expect(plazo).toEqual({ tono: 'warn', texto: 'Vence mañana' })
    })

    it('2 días todavía es advertencia; desde 3 días el tono es neutro', () => {
      const enDosDias = calcularPlazo('2026-10-22T10:00:00', 'En proceso', AHORA)
      const enTresDias = calcularPlazo('2026-10-23T10:00:00', 'En proceso', AHORA)

      expect(enDosDias).toEqual({ tono: 'warn', texto: 'Vence en 2 días' })
      expect(enTresDias).toEqual({ tono: 'muted', texto: 'Vence en 3 días' })
    })
  })

  describe('análisis de valores límite: plazo vencido', () => {
    it('venció hace unas horas: «Vencida hoy»', () => {
      const plazo = calcularPlazo('2026-10-20T08:00:00', 'Asignado', AHORA)

      expect(plazo).toEqual({ tono: 'bad', texto: 'Vencida hoy' })
    })

    it('venció el día anterior: «Vencida ayer»', () => {
      const plazo = calcularPlazo('2026-10-19T23:59:00', 'Asignado', AHORA)

      expect(plazo).toEqual({ tono: 'bad', texto: 'Vencida ayer' })
    })

    it('venció hace 2 días o más: «Vencida hace N días»', () => {
      const haceDosDias = calcularPlazo('2026-10-18T10:00:00', 'Asignado', AHORA)
      const haceCincoDias = calcularPlazo('2026-10-15T10:00:00', 'Asignado', AHORA)

      expect(haceDosDias).toEqual({ tono: 'bad', texto: 'Vencida hace 2 días' })
      expect(haceCincoDias).toEqual({ tono: 'bad', texto: 'Vencida hace 5 días' })
    })
  })
})
