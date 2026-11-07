import { BandejaSolicitudes } from '../features/solicitudes/components/BandejaSolicitudes'
import { fetchSolicitudesArea } from '../features/solicitudes/solicitudesApi'

export function SolicitudesAreaPage() {
  return (
    <BandejaSolicitudes
      titulo="Solicitudes"
      subtitulo="Clasifica, asigna y da seguimiento a las solicitudes de tu área."
      cacheKey="solicitudes-area"
      fetcher={fetchSolicitudesArea}
      detalleTo={(codigo) => `/supervisor/solicitudes/${codigo}`}
      vacio={{
        titulo: 'Tu área no tiene solicitudes',
        descripcion: 'Las solicitudes que registren los estudiantes de tu área aparecerán aquí.',
      }}
    />
  )
}
