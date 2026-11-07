import { BandejaSolicitudes } from '../features/solicitudes/components/BandejaSolicitudes'
import { fetchAsignadas } from '../features/solicitudes/solicitudesApi'

export function AsignadasPage() {
  return (
    <BandejaSolicitudes
      titulo="Solicitudes asignadas"
      subtitulo="Todo lo que tienes a cargo, ordenado por prioridad."
      cacheKey="asignadas"
      fetcher={fetchAsignadas}
      detalleTo={(codigo) => `/tecnico/asignadas/${codigo}`}
      vacio={{
        titulo: 'No tienes solicitudes asignadas',
        descripcion: 'Cuando un supervisor te asigne una solicitud, la verás en esta lista.',
      }}
    />
  )
}
