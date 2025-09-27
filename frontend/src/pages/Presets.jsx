import { useMemo, useState } from 'react'

/** ── 프리셋 데이터 (간단 예시) ───────────────────────────────────────── */
const DOCKERFILE_PRESETS = [
  {
    id: 'node-vite-dev',
    kind: 'dockerfile',
    name: 'Node 22 + Vite (dev)',
    desc: 'Vite 개발 서버 (0.0.0.0:5173)',
    tags: ['node', 'vite', 'dev'],
    text: [
      'FROM node:22-alpine',
      'WORKDIR /app',
      'COPY package*.json ./',
      'RUN npm ci',
      'COPY . .',
      'EXPOSE 5173',
      'CMD ["npm","run","dev","--","--host","0.0.0.0","--port","5173"]',
      ''
    ].join('\n'),
    // 대상 페이지에 적용할 폼 패치 (DockerfileNew의 form shape)
    patch: {
      baseImage: 'node:22-alpine',
      workdir: '/app',
      exposeText: '5173',
      cmdText: 'npm run dev -- --host 0.0.0.0 --port 5173',
      entrypointText: '',
      copy: [],
      add: [],
      envMode: '',
      runText: '',
      volumeText: '',
      user: ''
    }
  },
  {
    id: 'spring-boot-jar',
    kind: 'dockerfile',
    name: 'Spring Boot (JRE 17)',
    desc: '단일 JAR 실행',
    tags: ['java', 'spring'],
    text: [
      'FROM eclipse-temurin:17-jre',
      'WORKDIR /app',
      'COPY build/libs/app.jar app.jar',
      'EXPOSE 8080',
      'ENTRYPOINT ["java","-jar","app.jar"]',
      ''
    ].join('\n'),
    patch: {
      baseImage: 'eclipse-temurin:17-jre',
      workdir: '/app',
      exposeText: '8080',
      entrypointText: '["java","-jar","app.jar"]',
      cmdText: '',
      copy: [{ source: 'build/libs/app.jar', target: '/app/app.jar' }],
      add: [],
      envMode: '',
      runText: '',
      volumeText: '',
      user: ''
    }
  },
  {
    id: 'nginx-static',
    kind: 'dockerfile',
    name: 'Nginx Static',
    desc: '빌드 산출물 정적 호스팅',
    tags: ['nginx', 'static'],
    text: [
      'FROM nginx:alpine',
      'COPY ./dist /usr/share/nginx/html',
      'EXPOSE 80',
      'CMD ["nginx","-g","daemon off;"]',
      ''
    ].join('\n'),
    patch: {
      baseImage: 'nginx:alpine',
      workdir: '',
      exposeText: '80',
      cmdText: '["nginx","-g","daemon off;"]',
      entrypointText: '',
      copy: [{ source: './dist', target: '/usr/share/nginx/html' }],
      add: [],
      runText: '',
      volumeText: '',
      user: ''
    }
  }
]

const COMPOSE_PRESETS = [
  {
    id: 'vite-dev',
    kind: 'compose',
    name: 'Vite Dev Only',
    desc: '프론트 단독 개발 환경',
    tags: ['vite', 'dev', 'frontend'],
    text: [
      'version: "3.9"',
      'services:',
      '  web:',
      '    image: node:22-alpine',
      '    working_dir: /app',
      '    volumes:',
      '      - ./frontend:/app',
      '      - frontend-node-modules:/app/node_modules',
      '    command: ["sh","-lc","[ -d node_modules ] || npm ci; npm run dev -- --host 0.0.0.0 --port 5173"]',
      '    ports: ["5173:5173"]',
      ''
    ].join('\n'),
    // ComposeNew의 state shape에 맞춘 패치
    patch: {
      version: '3.9',
      services: [
        {
          name: 'web',
          image: 'node:22-alpine',
          buildContext: '',
          portsText: '5173:5173',
          env: [{ key: '', value: '' }],
          volumesText: './frontend:/app\nfrontend-node-modules:/app/node_modules',
          commandText: '["sh","-lc","[ -d node_modules ] || npm ci; npm run dev -- --host 0.0.0.0 --port 5173"]',
          entrypointText: '',
          dependsOn: [],
          restart: '',
          collapsed: false
        }
      ]
    }
  },
  {
    id: 'spring-mongo-redis',
    kind: 'compose',
    name: 'Spring + Mongo + Redis (dev)',
    desc: '백엔드 + 데이터스토어 개발 환경',
    tags: ['spring', 'mongo', 'redis', 'dev'],
    text: [
      'version: "3.9"',
      'services:',
      '  api:',
      '    image: eclipse-temurin:17-jdk',
      '    volumes:',
      '      - ./backend:/app',
      '    command: ["./gradlew","bootRun","-x","test"]',
      '    ports: ["8080:8080"]',
      '    depends_on: ["mongo","redis"]',
      '  mongo:',
      '    image: mongo:7',
      '  redis:',
      '    image: redis:7',
      ''
    ].join('\n'),
    patch: {
      version: '3.9',
      services: [
        {
          name: 'api',
          image: 'eclipse-temurin:17-jdk',
          buildContext: '',
          portsText: '8080:8080',
          env: [{ key: '', value: '' }],
          volumesText: './backend:/app',
          commandText: '["./gradlew","bootRun","-x","test"]',
          entrypointText: '',
          dependsOn: ['mongo', 'redis'],
          restart: '',
          collapsed: false
        },
        {
          name: 'mongo',
          image: 'mongo:7',
          buildContext: '',
          portsText: '',
          env: [{ key: '', value: '' }],
          volumesText: '',
          commandText: '',
          entrypointText: '',
          dependsOn: [],
          restart: '',
          collapsed: true
        },
        {
          name: 'redis',
          image: 'redis:7',
          buildContext: '',
          portsText: '',
          env: [{ key: '', value: '' }],
          volumesText: '',
          commandText: '',
          entrypointText: '',
          dependsOn: [],
          restart: '',
          collapsed: true
        }
      ]
    }
  }
]

