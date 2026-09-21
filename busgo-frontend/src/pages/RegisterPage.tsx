import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { ApiError } from '../api/client'
import { FormField } from '../components/FormField'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import {
  PASSWORD_MIN_LENGTH,
  validateEmail,
  validateName,
  validateNewPassword,
  validatePasswordMatch,
} from '../utils/validation'
import { AuthTicket } from './AuthTicket'

interface FieldErrors {
  name?: string | null
  email?: string | null
  password?: string | null
  confirmPassword?: string | null
}

export default function RegisterPage() {
  const { register, isAuthenticated } = useAuth()
  const { showToast } = useToast()
  const location = useLocation()
  const redirectTo = (location.state as { from?: string } | null)?.from ?? '/'

  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [errors, setErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) {
    return <Navigate to={redirectTo} replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const nextErrors: FieldErrors = {
      name: validateName(name),
      email: validateEmail(email),
      password: validateNewPassword(password),
      confirmPassword: validatePasswordMatch(password, confirmPassword),
    }
    setErrors(nextErrors)
    setFormError(null)
    if (Object.values(nextErrors).some(Boolean)) return

    setSubmitting(true)
    try {
      await register({ name: name.trim(), email: email.trim(), password })
      showToast('Your account is ready. You are logged in.', 'success')
    } catch (error) {
      if (error instanceof ApiError && error.code === 'EMAIL_ALREADY_EXISTS') {
        setErrors({ email: 'An account with this email already exists. Try logging in instead.' })
      } else {
        setFormError(error instanceof ApiError ? error.message : 'Could not create your account. Please try again.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthTicket title="Create your account" subtitle="One account for searching, booking and cancelling trips.">
      <form onSubmit={handleSubmit} noValidate>
        {formError && (
          <p className="form-error" role="alert">
            {formError}
          </p>
        )}
        <FormField
          label="Full name"
          autoComplete="name"
          value={name}
          onChange={(event) => setName(event.target.value)}
          error={errors.name}
        />
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
          autoComplete="new-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          hint={`At least ${PASSWORD_MIN_LENGTH} characters.`}
          error={errors.password}
        />
        <FormField
          label="Confirm password"
          type="password"
          autoComplete="new-password"
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          error={errors.confirmPassword}
        />
        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? 'Creating account…' : 'Create account'}
        </button>
      </form>
      <p className="auth-switch">
        Already have an account? <Link to="/login" state={location.state}>Log in</Link>
      </p>
    </AuthTicket>
  )
}
