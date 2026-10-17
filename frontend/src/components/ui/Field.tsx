import type { ReactNode } from 'react'
import { Icon } from './Icon'

type FieldProps = {
  id: string
  label: string
  error?: string
  className?: string
  children: ReactNode
}

export function Field({ id, label, error, className = '', children }: FieldProps) {
  return (
    <div className={`block ${className}`}>
      <label htmlFor={id} className="block text-[13px] font-semibold">
        {label}
      </label>
      <div className="mt-1.5">{children}</div>
      {error && (
        <p id={`${id}-error`} className="mt-1.5 flex items-center gap-1 text-xs font-medium text-bad">
          <Icon name="alert" className="size-3.5" />
          {error}
        </p>
      )}
    </div>
  )
}
