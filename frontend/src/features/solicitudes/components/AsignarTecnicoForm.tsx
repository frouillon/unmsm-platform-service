import { useState } from 'react'
import { Button } from '../../../components/ui/Button'
import { Field } from '../../../components/ui/Field'
import { asignarSolicitud } from '../solicitudesApi'
import { ApiError } from '../../../lib/api'

type Props = {
  codigo: string
  onSuccess: () => void
}

export function AsignarTecnicoForm({ codigo, onSuccess }: Props) {
  const [tecnicoId, setTecnicoId] = useState('')
  const [notaTecnica, setNotaTecnica] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await asignarSolicitud(codigo, { tecnicoId: Number(tecnicoId), notaTecnica: notaTecnica.trim() || null })
      onSuccess()
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
      } else {
        setError('No se pudo asignar la solicitud.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mt-4 grid gap-3 rounded-card border border-line bg-surface p-4">
      <div className="text-sm font-semibold">Asignar técnico</div>
      <Field id="tecnicoId" label="ID del técnico">
        <input value={tecnicoId} onChange={(e) => setTecnicoId(e.target.value)} className="w-full rounded-md border border-line px-3 py-2 text-sm" />
      </Field>
      <Field id="notaTecnica" label="Comentario (opcional)">
        <textarea value={notaTecnica} onChange={(e) => setNotaTecnica(e.target.value)} rows={2} className="w-full rounded-md border border-line px-3 py-2 text-sm" />
      </Field>
      {error ? <div className="text-xs text-red-600">{error}</div> : null}
      <Button type="submit" disabled={loading || !tecnicoId}>
        {loading ? 'Asignando...' : 'Asignar'}
      </Button>
      <div className="text-xs text-muted">Ingresa el ID del usuario con rol TÉCNICO.</div>
    </form>
  )
}
