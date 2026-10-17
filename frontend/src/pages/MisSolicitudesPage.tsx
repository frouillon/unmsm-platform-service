import { Link, useSearchParams } from 'react-router'
import { Button } from '../components/ui/Button'
import { EmptyState } from '../components/ui/EmptyState'
import { Icon } from '../components/ui/Icon'
import { LoadError } from '../components/ui/LoadError'
import { PageHeader } from '../components/ui/PageHeader'
import { PlazoTag } from '../components/ui/PlazoTag'
import { PriorityTag } from '../components/ui/PriorityTag'
import { StatusChip } from '../components/ui/StatusChip'
import { fetchMisSolicitudes } from '../features/solicitudes/solicitudesApi'
import type { SolicitudResumen } from '../features/solicitudes/types'
import { ESTADOS, PRIORIDADES } from '../lib/domain'
import { formatFecha } from '../lib/format'
import { estaAtendida } from '../lib/plazo'
import { useApi } from '../lib/useApi'

const ORDENES = {
  recientes: 'Más recientes primero',
  antiguas: 'Más antiguas primero',
  vencimiento: 'Próximas a vencer',
} as const
type Orden = keyof typeof ORDENES

const selectClass =
  'h-10 rounded border border-line bg-field px-2.5 text-[13px] text-ink focus:border-primary focus:outline-2 focus:outline-offset-1 focus:outline-primary max-sm:min-h-11 max-sm:text-base'

const normalizar = (texto: string) =>
  texto
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()

function ordenar(lista: SolicitudResumen[], orden: Orden): SolicitudResumen[] {
  const copia = [...lista]
  if (orden === 'antiguas') return copia.sort((a, b) => a.fechaCreacion.localeCompare(b.fechaCreacion))
  if (orden === 'vencimiento') {
    return copia.sort(
      (a, b) =>
        Number(estaAtendida(a.estado)) - Number(estaAtendida(b.estado)) ||
        a.fechaVencimiento.localeCompare(b.fechaVencimiento),
    )
  }
  return copia.sort((a, b) => b.fechaCreacion.localeCompare(a.fechaCreacion))
}

