import { BandejaSolicitudes } from '../features/solicitudes/components/BandejaSolicitudes'
import { fetchAsignadas } from '../features/solicitudes/solicitudesApi'
import { estaAtendida } from '../lib/plazo'

export function HistorialTecnicoPage() {
  return (
    <BandejaSolicitudes
      titulo="Historial"
      subtitulo="Solicitudes que ya atendiste."
      cacheKey="historial-tecnico"
      fetcher={fetchAsignadas}
      detalleTo={(codigo) => `/tecnico/historial/${codigo}`}
      preparar={(lista) => lista.filter((s) => estaAtendida(s.estado))}
      vacio={{
        titulo: 'Aún no tienes solicitudes atendidas',
        descripcion: 'Cuando marques una solicitud como resuelta, quedará en este historial.',
      }}
    />
  )
}
