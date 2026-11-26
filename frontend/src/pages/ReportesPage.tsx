import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router'
import { Button } from '../components/ui/Button'
import { EmptyState } from '../components/ui/EmptyState'
import { Icon } from '../components/ui/Icon'
import { LoadError } from '../components/ui/LoadError'
import { PageHeader } from '../components/ui/PageHeader'
import { PriorityTag } from '../components/ui/PriorityTag'
import { StatusChip } from '../components/ui/StatusChip'
import { useAuth } from '../features/auth/useAuth'
import type { ConteoItem } from '../features/dashboard/types'
import { detalleSolicitudPath } from '../features/notificaciones/detallePath'
import {
  RANGOS,
  TONO_CUMPLIMIENTO,
  dias,
  nombreCsv,
  porcentaje,
  rangoRapido,
  validarRango,
  type Rango,
} from '../features/reportes/filtros'
import { fetchAreasReporte, fetchReporte, fetchReporteCsv } from '../features/reportes/reportesApi'
import type { FiltrosReporte, ReporteSolicitudes } from '../features/reportes/types'
import { ApiError } from '../lib/api'
import { formatFecha, formatFechaHoraCorta } from '../lib/format'
import { useApi } from '../lib/useApi'

const controlClass =
  'h-10 rounded border border-line bg-field px-2.5 text-[13px] text-ink focus:border-primary focus:outline-2 ' +
  'focus:outline-offset-1 focus:outline-primary max-sm:min-h-11 max-sm:text-base'

const thClass = 'border-b border-line px-4 py-2.5 font-semibold whitespace-nowrap'

function useFiltros(): [FiltrosReporte, (f: FiltrosReporte) => void] {
  const [params, setParams] = useSearchParams()
  const porDefecto = rangoRapido('30d')
  const filtros = {
    desde: params.get('desde') ?? porDefecto.desde,
    hasta: params.get('hasta') ?? porDefecto.hasta,
    areaId: params.get('area') ?? '',
  }
  const aplicar = (f: FiltrosReporte) => {
    const next = new URLSearchParams({ desde: f.desde, hasta: f.hasta })
    if (f.areaId) next.set('area', f.areaId)
    setParams(next, { replace: true })
  }
  return [filtros, aplicar]
}

