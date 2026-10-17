export const ESTADOS = ['Pendiente', 'En revisión', 'Asignado', 'En proceso', 'Resuelto', 'Cerrado'] as const
export type Estado = (typeof ESTADOS)[number]

export const PRIORIDADES = ['Baja', 'Media', 'Alta', 'Urgente'] as const
export type Prioridad = (typeof PRIORIDADES)[number]

export const ROLES = ['ESTUDIANTE', 'TECNICO', 'SUPERVISOR', 'ADMINISTRADOR'] as const
export type Rol = (typeof ROLES)[number]

export const ROL_LABEL: Record<Rol, string> = {
  ESTUDIANTE: 'Estudiante',
  TECNICO: 'Técnico',
  SUPERVISOR: 'Supervisor',
  ADMINISTRADOR: 'Administrador',
}

export function iniciales(nombres: string): string {
  return nombres
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('')
}
