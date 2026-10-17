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

export function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function plazoTexto(dias: number, habiles: boolean): string {
  const unidad = habiles ? (dias === 1 ? 'día hábil' : 'días hábiles') : dias === 1 ? 'día calendario' : 'días calendario'
  return `${dias} ${unidad}`
}