/** HU-23 y HU-24: indicadores del periodo, matriz de carga técnica y exportación (CSV o vista PDF). */
export function ReportesPage() {
  const { usuario } = useAuth()
  const esAdmin = usuario?.rol === 'ADMINISTRADOR'
  const [filtros, aplicar] = useFiltros()
  const [borrador, setBorrador] = useState(filtros)
  const [errorFiltro, setErrorFiltro] = useState('')
  const [descargando, setDescargando] = useState(false)
  const [errorCsv, setErrorCsv] = useState('')

  const clave = `reporte:${filtros.desde}:${filtros.hasta}:${filtros.areaId}`
  const reporte = useApi(() => fetchReporte(filtros), clave)
  const areas = useApi(fetchAreasReporte, 'reporte-areas')

  // Si la URL cambia (rango rápido, atrás/adelante), el formulario muestra los filtros vigentes.
  const [ultimaClave, setUltimaClave] = useState(clave)
  if (ultimaClave !== clave) {
    setUltimaClave(clave)
    setBorrador(filtros)
  }

  const generar = (e: FormEvent) => {
    e.preventDefault()
    const error = validarRango(borrador.desde, borrador.hasta)
    setErrorFiltro(error ?? '')
    if (!error) aplicar(borrador)
  }

  const usarRango = (rango: Rango) => {
    setErrorFiltro('')
    aplicar({ ...borrador, ...rangoRapido(rango) })
  }

  const descargarCsv = async () => {
    setErrorCsv('')
    setDescargando(true)
    try {
      const blob = await fetchReporteCsv(filtros)
      const url = URL.createObjectURL(blob)
      const enlace = document.createElement('a')
      enlace.href = url
      enlace.download = nombreCsv(filtros)
      enlace.click()
      URL.revokeObjectURL(url)
    } catch (err) {
      setErrorCsv(err instanceof ApiError ? err.message : 'No se pudo descargar el CSV.')
    } finally {
      setDescargando(false)
    }
  }

  const listo = reporte.status === 'success'

  return (
    <>
      <div className="print:hidden">
        <PageHeader
          title="Reportes"
          subtitle={
            esAdmin
              ? 'Solicitudes registradas en el periodo, cumplimiento de plazos y carga de cada equipo.'
              : 'Solicitudes de tu área en el periodo, cumplimiento de plazos y carga de tu equipo.'
          }
          actions={
            <>
              <Button onClick={descargarCsv} disabled={!listo || descargando}>
                <Icon name="file" className="size-4" />
                {descargando ? 'Descargando…' : 'Descargar CSV'}
              </Button>
              <Button variant="primary" onClick={() => window.print()} disabled={!listo}>
                <Icon name="chart" className="size-4" />
                Vista PDF
              </Button>
            </>
          }
        />
        {errorCsv && (
          <p role="alert" className="-mt-3 mb-4 text-[13px] text-bad">
            {errorCsv}
          </p>
        )}

        <form
          onSubmit={generar}
          aria-label="Filtros del reporte"
          className="mb-6 rounded-card border border-line bg-toolbar p-4 sm:p-5"
        >
          <div className="flex flex-wrap items-end gap-3">
            <label className="grid gap-1.5 text-[13px] font-semibold">
              Desde
              <input
                type="date"
                value={borrador.desde}
                max={borrador.hasta || undefined}
                onChange={(e) => setBorrador({ ...borrador, desde: e.target.value })}
                className={controlClass}
              />
            </label>
            <label className="grid gap-1.5 text-[13px] font-semibold">
              Hasta
              <input
                type="date"
                value={borrador.hasta}
                min={borrador.desde || undefined}
                onChange={(e) => setBorrador({ ...borrador, hasta: e.target.value })}
                className={controlClass}
              />
            </label>
            {esAdmin ? (
              <label className="grid gap-1.5 text-[13px] font-semibold">
                Área
                <select
                  value={borrador.areaId}
                  onChange={(e) => setBorrador({ ...borrador, areaId: e.target.value })}
                  className={`${controlClass} min-w-[220px]`}
                >
                  <option value="">Todas las áreas</option>
                  {areas.data?.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.nombre}
                    </option>
                  ))}
                </select>
              </label>
            ) : (
              <p className="grid gap-1.5 text-[13px] font-semibold">
                Área
                <span className="flex h-10 items-center font-normal text-muted">
                  {areas.data?.[0]?.nombre ?? 'Tu área'}
                </span>
              </p>
            )}
            <Button type="submit" variant="primary">
              Generar reporte
            </Button>
          </div>
          <div className="mt-3 flex flex-wrap items-center gap-2 text-[13px]">
            <span className="text-muted">Rápido:</span>
            {(Object.keys(RANGOS) as Rango[]).map((r) => (
              <button
                key={r}
                type="button"
                onClick={() => usarRango(r)}
                className="rounded-full border border-line bg-surface px-3 py-1 font-medium hover:bg-line-soft"
              >
                {RANGOS[r]}
              </button>
            ))}
          </div>
          {errorFiltro && (
            <p role="alert" className="mt-3 flex items-center gap-1 text-xs font-medium text-bad">
              <Icon name="alert" className="size-3.5" />
              {errorFiltro}
            </p>
          )}
        </form>
      </div>

      {reporte.status === 'loading' && (
        <div aria-label="Generando reporte" className="grid gap-3">
          {[0, 1, 2].map((i) => (
            <span key={i} className="h-16 animate-pulse rounded bg-line-soft" />
          ))}
        </div>
      )}
      {reporte.status === 'error' && (
        <div className="rounded-card border border-line bg-surface">
          <LoadError message={reporte.error.message} onRetry={reporte.reload} />
        </div>
      )}
      {listo && usuario && (
        <ContenidoReporte
          data={reporte.data}
          detalleTo={(codigo) => detalleSolicitudPath(usuario.rol, codigo)}
        />
      )}
    </>
  )
}

