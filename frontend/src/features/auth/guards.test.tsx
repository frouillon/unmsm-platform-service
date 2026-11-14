import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it, vi } from 'vitest'
import type { Rol } from '../../lib/domain'
import { AuthContext, type AuthContextValue } from './authContext'
import { GuestOnly, RequireAuth } from './guards'
import type { UsuarioActual } from './types'

const usuarioCon = (rol: Rol): UsuarioActual => ({
  id: 1,
  nombres: 'Usuario de prueba',
  correo: 'prueba@unmsm.edu.pe',
  rol,
  areaId: null,
  areaNombre: null,
})

const sinSesion: Pick<AuthContextValue, 'status' | 'usuario'> = { status: 'anonymous', usuario: null }

const sesionDe = (rol: Rol): Pick<AuthContextValue, 'status' | 'usuario'> => ({
  status: 'authenticated',
  usuario: usuarioCon(rol),
})

/** Abre `ruta` en una aplicación mínima: una zona de administración, el login y los inicios por rol. */
function abrir(ruta: string, sesion: Pick<AuthContextValue, 'status' | 'usuario'>) {
  const auth: AuthContextValue = { ...sesion, sessionExpired: false, login: vi.fn(), logout: vi.fn() }
  return render(
    <AuthContext.Provider value={auth}>
      <MemoryRouter initialEntries={[ruta]}>
        <Routes>
          <Route element={<RequireAuth roles={['ADMINISTRADOR']} />}>
            <Route path="/admin" element={<p>Panel de administración</p>} />
          </Route>
          <Route element={<GuestOnly />}>
            <Route path="/login" element={<p>Formulario de inicio de sesión</p>} />
          </Route>
          <Route path="/inicio" element={<p>Inicio del estudiante</p>} />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  )
}

/*
 * HU-02 · RF-06 — Protección de rutas en el frontend.
 * Técnica: tabla de decisión (estado de la sesión × rol exigido por la ruta).
 */
describe('protección de rutas — tabla de decisión', () => {
  it('sin sesión, una ruta protegida lleva al inicio de sesión', () => {
    abrir('/admin', sinSesion)

    expect(screen.getByText('Formulario de inicio de sesión')).toBeInTheDocument()
    expect(screen.queryByText('Panel de administración')).not.toBeInTheDocument()
  })

  it('con sesión, solo el rol autorizado entra; otro rol ve «acceso denegado»', () => {
    const comoEstudiante = abrir('/admin', sesionDe('ESTUDIANTE'))

    expect(screen.getByText('No tienes acceso a esta sección')).toBeInTheDocument()
    expect(screen.queryByText('Panel de administración')).not.toBeInTheDocument()

    comoEstudiante.unmount()
    abrir('/admin', sesionDe('ADMINISTRADOR'))

    expect(screen.getByText('Panel de administración')).toBeInTheDocument()
  })

  it('con sesión, abrir el login lleva al inicio del propio rol', () => {
    abrir('/login', sesionDe('ESTUDIANTE'))

    expect(screen.getByText('Inicio del estudiante')).toBeInTheDocument()
    expect(screen.queryByText('Formulario de inicio de sesión')).not.toBeInTheDocument()
  })
})
