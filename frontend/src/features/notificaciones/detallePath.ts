import type { Rol } from '../../lib/domain'

/** Ruta de detalle de una solicitud según el rol autenticado. */
export function detalleSolicitudPath(rol: Rol, codigo: string): string {
  switch (rol) {
    case 'TECNICO':
      return `/tecnico/asignadas/${codigo}`
    case 'SUPERVISOR':
      return `/supervisor/solicitudes/${codigo}`
    case 'ADMINISTRADOR':
      return `/admin/solicitudes/${codigo}`
    default:
      return `/solicitudes/${codigo}`
  }
}
