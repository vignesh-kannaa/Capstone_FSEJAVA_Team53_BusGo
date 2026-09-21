import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { AuthContext, type AuthContextValue } from '../context/AuthContext'
import { ToastContext } from '../context/ToastContext'
import LoginPage from './LoginPage'

function renderLoginPage(login: AuthContextValue['login']) {
  const auth: AuthContextValue = {
    user: null,
    isAuthenticated: false,
    isAdmin: false,
    isLoading: false,
    login,
    register: vi.fn(),
    logout: vi.fn(),
  }
  return render(
    <MemoryRouter>
      <ToastContext.Provider value={{ showToast: vi.fn() }}>
        <AuthContext.Provider value={auth}>
          <LoginPage />
        </AuthContext.Provider>
      </ToastContext.Provider>
    </MemoryRouter>,
  )
}

describe('LoginPage', () => {
  it('shows validation messages and does not call the API for an empty form', async () => {
    const login = vi.fn()
    renderLoginPage(login)

    await userEvent.click(screen.getByRole('button', { name: 'Log in' }))

    expect(screen.getByText('Enter your email address.')).toBeInTheDocument()
    expect(screen.getByText('Enter your password.')).toBeInTheDocument()
    expect(login).not.toHaveBeenCalled()
  })

  it('submits the trimmed email and the password', async () => {
    const login = vi.fn().mockResolvedValue(undefined)
    renderLoginPage(login)

    await userEvent.type(screen.getByLabelText('Email'), '  jane@example.com ')
    await userEvent.type(screen.getByLabelText('Password'), 'Passw0rd!')
    await userEvent.click(screen.getByRole('button', { name: 'Log in' }))

    expect(login).toHaveBeenCalledWith({ email: 'jane@example.com', password: 'Passw0rd!' })
  })

  it('shows the server message when the credentials are wrong', async () => {
    const login = vi.fn().mockRejectedValue(new ApiError(401, 'INVALID_CREDENTIALS', 'Invalid email or password'))
    renderLoginPage(login)

    await userEvent.type(screen.getByLabelText('Email'), 'jane@example.com')
    await userEvent.type(screen.getByLabelText('Password'), 'wrong-password')
    await userEvent.click(screen.getByRole('button', { name: 'Log in' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid email or password')
  })
})
