import { useEffect, useMemo, useState } from 'react'

/** ──────────────────────────────────────────────────────────────────
 *  간단 데이터셋 (원하면 별도 파일로 분리 가능)
 *  id/bin/title/category/desc/syntax/examples/flags/tips
 *  ────────────────────────────────────────────────────────────────── */
const CATS = {
  fs: '파일/디렉터리',
  search: '검색/텍스트',
  proc: '프로세스',
  net: '네트워크',
  perm: '권한',
  archive: '압축/아카이브',
  docker: 'Docker',
  sys: '시스템',
}

const CMDS = [
  {
    id: 'ls', bin: 'ls', title: '목록 보기', category: 'fs',
    desc: '디렉터리 파일 목록을 출력합니다.',
    syntax: 'ls [옵션] [경로]',
    examples: ['ls -al', 'ls -lh /var/log'],
    flags: [
      ['-a', '숨김 포함'],
      ['-l', '자세히'],
      ['-h', '사이즈 사람 친화적'],
      ['-t', '시간순 정렬'],
    ],
    tips: '보통 -alh 조합을 많이 사용합니다.',
  },
  {
    id: 'cd', bin: 'cd', title: '디렉터리 이동', category: 'fs',
    desc: '현재 작업 디렉터리를 변경합니다.',
    syntax: 'cd [경로]',
    examples: ['cd /etc', 'cd ..', 'cd -'],
    flags: [],
    tips: '`-` 는 이전 디렉터리로 이동.',
  },
  {
    id: 'mkdir', bin: 'mkdir', title: '디렉터리 생성', category: 'fs',
    desc: '새 디렉터리를 생성합니다.',
    syntax: 'mkdir [-p] 경로',
    examples: ['mkdir src/components', 'mkdir -p a/b/c'],
    flags: [['-p', '중간 경로까지 생성']],
    tips: '',
  },
  {
    id: 'rm', bin: 'rm', title: '파일/폴더 삭제', category: 'fs',
    desc: '파일 또는 디렉터리를 삭제합니다.',
    syntax: 'rm [-rf] 대상',
    examples: ['rm file', 'rm -rf node_modules'],
    flags: [['-r', '재귀 삭제'], ['-f', '강제']],
    tips: '**주의**: rm -rf 는 되돌릴 수 없습니다.',
  },
  {
    id: 'cp', bin: 'cp', title: '복사', category: 'fs',
    desc: '파일/디렉터리를 복사합니다.',
    syntax: 'cp [-r] 원본 대상',
    examples: ['cp .env.example .env', 'cp -r src dist'],
    flags: [['-r', '디렉터리 재귀 복사']],
    tips: '',
  },
  {
    id: 'mv', bin: 'mv', title: '이동/이름 변경', category: 'fs',
    desc: '파일/디렉터리를 이동하거나 이름 변경합니다.',
    syntax: 'mv 원본 대상',
    examples: ['mv old.txt new.txt', 'mv dist/* /var/www/'],
    flags: [],
    tips: '',
  },
  {
    id: 'grep', bin: 'grep', title: '문자열 검색', category: 'search',
    desc: '패턴과 일치하는 줄을 출력합니다.',
    syntax: 'grep [옵션] 패턴 [파일...]',
    examples: ['grep -R "ERROR" ./logs', 'ps aux | grep node'],
    flags: [['-R', '재귀'], ['-n', '라인 번호'], ['-i', '대소문자 무시']],
    tips: 'ripgrep(`rg`) 대체도 인기.',
  },
  {
    id: 'find', bin: 'find', title: '파일 찾기', category: 'search',
    desc: '조건에 맞는 파일을 찾습니다.',
    syntax: 'find 경로 [조건] [동작]',
    examples: ['find . -name "*.log" -mtime +7 -delete'],
    flags: [['-name', '이름'], ['-type', '파일타입'], ['-mtime', '수정일']],
    tips: '',
  },
  {
    id: 'chmod', bin: 'chmod', title: '권한 변경', category: 'perm',
    desc: '파일 권한을 변경합니다.',
    syntax: 'chmod [모드] 파일',
    examples: ['chmod +x script.sh', 'chmod 644 index.html'],
    flags: [],
    tips: 'u/g/o, r/w/x 조합 또는 755(8진수) 사용.',
  },
  {
    id: 'chown', bin: 'chown', title: '소유자 변경', category: 'perm',
    desc: '파일 소유자/그룹을 변경합니다.',
    syntax: 'chown [옵션] 사용자[:그룹] 파일',
    examples: ['sudo chown -R www-data:www-data /var/www'],
    flags: [['-R', '재귀']],
    tips: '',
  },
  {
    id: 'tar', bin: 'tar', title: '압축/해제', category: 'archive',
    desc: 'tar 아카이브를 생성/해제합니다.',
    syntax: 'tar [czxf] 파일',
    examples: ['tar czf app.tar.gz dist/', 'tar xzf app.tar.gz -C /opt'],
    flags: [['c', '생성'], ['x', '해제'], ['z', 'gzip'], ['f', '파일 지정']],
    tips: '',
  },
  {
    id: 'curl', bin: 'curl', title: 'HTTP 요청', category: 'net',
    desc: 'URL로 데이터를 전송/수신합니다.',
    syntax: 'curl [옵션] URL',
    examples: ['curl -I https://example.com', 'curl -X POST -d \'{"a":1}\' -H "Content-Type: application/json" http://localhost:8080/api'],
    flags: [['-I', '헤더만'], ['-X', '메서드'], ['-H', '헤더'], ['-d', '데이터']],
    tips: 'JSON은 -H "Content-Type: application/json" 필수.',
  },
  {
    id: 'wget', bin: 'wget', title: '다운로드', category: 'net',
    desc: '파일을 다운로드합니다.',
    syntax: 'wget [옵션] URL',
    examples: ['wget -O out.zip https://...'],
    flags: [['-O', '파일명 지정'], ['-c', '이어받기']],
    tips: '',
  },
  {
    id: 'ps', bin: 'ps', title: '프로세스 목록', category: 'proc',
    desc: '현재 실행 중인 프로세스 목록을 보여줍니다.',
    syntax: 'ps aux | grep PATTERN',
    examples: ['ps aux | grep node'],
    flags: [],
    tips: '`top`, `htop`도 참고.',
  },
  {
    id: 'docker', bin: 'docker', title: 'Docker CLI', category: 'docker',
    desc: '도커 컨테이너/이미지/볼륨 관리.',
    syntax: 'docker [명령] [옵션]',
    examples: ['docker ps', 'docker run --rm -it alpine sh'],
    flags: [],
    tips: 'docker compose는 별도 하위 명령.',
  },
  {
    id: 'docker-compose', bin: 'docker compose', title: 'Docker Compose', category: 'docker',
    desc: 'Compose로 멀티 컨테이너 앱 관리.',
    syntax: 'docker compose [명령] [옵션]',
    examples: ['docker compose up -d', 'docker compose logs -f web'],
    flags: [],
    tips: '`docker-compose` 구버전 명령도 존재.',
  },
]

