import { useEffect, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router'
import { useAuth } from '../auth/useAuth'
import { BottomNav } from './BottomNav'
import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'

const DESKTOP = '(min-width: 1024px)'

export function AppShell() {
  const { usuario, logout } = useAuth()
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const [collapsed, setCollapsed] = useState(false)
  const [open, setOpen] = useState(false)
  const [lastPath, setLastPath] = useState(pathname)

  if (pathname !== lastPath) {
    setLastPath(pathname)
    setOpen(false)
  }

  useEffect(() => {
    if (!open) return
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && setOpen(false)
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [open])

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname])

  if (!usuario) return null

  const toggleMenu = () => {
    if (window.matchMedia(DESKTOP).matches) setCollapsed((c) => !c)
    else setOpen((o) => !o)
  }

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <>
      <a
        href="#contenido"
        className="fixed -top-16 left-2 z-[100] rounded-md bg-ink px-3.5 py-2.5 font-semibold text-canvas focus:top-2"
      >
        Saltar al contenido
      </a>

      <Sidebar usuario={usuario} collapsed={collapsed} open={open} />
      {open && (
        <div aria-hidden onClick={() => setOpen(false)} className="fixed inset-0 z-[25] bg-ink/35 lg:hidden" />
      )}

      <div className={`transition-[margin] duration-200 sm:ml-[76px] ${collapsed ? '' : 'lg:ml-[264px]'}`}>
        <Topbar usuario={usuario} onMenu={toggleMenu} onLogout={handleLogout} />
        <main id="contenido" className="mx-auto max-w-[1220px] px-4 pt-6 pb-[100px] sm:px-6 sm:pb-16 lg:px-9 lg:pt-[38px]">
          <Outlet />
        </main>
      </div>

      <BottomNav rol={usuario.rol} />
    </>
  )
}
