import type { Prioridad } from '../../lib/domain'

const tones: Record<Prioridad, { text: string; mark: string }> = {
  Baja: { text: 'text-muted', mark: 'bg-line' },
  Media: { text: 'text-subtle', mark: 'bg-muted' },
  Alta: { text: 'text-warn', mark: 'bg-warn' },
  Urgente: { text: 'text-bad', mark: 'bg-bad' },
}

export function PriorityTag({ prioridad }: { prioridad: Prioridad }) {
  const tone = tones[prioridad]
  return (
    <span className={`inline-flex items-center gap-1.5 text-[13px] font-semibold ${tone.text}`}>
      <i aria-hidden className={`size-2 rounded-[2px] ${tone.mark}`} />
      {prioridad}
    </span>
  )
}
