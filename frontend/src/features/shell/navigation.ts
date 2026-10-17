import type { IconName } from '../../components/ui/Icon'
import type { Rol } from '../../lib/domain'

export type NavItem = {
  to: string
  icon: IconName
  label: string
  /** Etiqueta corta para la barra inferior móvil. */
  short?: string
  /** Botón destacado (+) en la barra inferior móvil. */
  fab?: boolean
}

type RoleNavigation = {
  items: NavItem[]
  /** Rutas que aparecen en la barra inferior móvil (máximo 5). */
  bottom: string[]
  /** Listado principal del rol, usado por el buscador de la cabecera. */
  listPath: string
}

export const NAVIGATION: Record<Rol, RoleNavigation> = {
  ESTUDIANTE: {
    items: [
      { to: '/inicio', icon: 'home', label: 'Inicio' },
      { to: '/solicitudes', icon: 'list', label: 'Mis solicitudes', short: 'Solicitudes' },
      { to: '/solicitudes/nueva', icon: 'plus', label: 'Nueva solicitud', short: 'Nueva', fab: true },
      { to: '/notificaciones', icon: 'bell', label: 'Notificaciones', short: 'Avisos' },
      { to: '/perfil', icon: 'user', label: 'Perfil' },
    ],
    bottom: ['/inicio', '/solicitudes', '/solicitudes/nueva', '/notificaciones', '/perfil'],
    listPath: '/solicitudes',
  },
  TECNICO: {
    items: [
      { to: '/tecnico', icon: 'home', label: 'Dashboard' },
      { to: '/tecnico/asignadas', icon: 'list', label: 'Solicitudes asignadas', short: 'Asignadas' },
      { to: '/tecnico/historial', icon: 'clock', label: 'Historial' },
      { to: '/notificaciones', icon: 'bell', label: 'Notificaciones', short: 'Avisos' },
      { to: '/perfil', icon: 'user', label: 'Perfil' },
    ],
    bottom: ['/tecnico', '/tecnico/asignadas', '/tecnico/historial', '/notificaciones', '/perfil'],
    listPath: '/tecnico/asignadas',
  },
  SUPERVISOR: {
    items: [
      { to: '/supervisor', icon: 'home', label: 'Dashboard' },
      { to: '/supervisor/solicitudes', icon: 'list', label: 'Solicitudes' },
      { to: '/supervisor/tecnicos', icon: 'users', label: 'Técnicos' },
      { to: '/supervisor/reportes', icon: 'chart', label: 'Reportes' },
      { to: '/notificaciones', icon: 'bell', label: 'Notificaciones', short: 'Avisos' },
    ],
    bottom: ['/supervisor', '/supervisor/solicitudes', '/supervisor/tecnicos', '/supervisor/reportes', '/notificaciones'],
    listPath: '/supervisor/solicitudes',
  },
  ADMINISTRADOR: {
    items: [
      { to: '/admin', icon: 'home', label: 'Dashboard' },
      { to: '/admin/solicitudes', icon: 'list', label: 'Solicitudes' },
      { to: '/admin/usuarios', icon: 'users', label: 'Usuarios' },
      { to: '/admin/roles', icon: 'shield', label: 'Roles' },
      { to: '/admin/categorias', icon: 'tag', label: 'Categorías' },
      { to: '/admin/prioridades', icon: 'flag', label: 'Prioridades' },
      { to: '/admin/estados', icon: 'layers', label: 'Estados' },
      { to: '/admin/areas', icon: 'building', label: 'Áreas' },
      { to: '/admin/configuracion', icon: 'sliders', label: 'Configuración' },
    ],
    bottom: ['/admin', '/admin/solicitudes', '/admin/usuarios', '/admin/configuracion'],
    listPath: '/admin/solicitudes',
  },
}

/** Ítem activo: el de ruta más larga que coincide con la URL actual (así "/tecnico" no queda activo en sus subrutas). */
export function activeItem(items: NavItem[], pathname: string): NavItem | undefined {
  return items
    .filter((item) => pathname === item.to || pathname.startsWith(`${item.to}/`))
    .sort((a, b) => b.to.length - a.to.length)[0]
}
