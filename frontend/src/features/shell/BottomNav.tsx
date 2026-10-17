import { Link, useLocation } from 'react-router'
import { Icon } from '../../components/ui/Icon'
import type { Rol } from '../../lib/domain'
import { NAVIGATION, activeItem } from './navigation'

export function BottomNav({ rol }: { rol: Rol }) {
  const { pathname } = useLocation()
  const { items, bottom } = NAVIGATION[rol]
  const current = activeItem(items, pathname)
  const visible = bottom.map((to) => items.find((item) => item.to === to)!)

  return (
    <nav
      aria-label="Navegación móvil"
      style={{ gridTemplateColumns: `repeat(${visible.length}, minmax(0, 1fr))` }}
      className="fixed inset-x-0 bottom-0 z-40 grid border-t border-line bg-surface px-1 pt-1.5 pb-[calc(6px+env(safe-area-inset-bottom,0px))] sm:hidden"
    >
      {visible.map((item) => {
        const active = item === current
        return (
          <Link
            key={item.to}
            to={item.to}
            aria-current={active ? 'page' : undefined}
            className={`flex min-h-[52px] flex-col items-center justify-center gap-0.5 py-1 text-[11px] ${
              active ? 'font-semibold text-primary-hover' : 'font-medium text-muted'
            }`}
          >
            {item.fab ? (
              <span className="-mt-3.5 grid size-10 place-items-center rounded-xl bg-primary text-white">
                <Icon name={item.icon} />
              </span>
            ) : (
              <Icon name={item.icon} />
            )}
            {item.short ?? item.label}
          </Link>
        )
      })}
    </nav>
  )
}
