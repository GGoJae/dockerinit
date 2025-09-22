import { useState } from 'react'

export default function App() {
  const [count, setCount] = useState(0)

  return (
    <main className="min-h-dvh w-full p-8 flex flex-col items-center justify-center gap-6 animate-in fade-in duration-300">
      <h1 className="text-3xl font-bold tracking-tight">UI 스타트! 🚀</h1>
      <p className="text-sm text-muted-foreground">Tailwind v4 + Vite Dev (Docker)</p>
      <button
        className="rounded-lg border px-4 py-2 text-sm hover:border-indigo-400 transition"
        onClick={() => setCount((c) => c + 1)}
      >
        count: {count}
      </button>
    </main>
  )
}
