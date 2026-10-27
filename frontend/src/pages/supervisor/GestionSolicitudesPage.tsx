import { Link } from 'react-router'
import { EmptyState } from '../../components/ui/EmptyState'
import { LoadError } from '../../components/ui/LoadError'
import { PlazoTag } from '../../components/ui/PlazoTag'
import { PriorityTag } from '../../components/ui/PriorityTag'
import { StatusChip } from '../../components/ui/StatusChip'
import { fetchGestion } from '../../features/solicitudes/solicitudesApi'
import { formatFechaHoraCorta } from '../../lib/format'
import { useApi } from '../../lib/useApi'

const card = 'rounded-card border border-line bg-surface'

export function GestionSolicitudesPage() {
  const data = useApi(fetchGestion, 'solicitudes-gestion')

  if (data.status === 'error') {
    return <LoadError message={data.error.message} onRetry={data.reload} />
  }

  if (data.status === 'loading') {
    return <div className="h-40 animate-pulse rounded-card bg-line-soft" />
  }

  if (data.data.length === 0) {
    return <EmptyState icon="list" title="Sin solicitudes" description="No hay solicitudes para gestionar en tu alcance." />
  }

  return (
    <div className="grid gap-3">
      {data.data.map((s) => (
        <Link key={s.codigo} to={`/solicitudes/${s.codigo}`} className={`${card} grid gap-2 p-4 hover:border-primary/40`}>
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <span className="font-mono text-sm font-semibold text-primary-hover">{s.codigo}</span>
              <StatusChip estado={s.estado as any} />
              <PriorityTag prioridad={s.prioridad as any} />
            </div>
            <PlazoTag fechaVencimiento={s.fechaVencimiento} estado={s.estado as any} />
          </div>
          <div className="text-base font-semibold">{s.asunto}</div>
          <div className="flex flex-wrap gap-4 text-xs text-muted">
            <span>Categoría: {s.categoria}</span>
            <span>Área: {s.area}</span>
            <span>Solicitante: {s.solicitante}</span>
            <span>Técnico: {s.tecnico ?? 'Sin asignar'}</span>
            <span>Registro: {formatFechaHoraCorta(s.fechaCreacion)}</span>
          </div>
        </Link>
      ))}
    </div>
  )
}
