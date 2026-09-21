import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { ApiError } from '../api/client'
import { FormField } from '../components/FormField'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { validateEmail, validateLoginPassword } from '../utils/validation'
import { AuthTicket } from './AuthTicket'

interface FieldErrors {
  email?: string | null
  password?: string | null
}

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const { showToast } = useToast()
  const location = useLocation()
  const redirectTo = (location.state as { from?: string } | null)?.from ?? '/'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) {
    return <Navigate to={redirectTo} replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const nextErrors: FieldErrors = {
      email: validateEmail(email),
      password: validateLoginPassword(password),
    }
    setErrors(nextErrors)
    setFormError(null)
    if (nextErrors.email || nextErrors.password) return

    setSubmitting(true)
    try {
      await login({ email: email.trim(), password })
      showToast('Welcome back! You are logged in.', 'success')
    } catch (error) {
      setFormError(error instanceof ApiError ? error.message : 'Could not log in. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthTicket title="Log in" subtitle="Log in to book seats and manage your trips.">
      <form onSubmit={handleSubmit} noValidate>
        {formError && (
          <p className="form-error" role="alert">
            {formError}
          </p>
        )}
        <FormField
          label="Email"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          error={errors.email}
        />
        <FormField
          label="Password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          error={errors.password}
        />
        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? 'Logging in…' : 'Log in'}
        </button>
      </form>
      <p className="auth-switch">
        New to BusGo? <Link to="/register" state={location.state}>Create an account</Link>
      </p>
    </AuthTicket>
  )
}
