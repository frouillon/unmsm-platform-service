import { useRef, useState, type DragEvent } from 'react'
import { Icon } from '../../../components/ui/Icon'
import { formatBytes } from '../../../lib/format'
import { EVIDENCIAS } from '../validacion'

type EvidenciasInputProps = {
  id: string
  archivos: File[]
  onAgregar: (archivos: File[]) => void
  onQuitar: (index: number) => void
  invalid: boolean
  describedBy?: string
  disabled?: boolean
}

export function EvidenciasInput({ id, archivos, onAgregar, onQuitar, invalid, describedBy, disabled }: EvidenciasInputProps) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [over, setOver] = useState(false)

  const handleDrop = (event: DragEvent<HTMLButtonElement>) => {
    event.preventDefault()
    setOver(false)
    if (!disabled) onAgregar(Array.from(event.dataTransfer.files))
  }

  return (
    <>
      <input
        ref={inputRef}
        type="file"
        multiple
        accept={EVIDENCIAS.accept}
        className="sr-only"
        tabIndex={-1}
        aria-hidden
        onChange={(e) => {
          onAgregar(Array.from(e.target.files ?? []))
          e.target.value = ''
        }}
      />
      <button
        type="button"
        id={id}
        disabled={disabled}
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => {
          e.preventDefault()
          setOver(true)
        }}
        onDragLeave={() => setOver(false)}
        onDrop={handleDrop}
        aria-describedby={describedBy}
        className={`mt-1.5 block w-full rounded-lg border-[1.5px] border-dashed px-4 py-7 text-center transition-colors disabled:cursor-not-allowed ${
          over ? 'border-primary bg-primary-soft text-primary' : `bg-canvas text-muted ${invalid ? 'border-bad' : 'border-line'}`
        }`}
      >
        <Icon name="upload" className="mx-auto mb-1.5 size-6" />
        <span className="block text-[13px]">
          Arrastra tus archivos aquí o <b className="text-primary">selecciónalos</b>
        </span>
        <small className="mt-0.5 block text-[11px]">PNG, JPG o PDF · hasta 5 archivos de 5 MB</small>
      </button>

      {archivos.length > 0 && (
        <ul className="mt-3 grid gap-2" aria-label="Archivos adjuntos">
          {archivos.map((archivo, index) => (
            <li
              key={`${archivo.name}-${archivo.lastModified}-${index}`}
              className="flex items-center gap-2.5 rounded border border-line bg-surface px-2.5 py-2"
            >
              <span className="grid size-8 flex-none place-items-center rounded-lg bg-primary-soft text-primary">
                <Icon name={/\.pdf$/i.test(archivo.name) ? 'file' : 'image'} className="size-4" />
              </span>
              <span className="min-w-0 flex-1 truncate text-[13px] font-medium">{archivo.name}</span>
              <small className="text-xs text-muted">{formatBytes(archivo.size)}</small>
              <button
                type="button"
                onClick={() => onQuitar(index)}
                disabled={disabled}
                aria-label={`Quitar ${archivo.name}`}
                className="grid size-8 place-items-center rounded-lg text-muted hover:bg-line-soft hover:text-ink max-sm:size-11"
              >
                <Icon name="x" className="size-4" />
              </button>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
