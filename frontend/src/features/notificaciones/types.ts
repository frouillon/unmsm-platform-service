export type Notificacion = {
  id: number
  tipo: 'CAMBIO_ESTADO' | 'ASIGNACION' | 'COMENTARIO' | string
  mensaje: string
  codigoSolicitud: string
  leida: boolean
  fecha: string
}
