import { BandejaSolicitudes } from '../features/solicitudes/components/BandejaSolicitudes'
import { fetchTodas } from '../features/solicitudes/solicitudesApi'

export function SolicitudesAdminPage() {
  return (
    <BandejaSolicitudes
      titulo="Solicitudes"
      subtitulo="Todas las solicitudes registradas en el sistema."
      cacheKey="solicitudes-todas"
      fetcher={fetchTodas}
      detalleTo={(codigo) => `/admin/solicitudes/${codigo}`}
      vacio={{
        titulo: 'Todavía no hay solicitudes',
        descripcion: 'Cuando un estudiante registre una, aparecerá en esta lista.',
      }}
    />
  )
}
