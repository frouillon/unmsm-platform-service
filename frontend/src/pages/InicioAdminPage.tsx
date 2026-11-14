import { resumenCola } from '../features/dashboard/conteos'
import { ColaInicio } from '../features/solicitudes/components/ColaInicio'
import { fetchTodas } from '../features/solicitudes/solicitudesApi'

export function InicioAdminPage() {
  return (
    <ColaInicio
      subtitulo="Revisa y asigna solicitudes de cualquier área."
      cacheKey="solicitudes-todas"
      fetcher={fetchTodas}
      detalleTo={(codigo) => `/admin/solicitudes/${codigo}`}
      verTodasTo="/admin/solicitudes"
      tituloLista="Pendientes"
      esVisible={(s) => s.estado === 'Pendiente' || s.estado === 'En revisión'}
      vacio={{
        titulo: 'No hay solicitudes por revisar',
        descripcion: 'Cuando un estudiante registre una solicitud, aparecerá aquí.',
      }}
      resumen={resumenCola}
    />
  )
}
