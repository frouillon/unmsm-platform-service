import { Button } from './Button'
import { Icon } from './Icon'

export function LoadError({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <div role="alert" className="grid justify-items-center gap-3 px-6 py-12 text-center">
      <span className="grid size-12 place-items-center rounded-[14px] bg-bad-soft text-bad">
        <Icon name="wifi-off" className="size-6" />
      </span>
      <p className="max-w-[380px] text-muted">{message}</p>
      <Button size="sm" onClick={onRetry}>
        <Icon name="refresh" className="size-4" />
        Reintentar
      </Button>
    </div>
  )
}
