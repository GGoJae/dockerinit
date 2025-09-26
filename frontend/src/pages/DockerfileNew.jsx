import { useMemo, useReducer, useState } from 'react'

/** 1) 초기 상태 – 백엔드 DTO를 의식한 형태(입력 편의는 Text 필드로) */
const initialForm = {
  // 필수/핵심
  baseImage: 'openjdk:17',
  workdir: '/app',
  exposeText: '8080',              // EXPOSE: 문자열 → List<Integer>
  cmdText: 'java -jar app.jar',    // CMD: 문자열/JSON배열 → List<String>
  entrypointText: '',              // ENTRYPOINT: 문자열/JSON배열 → List<String>

  // 파일 지시자
  copy: [{ source: '', target: '' }], // List<CopyDirective>
  add: [],

  // 환경 / 라벨 / 인자
  envMode: '',                        // Mode (백엔드 enum) – 지금은 자유 입력
  envVars: [{ key: 'SPRING_PROFILES_ACTIVE', value: 'prod' }], // Map -> 배열로 편집
  label: [{ key: 'maintainer', value: 'gojae@example.com' }],  // Map -> 배열로 편집
  args: [{ key: 'VERSION', value: '1.0.0' }],                  // Map -> 배열로 편집

  // 기타
  runText: '',                       // RUN: 줄단위 → List<String>
  user: '',                          // USER
  volumeText: '/data\n/var/log',     // VOLUME: 줄단위 → List<String>

  // Healthcheck
  useHealthcheck: false,
  healthcheckTestText: '',           // ["CMD","curl","-f","http://localhost:8080"] 또는 쉘
  healthcheckInterval: '',           // 예: 30s
  healthcheckTimeout: '',            // 예: 3s
  healthcheckRetries: '',            // 예: 3
  healthcheckStartPeriod: '',        // 예: 5s

  // 추가 산출물
  additionalFiles: []                // Set<AdditionalFile> → ["ENV","README"] 등
}

/** 2) 리듀서 */
function reducer(state, action) {
  switch (action.type) {
    case 'set': {
      return { ...state, [action.key]: action.value }
    }
    case 'arraySet': {
      const next = [...state[action.key]]
      next[action.index] = action.value
      return { ...state, [action.key]: next }
    }
    case 'arrayPush': {
      const next = [...state[action.key], action.value]
      return { ...state, [action.key]: next }
    }
    case 'arrayRemove': {
      const next = state[action.key].filter((_, i) => i !== action.index)
      return { ...state, [action.key]: next }
    }
    case 'kvSet': {
      const list = [...state[action.key]]
      list[action.index] = { ...list[action.index], [action.k]: action.v }
      return { ...state, [action.key]: list }
    }
    case 'kvAdd': {
      return { ...state, [action.key]: [...state[action.key], { key: '', value: '' }] }
    }
    case 'kvRemove': {
      return { ...state, [action.key]: state[action.key].filter((_, i) => i !== action.index) }
    }
    case 'toggleInArray': {
      const arr = new Set(state[action.key])
      if (arr.has(action.value)) arr.delete(action.value)
      else arr.add(action.value)
      return { ...state, [action.key]: Array.from(arr) }
    }
    case 'reset':
      return initialForm
    default:
      return state
  }
}

