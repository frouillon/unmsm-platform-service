import type { Dashboard } from './types'

function Fila({ titulo, items }: { titulo: string; items: { nombre: string; cantidad: number }[] }) {
  if (items.length === 0) return null
  const max = Math.max(...items.map((i) => i.cantidad), 1)
  return (
    <div>
      <h3 className="mb-2.5 text-[11px] font-semibold tracking-[.06em] text-muted uppercase">{titulo}</h3>
      <ul className="grid gap-2">
        {items.map((item) => (
          <li key={item.nombre} className="grid grid-cols-[minmax(0,1fr)_40px] items-center gap-3 text-[13px]">
            <span className="min-w-0">
              <span className="mb-1 flex justify-between gap-2">
                <span className="truncate">{item.nombre}</span>
              </span>
              <span className="block h-1.5 overflow-hidden bg-line-soft">
                <span
                  className="block h-full bg-primary"
                  style={{ width: `${Math.round((item.cantidad / max) * 100)}%` }}
                />
              </span>
            </span>
            <b className="text-right tabular-nums">{item.cantidad}</b>
          </li>
        ))}
      </ul>
    </div>
  )
}

/** Distribuciones RF-40: estado, prioridad y área (si hay más de una). */
export function IndicadoresInicio({ data }: { data: Dashboard }) {
  if (data.total === 0) return null

  return (
    <section aria-label="Indicadores" className="mb-10 border-y border-line py-6">
      <div className="mb-5 flex flex-wrap items-baseline gap-x-6 gap-y-1">
        <p className="text-[13px] text-muted">
          <b className="mr-1.5 text-lg text-ink tabular-nums">{data.total}</b>
          {data.total === 1 ? 'solicitud' : 'solicitudes'} en tu alcance
        </p>
        {data.vencidas > 0 && (
          <p className="text-[13px] text-bad">
            <b className="mr-1.5 text-lg tabular-nums">{data.vencidas}</b>
            {data.vencidas === 1 ? 'vencida' : 'vencidas'}
          </p>
        )}
      </div>
      <div
        className={`grid gap-8 ${data.porArea.length > 1 ? 'min-[900px]:grid-cols-3' : 'min-[700px]:grid-cols-2'}`}
      >
        <Fila titulo="Por estado" items={data.porEstado} />
        <Fila titulo="Por prioridad" items={data.porPrioridad} />
        {data.porArea.length > 1 && <Fila titulo="Por área" items={data.porArea} />}
      </div>
    </section>
  )
}
