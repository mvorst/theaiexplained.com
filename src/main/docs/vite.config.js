import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: 'dev.theaiexplained.com',
    port: 3011,
    https: false,
    cors: {
      origin: 'http://dev.theaiexplained.com',
      methods: ['DELETE', 'GET', 'POST', 'PUT', 'OPTIONS'],
    },
  }
})

