import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
const backendTarget = `http://127.0.0.1:${process.env.BACKEND_PORT ?? '8080'}`

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Backend defaults to 127.0.0.1:8080 (override with BACKEND_PORT for
      // local dev if that port's taken). Cookies (access_token/
      // refresh_token) only work same-origin from the browser's
      // perspective, so both HTTP and the STOMP WebSocket handshake go
      // through this proxy rather than a cross-origin backend URL.
      '/api': {
        target: backendTarget,
        changeOrigin: true,
      },
      '/ws': {
        target: backendTarget,
        ws: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/setupTests.ts'],
    globals: true,
  },
})
