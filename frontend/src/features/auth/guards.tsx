import { Navigate, Outlet, useLocation } from 'react-router'
import type { Rol } from '../../lib/domain'
import { ForbiddenPage } from '../../pages/ForbiddenPage'
import { homePath } from './homePath'
import { useAuth } from './useAuth'

function SessionLoading() {
  return (
    <div role="status" className="grid min-h-dvh place-items-center">
      <span className="size-6 animate-spin rounded-full border-2 border-line border-t-primary" />
      <span className="sr-only">Cargando sesión…</span>
    </div>
  )
}

export function RequireAuth({ roles }: { roles?: readonly Rol[] }) {
  const { status, usuario } = useAuth()
  const location = useLocation()

  if (status === 'loading') return <SessionLoading />
  if (status === 'anonymous' || !usuario) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  if (roles && !roles.includes(usuario.rol)) return <ForbiddenPage />
  return <Outlet />
}

export function GuestOnly() {
  const { status, usuario } = useAuth()
  if (status === 'loading') return <SessionLoading />
  if (status === 'authenticated' && usuario) return <Navigate to={homePath(usuario.rol)} replace />
  return <Outlet />
}

export function HomeRedirect() {
  const { usuario } = useAuth()
  return <Navigate to={usuario ? homePath(usuario.rol) : '/login'} replace />
}
