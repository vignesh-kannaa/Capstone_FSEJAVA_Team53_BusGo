import { Link } from 'react-router-dom'

export default function ForbiddenPage() {
  return (
    <section className="message-page">
      <h1>You don't have access to this page</h1>
      <p>Only administrators can manage buses and schedules.</p>
      <Link to="/" className="btn btn-primary">
        Back to search
      </Link>
    </section>
  )
}
