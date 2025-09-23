import { Outlet, Link, NavLink } from 'react-router-dom'

const linkClass = ({ isActive }) =>
  `px-3 py-2 rounded-md text-sm transition
   ${isActive ? 'bg-zinc-800 text-white' : 'text-zinc-300 hover:text-white hover:bg-zinc-700'}`

export default function App() {
  return (
    <div className="min-h-dvh grid grid-rows-[auto,1fr]">
      <header className="sticky top-0 z-40 border-b border-zinc-800 bg-zinc-900/70 backdrop-blur">
        <nav className="mx-auto w-full max-w-6xl px-4 py-3 flex items-center gap-1">
          <Link to="/" className="mr-2 text-lg font-bold tracking-tight">DockerInit</Link>
          <NavLink to="/compose/new" className={linkClass}>Compose</NavLink>
          <NavLink to="/dockerfile/new" className={linkClass}>Dockerfile</NavLink>
          <NavLink to="/presets" className={linkClass}>Presets</NavLink>
          <NavLink to="/settings" className={linkClass}>Settings</NavLink>
          <span className="ml-auto text-xs text-zinc-400">v0.1.0</span>
        </nav>
      </header>
      <main className="mx-auto w-full max-w-6xl p-4">
        <Outlet />
      </main>
    </div>
  )
}
