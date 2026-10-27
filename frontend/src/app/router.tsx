import { createBrowserRouter, type RouteObject } from 'react-router'
import { GuestOnly, HomeRedirect, RequireAuth } from '../features/auth/guards'
import { AppShell } from '../features/shell/AppShell'
import type { Rol } from '../lib/domain'
import { DetalleSolicitudPage } from '../pages/DetalleSolicitudPage'
import { InicioEstudiantePage } from '../pages/InicioEstudiantePage'
import { LoginPage } from '../pages/LoginPage'
import { MisSolicitudesPage } from '../pages/MisSolicitudesPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { NuevaSolicitudPage } from '../pages/NuevaSolicitudPage'
import { SectionPlaceholder as Section } from '../pages/SectionPlaceholder'
import { AsignadasPage } from '../pages/tecnico/AsignadasPage'
import { GestionSolicitudesPage } from '../pages/supervisor/GestionSolicitudesPage'
import { SolicitudesAdminPage } from '../pages/admin/SolicitudesAdminPage'

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
            { path: '/inicio', element: <InicioEstudiantePage /> },
            { path: '/solicitudes', element: <MisSolicitudesPage /> },
            { path: '/solicitudes/nueva', element: <NuevaSolicitudPage /> },
            { path: '/solicitudes/:codigo', element: <DetalleSolicitudPage /> },
          ]),
          forRole('TECNICO', [
            { path: '/tecnico', element: <AsignadasPage /> },
            { path: '/tecnico/asignadas', element: <AsignadasPage /> },
            { path: '/tecnico/historial', element: <Section title="Historial" subtitle="Solicitudes que ya atendiste." icon="clock" /> },
          ]),
          forRole('SUPERVISOR', [
            { path: '/supervisor', element: <GestionSolicitudesPage /> },
            { path: '/supervisor/solicitudes', element: <GestionSolicitudesPage /> },
            { path: '/supervisor/tecnicos', element: <Section title="Técnicos" subtitle="Carga de trabajo del equipo de atención." icon="users" /> },
            { path: '/supervisor/reportes', element: <Section title="Reportes" subtitle="Indicadores de desempeño del servicio." icon="chart" /> },
          ]),
          forRole('ADMINISTRADOR', [
            { path: '/admin', element: <Section subtitle="Resumen de usuarios y configuración del sistema." icon="home" /> },
            {
              path: '/admin/solicitudes',
              element: <SolicitudesAdminPage />,
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
