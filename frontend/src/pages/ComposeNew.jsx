import { useEffect, useMemo, useReducer } from 'react'

/** 1) 상태 모델 */
const initialService = (name = 'web') => ({
  name,
  image: 'node:22-alpine',
  buildContext: '',
  portsText: '5173:5173',
  env: [{ key: '', value: '' }],
  volumesText: '',
  commandText: '',
  entrypointText: '',
  dependsOn: [],
  restart: '',
  collapsed: true, // ✅ 기본은 "이름만 보이는" 접힘 상태
})

const initialState = {
  version: '3.9',
  services: [initialService('web')],
}

function reducer(state, action) {
  switch (action.type) {
    case 'replace':
        return action.value
    case 'set':
      return { ...state, [action.key]: action.value }

    // ── 서비스 단일 필드/추가/삭제/이름 변경 ───────────────────────────────
    case 'svc.set': {
      const next = [...state.services]
      next[action.index] = { ...next[action.index], [action.key]: action.value }
      return { ...state, services: next }
    }
    case 'svc.add':
      return { ...state, services: [...state.services, initialService(`svc${state.services.length + 1}`)] }
    case 'svc.del': {
      const next = state.services.filter((_, i) => i !== action.index)
      const removed = state.services[action.index]?.name
      const cleaned = next.map(s => ({ ...s, dependsOn: s.dependsOn.filter(d => d !== removed) }))
      return { ...state, services: cleaned.length ? cleaned : [initialService('web')] }
    }
    case 'svc.rename': {
      const { index, value } = action
      const prevName = state.services[index].name
      const next = state.services.map((s, i) => {
        if (i === index) return { ...s, name: value }
        return { ...s, dependsOn: s.dependsOn.map(d => (d === prevName ? value : d)) }
      })
      return { ...state, services: next }
    }

    // ✅ 접기/펼치기
    case 'svc.toggleCollapsed': {
      const next = [...state.services]
      next[action.index] = { ...next[action.index], collapsed: !next[action.index].collapsed }
      return { ...state, services: next }
    }
    case 'svc.setAllCollapsed': {
      return {
        ...state,
        services: state.services.map(s => ({ ...s, collapsed: action.value })),
      }
    }

    // ── ENV 편집 ───────────────────────────────────────────────────────────
    case 'svc.env.set': {
      const list = [...state.services]
      const svc = { ...list[action.index] }
      const env = [...svc.env]
      env[action.envIndex] = { ...env[action.envIndex], [action.k]: action.v }
      svc.env = env
      list[action.index] = svc
      return { ...state, services: list }
    }
    case 'svc.env.add': {
      const list = [...state.services]
      const svc = { ...list[action.index] }
      svc.env = [...svc.env, { key: '', value: '' }]
      list[action.index] = svc
      return { ...state, services: list }
    }
    case 'svc.env.del': {
      const list = [...state.services]
      const svc = { ...list[action.index] }
      svc.env = svc.env.filter((_, i) => i !== action.envIndex)
      if (!svc.env.length) svc.env = [{ key: '', value: '' }]
      list[action.index] = svc
      return { ...state, services: list }
    }

    // ── depends_on 토글 ───────────────────────────────────────────────────
    case 'svc.dep.toggle': {
      const list = [...state.services]
      const svc = { ...list[action.index] }
      const set = new Set(svc.dependsOn)
      set.has(action.name) ? set.delete(action.name) : set.add(action.name)
      svc.dependsOn = Array.from(set)
      list[action.index] = svc
      return { ...state, services: list }
    }

    default:
      return state
  }
}

/** 2) helpers */
const splitList = s =>
  (s || '')
    .split(/\r?\n|,/)
    .map(t => t.trim())
    .filter(Boolean)

function parseCmdOrArray(text) {
  if (!text) return undefined
  try {
    const arr = JSON.parse(text)
    if (Array.isArray(arr) && arr.every(x => typeof x === 'string')) return arr
  } catch {}
  const parts = text.match(/\\".*?\\"|'[^']*'|[^\s]+/g) ?? []
  return parts.map(s => s.replace(/^"|"$/g, '').replace(/^'|'$/g, ''))
}
function envListToObject(list) {
  const out = {}
  for (const { key, value } of list || []) if (key) out[key] = value ?? ''
  return out
}

