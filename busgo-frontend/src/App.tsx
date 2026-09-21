import { Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/AppLayout'
import { ProtectedRoute } from './components/ProtectedRoute'
import AdminPage from './pages/AdminPage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import MyTripsPage from './pages/MyTripsPage'
import NotFoundPage from './pages/NotFoundPage'
import RegisterPage from './pages/RegisterPage'

/**
 * Route table for the whole app. Feature owners: add your page inside the right group.
 *  - public:      anyone (search, seat map)
 *  - logged in:   needs a session, otherwise redirected to /login
 *  - admin only:  needs isAdmin, otherwise a "no access" page
 */
export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        {/* Public */}
        <Route index element={<HomePage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />

        {/* Logged-in users */}
        <Route element={<ProtectedRoute />}>
          <Route path="my-trips" element={<MyTripsPage />} />
        </Route>

        {/* Admins only */}
        <Route element={<ProtectedRoute requireAdmin />}>
          <Route path="admin/*" element={<AdminPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
