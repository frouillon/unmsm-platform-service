import { useState } from 'react'
import { Button } from '../../../components/ui/Button'
import { Field } from '../../../components/ui/Field'
import { cambiarEstadoSolicitud } from '../solicitudesApi'
import { ApiError } from '../../../lib/api'

const TRANSICIONES: Record<string, Record<string, string[]>> = {
  PENDIENTE: {
    SUPERVISOR: ['EN_PROCESO'],
    ADMINISTRADOR: ['EN_PROCESO', 'CERRADO'],
  },
  'EN PROCESO': {
    TECNICO: ['RESUELTO'],
    SUPERVISOR: ['RESUELTO', 'CERRADO'],
    ADMINISTRADOR: ['RESUELTO', 'CERRADO'],
  },
  RESUELTO: {
    SUPERVISOR: ['CERRADO'],
    ADMINISTRADOR: ['CERRADO'],
  },
}

type Props = {
  codigo: string
  estadoActual: string
  rol: string
  onSuccess: () => void
}

export function CambiarEstadoForm({ codigo, estadoActual, rol, onSuccess }: Props) {
  const destinos = TRANSICIONES[estadoActual]?.[rol] ?? []
  const [estadoDestino, setEstadoDestino] = useState(destinos[0] ?? '')
  const [notaTecnica, setNotaTecnica] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (destinos.length === 0) {
    return null
  }

  const requiereNota = estadoDestino === 'RESUELTO' || estadoDestino === 'CERRADO'

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await cambiarEstadoSolicitud(codigo, { estadoDestino, notaTecnica: notaTecnica.trim() || null })
      onSuccess()
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
      } else {
        setError('No se pudo cambiar el estado.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mt-4 grid gap-3 rounded-card border border-line bg-surface p-4">
      <div className="text-sm font-semibold">Cambiar estado</div>
      <Field id="estadoDestino" label="Estado destino">
        <select value={estadoDestino} onChange={(e) => setEstadoDestino(e.target.value)} className="w-full rounded-md border border-line px-3 py-2 text-sm">
          {destinos.map((d) => (
            <option key={d} value={d}>
              {d}
            </option>
          ))}
        </select>
      </Field>
      <Field id="notaTecnica" label={requiereNota ? 'Comentario / Nota técnica *' : 'Comentario / Nota técnica (opcional)'}>
        <textarea
          value={notaTecnica}
          onChange={(e) => setNotaTecnica(e.target.value)}
          rows={3}
          className="w-full rounded-md border border-line px-3 py-2 text-sm"
        />
      </Field>
      {error ? <div className="text-xs text-red-600">{error}</div> : null}
      <Button type="submit" disabled={loading}>
        {loading ? 'Guardando...' : 'Guardar cambio'}
      </Button>
    </form>
  )
}
