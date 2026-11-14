import { describe, expect, it } from 'vitest'
import type { NuevaSolicitud } from './types'
import { agregarArchivos, validarSolicitud } from './validacion'

const MB = 1024 * 1024

const solicitudValida: NuevaSolicitud = {
  categoriaId: 1,
  prioridadId: 2,
  asunto: 'El proyector no enciende',
  descripcion: 'El proyector del aula 204 no enciende desde ayer.',
  pabellon: '',
  nivel: '',
  ambiente: '',
}

/** Solicitud válida con solo los campos indicados cambiados. */
const solicitudCon = (cambios: Partial<NuevaSolicitud>): NuevaSolicitud => ({ ...solicitudValida, ...cambios })

/** Archivo de prueba con el nombre y el tamaño indicados. */
const archivo = (nombre: string, bytes = 1024): File => new File([new Uint8Array(bytes)], nombre)

/*
 * HU-04 · RF-11 — Validación del formulario de registro.
 * Técnicas: partición de equivalencia (campo presente / ausente)
 * y análisis de valores límite (longitudes mínima y máxima).
 */
describe('validarSolicitud', () => {
  describe('partición de equivalencia', () => {
    it('acepta un formulario completo', () => {
      expect(validarSolicitud(solicitudValida)).toEqual({})
    })

    it('exige una categoría', () => {
      const errores = validarSolicitud(solicitudCon({ categoriaId: null }))

      expect(errores.categoriaId).toBe('Selecciona una categoría.')
    })

    it('rechaza un asunto vacío o que solo tiene espacios', () => {
      const mensaje = 'Escribe el asunto de la solicitud.'

      expect(validarSolicitud(solicitudCon({ asunto: '' })).asunto).toBe(mensaje)
      expect(validarSolicitud(solicitudCon({ asunto: '     ' })).asunto).toBe(mensaje)
    })

    it('exige una prioridad', () => {
      const errores = validarSolicitud(solicitudCon({ prioridadId: null }))

      expect(errores.prioridadId).toBe('Selecciona una prioridad.')
    })
  })

  describe('análisis de valores límite', () => {
    it('asunto: 100 caracteres pasa y 101 falla', () => {
      const enElLimite = validarSolicitud(solicitudCon({ asunto: 'a'.repeat(100) }))
      const sobreElLimite = validarSolicitud(solicitudCon({ asunto: 'a'.repeat(101) }))

      expect(enElLimite.asunto).toBeUndefined()
      expect(sobreElLimite.asunto).toBe('El asunto admite hasta 100 caracteres.')
    })

    it('descripción: 19 caracteres falla y 20 pasa', () => {
      const bajoElMinimo = validarSolicitud(solicitudCon({ descripcion: 'a'.repeat(19) }))
      const enElMinimo = validarSolicitud(solicitudCon({ descripcion: 'a'.repeat(20) }))

      expect(bajoElMinimo.descripcion).toBe('La descripción debe tener entre 20 y 2000 caracteres.')
      expect(enElMinimo.descripcion).toBeUndefined()
    })

    it('descripción: 2000 caracteres pasa y 2001 falla', () => {
      const enElMaximo = validarSolicitud(solicitudCon({ descripcion: 'a'.repeat(2000) }))
      const sobreElMaximo = validarSolicitud(solicitudCon({ descripcion: 'a'.repeat(2001) }))

      expect(enElMaximo.descripcion).toBeUndefined()
      expect(sobreElMaximo.descripcion).toBe('La descripción debe tener entre 20 y 2000 caracteres.')
    })
  })
})

/*
 * HU-05 · RF-12 — Adjuntar evidencias (hasta 5 archivos PNG, JPG o PDF de 5 MB como máximo).
 * Técnicas: partición de equivalencia (tipo de archivo), análisis de valores límite
 * (cantidad y tamaño) y conjetura de errores (lote con un archivo malo en medio).
 */
describe('agregarArchivos', () => {
  describe('partición de equivalencia', () => {
    it('acepta PNG, JPG, JPEG y PDF sin importar las mayúsculas', () => {
      const nuevos = [archivo('foto.png'), archivo('escaneo.JPG'), archivo('captura.jpeg'), archivo('informe.pdf')]

      const resultado = agregarArchivos([], nuevos)

      expect(resultado.archivos).toEqual(nuevos)
      expect(resultado.error).toBeUndefined()
    })

    it('rechaza una extensión no permitida', () => {
      const resultado = agregarArchivos([], [archivo('instalador.exe')])

      expect(resultado.archivos).toEqual([])
      expect(resultado.error).toBe('“instalador.exe” no es PNG, JPG ni PDF.')
    })
  })

  describe('análisis de valores límite', () => {
    it('cantidad: el quinto archivo entra y el sexto se rechaza', () => {
      const cuatro = [archivo('1.png'), archivo('2.png'), archivo('3.png'), archivo('4.png')]

      const conCinco = agregarArchivos(cuatro, [archivo('5.png')])
      const conSeis = agregarArchivos(conCinco.archivos, [archivo('6.png')])

      expect(conCinco.archivos).toHaveLength(5)
      expect(conCinco.error).toBeUndefined()
      expect(conSeis.archivos).toHaveLength(5)
      expect(conSeis.error).toBe('Puedes adjuntar hasta 5 archivos.')
    })

    it('tamaño: rechaza un archivo de 5 MB más un byte', () => {
      const resultado = agregarArchivos([], [archivo('plano.pdf', 5 * MB + 1)])

      expect(resultado.archivos).toEqual([])
      expect(resultado.error).toBe('“plano.pdf” supera los 5 MB permitidos.')
    })

    it('tamaño: acepta un archivo de exactamente 5 MB', () => {
      const resultado = agregarArchivos([], [archivo('plano.pdf', 5 * MB)])

      expect(resultado.archivos).toHaveLength(1)
      expect(resultado.error).toBeUndefined()
    })

    it('tamaño: rechaza un archivo vacío (0 bytes)', () => {
      const resultado = agregarArchivos([], [archivo('vacio.png', 0)])

      expect(resultado.archivos).toEqual([])
      expect(resultado.error).toBe('“vacio.png” está vacío.')
    })
  })

  describe('conjetura de errores', () => {
    it('si un archivo del lote falla, conserva los anteriores y descarta el resto', () => {
      const yaAdjunto = archivo('anterior.png')
      const valido = archivo('valido.pdf')
      const invalido = archivo('notas.txt')
      const posterior = archivo('posterior.png')

      const resultado = agregarArchivos([yaAdjunto], [valido, invalido, posterior])

      expect(resultado.archivos).toEqual([yaAdjunto, valido])
      expect(resultado.error).toBe('“notas.txt” no es PNG, JPG ni PDF.')
    })
  })
})
