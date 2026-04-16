import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const isDocker = process.env.NODE_ENV === 'docker'

export default defineConfig({
  plugins: [react()],

  server: {
    host: "0.0.0.0",
    port: 5173,
    proxy: {
      '/api': {
        target: isDocker
          ? 'http://backend:8080'
          : 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})