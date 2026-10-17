import { useState } from 'react'
import { Icon } from '../../../components/ui/Icon'
import { ApiError } from '../../../lib/api'
import { formatBytes } from '../../../lib/format'
import { fetchEvidencia } from '../solicitudesApi'
import type { EvidenciaDetalle } from '../types'

export function EvidenciasLista({ codigo, evidencias }: { codigo: string; evidencias: EvidenciaDetalle[] }) {
  const [abriendo, setAbriendo] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  if (evidencias.length === 0) return <p className="text-[13px] text-muted">No se adjuntaron evidencias.</p>

  const abrir = async (evidencia: EvidenciaDetalle) => {
    // La ventana se abre antes del fetch para que el navegador no la trate como ventana emergente.
    const ventana = window.open('', '_blank')
    setAbriendo(evidencia.id)
    setError(null)
    try {
      const url = URL.createObjectURL(await fetchEvidencia(codigo, evidencia.id))
      if (ventana) ventana.location.href = url
      else window.location.assign(url)
      setTimeout(() => URL.revokeObjectURL(url), 60_000)
    } catch (e) {
      ventana?.close()
      setError(e instanceof ApiError ? e.message : 'No se pudo abrir el archivo.')
    } finally {
      setAbriendo(null)
    }
  }

  return (
    <>
      <ul className="grid gap-2">
        {evidencias.map((e) => (
          <li key={e.id}>
            <button
              type="button"
              onClick={() => abrir(e)}
              disabled={abriendo === e.id}
              className="flex w-full items-center gap-2.5 rounded border border-line bg-surface px-2.5 py-2 text-left hover:bg-row-hover disabled:cursor-wait"
            >
              <span className="grid size-8 flex-none place-items-center rounded-lg bg-primary-soft text-primary">
                <Icon name={e.tipoMime === 'application/pdf' ? 'file' : 'image'} className="size-4" />
              </span>
              <span className="min-w-0 flex-1 truncate text-[13px] font-medium">{e.nombre}</span>
              <small className="text-xs text-muted">{abriendo === e.id ? 'Abriendo…' : formatBytes(e.tamanoBytes)}</small>
            </button>
          </li>
        ))}
      </ul>
      {error && (
        <p role="alert" className="mt-2 flex items-center gap-1 text-xs font-medium text-bad">
          <Icon name="alert" className="size-3.5" />
          {error}
        </p>
      )}
    </>
  )
}
