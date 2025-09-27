import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import App from './App.jsx'
import ComposeNew from './pages/ComposeNew.jsx'
import DockerfileNew from './pages/DockerfileNew.jsx'
import Presets from './pages/Presets.jsx'
import Settings from './pages/Settings.jsx'
import NotFound from './pages/NotFound.jsx'
import './styles.css'
import Commands from './pages/Commands.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<App />}>
          {/* 홈 대신 바로 작업 시작 */}
          <Route index element={<Navigate to="/compose/new" replace />} />
          <Route path="compose/new" element={<ComposeNew />} />
          <Route path="dockerfile/new" element={<DockerfileNew />} />
          <Route path="presets" element={<Presets />} />
          <Route path="/cli" element={<Commands />} />
          <Route path="settings" element={<Settings />} />
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </StrictMode>
)
