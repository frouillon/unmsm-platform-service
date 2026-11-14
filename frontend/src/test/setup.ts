import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

// Desmonta lo renderizado y limpia la sesión para que ninguna prueba dependa de otra.
afterEach(() => {
  cleanup()
  sessionStorage.clear()
})
