import type { Rol } from '../../lib/domain'

export type UsuarioActual = {
  id: number
  nombres: string
  correo: string
  rol: Rol
  areaId: number | null
  areaNombre: string | null
}

export type LoginResponse = {
  token: string
  tipo: 'Bearer'
  expiraEn: string
  usuario: UsuarioActual
}
