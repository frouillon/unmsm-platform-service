import { resumenCola } from '../features/dashboard/conteos'
import { ColaInicio } from '../features/solicitudes/components/ColaInicio'
import { fetchSolicitudesArea } from '../features/solicitudes/solicitudesApi'

export function InicioSupervisorPage() {
  return (
    <ColaInicio
      subtitulo="Revisa y asigna las solicitudes de tu área."
      cacheKey="solicitudes-area"
      fetcher={fetchSolicitudesArea}
      detalleTo={(codigo) => `/supervisor/solicitudes/${codigo}`}
      verTodasTo="/supervisor/solicitudes"
      tituloLista="Pendientes de tu área"
      esVisible={(s) => s.estado === 'Pendiente' || s.estado === 'En revisión'}
      vacio={{
        titulo: 'No hay solicitudes por revisar',
        descripcion: 'Cuando un estudiante registre una solicitud de tu área, aparecerá aquí.',
      }}
      resumen={resumenCola}
    />
  )
}