/** ── 유틸 ─────────────────────────────────────────────────────────────── */
function download(filename, content, mime = 'text/plain;charset=utf-8') {
  const blob = new Blob([content], { type: mime })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  setTimeout(() => URL.revokeObjectURL(url), 0)
}

/** ── 컴포넌트 ────────────────────────────────────────────────────────── */
export default function Presets() {
  const [tab, setTab] = useState('dockerfile') // 'dockerfile' | 'compose'
  const [q, setQ] = useState('')
  const [selectedId, setSelectedId] = useState(null)

  const list = tab === 'dockerfile' ? DOCKERFILE_PRESETS : COMPOSE_PRESETS
  const filtered = useMemo(() => {
    const keyword = q.trim().toLowerCase()
    if (!keyword) return list
    return list.filter(p =>
      [p.name, p.desc, ...(p.tags || [])].join(' ').toLowerCase().includes(keyword)
    )
  }, [list, q])

  const selected = filtered.find(p => p.id === selectedId) || filtered[0]

  const apply = (p) => {
    if (p.kind === 'dockerfile') {
      localStorage.setItem('dockerfileFormPatch', JSON.stringify(p.patch || {}))
      window.location.href = '/dockerfile/new'
    } else {
      localStorage.setItem('composeStatePatch', JSON.stringify(p.patch || {}))
      window.location.href = '/compose/new'
    }
  }

  const copy = (p) => navigator.clipboard.writeText(p.text || '')
  const dl = (p) => {
    const name = p.kind === 'dockerfile' ? 'Dockerfile' : 'docker-compose.yml'
    const mime = p.kind === 'dockerfile' ? 'text/plain;charset=utf-8' : 'text/yaml;charset=utf-8'
    download(name, p.text || '', mime)
  }

  return (
    <div className="grid gap-y-6 lg:grid-cols-2 lg:gap-x-10">
      {/* 좌: 목록/검색/필터 */}
      <section className="space-y-4">
        <div className="flex items-center gap-2">
          <div className="inline-flex rounded-md border border-zinc-800 p-1 bg-zinc-900">
            <button
              className={`px-3 py-1.5 text-sm rounded ${tab === 'dockerfile' ? 'bg-zinc-800' : ''}`}
              onClick={() => setTab('dockerfile')}
            >
              Dockerfile
            </button>
            <button
              className={`px-3 py-1.5 text-sm rounded ${tab === 'compose' ? 'bg-zinc-800' : ''}`}
              onClick={() => setTab('compose')}
            >
              Compose
            </button>
          </div>
          <input
            className="input max-w-sm"
            placeholder="검색 (이름/태그)"
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
        </div>

        <div className="grid gap-3">
          {filtered.map(p => (
            <article key={p.id} className="card p-4">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h3 className="font-semibold">{p.name}</h3>
                  <p className="text-sm text-zinc-400">{p.desc}</p>
                  <div className="mt-2 flex flex-wrap gap-1">
                    {p.tags?.map(t => (
                      <span key={t} className="text-[11px] px-2 py-0.5 rounded-full border border-zinc-800 text-zinc-400">
                        #{t}
                      </span>
                    ))}
                  </div>
                </div>
                <div className="flex flex-wrap gap-2">
                  <button className="btn" onClick={() => setSelectedId(p.id)}>미리보기</button>
                  <button className="btn" onClick={() => copy(p)}>복사</button>
                  <button className="btn" onClick={() => dl(p)}>다운로드</button>
                  <button className="btn" onClick={() => apply(p)}>적용</button>
                </div>
              </div>
            </article>
          ))}
          {!filtered.length && (
            <div className="text-sm text-zinc-500">검색 결과가 없습니다.</div>
          )}
        </div>
      </section>

      {/* 우: 프리뷰 */}
      <aside className="space-y-2 lg:sticky lg:self-start lg:top-16 lg:pl-8 lg:border-l lg:border-zinc-800">
        <h2 className="text-sm font-semibold text-zinc-400">미리보기</h2>
        <pre className="card p-4 overflow-auto text-sm leading-relaxed whitespace-pre max-h-[calc(100dvh-5rem)]">
{selected?.text || ''}
        </pre>
        {selected && (
          <div className="flex gap-2">
            <button className="btn" onClick={() => copy(selected)}>복사</button>
            <button className="btn" onClick={() => dl(selected)}>다운로드</button>
            <button className="btn" onClick={() => apply(selected)}>적용</button>
          </div>
        )}
      </aside>
    </div>
  )
}
