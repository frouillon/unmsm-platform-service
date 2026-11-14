import { describe, expect, it } from 'vitest'
import { homePath } from './homePath'

/*
 * HU-01 · RF-05 — Tras iniciar sesión, cada rol llega a su propia página de inicio.
 * Técnica: tabla de decisión (rol → ruta).
 */
describe('homePath — tabla de decisión', () => {
  it('lleva a cada rol a su página de inicio', () => {
    expect(homePath('ESTUDIANTE')).toBe('/inicio')
    expect(homePath('TECNICO')).toBe('/tecnico')
    expect(homePath('SUPERVISOR')).toBe('/supervisor')
    expect(homePath('ADMINISTRADOR')).toBe('/admin')
  })
})
