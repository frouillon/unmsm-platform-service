const fechaHora = new Intl.DateTimeFormat('es-PE', {
  weekday: 'short',
  day: 'numeric',
  month: 'short',
  hour: '2-digit',
  minute: '2-digit',
})

/** Fechas del backend (LocalDateTime sin zona, hora de Lima). */
export function formatFechaHora(iso: string): string {
  return fechaHora.format(new Date(iso))
}

const fecha = new Intl.DateTimeFormat('es-PE', { day: '2-digit', month: 'short', year: 'numeric' })
const horaMinuto = new Intl.DateTimeFormat('es-PE', { hour: '2-digit', minute: '2-digit', hour12: false })

/** "02 oct 2026" */
export function formatFecha(iso: string): string {
  return fecha.format(new Date(iso)).replace(/\./g, '')
}

/** "02 oct 2026 · 09:12" */
export function formatFechaHoraCorta(iso: string): string {
  return `${formatFecha(iso)} · ${horaMinuto.format(new Date(iso))}`
}

const relativo = new Intl.RelativeTimeFormat('es', { numeric: 'auto' })

/** "hace 25 minutos", "ayer", "hace 3 días" */
export function haceTiempo(iso: string, ahora = new Date()): string {
  const segundos = Math.round((new Date(iso).getTime() - ahora.getTime()) / 1000)
  const abs = Math.abs(segundos)
  if (abs < 60) return 'hace un momento'
  if (abs < 3600) return relativo.format(Math.round(segundos / 60), 'minute')
  if (abs < 86400) return relativo.format(Math.round(segundos / 3600), 'hour')
  if (abs < 30 * 86400) return relativo.format(Math.round(segundos / 86400), 'day')
  return `el ${formatFecha(iso)}`
}

export function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function plazoTexto(dias: number, habiles: boolean): string {
  const unidad = habiles ? (dias === 1 ? 'día hábil' : 'días hábiles') : dias === 1 ? 'día calendario' : 'días calendario'
  return `${dias} ${unidad}`
}
