import { createBrowserRouter, type RouteObject } from 'react-router'
import { GuestOnly, HomeRedirect, RequireAuth } from '../features/auth/guards'
import { AppShell } from '../features/shell/AppShell'
import type { Rol } from '../lib/domain'
import { AsignadasPage } from '../pages/AsignadasPage'
import { DetalleSolicitudPage } from '../pages/DetalleSolicitudPage'
import { HistorialTecnicoPage } from '../pages/HistorialTecnicoPage'
import { InicioAdminPage } from '../pages/InicioAdminPage'
import { InicioEstudiantePage } from '../pages/InicioEstudiantePage'
import { InicioSupervisorPage } from '../pages/InicioSupervisorPage'
import { InicioTecnicoPage } from '../pages/InicioTecnicoPage'
import { LoginPage } from '../pages/LoginPage'
import { MisSolicitudesPage } from '../pages/MisSolicitudesPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { NotificacionesPage } from '../pages/NotificacionesPage'
import { NuevaSolicitudPage } from '../pages/NuevaSolicitudPage'
import { ReportesPage } from '../pages/ReportesPage'
import { SectionPlaceholder as Section } from '../pages/SectionPlaceholder'
import { SolicitudesAdminPage } from '../pages/SolicitudesAdminPage'
import { SolicitudesAreaPage } from '../pages/SolicitudesAreaPage'

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
            { path: '/tecnico', element: <InicioTecnicoPage /> },
            { path: '/tecnico/asignadas', element: <AsignadasPage /> },
            { path: '/tecnico/asignadas/:codigo', element: <DetalleSolicitudPage /> },
            { path: '/tecnico/historial', element: <HistorialTecnicoPage /> },
            { path: '/tecnico/historial/:codigo', element: <DetalleSolicitudPage /> },
          ]),
          forRole('SUPERVISOR', [
            { path: '/supervisor', element: <InicioSupervisorPage /> },
            { path: '/supervisor/solicitudes', element: <SolicitudesAreaPage /> },
            { path: '/supervisor/solicitudes/:codigo', element: <DetalleSolicitudPage /> },
            {
              path: '/supervisor/tecnicos',
              element: <Section title="Técnicos" subtitle="Carga de trabajo del equipo de atención." icon="users" />,
            },
            { path: '/supervisor/reportes', element: <ReportesPage /> },
          ]),
          forRole('ADMINISTRADOR', [
            { path: '/admin', element: <InicioAdminPage /> },
            { path: '/admin/solicitudes', element: <SolicitudesAdminPage /> },
            { path: '/admin/solicitudes/:codigo', element: <DetalleSolicitudPage /> },
            { path: '/admin/reportes', element: <ReportesPage /> },
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
          { path: '/notificaciones', element: <NotificacionesPage /> },
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
