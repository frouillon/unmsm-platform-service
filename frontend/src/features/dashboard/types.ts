export type ConteoItem = {
  nombre: string
  cantidad: number
}

export type Dashboard = {
  total: number
  vencidas: number
  porEstado: ConteoItem[]
  porArea: ConteoItem[]
  porPrioridad: ConteoItem[]
}
