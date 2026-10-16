import { Link } from 'react-router'

export function NotFoundPage() {
  return (
    <main className="grid min-h-dvh place-items-center px-6">
      <div className="flex max-w-sm flex-col items-center gap-2 text-center text-muted">
        <span className="text-[11px] font-semibold tracking-[.14em] text-primary uppercase">Error 404</span>
        <h1 className="text-3xl font-bold text-ink">Página no encontrada</h1>
        <p>La dirección que buscas no existe o fue movida.</p>
        <Link
          to="/"
          className="mt-3 inline-flex h-10 items-center rounded border border-primary bg-primary px-4 font-semibold text-white hover:bg-primary-hover max-sm:min-h-11"
        >
          Volver al inicio
        </Link>
      </div>
    </main>
  )
}