export function MisSolicitudesPage() {
  const solicitudes = useApi(fetchMisSolicitudes, 'mis-solicitudes')
  const [params, setParams] = useSearchParams()
  const q = params.get('q') ?? ''
  const estado = params.get('estado') ?? ''
  const categoria = params.get('categoria') ?? ''
  const prioridad = params.get('prioridad') ?? ''
  const orden: Orden = (params.get('orden') as Orden) in ORDENES ? (params.get('orden') as Orden) : 'recientes'

  const setFiltro = (clave: string, valor: string) => {
    setParams(
      (prev) => {
        const next = new URLSearchParams(prev)
        if (valor) next.set(clave, valor)
        else next.delete(clave)
        return next
      },
      { replace: true },
    )
  }

  const todas = solicitudes.data ?? []
  const categorias = [...new Set(todas.map((s) => s.categoria))].sort()
  const termino = normalizar(q.trim())
  const filtradas = ordenar(
    todas.filter(
      (s) =>
        (!termino || normalizar(`${s.codigo} ${s.asunto}`).includes(termino)) &&
        (!estado || s.estado === estado) &&
        (!categoria || s.categoria === categoria) &&
        (!prioridad || s.prioridad === prioridad),
    ),
    orden,
  )
  const hayFiltros = Boolean(q || estado || categoria || prioridad)

  return (
    <>
      <PageHeader
        title="Mis solicitudes"
        subtitle="Consulta el estado y el historial de lo que has registrado."
        actions={
          <Link
            to="/solicitudes/nueva"
            className="inline-flex h-10 items-center justify-center gap-2 rounded border border-primary bg-primary px-4 text-sm font-semibold text-white transition-colors hover:border-primary-hover hover:bg-primary-hover max-sm:min-h-11"
          >
            <Icon name="plus" className="size-4" />
            Nueva solicitud
          </Link>
        }
      />

      <section className="overflow-hidden rounded-card border border-line bg-surface">
        <div role="search" className="flex flex-wrap gap-2.5 border-b border-line bg-toolbar px-4 py-3.5">
          <label className="relative min-w-0 flex-[1_1_240px]">
            <span className="sr-only">Buscar por código o asunto</span>
            <Icon name="search" className="pointer-events-none absolute top-[11px] left-3 size-[18px] text-muted" />
            <input
              type="search"
              value={q}
              onChange={(e) => setFiltro('q', e.target.value)}
              placeholder="Buscar por código o asunto"
              className={`${selectClass} w-full pl-[38px]`}
            />
          </label>
          <select aria-label="Estado" value={estado} onChange={(e) => setFiltro('estado', e.target.value)} className={selectClass}>
            <option value="">Todos los estados</option>
            {ESTADOS.map((e) => (
              <option key={e}>{e}</option>
            ))}
          </select>
          <select
            aria-label="Categoría"
            value={categoria}
            onChange={(e) => setFiltro('categoria', e.target.value)}
            className={selectClass}
          >
            <option value="">Todas las categorías</option>
            {categorias.map((c) => (
              <option key={c}>{c}</option>
            ))}
          </select>
          <select
            aria-label="Prioridad"
            value={prioridad}
            onChange={(e) => setFiltro('prioridad', e.target.value)}
            className={selectClass}
          >
            <option value="">Todas las prioridades</option>
            {PRIORIDADES.map((p) => (
              <option key={p}>{p}</option>
            ))}
          </select>
          <select
            aria-label="Orden"
            value={orden}
            onChange={(e) => setFiltro('orden', e.target.value === 'recientes' ? '' : e.target.value)}
            className={selectClass}
          >
            {Object.entries(ORDENES).map(([valor, texto]) => (
              <option key={valor} value={valor}>
                {texto}
              </option>
            ))}
          </select>
        </div>

        {solicitudes.status === 'error' ? (
          <LoadError message={solicitudes.error.message} onRetry={solicitudes.reload} />
        ) : solicitudes.status === 'loading' ? (
          <div aria-label="Cargando solicitudes" className="grid gap-3 p-5">
            {[0, 1, 2, 3].map((i) => (
              <span key={i} className="h-10 animate-pulse rounded bg-line-soft" />
            ))}
          </div>
        ) : todas.length === 0 ? (
          <EmptyState icon="inbox" title="Aún no tienes solicitudes" description="Registra tu primera solicitud y síguela desde aquí.">
            <Link to="/solicitudes/nueva" className="font-semibold text-primary hover:underline">
              Registrar una solicitud
            </Link>
          </EmptyState>
        ) : filtradas.length === 0 ? (
          <EmptyState icon="search" title="No hay solicitudes que coincidan" description="Prueba con otro término o quita los filtros.">
            <Button size="sm" onClick={() => setParams({}, { replace: true })}>
              Quitar filtros
            </Button>
          </EmptyState>
        ) : (
          <>
            <div className="overflow-x-auto max-md:hidden">
              <table className="w-full min-w-[960px] border-collapse text-[13px]">
                <thead>
                  <tr className="bg-table-head text-left text-[10px] tracking-[.05em] text-table-head-text uppercase">
                    {['Código', 'Categoría', 'Asunto', 'Fecha', 'Prioridad', 'Estado', 'Plazo', 'Responsable', 'Detalle'].map(
                      (th) => (
                        <th key={th} scope="col" className="border-b border-line px-5 py-2.5 font-semibold whitespace-nowrap">
                          {th}
                        </th>
                      ),
                    )}
                  </tr>
                </thead>
                <tbody>
                  {filtradas.map((s) => (
                    <tr key={s.id} className="border-b border-line-soft last:border-b-0 hover:bg-row-hover">
                      <td className="px-5 py-3.5 font-mono font-semibold tracking-[.02em] whitespace-nowrap text-primary-hover">
                        {s.codigo}
                      </td>
                      <td className="px-5 py-3.5">{s.categoria}</td>
                      <td className="max-w-[300px] px-5 py-3.5 font-medium">{s.asunto}</td>
                      <td className="px-5 py-3.5 whitespace-nowrap text-muted">{formatFecha(s.fechaCreacion)}</td>
                      <td className="px-5 py-3.5">
                        <PriorityTag prioridad={s.prioridad} />
                      </td>
                      <td className="px-5 py-3.5">
                        <StatusChip estado={s.estado} />
                      </td>
                      <td className="px-5 py-3.5">
                        <PlazoTag fechaVencimiento={s.fechaVencimiento} estado={s.estado} />
                      </td>
                      <td className={`px-5 py-3.5 ${s.tecnico ? '' : 'text-muted'}`}>{s.tecnico ?? 'Sin asignar'}</td>
                      <td className="px-5 py-3.5">
                        <Link
                          to={`/solicitudes/${s.codigo}`}
                          aria-label={`Ver detalle de ${s.codigo}`}
                          className="inline-flex h-8 items-center rounded border border-line bg-surface px-2.5 font-semibold whitespace-nowrap hover:bg-line-soft"
                        >
                          Ver detalle
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <ul className="md:hidden">
              {filtradas.map((s) => (
                <li key={s.id}>
                  <Link to={`/solicitudes/${s.codigo}`} className="block border-b border-line-soft px-4 py-3.5 last:border-b-0">
                    <span className="flex items-center justify-between gap-2">
                      <span className="font-mono text-[13px] font-semibold text-primary-hover">{s.codigo}</span>
                      <StatusChip estado={s.estado} />
                    </span>
                    <span className="mt-1.5 mb-0.5 block font-semibold">{s.asunto}</span>
                    <span className="mb-2 block text-[13px] text-muted">
                      {s.categoria} · {formatFecha(s.fechaCreacion)}
                    </span>
                    <span className="flex items-center justify-between gap-2 text-[13px]">
                      <PriorityTag prioridad={s.prioridad} />
                      <PlazoTag fechaVencimiento={s.fechaVencimiento} estado={s.estado} />
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          </>
        )}

        {solicitudes.status === 'success' && todas.length > 0 && (
          <p className="border-t border-line px-5 py-3 text-[13px] text-muted">
            Mostrando {filtradas.length} de {todas.length} {todas.length === 1 ? 'solicitud' : 'solicitudes'}
            {hayFiltros && filtradas.length > 0 && (
              <>
                {' · '}
                <button type="button" onClick={() => setParams({}, { replace: true })} className="font-semibold text-primary hover:underline">
                  Quitar filtros
                </button>
              </>
            )}
          </p>
        )}
      </section>
    </>
  )
}
