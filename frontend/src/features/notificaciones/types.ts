export type Notificacion = {
  id: number
  tipo:
    | 'CAMBIO_ESTADO'
    | 'ASIGNACION'
    | 'COMENTARIO'
    | 'SLA_EN_RIESGO'
    | 'SLA_VENCIDA'
    | 'CONSOLIDADO_DIARIO'
    | string
  mensaje: string
  /** Null en avisos generales, como el consolidado diario del supervisor. */
  codigoSolicitud: string | null
  leida: boolean
  fecha: string
}
