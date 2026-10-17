import { Link } from 'react-router'
import { Icon } from '../components/ui/Icon'

export function ForbiddenPage() {
  return (
    <main className="grid min-h-dvh place-items-center px-6">
      <div className="flex max-w-sm flex-col items-center gap-2 text-center text-muted">
        <span className="mb-1 grid size-12 place-items-center rounded-[14px] bg-bad-soft text-bad">
          <Icon name="shield" className="size-6" />
        </span>
        <h1 className="text-2xl font-bold text-ink">No tienes acceso a esta sección</h1>
        <p>Tu rol no tiene permisos para ver esta página.</p>
        <Link
          to="/"
          className="mt-3 inline-flex h-10 items-center rounded border border-primary bg-primary px-4 font-semibold text-white hover:bg-primary-hover max-sm:min-h-11"
        >
          Ir a mi inicio
        </Link>
      </div>
    </main>
  )
}
