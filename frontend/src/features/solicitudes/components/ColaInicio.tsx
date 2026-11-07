import { Link } from 'react-router'
import { EmptyState } from '../../../components/ui/EmptyState'
import { Icon } from '../../../components/ui/Icon'
import { LoadError } from '../../../components/ui/LoadError'
import { PlazoTag } from '../../../components/ui/PlazoTag'
import { useAuth } from '../../auth/useAuth'
import { haceTiempo } from '../../../lib/format'
import { useApi } from '../../../lib/useApi'
import { EstadoDot } from './EstadoDot'
import type { SolicitudResumen } from '../types'

const MAX = 5

type Conteo = { valor: number; uno: string; varios: string; tono?: 'bad' }

export function ColaInicio({
  subtitulo,
  cacheKey,
  fetcher,
  detalleTo,
  verTodasTo,
  tituloLista,
  esVisible,
  vacio,
  resumen,
}: {
  subtitulo: string
  cacheKey: string
  fetcher: () => Promise<SolicitudResumen[]>
  detalleTo: (codigo: string) => string
  verTodasTo: string
  tituloLista: string
  esVisible: (solicitud: SolicitudResumen) => boolean
  vacio: { titulo: string; descripcion: string }
  resumen: (lista: SolicitudResumen[]) => Conteo[]
}) {
  const { usuario } = useAuth()
  const solicitudes = useApi(fetcher, cacheKey)
  const nombre = usuario?.nombres.split(' ')[0] ?? ''
  const lista = solicitudes.data ?? []
  const visibles = lista.filter(esVisible).sort((a, b) => a.fechaVencimiento.localeCompare(b.fechaVencimiento))
  const conteos = solicitudes.status === 'success' ? resumen(lista) : []

  return (
    <div className="mx-auto max-w-[1120px]">
      <div className="mb-[34px]">
        <h1 className="text-[42px] leading-tight font-bold max-[700px]:text-[34px]">Hola, {nombre}.</h1>
        <p className="mt-[7px] text-[15px] text-muted">{subtitulo}</p>
      </div>

      <div className="mb-3 flex items-end justify-between gap-4">
        <h2 className="text-lg font-semibold">{tituloLista}</h2>
        <Link to={verTodasTo} className="text-[13px] font-semibold text-primary hover:underline">
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
      ) : visibles.length === 0 ? (
        <div className="border-t border-line">
          <EmptyState icon="inbox" title={vacio.titulo} description={vacio.descripcion} />
        </div>
      ) : (
        <ul className="border-t border-line">
          {visibles.slice(0, MAX).map((s) => (
            <li key={s.id}>
              <Link
                to={detalleTo(s.codigo)}
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

      {conteos.length > 0 && (
        <p className="mt-5 flex flex-wrap gap-[26px] text-xs text-muted">
          {conteos.map((c) => (
            <span key={c.varios} className={c.tono === 'bad' && c.valor > 0 ? 'text-bad' : undefined}>
              <b className={`mr-1 text-sm ${c.tono === 'bad' && c.valor > 0 ? '' : 'text-ink'}`}>{c.valor}</b>
              {c.valor === 1 ? c.uno : c.varios}
            </span>
          ))}
        </p>
      )}
    </div>
  )
}
