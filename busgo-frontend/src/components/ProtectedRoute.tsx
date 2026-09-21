import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import ForbiddenPage from '../pages/ForbiddenPage'

/**
 * Route guard. Wrap routes in it from App.tsx:
 *   <Route element={<ProtectedRoute />}> ... logged-in pages ... </Route>
 *   <Route element={<ProtectedRoute requireAdmin />}> ... admin pages ... </Route>
 * Logged-out visitors are sent to /login and returned to the page they wanted afterwards.
 * The backend enforces the same rules; this only keeps the UI honest.
 */
export function ProtectedRoute({ requireAdmin = false }: { requireAdmin?: boolean }) {
  const { isAuthenticated, isAdmin, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <p className="page-status">Checking your session…</p>
  }
  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  }
  if (requireAdmin && !isAdmin) {
    return <ForbiddenPage />
  }
  return <Outlet />
}
