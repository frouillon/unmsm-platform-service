import type { Rol } from '../../lib/domain'

const HOME: Record<Rol, string> = {
  ESTUDIANTE: '/inicio',
  TECNICO: '/tecnico',
  SUPERVISOR: '/supervisor',
  ADMINISTRADOR: '/admin',
}

export function homePath(rol: Rol): string {
  return HOME[rol]
}
