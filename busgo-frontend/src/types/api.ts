/** Shapes of the REST contract shared with the Spring Boot backend. */

export interface User {
  id: string
  email: string
  name: string
  isAdmin: boolean
}

export interface AuthResponse {
  token: string
  user: User
}

export interface RegisterPayload {
  name: string
  email: string
  password: string
}

export interface LoginPayload {
  email: string
  password: string
}

/** Standard error body returned by every failing endpoint. */
export interface ApiErrorBody {
  timestamp: string
  path: string
  error: string
  message: string
}
