import type { Estado } from '../../lib/domain'

const tones: Record<Estado, string> = {
  Pendiente: 'bg-subtle-soft text-subtle',
  'En revisión': 'bg-transparent text-muted ring-1 ring-inset ring-line',
  Asignado: 'bg-primary-soft text-primary',
  'En proceso': 'bg-warn-soft text-warn',
  Resuelto: 'bg-ok-soft text-ok',
  Cerrado: 'bg-line-soft text-muted',
}

export function StatusChip({ estado }: { estado: Estado }) {
  return (
    <span
      className={`inline-flex h-6 items-center gap-1.5 rounded px-2.5 text-xs font-semibold whitespace-nowrap ${tones[estado]}`}
    >
      <span aria-hidden className="size-1.5 rounded-full bg-current" />
      {estado}
    </span>
  )
}
