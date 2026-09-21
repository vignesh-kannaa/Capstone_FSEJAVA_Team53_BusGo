import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'

export function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    showToast('You have been logged out.', 'info')
    navigate('/')
  }

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="brand" aria-label="BusGo home">
          <svg viewBox="0 0 32 32" width="28" height="28" aria-hidden="true">
            <rect width="32" height="32" rx="7" fill="#0b6b4f" />
            <rect x="6" y="8" width="20" height="13" rx="3" fill="#fff" />
            <rect x="8.5" y="10.5" width="6" height="5" rx="1" fill="#0b6b4f" />
            <rect x="17.5" y="10.5" width="6" height="5" rx="1" fill="#0b6b4f" />
            <circle cx="11" cy="23" r="2.4" fill="#f4b63f" />
            <circle cx="21" cy="23" r="2.4" fill="#f4b63f" />
          </svg>
          BusGo
        </Link>

        <nav className="nav-links" aria-label="Main">
          <NavLink to="/" end>
            Search buses
          </NavLink>
          {isAuthenticated && <NavLink to="/my-trips">My trips</NavLink>}
          {isAdmin && <NavLink to="/admin">Admin</NavLink>}
        </nav>

        <div className="nav-actions">
          {isAuthenticated ? (
            <>
              <span className="nav-user" title={user?.email}>
                {user?.name}
              </span>
              <button type="button" className="btn btn-quiet" onClick={handleLogout}>
                Log out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-quiet">
                Log in
              </Link>
              <Link to="/register" className="btn btn-primary">
                Sign up
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