/** ── YAML 직렬화(심플) ─────────────────────────────── */
function formatScalar(v) {
  if (typeof v === 'number' || typeof v === 'boolean') return String(v)
  if (v == null) return '""'
  return JSON.stringify(String(v))
}
function toYAML(value, indent = '') {
  const I = '  '
  if (Array.isArray(value)) {
    if (!value.length) return indent + '[]'
    return value
      .map(item => {
        if (typeof item === 'object' && item !== null) {
          const head = indent + '-'
          const body = toYAML(item, indent + I)
          return head + (body.startsWith('\n') ? '' : ' ') + (typeof item === 'object' ? '\n' + body : body)
        }
        return indent + '- ' + formatScalar(item)
      })
      .join('\n')
  } else if (typeof value === 'object' && value !== null) {
    const keys = Object.keys(value)
    if (!keys.length) return indent + '{}'
    return keys
      .map(k => {
        const v = value[k]
        if (typeof v === 'object' && v !== null) {
          return `${indent}${k}:\n${toYAML(v, indent + I)}`
        }
        return `${indent}${k}: ${formatScalar(v)}`
      })
      .join('\n')
  } else {
    return indent + formatScalar(value)
  }
}

/** 3) Compose 객체 → YAML */
function buildComposeObject(state) {
  const obj = { version: state.version, services: {} }
  for (const svc of state.services) {
    const s = {}
    if (svc.buildContext) s.build = { context: svc.buildContext }
    if (svc.image && !s.build) s.image = svc.image
    const env = envListToObject(svc.env)
    if (Object.keys(env).length) s.environment = env
    const ports = splitList(svc.portsText)
    if (ports.length) s.ports = ports
    const vols = splitList(svc.volumesText)
    if (vols.length) s.volumes = vols
    const cmd = parseCmdOrArray(svc.commandText)
    if (cmd?.length) s.command = cmd
    const ep = parseCmdOrArray(svc.entrypointText)
    if (ep?.length) s.entrypoint = ep
    if (svc.dependsOn?.length) s.depends_on = svc.dependsOn
    if (svc.restart) s.restart = svc.restart
    obj.services[svc.name || 'service'] = s
  }
  return obj
}
function buildComposeYAML(state) {
  return toYAML(buildComposeObject(state))
}

