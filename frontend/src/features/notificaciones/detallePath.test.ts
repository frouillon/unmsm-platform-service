import { describe, expect, it } from 'vitest'
import { detalleSolicitudPath } from './detallePath'

describe('detalleSolicitudPath', () => {
  it('elige la ruta de detalle según el rol', () => {
    expect(detalleSolicitudPath('ESTUDIANTE', 'SOL-1')).toBe('/solicitudes/SOL-1')
    expect(detalleSolicitudPath('TECNICO', 'SOL-1')).toBe('/tecnico/asignadas/SOL-1')
    expect(detalleSolicitudPath('SUPERVISOR', 'SOL-1')).toBe('/supervisor/solicitudes/SOL-1')
    expect(detalleSolicitudPath('ADMINISTRADOR', 'SOL-1')).toBe('/admin/solicitudes/SOL-1')
  })
})