/** 즐겨찾기 초기값 */
const FAV_KEY = 'cli.favorites'
const loadFavs = () => {
  try { return JSON.parse(localStorage.getItem(FAV_KEY) || '[]') } catch { return [] }
}
const saveFavs = (arr) => localStorage.setItem(FAV_KEY, JSON.stringify(arr))

/** 간단 파서: 첫 토큰(또는 첫 두 토큰이 "docker compose"면 둘) */
function parseBin(input) {
  const parts = (input || '').trim().match(/\S+/g) || []
  if (parts.length >= 2 && parts[0] === 'docker' && parts[1] === 'compose') return 'docker compose'
  return parts[0] || ''
}

function Suggestions({ q, onPick }) {
  const items = useMemo(() => {
    const s = (q || '').toLowerCase()
    if (!s) return []
    // bin, title, desc, tags에서 단순 includes
    return CMDS.filter(c =>
      c.bin.toLowerCase().includes(s) ||
      c.title.toLowerCase().includes(s) ||
      c.desc.toLowerCase().includes(s)
    ).slice(0, 8)
  }, [q])

  if (!items.length) return null
  return (
    <div className="mt-1 card p-2 text-sm">
      {items.map(it => (
        <button key={it.id} className="w-full text-left px-2 py-1 rounded hover:bg-zinc-800"
                onClick={() => onPick(it)}>
          <span className="font-mono">{it.bin}</span> — <span className="text-zinc-300">{it.title}</span>
        </button>
      ))}
    </div>
  )
}

function Explain({ cmd, favs, setFavs }) {
  if (!cmd) return <div className="text-sm text-zinc-400">명령어를 선택하거나 검색하세요.</div>
  const toggleFav = () => {
    const set = new Set(favs)
    set.has(cmd.id) ? set.delete(cmd.id) : set.add(cmd.id)
    const next = Array.from(set)
    setFavs(next); saveFavs(next)
  }
  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3">
        <h2 className="text-lg font-semibold">{cmd.title}</h2>
        <span className="text-xs px-2 py-0.5 rounded-full border border-zinc-800 text-zinc-400">{CATS[cmd.category] || cmd.category}</span>
        <button className="btn" onClick={toggleFav}>{favs.includes(cmd.id) ? '★ 즐겨찾기 해제' : '☆ 즐겨찾기'}</button>
      </div>
      <p className="text-sm text-zinc-300">{cmd.desc}</p>

      <div className="card p-3">
        <div className="text-xs text-zinc-400 mb-1">Syntax</div>
        <pre className="whitespace-pre-wrap text-sm">{cmd.syntax}</pre>
      </div>

      {!!cmd.flags?.length && (
        <div className="card p-3">
          <div className="text-xs text-zinc-400 mb-2">주요 옵션</div>
          <ul className="grid sm:grid-cols-2 gap-2 text-sm">
            {cmd.flags.map(([k, v]) => (
              <li key={k}><span className="font-mono">{k}</span> — {v}</li>
            ))}
          </ul>
        </div>
      )}

      {!!cmd.examples?.length && (
        <div className="card p-3">
          <div className="text-xs text-zinc-400 mb-2">예시</div>
          <ul className="grid gap-1 text-sm">
            {cmd.examples.map((ex, i) => (
              <li key={i}><code>{ex}</code> <button className="btn ml-2" onClick={() => navigator.clipboard.writeText(ex)}>복사</button></li>
            ))}
          </ul>
        </div>
      )}

      {cmd.tips && (
        <div className="text-sm text-zinc-400">{cmd.tips}</div>
      )}
    </div>
  )
}

