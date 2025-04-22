import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: 'theaiexplained.local',
    port: 3011,
    https: false,
    cors: {
      origin: 'http://theaiexplained.local',
      methods: ['DELETE', 'GET', 'POST', 'PUT', 'OPTIONS'],
    },
  }
})

