import { Link } from 'react-router'
import { EmptyState } from '../components/ui/EmptyState'
import { Icon } from '../components/ui/Icon'
import { LoadError } from '../components/ui/LoadError'
import { PlazoTag } from '../components/ui/PlazoTag'
import { useAuth } from '../features/auth/useAuth'
import { EstadoDot } from '../features/solicitudes/components/EstadoDot'
import { fetchMisSolicitudes } from '../features/solicitudes/solicitudesApi'
import { haceTiempo } from '../lib/format'
import { calcularPlazo, estaAtendida } from '../lib/plazo'
import { useApi } from '../lib/useApi'

const ACCESOS = [
  { titulo: 'Reportar una incidencia', categoria: 'Mantenimiento' },
  { titulo: 'Solicitar soporte tecnológico', categoria: 'Soporte informático' },
  { titulo: 'Reservar un ambiente', categoria: 'Reserva de ambientes' },
  { titulo: 'Realizar un trámite', categoria: 'Administrativa' },
]

const MAX_ACTIVAS = 5

export function InicioEstudiantePage() {
  const { usuario } = useAuth()
  const solicitudes = useApi(fetchMisSolicitudes, 'mis-solicitudes')
  const nombre = usuario?.nombres.split(' ')[0] ?? ''

  const activas = (solicitudes.data ?? [])
    .filter((s) => !estaAtendida(s.estado))
    .sort((a, b) => a.fechaVencimiento.localeCompare(b.fechaVencimiento))
  const atendidas = (solicitudes.data ?? []).length - activas.length
  const vencidas = activas.filter((s) => calcularPlazo(s.fechaVencimiento, s.estado).tono === 'bad').length

  return (
    <div className="mx-auto max-w-[1120px]">
      <div className="mb-[34px] flex items-end justify-between gap-5 max-[700px]:flex-col max-[700px]:items-start">
        <div>
          <h1 className="text-[42px] leading-tight font-bold max-[700px]:text-[34px]">Hola, {nombre}.</h1>
          <p className="mt-[7px] text-[15px] text-muted">¿Qué necesitas hacer hoy?</p>
        </div>
        <Link
          to="/solicitudes/nueva"
          className="inline-flex h-10 flex-none items-center gap-2 rounded border border-primary bg-primary px-4 text-sm font-semibold text-white transition-colors hover:border-primary-hover hover:bg-primary-hover max-sm:min-h-11"
        >
          <Icon name="plus" className="size-4" />
          Nueva solicitud
        </Link>
      </div>

      <nav aria-label="Accesos rápidos" className="mb-[42px] grid border-y border-line min-[701px]:grid-cols-2">
        {ACCESOS.map((acceso, index) => (
          <Link
            key={acceso.categoria}
            to={`/solicitudes/nueva?categoria=${encodeURIComponent(acceso.categoria)}`}
            className={`group grid grid-cols-[34px_1fr_20px] items-center gap-3.5 border-line py-[22px] max-[700px]:border-b max-[700px]:last:border-b-0 min-[701px]:px-1 ${
              index < 2 ? 'min-[701px]:border-b' : ''
            } ${index % 2 === 0 ? 'min-[701px]:border-r min-[701px]:pr-6' : 'min-[701px]:pl-6'}`}
          >
            <span className="font-mono text-[11px] font-medium tracking-[.08em] text-primary">
              {String(index + 1).padStart(2, '0')}
            </span>
            <b className="text-[15px] group-hover:text-primary">{acceso.titulo}</b>
            <Icon name="arrow" className="size-[18px] text-muted" />
          </Link>
        ))}
      </nav>

      <div className="mb-2 flex items-end justify-between">
        <h2 className="text-2xl font-bold">Solicitudes activas</h2>
        <Link to="/solicitudes" className="text-[13px] font-semibold text-primary hover:underline">
          Ver todas
        </Link>
      </div>

      {solicitudes.status === 'error' ? (
        <LoadError message={solicitudes.error.message} onRetry={solicitudes.reload} />
      ) : solicitudes.status === 'loading' ? (
        <ul aria-label="Cargando solicitudes" className="border-t border-line">
          {[0, 1, 2].map((i) => (
            <li key={i} className="flex items-center gap-5 border-b border-line py-5">
              <span className="h-3 w-28 animate-pulse rounded bg-line-soft" />
              <span className="h-4 flex-1 animate-pulse rounded bg-line-soft" />
            </li>
          ))}
        </ul>
      ) : activas.length === 0 ? (
        <div className="border-t border-line">
          <EmptyState
            icon="inbox"
            title="No tienes solicitudes activas"
            description="Cuando registres una solicitud, podrás seguir su avance desde aquí."
          />
        </div>
      ) : (
        <ul className="border-t border-line">
          {activas.slice(0, MAX_ACTIVAS).map((s) => (
            <li key={s.id}>
              <Link
                to={`/solicitudes/${s.codigo}`}
                className="group grid grid-cols-[150px_1fr_170px_18px] items-center gap-[18px] border-b border-line py-[19px] max-[900px]:grid-cols-[120px_1fr_150px_18px] max-[700px]:grid-cols-[1fr_20px] max-[700px]:gap-x-3 max-[700px]:gap-y-1.5"
              >
                <span className="font-mono text-xs font-medium text-muted max-[700px]:col-start-1">{s.codigo}</span>
                <span className="min-w-0 max-[700px]:col-start-1">
                  <span className="block truncate font-semibold group-hover:text-primary">{s.asunto}</span>
                  <span className="mt-1 block text-xs text-muted">
                    {s.categoria} · actualizado {haceTiempo(s.ultimaActualizacion)}
                  </span>
                </span>
                <span className="grid gap-1 max-[700px]:col-start-1">
                  <EstadoDot estado={s.estado} />
                  <PlazoTag fechaVencimiento={s.fechaVencimiento} estado={s.estado} />
                </span>
                <Icon
                  name="arrow"
                  className="size-[18px] text-muted max-[700px]:col-start-2 max-[700px]:row-span-3 max-[700px]:row-start-1"
                />
              </Link>
            </li>
          ))}
        </ul>
      )}

      {solicitudes.status === 'success' && solicitudes.data.length > 0 && (
        <p className="mt-5 flex flex-wrap gap-[26px] text-xs text-muted">
          <span>
            <b className="mr-1 text-sm text-ink">{activas.length}</b>
            {activas.length === 1 ? 'activa' : 'activas'}
          </span>
          {vencidas > 0 && (
            <span className="text-bad">
              <b className="mr-1 text-sm">{vencidas}</b>
              {vencidas === 1 ? 'vencida' : 'vencidas'}
            </span>
          )}
          <span>
            <b className="mr-1 text-sm text-ink">{atendidas}</b>
            {atendidas === 1 ? 'atendida' : 'atendidas'}
          </span>
        </p>
      )}
    </div>
  )
}
