import { Link } from 'react-router'
import { Icon } from '../components/ui/Icon'
import { BandejaSolicitudes } from '../features/solicitudes/components/BandejaSolicitudes'
import { fetchMisSolicitudes } from '../features/solicitudes/solicitudesApi'

export function MisSolicitudesPage() {
  return (
    <BandejaSolicitudes
      titulo="Mis solicitudes"
      subtitulo="Consulta el estado y el historial de lo que has registrado."
      cacheKey="mis-solicitudes"
      fetcher={fetchMisSolicitudes}
      detalleTo={(codigo) => `/solicitudes/${codigo}`}
      acciones={
        <Link
          to="/solicitudes/nueva"
          className="inline-flex h-10 items-center justify-center gap-2 rounded border border-primary bg-primary px-4 text-sm font-semibold text-white transition-colors hover:border-primary-hover hover:bg-primary-hover max-sm:min-h-11"
        >
          <Icon name="plus" className="size-4" />
          Nueva solicitud
        </Link>
      }
      vacio={{
        titulo: 'Aún no tienes solicitudes',
        descripcion: 'Registra tu primera solicitud y síguela desde aquí.',
        accion: (
          <Link to="/solicitudes/nueva" className="font-semibold text-primary hover:underline">
            Registrar una solicitud
          </Link>
        ),
      }}
    />
  )
}
