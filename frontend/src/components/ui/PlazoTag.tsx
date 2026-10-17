import type { Estado } from '../../lib/domain'
import { calcularPlazo, type TonoPlazo } from '../../lib/plazo'
import { Icon } from './Icon'

const tonos: Record<TonoPlazo, string> = {
  ok: 'text-ok',
  bad: 'text-bad',
  warn: 'text-warn',
  muted: 'text-muted',
}

export function PlazoTag({ fechaVencimiento, estado }: { fechaVencimiento: string; estado: Estado }) {
  const plazo = calcularPlazo(fechaVencimiento, estado)
  return (
    <span className={`inline-flex items-center gap-1 text-xs font-semibold whitespace-nowrap ${tonos[plazo.tono]}`}>
      <Icon name={plazo.tono === 'bad' ? 'alert' : plazo.tono === 'ok' ? 'tick' : 'clock'} className="size-3.5" />
      {plazo.texto}
    </span>
  )
}
