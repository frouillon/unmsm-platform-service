import { apiFetch } from '../../lib/api'
import type { Dashboard } from './types'

export function fetchDashboard() {
  return apiFetch<Dashboard>('/api/dashboard')
}
