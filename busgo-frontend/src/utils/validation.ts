/** Client-side form checks. They mirror the backend rules (which remain the source of truth). */

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export const PASSWORD_MIN_LENGTH = 8
export const PASSWORD_MAX_LENGTH = 72

export function validateEmail(value: string): string | null {
  const email = value.trim()
  if (email === '') return 'Enter your email address.'
  if (email.length > 254 || !EMAIL_PATTERN.test(email)) return 'Enter a valid email address, like name@example.com.'
  return null
}

export function validateName(value: string): string | null {
  const name = value.trim()
  if (name === '') return 'Enter your full name.'
  if (name.length > 100) return 'Use 100 characters or fewer for your name.'
  return null
}

export function validateLoginPassword(value: string): string | null {
  return value === '' ? 'Enter your password.' : null
}

export function validateNewPassword(value: string): string | null {
  if (value === '') return 'Choose a password.'
  if (value.length < PASSWORD_MIN_LENGTH) return `Use at least ${PASSWORD_MIN_LENGTH} characters.`
  if (value.length > PASSWORD_MAX_LENGTH) return `Use ${PASSWORD_MAX_LENGTH} characters or fewer.`
  return null
}

export function validatePasswordMatch(password: string, confirmation: string): string | null {
  return password === confirmation ? null : 'The passwords do not match.'
}
