import { ColaInicio } from '../features/solicitudes/components/ColaInicio'
import { fetchAsignadas } from '../features/solicitudes/solicitudesApi'
import { estaAtendida } from '../lib/plazo'

export function InicioTecnicoPage() {
  return (
    <ColaInicio
      subtitulo="Estas son las solicitudes que tienes a cargo."
      cacheKey="asignadas"
      fetcher={fetchAsignadas}
      detalleTo={(codigo) => `/tecnico/asignadas/${codigo}`}
      verTodasTo="/tecnico/asignadas"
      tituloLista="Por atender"
      esVisible={(s) => s.estado === 'Asignado' || s.estado === 'En proceso'}
      vacio={{
        titulo: 'No tienes solicitudes a cargo',
        descripcion: 'Cuando te asignen una solicitud, podrás atenderla desde aquí.',
      }}
      resumen={(lista) => [
        { valor: lista.filter((s) => s.estado === 'Asignado').length, uno: 'por atender', varios: 'por atender' },
        { valor: lista.filter((s) => s.estado === 'En proceso').length, uno: 'en proceso', varios: 'en proceso' },
        { valor: lista.filter((s) => estaAtendida(s.estado)).length, uno: 'atendida', varios: 'atendidas' },
      ]}
    />
  )
}
