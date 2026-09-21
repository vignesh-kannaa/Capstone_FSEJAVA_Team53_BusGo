import type { ApiErrorBody } from '../types/api'

const BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? ''

export const TOKEN_KEY = 'busgo.token'

/** Error thrown for every failed API call. `code` is the backend's `error` field (e.g. VALIDATION_ERROR). */
export class ApiError extends Error {
  readonly status: number
  readonly code: string

  constructor(status: number, code: string, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

let unauthorizedHandler: (() => void) | null = null

/** AuthProvider registers a callback here so an expired/revoked token logs the user out everywhere. */
export function setUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler
}

export function getStoredToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function storeToken(token: string | null): void {
  try {
    if (token === null) {
      localStorage.removeItem(TOKEN_KEY)
    } else {
      localStorage.setItem(TOKEN_KEY, token)
    }
  } catch {
    // Storage can be unavailable (private mode); the session then simply lasts until reload.
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  /** Attach the stored JWT (default true). Pass false for login/register. */
  auth?: boolean
  signal?: AbortSignal
}

/**
 * Small fetch wrapper used by every feature (search, seats, bookings, admin...).
 * Adds the Bearer token, parses JSON and converts failures into ApiError.
 */
export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, auth = true, signal } = options

  const headers: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  const token = getStoredToken()
  if (auth && token) {
    headers.Authorization = `Bearer ${token}`
  }

  let response: Response
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw error
    }
    throw new ApiError(0, 'NETWORK_ERROR', 'Cannot reach the server. Check your connection and try again.')
  }

  if (response.status === 204) {
    return undefined as T
  }

  const data: unknown = await response.json().catch(() => null)

  if (!response.ok) {
    const errorBody = (data ?? {}) as Partial<ApiErrorBody>
    if (response.status === 401 && auth && token) {
      unauthorizedHandler?.()
    }
    throw new ApiError(
      response.status,
      errorBody.error ?? 'UNKNOWN_ERROR',
      errorBody.message ?? `Request failed (${response.status})`,
    )
  }

  return data as T
}
