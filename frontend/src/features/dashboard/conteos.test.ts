import { describe, expect, it } from 'vitest'
import { resumenCola, resumenEstudiante, resumenTecnico } from './conteos'
import type { Dashboard } from './types'

const base: Dashboard = {
  total: 10,
  vencidas: 2,
  porEstado: [
    { nombre: 'Pendiente', cantidad: 3 },
    { nombre: 'En revisión', cantidad: 1 },
    { nombre: 'Asignado', cantidad: 2 },
    { nombre: 'En proceso', cantidad: 1 },
    { nombre: 'Resuelto', cantidad: 2 },
    { nombre: 'Cerrado', cantidad: 1 },
  ],
  porArea: [],
  porPrioridad: [],
}

describe('resúmenes del dashboard', () => {
  it('calcula activas y atendidas del estudiante', () => {
    expect(resumenEstudiante(base)).toEqual([
      { valor: 7, uno: 'activa', varios: 'activas' },
      { valor: 2, uno: 'vencida', varios: 'vencidas', tono: 'bad' },
      { valor: 3, uno: 'atendida', varios: 'atendidas' },
    ])
  })

  it('resume la carga del técnico', () => {
    expect(resumenTecnico(base)).toEqual([
      { valor: 2, uno: 'por atender', varios: 'por atender' },
      { valor: 1, uno: 'en proceso', varios: 'en proceso' },
      { valor: 3, uno: 'atendida', varios: 'atendidas' },
    ])
  })

  it('resume la cola del supervisor o admin', () => {
    expect(resumenCola(base)).toEqual([
      { valor: 3, uno: 'por revisar', varios: 'por revisar' },
      { valor: 1, uno: 'por asignar', varios: 'por asignar' },
      { valor: 2, uno: 'vencida', varios: 'vencidas', tono: 'bad' },
    ])
  })
})
