import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router'
import { Icon } from '../../components/ui/Icon'
import { iniciales } from '../../lib/domain'
import type { UsuarioActual } from '../auth/types'
import { NAVIGATION } from './navigation'

const iconBase =
  'grid size-[38px] flex-none place-items-center text-muted transition-colors hover:bg-line-soft hover:text-ink max-sm:size-11'
const iconButton = `${iconBase} rounded-full`

type TopbarProps = {
  usuario: UsuarioActual
  onMenu: () => void
  onLogout: () => void
}

export function Topbar({ usuario, onMenu, onLogout }: TopbarProps) {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const [query, setQuery] = useState(params.get('q') ?? '')

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const q = query.trim()
    navigate(`${NAVIGATION[usuario.rol].listPath}${q ? `?q=${encodeURIComponent(q)}` : ''}`)
  }

  return (
    <header className="sticky top-0 z-20 flex h-[58px] items-center gap-1.5 border-b border-line bg-surface/92 px-3.5 backdrop-blur-md backdrop-saturate-[1.2] sm:h-[62px] sm:gap-2.5 sm:px-6 lg:px-[34px]">
      <button type="button" onClick={onMenu} aria-label="Expandir o contraer menú" className={`${iconBase} rounded-md max-sm:hidden`}>
        <Icon name="menu" />
      </button>

      <form role="search" onSubmit={handleSearch} className="group relative flex h-[42px] flex-1 items-center gap-2.5 pr-1 pl-0.5 text-muted sm:ml-1 sm:max-w-[430px] sm:flex-[0_1_430px]">
        <Icon name="search" className="size-[17px]" />
        <input
          type="search"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Buscar solicitud, código o servicio"
          aria-label="Buscar"
          className="h-full min-w-0 flex-1 bg-transparent text-[13px] tracking-[.005em] text-ink outline-none placeholder:text-muted/80 max-sm:text-base"
        />
        <span
          aria-hidden
          className="absolute inset-x-0 bottom-[5px] h-px origin-left bg-line transition-[background-color,transform] duration-150 group-focus-within:scale-y-200 group-focus-within:bg-primary"
        />
      </form>

      <span className="flex-1" />

      <Link to="/notificaciones" aria-label="Notificaciones" className={iconButton}>
        <Icon name="bell" />
      </Link>
      <button type="button" onClick={onLogout} aria-label="Cerrar sesión" className={iconButton}>
        <Icon name="logout" />
      </button>
      <Link
        to="/perfil"
        aria-label="Mi perfil"
        className="ml-1 grid size-9 flex-none place-items-center rounded-full border border-line text-xs font-semibold text-primary max-sm:hidden"
      >
        {iniciales(usuario.nombres)}
      </Link>
    </header>
  )
}
