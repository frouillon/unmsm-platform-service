import type { PrioridadCatalogo } from '../types'

type PrioridadSelectorProps = {
  id: string
  prioridades: PrioridadCatalogo[]
  value: number | null
  onChange: (id: number) => void
  invalid: boolean
  describedBy?: string
}

export function PrioridadSelector({ id, prioridades, value, onChange, invalid, describedBy }: PrioridadSelectorProps) {
  return (
    <div
      role="radiogroup"
      aria-labelledby={`${id}-label`}
      aria-describedby={describedBy}
      className="mt-1.5 grid grid-cols-2 gap-2 min-[420px]:grid-cols-4"
    >
      {prioridades.map((p, index) => (
        <label key={p.id} className="relative">
          <input
            type="radio"
            name={id}
            id={index === 0 ? id : undefined}
            value={p.id}
            checked={value === p.id}
            onChange={() => onChange(p.id)}
            aria-invalid={invalid || undefined}
            className="peer absolute inset-0 opacity-0"
          />
          <span
            className={`flex h-10 cursor-pointer items-center justify-center rounded border bg-surface text-[13px] font-semibold transition-colors peer-checked:border-primary peer-checked:bg-primary-soft peer-checked:text-primary peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-primary hover:bg-line-soft peer-checked:hover:bg-primary-soft max-sm:min-h-11 ${
              invalid ? 'border-bad' : 'border-line'
            }`}
          >
            {p.nombre}
          </span>
        </label>
      ))}
    </div>
  )
}