function ContenidoReporte({ data, detalleTo }: { data: ReporteSolicitudes; detalleTo: (codigo: string) => string }) {
  const { resumen } = data
  const periodo = `${formatFecha(`${data.desde}T00:00:00`)} – ${formatFecha(`${data.hasta}T00:00:00`)}`

  return (
    <article aria-label="Reporte de solicitudes">
      {/* Encabezado que solo aparece al imprimir o guardar como PDF. */}
      <header className="mb-6 hidden items-center gap-4 border-b border-line pb-4 print:flex">
        <img src="/escudo-unmsm.png" alt="" className="h-14 w-auto" />
        <div>
          <p className="text-[11px] font-semibold tracking-[.08em] text-muted uppercase">
            Universidad Nacional Mayor de San Marcos · San Marcos Servicios
          </p>
          <h1 className="text-2xl font-bold">Reporte de solicitudes</h1>
          <p className="text-[13px] text-muted">
            {data.alcance} · {periodo} · Generado el {formatFechaHoraCorta(data.generadoEn)}
          </p>
        </div>
      </header>

      <p className="mb-4 text-[13px] text-muted print:hidden">
        <b className="text-ink">{data.alcance}</b> · {periodo} · generado el {formatFechaHoraCorta(data.generadoEn)}
      </p>

      {resumen.total === 0 ? (
        <div className="rounded-card border border-line bg-surface">
          <EmptyState
            icon="chart"
            title="No hay solicitudes en este periodo"
            description="Prueba con un rango de fechas más amplio."
          />
        </div>
      ) : (
        <>
          <section
            aria-label="Indicadores del periodo"
            className="mb-8 grid grid-cols-2 gap-px overflow-hidden rounded-card border border-line bg-line sm:grid-cols-3 lg:grid-cols-6 print:grid-cols-6"
          >
            <Kpi etiqueta="Registradas" valor={String(resumen.total)} />
            <Kpi etiqueta="Atendidas" valor={String(resumen.atendidas)} />
            <Kpi etiqueta="Abiertas" valor={String(resumen.abiertas)} />
            <Kpi etiqueta="Vencidas" valor={String(resumen.vencidas)} tono={resumen.vencidas > 0 ? 'text-bad' : ''} />
            <Kpi
              etiqueta="Cumplimiento SLA"
              valor={porcentaje(resumen.cumplimientoSla)}
              nota={resumen.atendidasFueraDePlazo > 0 ? `${resumen.atendidasFueraDePlazo} fuera de plazo` : undefined}
            />
            <Kpi etiqueta="Tiempo medio (MTTR)" valor={dias(resumen.mttrDias)} />
          </section>

          <section aria-label="Distribuciones" className="mb-8 grid gap-8 min-[800px]:grid-cols-3 print:grid-cols-3">
            <Distribucion titulo="Por estado" items={resumen.porEstado} />
            <Distribucion titulo="Por categoría" items={resumen.porCategoria} />
            <Distribucion titulo="Por prioridad" items={resumen.porPrioridad} />
          </section>

          {data.cargaTecnica.length > 0 && (
            <section aria-labelledby="carga-titulo" className="mb-8 break-inside-avoid">
              <h2 id="carga-titulo" className="mb-3 text-xl font-bold">
                Carga técnica
              </h2>
              <div className="overflow-x-auto rounded-card border border-line bg-surface">
                <table className="w-full border-collapse text-[13px]">
                  <thead>
                    <tr className="bg-table-head text-left text-[10px] tracking-[.05em] text-table-head-text uppercase">
                      <th scope="col" className={thClass}>
                        Técnico
                      </th>
                      {['Asignadas', 'En proceso', 'Resueltas', 'Total'].map((th) => (
                        <th key={th} scope="col" className={`${thClass} text-right`}>
                          {th}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {data.cargaTecnica.map((c) => (
                      <tr key={c.tecnico} className="border-b border-line-soft last:border-b-0">
                        <td className="px-4 py-2.5 font-medium">{c.tecnico}</td>
                        <td className="px-4 py-2.5 text-right tabular-nums">{c.asignadas}</td>
                        <td className="px-4 py-2.5 text-right tabular-nums">{c.enProceso}</td>
                        <td className="px-4 py-2.5 text-right tabular-nums">{c.resueltas}</td>
                        <td className="px-4 py-2.5 text-right font-semibold tabular-nums">
                          {c.asignadas + c.enProceso + c.resueltas}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
          )}

          <section aria-labelledby="detalle-titulo">
            <h2 id="detalle-titulo" className="mb-3 text-xl font-bold">
              Detalle de solicitudes
            </h2>
            <div className="overflow-x-auto rounded-card border border-line bg-surface print:overflow-visible">
              <table className="w-full min-w-[980px] border-collapse text-[13px] print:min-w-0 print:text-[10px]">
                <thead>
                  <tr className="bg-table-head text-left text-[10px] tracking-[.05em] text-table-head-text uppercase">
                    {['Código', 'Registro', 'Categoría', 'Prioridad', 'Estado', 'Técnico', 'Fecha límite', 'Resolución', 'Días', 'Plazo'].map(
                      (th) => (
                        <th key={th} scope="col" className={thClass}>
                          {th}
                        </th>
                      ),
                    )}
                  </tr>
                </thead>
                <tbody>
                  {data.filas.map((f) => (
                    <tr key={f.codigo} className="border-b border-line-soft last:border-b-0 break-inside-avoid">
                      <td className="px-4 py-2.5 font-mono font-semibold whitespace-nowrap">
                        <Link to={detalleTo(f.codigo)} className="text-primary-hover hover:underline">
                          {f.codigo}
                        </Link>
                      </td>
                      <td className="px-4 py-2.5 whitespace-nowrap text-muted">{formatFecha(f.fechaCreacion)}</td>
                      <td className="px-4 py-2.5">{f.categoria}</td>
                      <td className="px-4 py-2.5">
                        <PriorityTag prioridad={f.prioridad} />
                      </td>
                      <td className="px-4 py-2.5">
                        <StatusChip estado={f.estado} />
                      </td>
                      <td className={`px-4 py-2.5 ${f.tecnico ? '' : 'text-muted'}`}>{f.tecnico ?? 'Sin asignar'}</td>
                      <td className="px-4 py-2.5 whitespace-nowrap">{formatFecha(f.fechaVencimiento)}</td>
                      <td className="px-4 py-2.5 whitespace-nowrap text-muted">
                        {f.fechaResolucion ? formatFecha(f.fechaResolucion) : '—'}
                      </td>
                      <td className="px-4 py-2.5 text-right tabular-nums">{f.diasAtencion ?? '—'}</td>
                      <td className="px-4 py-2.5">
                        <span
                          className={`inline-flex h-6 items-center rounded px-2 text-xs font-semibold whitespace-nowrap ${TONO_CUMPLIMIENTO[f.cumplimiento]}`}
                        >
                          {f.cumplimiento}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <p className="mt-3 text-xs text-muted">
              «Plazo» compara la fecha de resolución con la fecha límite del SLA. «Días» es el tiempo entre el registro y
              la resolución.
            </p>
          </section>
        </>
      )}
    </article>
  )
}

function Kpi({ etiqueta, valor, tono = '', nota }: { etiqueta: string; valor: string; tono?: string; nota?: string }) {
  return (
    <div className="bg-surface px-4 py-4">
      <p className="text-[11px] font-semibold tracking-[.06em] text-muted uppercase">{etiqueta}</p>
      <p className={`mt-1 font-serif text-[26px] leading-tight font-bold tabular-nums ${tono}`}>{valor}</p>
      {nota && <p className="text-xs text-warn">{nota}</p>}
    </div>
  )
}

function Distribucion({ titulo, items }: { titulo: string; items: ConteoItem[] }) {
  const max = Math.max(...items.map((i) => i.cantidad), 1)
  return (
    <div className="break-inside-avoid">
      <h3 className="mb-2.5 text-[11px] font-semibold tracking-[.06em] text-muted uppercase">{titulo}</h3>
      <ul className="grid gap-2">
        {items.map((item) => (
          <li key={item.nombre} className="grid grid-cols-[minmax(0,1fr)_40px] items-center gap-3 text-[13px]">
            <span className="min-w-0">
              <span className="mb-1 block truncate">{item.nombre}</span>
              <span className="block h-1.5 overflow-hidden bg-line-soft print:border print:border-line">
                <span className="block h-full bg-primary" style={{ width: `${Math.round((item.cantidad / max) * 100)}%` }} />
              </span>
            </span>
            <b className="text-right tabular-nums">{item.cantidad}</b>
          </li>
        ))}
      </ul>
    </div>
  )
}
