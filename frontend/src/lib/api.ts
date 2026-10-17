import { getToken } from './session'

const BASE_URL = import.meta.env.VITE_API_URL ?? ''

let onUnauthorized: (() => void) | null = null

/** Se invoca cuando una petición con token recibe 401 (token expirado o revocado). */
export function setUnauthorizedHandler(handler: (() => void) | null): void {
  onUnauthorized = handler
}

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

type ApiOptions = Omit<RequestInit, 'body' | 'headers'> & {
  body?: unknown
  headers?: Record<string, string>
}

/** `body` se envía como JSON, salvo `FormData`, que el navegador codifica como multipart. */
export async function apiFetch<T>(path: string, { body, headers, ...init }: ApiOptions = {}): Promise<T> {
  const token = getToken()
  const isForm = body instanceof FormData
  let response: Response
  try {
    response = await fetch(BASE_URL + path, {
      ...init,
      headers: {
        Accept: 'application/json',
        ...(body !== undefined && !isForm && { 'Content-Type': 'application/json' }),
        ...(token && { Authorization: `Bearer ${token}` }),
        ...headers,
      },
      body: body === undefined ? undefined : isForm ? body : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(0, 'No se pudo conectar con el servidor. Verifica tu conexión e inténtalo nuevamente.')
  }

  if (response.status === 204) return undefined as T

  const data = await response.json().catch(() => null)
  if (response.status === 401 && token) onUnauthorized?.()
  if (!response.ok) {
    throw new ApiError(
      response.status,
      data?.message ?? 'Ocurrió un error inesperado. Inténtalo nuevamente.',
      data?.fieldErrors ?? {},
    )
  }
  return data as T
}
