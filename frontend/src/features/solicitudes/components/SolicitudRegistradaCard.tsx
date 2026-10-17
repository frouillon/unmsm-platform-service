import { useEffect, useRef } from 'react'
import { Link } from 'react-router'
import { Icon } from '../../../components/ui/Icon'
import { formatFechaHora } from '../../../lib/format'
import type { SolicitudRegistrada } from '../types'

type SolicitudRegistradaCardProps = {
  solicitud: SolicitudRegistrada
  onOtra: () => void
}

export function SolicitudRegistradaCard({ solicitud, onOtra }: SolicitudRegistradaCardProps) {
  const headingRef = useRef<HTMLHeadingElement>(null)
  useEffect(() => headingRef.current?.focus(), [])

  return (
    <section className="mx-auto my-10 max-w-[480px] rounded-card border border-line bg-surface px-7 py-9 text-center max-sm:px-5">
      <div className="mx-auto mb-4 grid size-14 place-items-center rounded-full bg-ok-soft text-ok">
        <Icon name="tick" className="size-7" />
      </div>
      <h2 ref={headingRef} tabIndex={-1} className="text-2xl font-bold outline-none">
        Solicitud registrada
      </h2>
      <p className="mt-2 text-muted">
        Tu solicitud <b className="font-mono text-ink">{solicitud.codigo}</b> quedó en estado{' '}
        <b className="text-ink">{solicitud.estado}</b> y la atenderá el área de{' '}
        <b className="text-ink">{solicitud.area}</b>.
      </p>
      <dl className="mt-5 grid gap-1 rounded-lg bg-canvas px-4 py-3 text-[13px]">
        <div className="flex justify-between gap-3">
          <dt className="text-muted">Prioridad</dt>
          <dd className="font-semibold">{solicitud.prioridad}</dd>
        </div>
        <div className="flex justify-between gap-3">
          <dt className="text-muted">Fecha límite de atención</dt>
          <dd className="font-semibold">{formatFechaHora(solicitud.fechaVencimiento)}</dd>
        </div>
        {solicitud.evidencias > 0 && (
          <div className="flex justify-between gap-3">
            <dt className="text-muted">Evidencias</dt>
            <dd className="font-semibold">{solicitud.evidencias}</dd>
          </div>
        )}
      </dl>
      <div className="mt-6 flex flex-wrap justify-center gap-2.5 max-sm:flex-col">
        <Link
          to={`/solicitudes/${solicitud.codigo}`}
          className="inline-flex h-10 items-center justify-center rounded border border-primary bg-primary px-4 text-sm font-semibold text-white transition-colors hover:border-primary-hover hover:bg-primary-hover max-sm:min-h-11"
        >
          Ver solicitud
        </Link>
        <button
          type="button"
          onClick={onOtra}
          className="inline-flex h-10 items-center justify-center rounded border border-line bg-surface px-4 text-sm font-semibold transition-colors hover:bg-line-soft max-sm:min-h-11"
        >
          Registrar otra
        </button>
      </div>
    </section>
  )
}
