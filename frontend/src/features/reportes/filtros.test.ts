import { describe, expect, it } from 'vitest'
import { consultaReporte, dias, isoLocal, nombreCsv, porcentaje, rangoRapido, validarRango } from './filtros'

const HOY = new Date(2026, 10, 26, 15, 30) // 26 nov 2026

describe('rangoRapido', () => {
  it('calcula los rangos con ambos extremos inclusive', () => {
    expect(rangoRapido('7d', HOY)).toEqual({ desde: '2026-11-20', hasta: '2026-11-26' })
    expect(rangoRapido('30d', HOY)).toEqual({ desde: '2026-10-28', hasta: '2026-11-26' })
    expect(rangoRapido('mes', HOY)).toEqual({ desde: '2026-11-01', hasta: '2026-11-26' })
    expect(rangoRapido('mes-anterior', HOY)).toEqual({ desde: '2026-10-01', hasta: '2026-10-31' })
  })

  it('el mes anterior de enero es diciembre del año pasado', () => {
    expect(rangoRapido('mes-anterior', new Date(2027, 0, 10))).toEqual({ desde: '2026-12-01', hasta: '2026-12-31' })
  })
})

describe('validarRango', () => {
  it('acepta rangos de hasta un año', () => {
    expect(validarRango('2026-11-01', '2026-11-30')).toBeNull()
    expect(validarRango('2026-11-26', '2026-11-26')).toBeNull()
    expect(validarRango('2025-11-26', '2026-11-26')).toBeNull()
  })

  it('rechaza rangos invertidos, incompletos o mayores a un año', () => {
    expect(validarRango('2026-11-30', '2026-11-01')).toMatch(/posterior/)
    expect(validarRango('', '2026-11-01')).toMatch(/ambas/)
    expect(validarRango('2025-01-01', '2026-11-01')).toMatch(/un año/)
  })
})

describe('formato', () => {
  it('arma la consulta sin área cuando se piden todas', () => {
    expect(consultaReporte({ desde: '2026-11-01', hasta: '2026-11-30', areaId: '' })).toBe(
      'desde=2026-11-01&hasta=2026-11-30',
    )
    expect(consultaReporte({ desde: '2026-11-01', hasta: '2026-11-30', areaId: '3' })).toBe(
      'desde=2026-11-01&hasta=2026-11-30&areaId=3',
    )
  })

  it('nombra el CSV igual que el backend', () => {
    expect(nombreCsv({ desde: '2026-11-01', hasta: '2026-11-30', areaId: '' })).toBe(
      'reporte-solicitudes_2026-11-01_2026-11-30.csv',
    )
  })

  it('muestra porcentajes y días con un decimal o una raya si no hay datos', () => {
    expect(porcentaje(93.3)).toBe('93.3 %')
    expect(porcentaje(null)).toBe('—')
    expect(dias(2.4)).toBe('2.4 días')
    expect(dias(1)).toBe('1 día')
    expect(dias(null)).toBe('—')
  })

  it('isoLocal usa la fecha local, no UTC', () => {
    expect(isoLocal(new Date(2026, 0, 5, 23, 59))).toBe('2026-01-05')
  })
})
