import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <section className="message-page">
      <h1>We couldn't find that page</h1>
      <p>The link may be broken, or the page may have moved.</p>
      <Link to="/" className="btn btn-primary">
        Search buses
      </Link>
    </section>
  )
}
