import { Icon } from '../../../components/ui/Icon'
import { ESTADOS, ROL_LABEL, type Estado, type Rol } from '../../../lib/domain'
import { formatFechaHoraCorta } from '../../../lib/format'
import type { Movimiento } from '../types'

const TITULOS: Record<Estado, string> = {
  Pendiente: 'Solicitud registrada',
  'En revisión': 'En revisión',
  Asignado: 'Técnico asignado',
  'En proceso': 'En proceso',
  Resuelto: 'Resuelto',
  Cerrado: 'Cerrado',
}

type Paso =
  | { tipo: 'hecho' | 'actual' | 'final'; movimiento: Movimiento }
  | { tipo: 'pendiente'; estado: Estado }

function pasos(historial: Movimiento[], estadoActual: Estado): Paso[] {
  const hechos: Paso[] = historial.map((movimiento, i) => ({
    tipo: i < historial.length - 1 ? 'hecho' : estadoActual === 'Cerrado' ? 'final' : 'actual',
    movimiento,
  }))
  const siguientes = ESTADOS.slice(ESTADOS.indexOf(estadoActual) + 1).map<Paso>((estado) => ({ tipo: 'pendiente', estado }))
  return [...hechos, ...siguientes]
}

const punto: Record<Paso['tipo'], string> = {
  hecho: 'border-primary bg-primary text-white',
  final: 'border-ok bg-ok text-white',
  actual: 'border-warn bg-warn-soft',
  pendiente: 'border-line bg-surface',
}

export function Seguimiento({ historial, estado }: { historial: Movimiento[]; estado: Estado }) {
  const lista = pasos(historial, estado)
  return (
    <ol className="grid">
      {lista.map((paso, i) => {
        const ultimo = i === lista.length - 1
        const titulo = paso.tipo === 'pendiente' ? TITULOS[paso.estado] : TITULOS[paso.movimiento.estadoDestino]
        return (
          <li key={i} className={`relative pl-9 ${ultimo ? '' : 'pb-[22px]'}`} aria-current={paso.tipo === 'actual' ? 'step' : undefined}>
            {!ultimo && (
              <span
                aria-hidden
                className={`absolute top-[26px] bottom-0.5 left-[11px] w-0.5 ${paso.tipo === 'hecho' ? 'bg-primary' : 'bg-line'}`}
              />
            )}
            <span aria-hidden className={`absolute top-0 left-0 grid size-6 place-items-center rounded-full border-2 ${punto[paso.tipo]}`}>
              {(paso.tipo === 'hecho' || paso.tipo === 'final') && <Icon name="tick" className="size-[13px] [stroke-width:2.5]" />}
              {paso.tipo === 'actual' && <span className="size-2 rounded-full bg-warn" />}
            </span>
            <b className={`block text-sm ${paso.tipo === 'pendiente' ? 'font-medium text-muted' : ''}`}>{titulo}</b>
            {paso.tipo === 'pendiente' ? (
              <small className="block text-xs text-muted">Pendiente</small>
            ) : (
              <>
                <small className="block text-xs text-muted">{formatFechaHoraCorta(paso.movimiento.fecha)}</small>
                <small className="block text-xs text-muted">
                  por {paso.movimiento.usuario} · {ROL_LABEL[paso.movimiento.rol as Rol] ?? paso.movimiento.rol}
                </small>
                {paso.movimiento.nota && (
                  <p className="mt-1.5 rounded-md bg-canvas px-2.5 py-1.5 text-xs whitespace-pre-line">{paso.movimiento.nota}</p>
                )}
              </>
            )}
          </li>
        )
      })}
    </ol>
  )
}
