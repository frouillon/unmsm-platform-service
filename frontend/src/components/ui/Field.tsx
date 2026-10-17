import type { ReactNode } from 'react'
import { Icon } from './Icon'

type FieldProps = {
  id: string
  label: string
  error?: string
  hint?: ReactNode
  required?: boolean
  optional?: boolean
  className?: string
  children: ReactNode
}

export function Field({ id, label, error, hint, required, optional, className = '', children }: FieldProps) {
  return (
    <div className={`block ${className}`}>
      <label htmlFor={id} className="block text-[13px] font-semibold">
        {label}
        {required && <span className="text-bad"> *</span>}
        {optional && <span className="font-normal text-muted"> (opcional)</span>}
      </label>
      <div className="mt-1.5">{children}</div>
      {hint && (
        <p id={`${id}-hint`} className="mt-1.5 text-xs text-muted">
          {hint}
        </p>
      )}
      <FieldError id={`${id}-error`} message={error} />
    </div>
  )
}

export function FieldError({ id, message }: { id: string; message?: string }) {
  if (!message) return null
  return (
    <p id={id} className="mt-1.5 flex items-center gap-1 text-xs font-medium text-bad">
      <Icon name="alert" className="size-3.5" />
      {message}
    </p>
  )
}
