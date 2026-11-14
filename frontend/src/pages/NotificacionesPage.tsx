import { useState } from 'react'
import { Link } from 'react-router'
import { Button } from '../components/ui/Button'
import { EmptyState } from '../components/ui/EmptyState'
import { Icon } from '../components/ui/Icon'
import { LoadError } from '../components/ui/LoadError'
import { PageHeader } from '../components/ui/PageHeader'
import { useAuth } from '../features/auth/useAuth'
import { detalleSolicitudPath } from '../features/notificaciones/detallePath'
import {
  fetchNotificaciones,
  marcarLeida,
  marcarTodasLeidas,
} from '../features/notificaciones/notificacionesApi'
import type { Notificacion } from '../features/notificaciones/types'
import { ApiError } from '../lib/api'
import { haceTiempo } from '../lib/format'
import { useApi } from '../lib/useApi'

const TIPO_ICON: Record<string, 'refresh' | 'user' | 'mail'> = {
  CAMBIO_ESTADO: 'refresh',
  ASIGNACION: 'user',
  COMENTARIO: 'mail',
}

export function NotificacionesPage() {
  const { usuario } = useAuth()
  const { status, data, error, reload } = useApi(fetchNotificaciones, 'notificaciones')
  const [accionError, setAccionError] = useState('')
  const [marcando, setMarcando] = useState(false)

  const noLeidas = data?.filter((n) => !n.leida).length ?? 0

  const leerUna = async (n: Notificacion) => {
    if (n.leida) return
    try {
      await marcarLeida(n.id)
      reload()
    } catch (err) {
      setAccionError(err instanceof ApiError ? err.message : 'No se pudo marcar como leída.')
    }
  }

  const leerTodas = async () => {
    setAccionError('')
    setMarcando(true)
    try {
      await marcarTodasLeidas()
      reload()
    } catch (err) {
      setAccionError(err instanceof ApiError ? err.message : 'No se pudieron marcar como leídas.')
    } finally {
      setMarcando(false)
    }
  }

  return (
    <>
      <PageHeader
        title="Notificaciones"
        subtitle="Avisos sobre el avance de tus solicitudes."
        actions={
          noLeidas > 0 ? (
            <Button size="sm" onClick={leerTodas} disabled={marcando}>
              Marcar todas como leídas
            </Button>
          ) : undefined
        }
      />

      {accionError && (
        <p role="alert" className="mb-4 text-[13px] text-bad">
          {accionError}
        </p>
      )}

      <div className="rounded-card border border-line bg-surface">
        {status === 'loading' && (
          <p className="px-6 py-12 text-center text-muted">Cargando notificaciones…</p>
        )}
        {status === 'error' && <LoadError message={error.message} onRetry={reload} />}
        {status === 'success' && data.length === 0 && (
          <EmptyState
            icon="bell"
            title="No tienes notificaciones"
            description="Cuando avance una solicitud o alguien comente, verás el aviso aquí."
          />
        )}
        {status === 'success' && data.length > 0 && usuario && (
          <ul className="divide-y divide-line-soft">
            {data.map((n) => {
              const to = detalleSolicitudPath(usuario.rol, n.codigoSolicitud)
              const icon = TIPO_ICON[n.tipo] ?? 'bell'
              return (
                <li key={n.id}>
                  <Link
                    to={to}
                    onClick={() => void leerUna(n)}
                    className={`flex items-start gap-3 px-4 py-3.5 transition-colors hover:bg-line-soft/60 sm:px-5 ${
                      n.leida ? 'opacity-70' : ''
                    }`}
                  >
                    <span
                      className={`mt-0.5 grid size-9 flex-none place-items-center rounded-lg ${
                        n.leida ? 'bg-subtle-soft text-subtle' : 'bg-primary-soft text-primary'
                      }`}
                    >
                      <Icon name={icon} className="size-4" />
                    </span>
                    <span className="min-w-0 flex-1">
                      <span className={`block text-[13px] ${n.leida ? 'font-normal' : 'font-semibold'}`}>
                        {n.mensaje}
                      </span>
                      <span className="mt-0.5 flex flex-wrap gap-x-2 text-[11px] text-muted">
                        <span>{n.codigoSolicitud}</span>
                        <span aria-hidden>·</span>
                        <span>{haceTiempo(n.fecha)}</span>
                      </span>
                    </span>
                    {!n.leida && (
                      <span className="mt-2 size-2 flex-none rounded-full bg-primary" aria-label="No leída" />
                    )}
                  </Link>
                </li>
              )
            })}
          </ul>
        )}
      </div>
    </>
  )
}
