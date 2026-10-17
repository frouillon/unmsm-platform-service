import type { ReactNode } from 'react'
import { Icon, type IconName } from './Icon'

type Tone = 'neutral' | 'error' | 'ok' | 'warn'

const tones: Record<Tone, string> = {
  neutral: 'bg-subtle-soft text-subtle',
  error: 'bg-bad-soft text-bad',
  ok: 'bg-ok-soft text-ok',
  warn: 'bg-warn-soft text-warn',
}

type EmptyStateProps = {
  icon: IconName
  title: string
  description?: string
  tone?: Tone
  children?: ReactNode
}

export function EmptyState({ icon, title, description, tone = 'neutral', children }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center gap-2 px-6 py-12 text-center text-muted">
      <span className={`mb-1 grid size-12 place-items-center rounded-[14px] ${tones[tone]}`}>
        <Icon name={icon} className="size-6" />
      </span>
      <h3 className="text-base font-semibold text-ink">{title}</h3>
      {description && <p className="max-w-[380px]">{description}</p>}
      {children && <div className="mt-3 flex justify-center gap-2.5">{children}</div>}
    </div>
  )
}
