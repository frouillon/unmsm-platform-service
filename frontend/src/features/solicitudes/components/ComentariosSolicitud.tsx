import { useState } from 'react'
import { Button } from '../../../components/ui/Button'
import { Field } from '../../../components/ui/Field'
import { Icon } from '../../../components/ui/Icon'
import { inputClass } from '../../../components/ui/inputClass'
import { ApiError } from '../../../lib/api'
import { ROL_LABEL, type Rol } from '../../../lib/domain'
import { formatFechaHoraCorta } from '../../../lib/format'
import { crearComentario } from '../solicitudesApi'
import type { Comentario } from '../types'

export function ComentariosSolicitud({
  codigo,
  comentarios,
  puedeComentar,
  onHecho,
}: {
  codigo: string
  comentarios: Comentario[]
  puedeComentar: boolean
  onHecho: () => void
}) {
  const [texto, setTexto] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState('')
  const [errorCampo, setErrorCampo] = useState('')

  const enviar = async () => {
    setError('')
    setErrorCampo('')
    if (!texto.trim()) {
      setErrorCampo('Escribe un comentario.')
      return
    }
    setEnviando(true)
    try {
      await crearComentario(codigo, texto)
      setTexto('')
      onHecho()
    } catch (err) {
      const api = err instanceof ApiError ? err : new ApiError(0, 'Ocurrió un error inesperado.')
      setErrorCampo(api.fieldErrors.contenido ?? '')
      setError(api.fieldErrors.contenido ? '' : api.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="rounded-card border border-line bg-surface p-5" aria-labelledby="comentarios">
      <h2 id="comentarios" className="mb-4 font-sans text-[15px] font-semibold tracking-normal">
        Comentarios
      </h2>

      {comentarios.length === 0 ? (
        <p className="mb-4 text-[13px] text-muted">Todavía no hay comentarios en esta solicitud.</p>
      ) : (
        <ul className="mb-4 grid gap-3">
          {comentarios.map((c) => (
            <li key={c.id} className="rounded-lg border border-line-soft bg-canvas px-3.5 py-3">
              <div className="mb-1.5 flex flex-wrap items-baseline gap-x-2 gap-y-0.5">
                <span className="text-[13px] font-semibold">{c.autor}</span>
                <span className="text-[11px] font-medium text-muted">
                  {ROL_LABEL[c.rol as Rol] ?? c.rol}
                </span>
                <span className="text-[11px] text-muted">{formatFechaHoraCorta(c.fecha)}</span>
              </div>
              <p className="text-[13px] whitespace-pre-line">{c.contenido}</p>
            </li>
          ))}
        </ul>
      )}

      {puedeComentar ? (
        <>
          <Field id="nuevo-comentario" label="Nuevo comentario" error={errorCampo} required>
            <textarea
              id="nuevo-comentario"
              value={texto}
              maxLength={2000}
              rows={3}
              onChange={(e) => setTexto(e.target.value)}
              placeholder="Escribe para el equipo o el solicitante"
              aria-invalid={!!errorCampo}
              className={`${inputClass(!!errorCampo)} h-auto py-2`}
            />
          </Field>
          {error && (
            <p role="alert" className="mt-3 flex items-center gap-1 text-xs font-medium text-bad">
              <Icon name="alert" className="size-3.5" />
              {error}
            </p>
          )}
          <Button variant="primary" className="mt-3 w-full sm:w-auto" disabled={enviando} onClick={enviar}>
            {enviando ? 'Publicando…' : 'Publicar comentario'}
          </Button>
        </>
      ) : (
        <p className="text-[13px] text-muted">Esta solicitud está cerrada; ya no admite comentarios.</p>
      )}
    </section>
  )
}
