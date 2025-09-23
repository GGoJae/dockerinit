import { useMemo, useState } from 'react'

export default function DockerfileNew() {
  const [base, setBase] = useState('node:22-alpine')
  const [workdir, setWorkdir] = useState('/app')
  const [expose, setExpose] = useState('5173')          // "5173, 8080" 같은 문자열
  const [cmd, setCmd] = useState('npm run dev')
  const [entrypoint, setEntrypoint] = useState('')

  const [showOptional, setShowOptional] = useState(false)

  const dockerfile = useMemo(() => {
    const lines = []
    if (base) lines.push(`FROM ${base}`)
    if (workdir) lines.push(`WORKDIR ${workdir}`)

    const ports = expose.split(/[\s,]+/).map(s => s.trim()).filter(Boolean)
    if (ports.length) lines.push(`EXPOSE ${ports.join(' ')}`)

    if (entrypoint) lines.push(toExec('ENTRYPOINT', entrypoint))
    if (cmd) lines.push(toExec('CMD', cmd))
    return lines.join('\n') + '\n'
  }, [base, workdir, expose, cmd, entrypoint])

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      {/* 왼쪽: 폼 */}
      <section className="space-y-4">
        <h1 className="text-2xl font-bold">Dockerfile 만들기</h1>

        <div className="grid sm:grid-cols-2 gap-3">
          <label className="label">
            <span>Base image</span>
            <input className="input" value={base} onChange={e => setBase(e.target.value)} placeholder="node:22-alpine" />
          </label>
          <label className="label">
            <span>Workdir</span>
            <input className="input" value={workdir} onChange={e => setWorkdir(e.target.value)} placeholder="/app" />
          </label>
          <label className="label">
            <span>Expose Ports</span>
            <input className="input" value={expose} onChange={e => setExpose(e.target.value)} placeholder="5173, 8080" />
          </label>
          <label className="label">
            <span>CMD</span>
            <input className="input" value={cmd} onChange={e => setCmd(e.target.value)} placeholder='npm run dev 또는 ["npm","run","dev"]' />
          </label>
          <label className="label sm:col-span-2">
            <span>ENTRYPOINT (선택)</span>
            <input className="input" value={entrypoint} onChange={e => setEntrypoint(e.target.value)} placeholder='node 또는 ["node","server.js"]' />
          </label>
        </div>

        <div className="flex items-center gap-4">
          <label className="inline-flex items-center gap-2 text-sm">
            <input type="checkbox" checked={showOptional} onChange={e => setShowOptional(e.target.checked)} />
            추가 필드 보기 (ENV/ARG/LABEL은 다음 단계에서)
          </label>
        </div>

        <div className="flex gap-2">
          <button className="btn" onClick={() => navigator.clipboard.writeText(dockerfile)}>복사</button>
          <button className="btn" onClick={() => download('Dockerfile', dockerfile)}>다운로드</button>
          <button className="btn" onClick={() => { setBase(''); setWorkdir(''); setExpose(''); setCmd(''); setEntrypoint(''); }}>초기화</button>
        </div>
      </section>

      {/* 오른쪽: 미리보기 */}
      <aside className="space-y-2">
        <h2 className="text-sm font-semibold text-zinc-400">미리보기</h2>
        <pre className="card p-4 overflow-auto text-sm leading-relaxed whitespace-pre-wrap">
{dockerfile}
        </pre>
      </aside>
    </div>
  )
}

function toExec(kind, v) {
  // 사용자가 ["node","server.js"] 같이 입력하면 그대로, 아니면 공백 split
  try {
    const arr = JSON.parse(v)
    if (Array.isArray(arr)) return `${kind} ${JSON.stringify(arr)}`
  } catch {}
  const parts = v.match(/\\".*?\\"|'[^']*'|[^\s]+/g) ?? []
  const cleaned = parts.map(s => s.replace(/^"|"$/g, '').replace(/^'|'$/g, ''))
  return `${kind} ${JSON.stringify(cleaned)}`
}

function download(filename, content) {
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  setTimeout(() => URL.revokeObjectURL(url), 0)
}
