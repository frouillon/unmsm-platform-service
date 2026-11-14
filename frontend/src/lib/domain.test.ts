import { describe, expect, it } from 'vitest'
import { iniciales } from './domain'

/*
 * Iniciales que se muestran en el avatar del usuario.
 * Técnica: partición de equivalencia (un nombre, dos, más de dos, espacios de sobra).
 */
describe('iniciales — partición de equivalencia', () => {
  it('toma la primera letra de los dos primeros nombres, en mayúscula', () => {
    expect(iniciales('Andrea Mendoza')).toBe('AM')
    expect(iniciales('Lucía')).toBe('L')
    expect(iniciales('carlos alberto quispe')).toBe('CA')
    expect(iniciales('  Diana   Cárdenas  ')).toBe('DC')
  })
})
