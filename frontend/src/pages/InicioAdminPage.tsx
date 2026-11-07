import { ColaInicio } from '../features/solicitudes/components/ColaInicio'
import { fetchTodas } from '../features/solicitudes/solicitudesApi'
import { calcularPlazo } from '../lib/plazo'

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
      resumen={(lista) => [
        { valor: lista.filter((s) => s.estado === 'Pendiente').length, uno: 'por revisar', varios: 'por revisar' },
        { valor: lista.filter((s) => s.estado === 'En revisión').length, uno: 'por asignar', varios: 'por asignar' },
        {
          valor: lista.filter((s) => calcularPlazo(s.fechaVencimiento, s.estado).tono === 'bad').length,
          uno: 'vencida',
          varios: 'vencidas',
          tono: 'bad',
        },
      ]}
    />
  )
}
