import { render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { getToken, setToken } from '../../lib/session'
import { AuthProvider } from './AuthProvider'
import { useAuth } from './useAuth'

/** Muestra el estado de la sesión para poder observarlo desde la prueba. */
function EstadoDeSesion() {
  const { status, sessionExpired } = useAuth()
  return <p>{sessionExpired ? 'sesión expirada' : `sesión ${status}`}</p>
}

/** Simula un backend que responde 401 a cualquier petición. */
function backendQueResponde401() {
  const respuesta = new Response(JSON.stringify({ message: 'Debes iniciar sesión para continuar.' }), { status: 401 })
  const fetchSimulado = vi.fn().mockResolvedValue(respuesta)
  vi.stubGlobal('fetch', fetchSimulado)
  return fetchSimulado
}

afterEach(() => {
  vi.unstubAllGlobals()
})

/*
 * HU-02 · RF-07 — Token vencido: se purgan las credenciales locales.
 * Técnica: transición de estados de la sesión (cargando → expirada).
 * Recorre juntos apiFetch, el almacén del token y AuthProvider.
 */
describe('sesión con token vencido — transición de estados', () => {
  it('envía el token guardado y, ante un 401, borra el token y marca la sesión como expirada', async () => {
    setToken('token-vencido')
    const fetchSimulado = backendQueResponde401()

    render(
      <AuthProvider>
        <EstadoDeSesion />
      </AuthProvider>,
    )

    expect(await screen.findByText('sesión expirada')).toBeInTheDocument()
    expect(getToken()).toBeNull()
    expect(fetchSimulado).toHaveBeenCalledWith(
      '/api/auth/me',
      expect.objectContaining({ headers: expect.objectContaining({ Authorization: 'Bearer token-vencido' }) }),
    )
  })
})
