import { Link, useLocation } from 'react-router'
import { Icon } from '../../components/ui/Icon'
import { ROL_LABEL, iniciales } from '../../lib/domain'
import type { UsuarioActual } from '../auth/types'
import { NAVIGATION, activeItem } from './navigation'

type SidebarProps = {
  usuario: UsuarioActual
  /** Escritorio: barra contraída a solo íconos. */
  collapsed: boolean
  /** Tablet: barra desplegada sobre el contenido. */
  open: boolean
}

export function Sidebar({ usuario, collapsed, open }: SidebarProps) {
  const { pathname } = useLocation()
  const { items } = NAVIGATION[usuario.rol]
  const current = activeItem(items, pathname)

  // Las etiquetas se ven en escritorio sin contraer, o en tablet con la barra desplegada.
  const label = `${open ? 'inline' : 'hidden'} ${collapsed ? '' : 'lg:inline'}`
  const block = `${open ? 'block' : 'hidden'} ${collapsed ? '' : 'lg:block'}`
  const wide = open ? 'w-[264px] px-4' : `w-[76px] px-3 ${collapsed ? '' : 'lg:w-[264px] lg:px-4'}`
  const itemLayout = open ? 'justify-start' : `justify-center ${collapsed ? '' : 'lg:justify-start'}`

  return (
    <aside
      aria-label="Navegación principal"
      className={`fixed inset-y-0 left-0 z-30 hidden flex-col bg-wine py-[22px] text-cream transition-[width] duration-200 sm:flex ${wide}`}
    >
      <Link to={NAVIGATION[usuario.rol].items[0].to} className={`mb-[26px] flex items-center gap-3 px-1.5 ${itemLayout}`}>
        <span className="grid size-11 flex-none place-items-center overflow-hidden border border-gold/55 bg-crest">
          <img src="/escudo-unmsm.png" alt="Escudo UNMSM" className="size-11 object-cover" />
        </span>
        <span className={`leading-[1.05] ${block}`}>
          <b className="block font-serif text-[17px] text-white">San Marcos</b>
          <small className="mt-1 block text-[11px] tracking-[.13em] text-brand-gold uppercase">Servicios</small>
        </span>
      </Link>

      <nav className="flex flex-col gap-1">
        {items.map((item) => {
          const active = item === current
          return (
            <Link
              key={item.to}
              to={item.to}
              title={item.label}
              aria-current={active ? 'page' : undefined}
              className={`relative flex items-center gap-3 rounded px-[11px] py-2.5 text-[13px] transition-colors ${itemLayout} ${
                active
                  ? 'bg-gold/12 font-semibold text-gold-light shadow-[inset_2px_0_var(--color-gold)]'
                  : 'font-medium text-wine-text hover:bg-white/7 hover:text-white'
              }`}
            >
              <Icon name={item.icon} />
              <span className={label}>{item.label}</span>
            </Link>
          )
        })}
      </nav>

      <div className={`mt-auto flex items-center gap-2.5 border-t border-white/16 px-1.5 pt-3.5 ${itemLayout}`}>
        <span className="grid size-[34px] flex-none place-items-center rounded-full bg-avatar text-xs font-semibold text-primary-hover">
          {iniciales(usuario.nombres)}
        </span>
        <div className={`min-w-0 ${block}`}>
          <b className="block truncate text-[13px] text-white">{usuario.nombres}</b>
          <small className="block text-xs text-wine-muted">{ROL_LABEL[usuario.rol]}</small>
        </div>
      </div>
    </aside>
  )
}
