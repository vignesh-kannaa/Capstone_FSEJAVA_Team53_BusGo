import { Outlet } from 'react-router-dom'
import { Navbar } from './Navbar'

/** Shell shared by every page: navbar on top, routed page below. */
export function AppLayout() {
  return (
    <>
      <a className="skip-link" href="#main">
        Skip to content
      </a>
      <Navbar />
      <main id="main" className="page">
        <Outlet />
      </main>
    </>
  )
}
