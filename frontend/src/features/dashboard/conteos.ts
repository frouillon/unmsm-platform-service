import type { Dashboard } from './types'

export type PieResumen = { valor: number; uno: string; varios: string; tono?: 'bad' }

function cantidad(dash: Dashboard, estado: string): number {
  return dash.porEstado.find((c) => c.nombre === estado)?.cantidad ?? 0
}

export function resumenEstudiante(dash: Dashboard): PieResumen[] {
  const atendidas = cantidad(dash, 'Resuelto') + cantidad(dash, 'Cerrado')
  const activas = dash.total - atendidas
  const items: PieResumen[] = [
    { valor: activas, uno: 'activa', varios: 'activas' },
  ]
  if (dash.vencidas > 0) {
    items.push({ valor: dash.vencidas, uno: 'vencida', varios: 'vencidas', tono: 'bad' })
  }
  items.push({ valor: atendidas, uno: 'atendida', varios: 'atendidas' })
  return items
}

export function resumenTecnico(dash: Dashboard): PieResumen[] {
  return [
    { valor: cantidad(dash, 'Asignado'), uno: 'por atender', varios: 'por atender' },
    { valor: cantidad(dash, 'En proceso'), uno: 'en proceso', varios: 'en proceso' },
    {
      valor: cantidad(dash, 'Resuelto') + cantidad(dash, 'Cerrado'),
      uno: 'atendida',
      varios: 'atendidas',
    },
  ]
}

export function resumenCola(dash: Dashboard): PieResumen[] {
  const items: PieResumen[] = [
    { valor: cantidad(dash, 'Pendiente'), uno: 'por revisar', varios: 'por revisar' },
    { valor: cantidad(dash, 'En revisión'), uno: 'por asignar', varios: 'por asignar' },
  ]
  if (dash.vencidas > 0) {
    items.push({ valor: dash.vencidas, uno: 'vencida', varios: 'vencidas', tono: 'bad' })
  }
  return items
}