/** 3) 미리보기용 Dockerfile 렌더 */
function toDockerfileString(ui) {
  const lines = []
  if (ui.baseImage) lines.push(`FROM ${ui.baseImage}`)
  if (ui.workdir) lines.push(`WORKDIR ${ui.workdir}`)

  // COPY / ADD
  for (const c of (ui.copy || []).filter(x => x.source && x.target)) {
    lines.push(`COPY ${c.source} ${c.target}`)
  }
  for (const a of (ui.add || []).filter(x => x.source && x.target)) {
    lines.push(`ADD ${a.source} ${a.target}`)
  }

  // ARG / ENV / LABEL
  for (const { key, value } of (ui.args || []).filter(kv => kv.key)) {
    lines.push(`ARG ${key}=${value ?? ''}`)
  }
  for (const { key, value } of (ui.envVars || []).filter(kv => kv.key)) {
    lines.push(`ENV ${key}=${value ?? ''}`)
  }
  for (const { key, value } of (ui.label || []).filter(kv => kv.key)) {
    lines.push(`LABEL ${key}=${JSON.stringify(value ?? '')}`)
  }

  // EXPOSE
  const ports = splitPorts(ui.exposeText)
  if (ports.length) lines.push(`EXPOSE ${ports.join(' ')}`)

  // RUN
  const runs = splitLines(ui.runText)
  for (const r of runs) lines.push(`RUN ${r}`)

  // USER
  if (ui.user) lines.push(`USER ${ui.user}`)

  // HEALTHCHECK
  if (ui.useHealthcheck && ui.healthcheckTestText) {
    const opts = []
    if (ui.healthcheckInterval) opts.push(`--interval=${ui.healthcheckInterval}`)
    if (ui.healthcheckTimeout) opts.push(`--timeout=${ui.healthcheckTimeout}`)
    if (ui.healthcheckRetries) opts.push(`--retries=${ui.healthcheckRetries}`)
    if (ui.healthcheckStartPeriod) opts.push(`--start-period=${ui.healthcheckStartPeriod}`)
    const test = toExecArray(ui.healthcheckTestText)
    lines.push(`HEALTHCHECK ${opts.join(' ')} CMD ${JSON.stringify(test)}`.trim())
  }

  // ENTRYPOINT / CMD
  if (ui.entrypointText) lines.push(toExec('ENTRYPOINT', ui.entrypointText))
  if (ui.cmdText)        lines.push(toExec('CMD', ui.cmdText))

  // VOLUME
  const vols = splitLines(ui.volumeText)
  if (vols.length) lines.push(`VOLUME ${JSON.stringify(vols)}`) // JSON array 형태

  return lines.join('\n') + '\n'
}

/** 4) 백엔드 전송용 DTO 변환기 (필요 시 fetch에 body로 사용) */
function toRequest(ui) {
  return {
    baseImage: ui.baseImage,
    workdir: ui.workdir || undefined,

    copy: (ui.copy || []).filter(x => x.source && x.target),
    add: (ui.add || []).filter(x => x.source && x.target),

    envMode: ui.envMode || undefined,

    envVars: kvArrayToObject(ui.envVars),
    expose: splitPorts(ui.exposeText),
    cmd: toExecArray(ui.cmdText),
    run: splitLines(ui.runText),
    entrypoint: ui.entrypointText ? toExecArray(ui.entrypointText) : [],

    label: kvArrayToObject(ui.label),
    user: ui.user || undefined,

    args: kvArrayToObject(ui.args),

    healthcheck: ui.useHealthcheck && ui.healthcheckTestText
      ? {
          test: toExecArray(ui.healthcheckTestText),
          interval: ui.healthcheckInterval || undefined,
          timeout: ui.healthcheckTimeout || undefined,
          retries: ui.healthcheckRetries ? Number(ui.healthcheckRetries) : undefined,
          startPeriod: ui.healthcheckStartPeriod || undefined,
        }
      : null,

    volume: splitLines(ui.volumeText),

    additionalFiles: ui.additionalFiles || []
  }
}

/** helpers */
const splitPorts = (s) =>
  (s || '')
    .split(/[,\s]+/)
    .map(t => t.trim())
    .filter(Boolean)
    .map(n => Number(n))
    .filter(n => Number.isFinite(n) && n > 0 && n <= 65535)

const splitLines = (s) =>
  (s || '')
    .split(/\r?\n/)
    .map(t => t.trim())
    .filter(Boolean)

function toExecArray(v) {
  if (!v) return []
  try {
    const arr = JSON.parse(v)
    if (Array.isArray(arr) && arr.every(x => typeof x === 'string')) return arr
  } catch {}
  const parts = v.match(/\\".*?\\"|'[^']*'|[^\s]+/g) ?? []
  return parts.map(s => s.replace(/^"|"$/g, '').replace(/^'|'$/g, ''))
}

function toExec(kind, v) {
  return `${kind} ${JSON.stringify(toExecArray(v))}`
}

function kvArrayToObject(arr) {
  const out = {}
  for (const { key, value } of (arr || []).filter(x => x.key)) out[key] = value ?? ''
  return out
}

