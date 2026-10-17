import type { Estado } from '../../../lib/domain'

const puntos: Record<Estado, string> = {
  Pendiente: 'bg-subtle',
  'En revisión': 'bg-muted',
  Asignado: 'bg-primary',
  'En proceso': 'bg-warn',
  Resuelto: 'bg-ok',
  Cerrado: 'bg-line',
}

/** Estado en formato compacto (punto + texto), como en la lista del Inicio. */
export function EstadoDot({ estado }: { estado: Estado }) {
  return (
    <span className="inline-flex items-center gap-[7px] text-xs">
      <i aria-hidden className={`size-[7px] rounded-full ${puntos[estado]}`} />
      {estado}
    </span>
  )
}
