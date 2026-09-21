import type { ReactNode } from 'react'

interface AuthTicketProps {
  title: string
  subtitle: string
  children: ReactNode
}

/** The ticket-shaped card used by the login and register pages: a route stub on the left, the form on the right. */
export function AuthTicket({ title, subtitle, children }: AuthTicketProps) {
  return (
    <div className="ticket">
      <aside className="ticket-stub" aria-hidden="true">
        <span className="stub-label">Route</span>
        <div className="stub-route">
          <span className="stub-city">Hyderabad</span>
          <span className="stub-line" />
          <span className="stub-city">Bangalore</span>
        </div>
        <span className="stub-seat">Seat 2A</span>
      </aside>
      <section className="ticket-body">
        <h1>{title}</h1>
        <p className="ticket-subtitle">{subtitle}</p>
        {children}
      </section>
    </div>
  )
}
