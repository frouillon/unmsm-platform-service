import type { NuevaSolicitud } from './types'

export type CampoSolicitud = keyof NuevaSolicitud | 'evidencias'
export type ErroresSolicitud = Partial<Record<CampoSolicitud, string>>

/** Orden visual del formulario: define a qué campo se lleva el foco cuando hay errores. */
export const ORDEN_CAMPOS: CampoSolicitud[] = [
  'categoriaId',
  'asunto',
  'descripcion',
  'pabellon',
  'nivel',
  'ambiente',
  'prioridadId',
  'evidencias',
]

export const LIMITES = {
  asunto: 100,
  descripcionMin: 20,
  descripcionMax: 2000,
  pabellon: 100,
  nivel: 50,
  ambiente: 100,
} as const

export const EVIDENCIAS = {
  max: 5,
  maxBytes: 5 * 1024 * 1024,
  accept: '.png,.jpg,.jpeg,.pdf,image/png,image/jpeg,application/pdf',
} as const

const EXTENSIONES = /\.(png|jpe?g|pdf)$/i

export function validarSolicitud(d: NuevaSolicitud): ErroresSolicitud {
  const e: ErroresSolicitud = {}
  if (d.categoriaId == null) e.categoriaId = 'Selecciona una categoría.'
  if (!d.asunto.trim()) e.asunto = 'Escribe el asunto de la solicitud.'
  else if (d.asunto.trim().length > LIMITES.asunto) e.asunto = 'El asunto admite hasta 100 caracteres.'
  const descripcion = d.descripcion.trim().length
  if (!descripcion) e.descripcion = 'Describe qué ocurre.'
  else if (descripcion < LIMITES.descripcionMin || descripcion > LIMITES.descripcionMax)
    e.descripcion = 'La descripción debe tener entre 20 y 2000 caracteres.'
  if (!d.pabellon.trim()) e.pabellon = 'Indica el pabellón.'
  if (!d.nivel.trim()) e.nivel = 'Indica el piso o nivel.'
  if (!d.ambiente.trim()) e.ambiente = 'Indica el aula o ambiente.'
  if (d.prioridadId == null) e.prioridadId = 'Selecciona una prioridad.'
  return e
}

/** Agrega archivos respetando cantidad, tamaño y formato; devuelve el primer motivo de rechazo. */
export function agregarArchivos(actuales: File[], nuevos: File[]): { archivos: File[]; error?: string } {
  const archivos = [...actuales]
  for (const archivo of nuevos) {
    if (archivos.length >= EVIDENCIAS.max) return { archivos, error: 'Puedes adjuntar hasta 5 archivos.' }
    if (!EXTENSIONES.test(archivo.name)) return { archivos, error: `“${archivo.name}” no es PNG, JPG ni PDF.` }
    if (archivo.size > EVIDENCIAS.maxBytes) return { archivos, error: `“${archivo.name}” supera los 5 MB permitidos.` }
    if (archivo.size === 0) return { archivos, error: `“${archivo.name}” está vacío.` }
    archivos.push(archivo)
  }
  return { archivos }
}
