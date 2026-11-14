import { useState } from 'react'
import { Button } from '../../../components/ui/Button'
import { Field, FieldError } from '../../../components/ui/Field'
import { Icon } from '../../../components/ui/Icon'
import { inputClass } from '../../../components/ui/inputClass'
import { ApiError } from '../../../lib/api'
import type { Estado } from '../../../lib/domain'
import { useApi } from '../../../lib/useApi'
import { avanzarSolicitud, fetchTecnicos } from '../solicitudesApi'
import type { AccionDisponible } from '../types'

const TEXTOS: Record<Estado, { titulo: string; ayuda: string; boton: string }> = {
  Pendiente: { titulo: 'Registrar', ayuda: '', boton: 'Registrar' },
  'En revisión': {
    titulo: 'Revisar solicitud',
    ayuda: 'Confirma que el área va a atender esta solicitud.',
    boton: 'Pasar a revisión',
  },
  Asignado: {
    titulo: 'Asignar técnico',
    ayuda: 'Elige a quién del área se la encargas.',
    boton: 'Asignar',
  },
  'En proceso': {
    titulo: 'Iniciar atención',
    ayuda: 'Indica que ya estás trabajando en esta solicitud.',
    boton: 'Iniciar atención',
  },
  Resuelto: {
    titulo: 'Marcar como resuelta',
    ayuda: 'Úsalo cuando el pedido ya quedó atendido.',
    boton: 'Marcar como resuelta',
  },
  Cerrado: {
    titulo: 'Cerrar solicitud',
    ayuda: 'Confirma que la atención quedó conforme.',
    boton: 'Cerrar solicitud',
  },
}

export function AccionEstado({
  codigo,
  accion,
  onHecho,
}: {
  codigo: string
  accion: AccionDisponible
  onHecho: () => void
}) {
  const tecnicos = useApi(
    () => (accion.requiereTecnico ? fetchTecnicos(codigo) : Promise.resolve([])),
    accion.requiereTecnico ? `tecnicos-${codigo}` : 'sin-tecnicos',
  )
  const [tecnicoId, setTecnicoId] = useState('')
  const [nota, setNota] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState('')
  const [errorTecnico, setErrorTecnico] = useState('')
  const texto = TEXTOS[accion.estado]
  const lista = accion.requiereTecnico && tecnicos.status === 'success' ? tecnicos.data : []

  const enviar = async () => {
    setError('')
    setErrorTecnico('')
    if (accion.requiereTecnico && !tecnicoId) {
      setErrorTecnico('Elige al técnico que la atenderá.')
      return
    }
    setEnviando(true)
    try {
      await avanzarSolicitud(codigo, accion.estado, tecnicoId ? Number(tecnicoId) : null, nota)
      onHecho()
    } catch (err) {
      const api = err instanceof ApiError ? err : new ApiError(0, 'Ocurrió un error inesperado.')
      setErrorTecnico(api.fieldErrors.tecnicoId ?? '')
      setError(api.fieldErrors.tecnicoId ? '' : api.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="rounded-card border border-line bg-surface p-5" aria-labelledby="siguiente-paso">
      <h2 id="siguiente-paso" className="font-sans text-[15px] font-semibold tracking-normal">
        {texto.titulo}
      </h2>
      <p className="mt-1.5 text-[13px] text-muted">{texto.ayuda}</p>

      {accion.requiereTecnico && (
        <Field id="accion-tecnico" label="Técnico" error={errorTecnico} required className="mt-4">
          {tecnicos.status === 'error' ? (
            <FieldError id="accion-tecnico-carga" message={tecnicos.error.message} />
          ) : (
            <select
              id="accion-tecnico"
              value={tecnicoId}
              onChange={(e) => setTecnicoId(e.target.value)}
              disabled={tecnicos.status === 'loading' || lista.length === 0}
              aria-invalid={!!errorTecnico}
              className={inputClass(!!errorTecnico)}
            >
              <option value="">{tecnicos.status === 'loading' ? 'Cargando técnicos…' : 'Elige un técnico'}</option>
              {lista.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.nombres}
                </option>
              ))}
            </select>
          )}
          {tecnicos.status === 'success' && lista.length === 0 && (
            <FieldError id="accion-tecnico-vacio" message="No hay técnicos activos en esta área." />
          )}
        </Field>
      )}

      <Field id="accion-nota" label="Nota" optional className="mt-4">
        <textarea
          id="accion-nota"
          value={nota}
          maxLength={500}
          rows={3}
          onChange={(e) => setNota(e.target.value)}
          placeholder="Queda en el seguimiento"
          className={`${inputClass(false)} h-auto py-2`}
        />
      </Field>

      {error && (
        <p role="alert" className="mt-3 flex items-center gap-1 text-xs font-medium text-bad">
          <Icon name="alert" className="size-3.5" />
          {error}
        </p>
      )}

      <Button
        variant="primary"
        className="mt-4 w-full"
        disabled={enviando || (accion.requiereTecnico && lista.length === 0)}
        onClick={enviar}
      >
        {enviando ? 'Guardando…' : texto.boton}
      </Button>
    </section>
  )
}
