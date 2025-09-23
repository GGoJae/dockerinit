import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

// 최소 설정: 도커에서 접근 가능하게
export default defineConfig({
  plugins: [react()],
  server: { host: '0.0.0.0', port: 5173, strictPort: true }
})
