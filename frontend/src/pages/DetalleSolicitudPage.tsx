import type { ReactNode } from 'react'
import { Link, useParams } from 'react-router'
import { EmptyState } from '../components/ui/EmptyState'
import { Icon } from '../components/ui/Icon'
import { LoadError } from '../components/ui/LoadError'
import { PlazoTag } from '../components/ui/PlazoTag'
import { PriorityTag } from '../components/ui/PriorityTag'
import { StatusChip } from '../components/ui/StatusChip'
import { EvidenciasLista } from '../features/solicitudes/components/EvidenciasLista'
import { Seguimiento } from '../features/solicitudes/components/Seguimiento'
import { fetchSolicitud } from '../features/solicitudes/solicitudesApi'
import { formatFechaHoraCorta } from '../lib/format'
import { useApi } from '../lib/useApi'

const card = 'rounded-card border border-line bg-surface'

function Dato({ etiqueta, children }: { etiqueta: string; children: ReactNode }) {
  return (
    <div className="min-w-0">
      <dt className="mb-1 text-xs font-semibold text-muted">{etiqueta}</dt>
      <dd className="break-words">{children}</dd>
    </div>
  )
}

function VolverLink() {
  return (
    <Link
      to="/solicitudes"
      className="mb-4 inline-flex items-center gap-1 text-[13px] font-semibold text-muted hover:text-ink max-sm:min-h-11"
    >
      <Icon name="chev" className="size-4" />
      Volver a mis solicitudes
    </Link>
  )
}

export function DetalleSolicitudPage() {
  const { codigo = '' } = useParams()
  const detalle = useApi(() => fetchSolicitud(codigo), `solicitud-${codigo}`)

  if (detalle.status === 'error') {
    return (
      <>
        <VolverLink />
        <div className={card}>
          {detalle.error.status === 404 ? (
            <EmptyState icon="search" title="No encontramos esta solicitud" description={detalle.error.message} />
          ) : (
            <LoadError message={detalle.error.message} onRetry={detalle.reload} />
          )}
        </div>
      </>
    )
  }

  if (detalle.status === 'loading') {
    return (
      <div role="status" aria-label="Cargando solicitud">
        <VolverLink />
        <span className="mb-3 block h-5 w-48 animate-pulse rounded bg-line-soft" />
        <span className="mb-6 block h-9 w-2/3 animate-pulse rounded bg-line-soft" />
        <span className="block h-40 animate-pulse rounded-card bg-line-soft" />
      </div>
    )
  }

  const s = detalle.data
  const ubicacion = [s.pabellon, s.nivel, s.ambiente].filter(Boolean).join(', ')

  return (
    <>
      <VolverLink />
      <div className="mb-6 border-b border-line pb-5">
        <div className="mb-2 flex flex-wrap items-center gap-2.5">
          <span className="font-mono text-[13px] font-semibold tracking-[.02em] text-primary-hover">{s.codigo}</span>
          <StatusChip estado={s.estado} />
        </div>
        <h1 className="text-[34px] leading-tight font-bold max-sm:text-[28px]">{s.asunto}</h1>
      </div>

      <div className="grid items-start gap-5 lg:grid-cols-[minmax(0,1fr)_340px]">
        <div className="grid min-w-0 gap-5">
          <dl className={`${card} grid grid-cols-3 gap-x-5 gap-y-[18px] p-5 text-[13px] max-md:grid-cols-2 max-[420px]:grid-cols-1`}>
            <Dato etiqueta="Estado">
              <StatusChip estado={s.estado} />
            </Dato>
            <Dato etiqueta="Prioridad">
              <PriorityTag prioridad={s.prioridad} />
            </Dato>
            <Dato etiqueta="Categoría">{s.categoria}</Dato>
            <Dato etiqueta="Ubicación">{ubicacion || <span className="text-muted">No indicada</span>}</Dato>
            <Dato etiqueta="Fecha de registro">{formatFechaHoraCorta(s.fechaCreacion)}</Dato>
            <Dato etiqueta="Fecha límite">
              <span className="block">{formatFechaHoraCorta(s.fechaVencimiento)}</span>
              <PlazoTag fechaVencimiento={s.fechaVencimiento} estado={s.estado} />
            </Dato>
            <Dato etiqueta="Área">{s.area}</Dato>
            <Dato etiqueta="Responsable">{s.tecnico ?? <span className="text-muted">Sin asignar</span>}</Dato>
          </dl>

          <section className={`${card} p-5`} aria-labelledby="descripcion">
            <h2 id="descripcion" className="mb-3 font-sans text-[15px] font-semibold tracking-normal">
              Descripción
            </h2>
            <p className="text-[13px] whitespace-pre-line">{s.descripcion}</p>
          </section>

          <section className={`${card} p-5`} aria-labelledby="evidencias">
            <h2 id="evidencias" className="mb-3 font-sans text-[15px] font-semibold tracking-normal">
              Evidencias
            </h2>
            <EvidenciasLista codigo={s.codigo} evidencias={s.evidencias} />
          </section>
        </div>

        <aside className={`${card} p-5`} aria-labelledby="seguimiento">
          <h2 id="seguimiento" className="mb-[18px] font-sans text-[15px] font-semibold tracking-normal">
            Seguimiento
          </h2>
          <Seguimiento historial={s.historial} estado={s.estado} />
        </aside>
      </div>
    </>
  )
}