/** 4) 컴포넌트 */
export default function ComposeNew() {
  const [state, dispatch] = useReducer(reducer, initialState)
  const yaml = useMemo(() => buildComposeYAML(state), [state])

  useEffect(() => {
    const raw = localStorage.getItem('composeStatePatch')
    if (raw) {
      try { dispatch({ type: 'replace', value: JSON.parse(raw) }) }
      finally { localStorage.removeItem('composeStatePatch') }
    }
  }, [])

  return (
    <div className="grid gap-y-6 lg:grid-cols-2 lg:gap-x-10">
      {/* 좌측: 서비스 카드들 */}
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold">Compose 만들기</h1>
          <div className="flex gap-2">
            <button className="btn" onClick={() => dispatch({ type: 'svc.setAllCollapsed', value: true })}>모두 접기</button>
            <button className="btn" onClick={() => dispatch({ type: 'svc.setAllCollapsed', value: false })}>모두 펼치기</button>
            <button className="btn" onClick={() => dispatch({ type: 'svc.add' })}>+ 서비스 추가</button>
          </div>
        </div>

        <div className="grid gap-4">
          {state.services.map((svc, i) => (
            <article key={i} className="card p-4 space-y-3">
              {/* 헤더 줄: 토글 + 이름 + 삭제 */}
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  className="btn px-2 py-1"
                  onClick={() => dispatch({ type: 'svc.toggleCollapsed', index: i })}
                  aria-label="접기/펼치기"
                  title="접기/펼치기"
                >
                  {svc.collapsed ? '▸' : '▾'}
                </button>

                <input
                  className="input"
                  value={svc.name}
                  onChange={e => dispatch({ type: 'svc.rename', index: i, value: e.target.value.trim() })}
                  placeholder="service name (예: web, api)"
                />

                <button className="btn" onClick={() => dispatch({ type: 'svc.del', index: i })}>삭제</button>
              </div>

              {/* ✅ 접힘 상태면 여기서 종료 */}
              {svc.collapsed ? null : (
                <div className="grid sm:grid-cols-2 gap-3">
                  <label className="label">
                    <span>Image</span>
                    <input
                      className="input"
                      value={svc.image}
                      onChange={e => dispatch({ type: 'svc.set', index: i, key: 'image', value: e.target.value })}
                      placeholder="nginx:alpine 또는 비워두고 Build 사용"
                    />
                  </label>

                  <label className="label">
                    <span>Build context</span>
                    <input
                      className="input"
                      value={svc.buildContext}
                      onChange={e => dispatch({ type: 'svc.set', index: i, key: 'buildContext', value: e.target.value })}
                      placeholder="./app (설정 시 image는 무시)"
                    />
                  </label>

                  <label className="label sm:col-span-2">
                    <span>Ports (줄/쉼표 구분)</span>
                    <textarea
                      className="input h-20"
                      value={svc.portsText}
                      onChange={e => dispatch({ type: 'svc.set', index: i, key: 'portsText', value: e.target.value })}
                      placeholder={'5173:5173\n8080:8080'}
                    />
                  </label>

                  <label className="label sm:col-span-2">
                    <span>Volumes (줄/쉼표 구분)</span>
                    <textarea
                      className="input h-20"
                      value={svc.volumesText}
                      onChange={e => dispatch({ type: 'svc.set', index: i, key: 'volumesText', value: e.target.value })}
                      placeholder={'./frontend:/app\nfrontend-node-modules:/app/node_modules'}
                    />
                  </label>

                  <label className="label">
                    <span>Command</span>
                    <input
                      className="input"
                      value={svc.commandText}
                      onChange={e => dispatch({ type: 'svc.set', index: i, key: 'commandText', value: e.target.value })}
                      placeholder='npm run dev 또는 ["npm","run","dev"]'
                    />
                  </label>

                  <label className="label">
                    <span>Entrypoint</span>
                    <input
                      className="input"
                      value={svc.entrypointText}
                      onChange={e => dispatch({ type: 'svc.set', index: i, key: 'entrypointText', value: e.target.value })}
                      placeholder='["/entrypoint.sh"] 또는 비우기'
                    />
                  </label>

                  <label className="label">
                    <span>Restart</span>
                    <select
                      className="select"
                      value={svc.restart}
                      onChange={e => dispatch({ type: 'svc.set', index: i, key: 'restart', value: e.target.value })}
                    >
                      <option value="">(none)</option>
                      <option value="always">always</option>
                      <option value="on-failure">on-failure</option>
                      <option value="unless-stopped">unless-stopped</option>
                    </select>
                  </label>

                  {/* depends_on */}
                  <div className="sm:col-span-2">
                    <span className="block text-sm mb-1">depends_on</span>
                    <div className="flex flex-wrap gap-3">
                      {state.services
                        .filter((_, j) => j !== i)
                        .map((other, j) => (
                          <label key={j} className="inline-flex items-center gap-2 text-sm">
                            <input
                              type="checkbox"
                              checked={svc.dependsOn.includes(other.name)}
                              onChange={() => dispatch({ type: 'svc.dep.toggle', index: i, name: other.name })}
                            />
                            {other.name}
                          </label>
                        ))}
                      {state.services.length <= 1 && (
                        <span className="text-xs text-zinc-500">다른 서비스가 없어요.</span>
                      )}
                    </div>
                  </div>

                  {/* ENV (key/value) */}
                  <div className="sm:col-span-2 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm">Environment</span>
                      <button className="btn" type="button" onClick={() => dispatch({ type: 'svc.env.add', index: i })}>
                        + 추가
                      </button>
                    </div>
                    <div className="grid gap-2">
                      {svc.env.map((kv, k) => (
                        <div key={k} className="grid grid-cols-[1fr_1fr_auto] gap-2">
                          <input
                            className="input"
                            placeholder="KEY"
                            value={kv.key}
                            onChange={e => dispatch({ type: 'svc.env.set', index: i, envIndex: k, k: 'key', v: e.target.value })}
                          />
                          <input
                            className="input"
                            placeholder="value"
                            value={kv.value}
                            onChange={e => dispatch({ type: 'svc.env.set', index: i, envIndex: k, k: 'value', v: e.target.value })}
                          />
                          <button className="btn" type="button" onClick={() => dispatch({ type: 'svc.env.del', index: i, envIndex: k })}>
                            −
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </article>
          ))}
        </div>
      </section>

      {/* 우측: 미리보기 (sticky) */}
      <aside className="space-y-2 lg:sticky lg:self-start lg:top-16 lg:pl-8 lg:border-l lg:border-zinc-800">
        <h2 className="text-sm font-semibold text-zinc-400">미리보기</h2>
        <pre className="card p-4 overflow-auto text-sm leading-relaxed whitespace-pre max-h-[calc(100dvh-5rem)]">
{yaml}
        </pre>
        <div className="flex gap-2">
          <button className="btn" onClick={() => navigator.clipboard.writeText(yaml)}>복사</button>
          <button className="btn" onClick={() => download('docker-compose.yml', yaml)}>다운로드</button>
        </div>
      </aside>
    </div>
  )
}

/** 5) 파일 다운로드 유틸 */
function download(filename, content) {
  const blob = new Blob([content], { type: 'text/yaml;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  setTimeout(() => URL.revokeObjectURL(url), 0)
}
