import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <section>
      <h1>404 Not Found</h1>
      <p><Link to="/compose/new">/compose/new</Link>로 이동</p>
    </section>
  )
}