import { Button } from '../components/ui/Button'
import { Icon } from '../components/ui/Icon'
import { useAuth } from '../features/auth/useAuth'

const ROL_LABEL = {
  ESTUDIANTE: 'Estudiante',
  TECNICO: 'Técnico',
  SUPERVISOR: 'Supervisor',
  ADMINISTRADOR: 'Administrador',
} as const

/** Temporal: en la Fase 2 se reemplaza por el layout con barra lateral de cada rol. */
export function RoleHomePage() {
  const { usuario, logout } = useAuth()
  if (!usuario) return null

  return (
    <main className="grid min-h-dvh place-items-center px-6">
      <div className="flex flex-col items-center gap-2 text-center">
        <span className="text-[11px] font-semibold tracking-[.14em] text-primary uppercase">
          {ROL_LABEL[usuario.rol]}
          {usuario.areaNombre && ` · ${usuario.areaNombre}`}
        </span>
        <h1 className="text-[34px] font-bold">Hola, {usuario.nombres.split(' ')[0]}</h1>
        <p className="text-muted">{usuario.correo}</p>
        <Button className="mt-4" onClick={logout}>
          <Icon name="logout" />
          Cerrar sesión
        </Button>
      </div>
    </main>
  )
}