function Cheatsheet({ onPick }) {
  // 카테고리 → 커맨드 묶음
  const byCat = useMemo(() => {
    const map = {}
    for (const c of CMDS) {
      (map[c.category] ||= []).push(c)
    }
    return map
  }, [])
  return (
    <div className="space-y-4">
      {Object.entries(byCat).map(([cat, list]) => (
        <details key={cat} className="card p-3" open>
          <summary className="cursor-pointer select-none">{CATS[cat] || cat}</summary>
          <div className="mt-2 grid sm:grid-cols-2 lg:grid-cols-3 gap-2">
            {list.map(c => (
              <button key={c.id} className="text-left px-2 py-1 rounded hover:bg-zinc-800"
                      onClick={() => onPick(c)}>
                <div className="font-mono">{c.bin}</div>
                <div className="text-xs text-zinc-400">{c.title}</div>
              </button>
            ))}
          </div>
        </details>
      ))}
    </div>
  )
}

function Favorites({ favs, onPick }) {
  const list = CMDS.filter(c => favs.includes(c.id))
  if (!list.length) return <div className="text-sm text-zinc-400">즐겨찾기가 비어 있습니다.</div>
  return (
    <div className="grid gap-2">
      {list.map(c => (
        <button key={c.id} className="card p-3 text-left hover:bg-zinc-900" onClick={() => onPick(c)}>
          <div className="font-mono">{c.bin}</div>
          <div className="text-xs text-zinc-400">{c.title}</div>
        </button>
      ))}
    </div>
  )
}

/** ──────────────────────────────────────────────────────────────────
 *  메인 페이지
 *  ────────────────────────────────────────────────────────────────── */
export default function Commands() {
  const [tab, setTab] = useState('cheatsheet') // 'cheatsheet' | 'favorites' | 'explain'
  const [q, setQ] = useState('')
  const [picked, setPicked] = useState(null)
  const [favs, setFavs] = useState(loadFavs)

  // 처음 진입 시 치트시트 탭이 기본
  useEffect(() => {
    const last = sessionStorage.getItem('cli.last')
    if (last) {
      try {
        const obj = JSON.parse(last)
        if (obj.pickedId) {
          const cmd = CMDS.find(c => c.id === obj.pickedId)
          if (cmd) { setPicked(cmd); setTab('explain') }
        }
      } catch {}
    }
  }, [])
  useEffect(() => {
    sessionStorage.setItem('cli.last', JSON.stringify({ pickedId: picked?.id || null }))
  }, [picked])

  const onPick = (cmd) => {
    setPicked(cmd)
    setTab('explain')
    setQ(cmd.bin)
  }

  const onSubmit = (e) => {
    e.preventDefault()
    const bin = parseBin(q)
    const cmd = CMDS.find(c => c.bin === bin)
    if (cmd) onPick(cmd)
    else setTab('cheatsheet')
  }

  return (
    <div className="grid lg:grid-cols-[260px_1fr] gap-6">
      {/* 사이드바 */}
      <aside className="lg:sticky lg:self-start lg:top-16 h-max">
        <nav className="card p-2">
          <button className={`w-full text-left px-3 py-2 rounded ${tab==='cheatsheet'?'bg-zinc-800':''}`} onClick={()=>setTab('cheatsheet')}>리눅스 명령어 치트시트</button>
          <button className={`w-full text-left px-3 py-2 rounded ${tab==='favorites'?'bg-zinc-800':''}`} onClick={()=>setTab('favorites')}>자주 쓰는 명령어</button>
          <button className={`w-full text-left px-3 py-2 rounded ${tab==='explain'?'bg-zinc-800':''}`} onClick={()=>setTab('explain')}>명령어 설명</button>
        </nav>
      </aside>

      {/* 메인 */}
      <section className="space-y-4">
        {/* 상단 입력바 */}
        <form onSubmit={onSubmit}>
          <div className="relative">
            <input
              className="input pr-28"
              placeholder='예: "docker compose logs -f web" 또는 "grep -R ERROR ."'
              value={q}
              onChange={(e)=>setQ(e.target.value)}
            />
            <div className="absolute right-2 top-1/2 -translate-y-1/2 flex gap-2">
              <button type="submit" className="btn">설명</button>
            </div>
          </div>
          {/* 자동완성 */}
          <Suggestions q={q} onPick={onPick} />
        </form>

        {/* 콘텐츠 */}
        {tab === 'cheatsheet' && <Cheatsheet onPick={onPick} />}
        {tab === 'favorites'  && <Favorites favs={favs} onPick={onPick} />}
        {tab === 'explain'    && <Explain cmd={picked} favs={favs} setFavs={setFavs} />}
      </section>
    </div>
  )
}
