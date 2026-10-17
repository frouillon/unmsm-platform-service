import { createBrowserRouter, type RouteObject } from 'react-router'
import { GuestOnly, HomeRedirect, RequireAuth } from '../features/auth/guards'
import { AppShell } from '../features/shell/AppShell'
import type { Rol } from '../lib/domain'
import { LoginPage } from '../pages/LoginPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { NuevaSolicitudPage } from '../pages/NuevaSolicitudPage'
import { SectionPlaceholder as Section } from '../pages/SectionPlaceholder'

const forRole = (rol: Rol, children: RouteObject[]): RouteObject => ({
  element: <RequireAuth roles={[rol]} />,
  children,
})

export const router = createBrowserRouter([
  {
    element: <GuestOnly />,
    children: [{ path: '/login', element: <LoginPage /> }],
  },
  {
    element: <RequireAuth />,
    children: [
      { path: '/', element: <HomeRedirect /> },
      {
        element: <AppShell />,
        children: [
          forRole('ESTUDIANTE', [
            { path: '/inicio', element: <Section subtitle="¿Qué necesitas hacer hoy?" icon="home" /> },
            {
              path: '/solicitudes',
              element: (
                <Section title="Mis solicitudes" subtitle="Consulta el estado y el historial de lo que has registrado." icon="list" />
              ),
            },
            { path: '/solicitudes/nueva', element: <NuevaSolicitudPage /> },
          ]),
          forRole('TECNICO', [
            { path: '/tecnico', element: <Section subtitle="Estas son las solicitudes que tienes a cargo." icon="home" /> },
            {
              path: '/tecnico/asignadas',
              element: (
                <Section title="Solicitudes asignadas" subtitle="Todo lo que tienes a cargo, ordenado por prioridad." icon="list" />
              ),
            },
            {
              path: '/tecnico/historial',
              element: <Section title="Historial" subtitle="Solicitudes que ya atendiste." icon="clock" />,
            },
          ]),
          forRole('SUPERVISOR', [
            { path: '/supervisor', element: <Section subtitle="Vista general de las solicitudes y los plazos." icon="home" /> },
            {
              path: '/supervisor/solicitudes',
              element: <Section title="Solicitudes" subtitle="Clasifica, asigna y da seguimiento a las solicitudes de tu área." icon="list" />,
            },
            {
              path: '/supervisor/tecnicos',
              element: <Section title="Técnicos" subtitle="Carga de trabajo del equipo de atención." icon="users" />,
            },
            {
              path: '/supervisor/reportes',
              element: <Section title="Reportes" subtitle="Indicadores de desempeño del servicio." icon="chart" />,
            },
          ]),
          forRole('ADMINISTRADOR', [
            { path: '/admin', element: <Section subtitle="Resumen de usuarios y configuración del sistema." icon="home" /> },
            {
              path: '/admin/solicitudes',
              element: <Section title="Solicitudes" subtitle="Todas las solicitudes registradas en el sistema." icon="list" />,
            },
            {
              path: '/admin/usuarios',
              element: <Section title="Usuarios" subtitle="Gestiona cuentas, roles y acceso al sistema." icon="users" />,
            },
            {
              path: '/admin/roles',
              element: <Section title="Roles" subtitle="Qué puede hacer cada perfil dentro del portal de servicios." icon="shield" />,
            },
            {
              path: '/admin/categorias',
              element: <Section title="Categorías" subtitle="Tipos de solicitud que pueden registrar los estudiantes." icon="tag" />,
            },
            {
              path: '/admin/prioridades',
              element: <Section title="Prioridades" subtitle="Niveles de urgencia y su plazo máximo de atención." icon="flag" />,
            },
            {
              path: '/admin/estados',
              element: <Section title="Estados" subtitle="Etapas por las que avanza una solicitud." icon="layers" />,
            },
            {
              path: '/admin/areas',
              element: <Section title="Áreas responsables" subtitle="Equipos que atienden cada categoría." icon="building" />,
            },
            {
              path: '/admin/configuracion',
              element: <Section title="Configuración" subtitle="Administra los catálogos y reglas que usa todo el sistema." icon="sliders" />,
            },
          ]),
          {
            path: '/notificaciones',
            element: <Section title="Notificaciones" subtitle="Avisos sobre el avance de tus solicitudes." icon="bell" />,
          },
          {
            path: '/perfil',
            element: <Section title="Perfil" subtitle="Tu cuenta institucional en San Marcos Servicios." icon="user" />,
          },
        ],
      },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
])
