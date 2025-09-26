import { Outlet, Link, NavLink } from 'react-router-dom'

const linkClass = ({ isActive }) =>
  `px-3 py-1.5 rounded-md text-sm leading-none transition
   ${isActive ? 'bg-zinc-800 text-white' : 'text-zinc-300 hover:text-white hover:bg-zinc-700'}`

export default function App() {
  return (
    <div className="min-h-screen"> 
      <header
        className="sticky top-0 z-40 border-b border-zinc-800 bg-zinc-900/70 backdrop-blur
                   [--hdr-h:48px] md:[--hdr-h:56px] h-[var(--hdr-h)]"
      >
        <nav className="mx-auto w-full max-w-7xl h-full px-3 md:px-4 flex items-center gap-1">
          <Link to="/" className="mr-2 text-base font-bold leading-none">DockerInit</Link>
          <NavLink to="/compose/new" className={linkClass}>Compose</NavLink>
          <NavLink to="/dockerfile/new" className={linkClass}>Dockerfile</NavLink>
          <NavLink to="/presets" className={linkClass}>Presets</NavLink>
          <NavLink to="/settings" className={linkClass}>Settings</NavLink>
          <span className="ml-auto text-xs text-zinc-400">v0.1.0</span>
        </nav>
      </header>

      <main className="min-h-[calc(100dvh-var(--hdr-h))] mx-auto w-full max-w-7xl px-4 md:px-6 pt-3 pb-6">
        <Outlet />
      </main>
    </div>
  )
}