/** 5) 컴포넌트 */
export default function DockerfileNew() {
  const [form, dispatch] = useReducer(reducer, initialForm)
  const [showOptional, setShowOptional] = useState(false)
  const dockerfile = useMemo(() => toDockerfileString(form), [form])

  const set = (key) => (e) => dispatch({ type: 'set', key, value: e.target.value })

  const submit = async () => {
    const body = toRequest(form)
    console.log('REQUEST DTO:', body) // 필요 시 fetch('/api/dockerfile', {...})
    // const res = await fetch('/api/dockerfile', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) })
  }

  return (
    <div className="grid gap-y-6 lg:grid-cols-2 lg:gap-x-20">
      {/* 좌: 폼 */}
      <section className="space-y-5">
        <h1 className="text-2xl font-bold">Dockerfile 만들기</h1>

        {/* 필수/핵심 */}
        <div className="grid sm:grid-cols-2 gap-3">
          <label className="label">
            <span>Base image *</span>
            <input className="input" value={form.baseImage} onChange={set('baseImage')} placeholder="openjdk:17" />
          </label>
          <label className="label">
            <span>Workdir</span>
            <input className="input" value={form.workdir} onChange={set('workdir')} placeholder="/app" />
          </label>
          <label className="label">
            <span>Expose Ports</span>
            <input className="input" value={form.exposeText} onChange={set('exposeText')} placeholder="8080, 443" />
          </label>
          <label className="label">
            <span>CMD</span>
            <input className="input" value={form.cmdText} onChange={set('cmdText')} placeholder='java -jar app.jar 또는 ["java","-jar","app.jar"]' />
          </label>
          <label className="label sm:col-span-2">
            <span>ENTRYPOINT (선택)</span>
            <input className="input" value={form.entrypointText} onChange={set('entrypointText')} placeholder='node 또는 ["node","server.js"]' />
          </label>
        </div>

        {/* 토글 */}
        <div className="flex items-center gap-4">
          <label className="inline-flex items-center gap-2 text-sm">
            <input type="checkbox" checked={showOptional} onChange={(e) => setShowOptional(e.target.checked)} />
            선택 필드들 보기
          </label>
        </div>

        {/* 선택 섹션 */}
        {showOptional && (
          <div className="space-y-6">
            {/* COPY / ADD */}
            <CopyAddSection title="COPY" listKey="copy" form={form} dispatch={dispatch} />
            <CopyAddSection title="ADD" listKey="add" form={form} dispatch={dispatch} />

            {/* ENV MODE / USER */}
            <div className="grid sm:grid-cols-2 gap-3">
              <label className="label">
                <span>Env Mode (백엔드 enum)</span>
                <input className="input" value={form.envMode} onChange={set('envMode')} placeholder="dev / prod / staging ..." />
              </label>
              <label className="label">
                <span>User</span>
                <input className="input" value={form.user} onChange={set('user')} placeholder="root" />
              </label>
            </div>

            {/* ENV / LABEL / ARG */}
            <KVSection title="ENV Vars" listKey="envVars" form={form} dispatch={dispatch} keyPlaceholder="NAME" valPlaceholder="value" />
            <KVSection title="LABEL" listKey="label" form={form} dispatch={dispatch} keyPlaceholder="key" valPlaceholder="value" />
            <KVSection title="ARG" listKey="args" form={form} dispatch={dispatch} keyPlaceholder="NAME" valPlaceholder="value" />

            {/* RUN / VOLUME */}
            <div className="grid sm:grid-cols-2 gap-3">
              <label className="label">
                <span>RUN (줄단위)</span>
                <textarea className="input h-28" value={form.runText} onChange={set('runText')} placeholder={"apt-get update\napt-get install -y curl"} />
              </label>
              <label className="label">
                <span>VOLUME (절대경로, 줄단위)</span>
                <textarea className="input h-28" value={form.volumeText} onChange={set('volumeText')} placeholder={"/data\n/var/log"} />
              </label>
            </div>

            {/* HEALTHCHECK */}
            <div className="space-y-3">
              <label className="inline-flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={form.useHealthcheck}
                  onChange={(e) => dispatch({ type: 'set', key: 'useHealthcheck', value: e.target.checked })}
                />
                Healthcheck 사용
              </label>

              {form.useHealthcheck && (
                <div className="grid sm:grid-cols-2 gap-3">
                  <label className="label">
                    <span>Test (CMD 배열 또는 문자열)</span>
                    <input className="input" value={form.healthcheckTestText} onChange={set('healthcheckTestText')}
                           placeholder='["CMD","curl","-f","http://localhost:8080"]' />
                  </label>
                  <label className="label">
                    <span>Interval</span>
                    <input className="input" value={form.healthcheckInterval} onChange={set('healthcheckInterval')} placeholder="30s" />
                  </label>
                  <label className="label">
                    <span>Timeout</span>
                    <input className="input" value={form.healthcheckTimeout} onChange={set('healthcheckTimeout')} placeholder="3s" />
                  </label>
                  <label className="label">
                    <span>Retries</span>
                    <input className="input" value={form.healthcheckRetries} onChange={set('healthcheckRetries')} placeholder="3" />
                  </label>
                  <label className="label">
                    <span>Start Period</span>
                    <input className="input" value={form.healthcheckStartPeriod} onChange={set('healthcheckStartPeriod')} placeholder="5s" />
                  </label>
                </div>
              )}
            </div>

            {/* Additional Files */}
            <div className="space-y-2">
              <h3 className="text-sm font-semibold text-zinc-300">추가 산출물</h3>
              <div className="flex flex-wrap gap-3">
                {['ENV','README'].map(name => (
                  <label key={name} className="inline-flex items-center gap-2 text-sm">
                    <input
                      type="checkbox"
                      checked={form.additionalFiles.includes(name)}
                      onChange={() => dispatch({ type: 'toggleInArray', key: 'additionalFiles', value: name })}
                    />
                    {name}
                  </label>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* 액션 */}
        <div className="flex gap-2">
          
          <button className="btn" onClick={() => download('Dockerfile', dockerfile)}>Dockerfile 다운로드</button>
          <button className="btn" onClick={submit}>요청 DTO 콘솔 출력</button>
          <button className="btn" onClick={() => dispatch({ type: 'reset' })}>초기화</button>
        </div>
      </section>

      {/* 우: 미리보기 */}
      <aside className="space-y-2">
        <h2 className="text-sm font-semibold text-zinc-400">미리보기</h2>
        <pre className="card p-4 overflow-auto text-sm leading-relaxed whitespace-pre-wrap">
{dockerfile}
        </pre>
        <button className="btn" onClick={() => navigator.clipboard.writeText(dockerfile)}>미리보기 복사</button>
      </aside>
    </div>
  )
}

/** 6) 작은 서브 컴포넌트들 */
function CopyAddSection({ title, listKey, form, dispatch }) {
  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold text-zinc-300">{title}</h2>
        <button type="button" className="btn" onClick={() => dispatch({ type: 'arrayPush', key: listKey, value: { source: '', target: '' } })}>
          + 행 추가
        </button>
      </div>
      <div className="grid gap-2">
        {form[listKey].map((row, i) => (
          <div key={i} className="grid grid-cols-[1fr_1fr_auto] gap-2">
            <input
              className="input"
              placeholder="source (예: ./build/)"
              value={row.source}
              onChange={(e) => dispatch({ type: 'arraySet', key: listKey, index: i, value: { ...row, source: e.target.value } })}
            />
            <input
              className="input"
              placeholder="target (예: /app/)"
              value={row.target}
              onChange={(e) => dispatch({ type: 'arraySet', key: listKey, index: i, value: { ...row, target: e.target.value } })}
            />
            <button type="button" className="btn" onClick={() => dispatch({ type: 'arrayRemove', key: listKey, index: i })}>−</button>
          </div>
        ))}
      </div>
    </div>
  )
}

function KVSection({ title, listKey, form, dispatch, keyPlaceholder, valPlaceholder }) {
  const list = form[listKey]
  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold text-zinc-300">{title}</h2>
        <button type="button" className="btn" onClick={() => dispatch({ type: 'kvAdd', key: listKey })}>+ 추가</button>
      </div>
      <div className="grid gap-2">
        {list.map((kv, i) => (
          <div key={i} className="grid grid-cols-[1fr_1fr_auto] gap-2">
            <input
              className="input"
              placeholder={keyPlaceholder}
              value={kv.key}
              onChange={(e) => dispatch({ type: 'kvSet', key: listKey, index: i, k: 'key', v: e.target.value })}
            />
            <input
              className="input"
              placeholder={valPlaceholder}
              value={kv.value}
              onChange={(e) => dispatch({ type: 'kvSet', key: listKey, index: i, k: 'value', v: e.target.value })}
            />
            <button type="button" className="btn" onClick={() => dispatch({ type: 'kvRemove', key: listKey, index: i })}>−</button>
          </div>
        ))}
      </div>
    </div>
  )
}

/** 7) 다운로드 유틸 */
function download(filename, content) {
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  setTimeout(() => URL.revokeObjectURL(url), 0)
}
