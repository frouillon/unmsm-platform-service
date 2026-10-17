import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { Button } from '../components/ui/Button'
import { Field, FieldError } from '../components/ui/Field'
import { Icon } from '../components/ui/Icon'
import { inputClass } from '../components/ui/inputClass'
import { PageHeader } from '../components/ui/PageHeader'
import { EvidenciasInput } from '../features/solicitudes/components/EvidenciasInput'
import { PrioridadSelector } from '../features/solicitudes/components/PrioridadSelector'
import { SolicitudRegistradaCard } from '../features/solicitudes/components/SolicitudRegistradaCard'
import { fetchCategorias, fetchPrioridades, registrarSolicitud } from '../features/solicitudes/solicitudesApi'
import type { Categoria, NuevaSolicitud, PrioridadCatalogo, SolicitudRegistrada } from '../features/solicitudes/types'
import {
  agregarArchivos,
  LIMITES,
  ORDEN_CAMPOS,
  validarSolicitud,
  type ErroresSolicitud,
} from '../features/solicitudes/validacion'
import { ApiError } from '../lib/api'
import { plazoTexto } from '../lib/format'

const VACIA: NuevaSolicitud = {
  categoriaId: null,
  prioridadId: null,
  asunto: '',
  descripcion: '',
  pabellon: '',
  nivel: '',
  ambiente: '',
}

type Catalogos = { categorias: Categoria[]; prioridades: PrioridadCatalogo[] }

const campoId = (campo: string) => `ns-${campo}`

const describedBy = (campo: string, error?: string, hint = false) =>
  [hint && `${campoId(campo)}-hint`, error && `${campoId(campo)}-error`].filter(Boolean).join(' ') || undefined

export function NuevaSolicitudPage() {
  const navigate = useNavigate()
  const [catalogos, setCatalogos] = useState<Catalogos | null>(null)
  const [catalogoError, setCatalogoError] = useState<string | null>(null)
  const [intento, setIntento] = useState(0)

  const [datos, setDatos] = useState<NuevaSolicitud>(VACIA)
  const [archivos, setArchivos] = useState<File[]>([])
  const [errores, setErrores] = useState<ErroresSolicitud>({})
  const [enviado, setEnviado] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [aviso, setAviso] = useState<string | null>(null)
  const [creada, setCreada] = useState<SolicitudRegistrada | null>(null)

  useEffect(() => {
    let vigente = true
    Promise.all([fetchCategorias(), fetchPrioridades()])
      .then(([categorias, prioridades]) => {
        if (!vigente) return
        setCatalogos({ categorias, prioridades })
        const media = prioridades.find((p) => p.nombre === 'Media') ?? prioridades[0]
        setDatos((d) => (d.prioridadId == null && media ? { ...d, prioridadId: media.id } : d))
      })
      .catch((error: unknown) => {
        if (vigente) setCatalogoError(error instanceof ApiError ? error.message : 'No se pudo cargar el formulario.')
      })
    return () => {
      vigente = false
    }
  }, [intento])

  const actualizar = <K extends keyof NuevaSolicitud>(campo: K, valor: NuevaSolicitud[K]) => {
    const siguiente = { ...datos, [campo]: valor }
    setDatos(siguiente)
    if (enviado) setErrores((prev) => ({ ...validarSolicitud(siguiente), evidencias: prev.evidencias }))
  }

  const enfocarPrimerError = (encontrados: ErroresSolicitud) => {
    const campo = ORDEN_CAMPOS.find((c) => encontrados[c])
    if (campo) document.getElementById(campoId(campo))?.focus()
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setEnviado(true)
    setAviso(null)
    const encontrados = validarSolicitud(datos)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return enfocarPrimerError(encontrados)

    setEnviando(true)
    try {
      setCreada(await registrarSolicitud(datos, archivos))
      window.scrollTo({ top: 0 })
    } catch (error) {
      const apiError = error instanceof ApiError ? error : null
      if (apiError && Object.keys(apiError.fieldErrors).length > 0) {
        setErrores(apiError.fieldErrors)
        enfocarPrimerError(apiError.fieldErrors)
      } else {
        setAviso(apiError?.message ?? 'Ocurrió un error inesperado. Inténtalo nuevamente.')
      }
    } finally {
      setEnviando(false)
    }
  }

  const reiniciar = () => {
    const media = catalogos?.prioridades.find((p) => p.nombre === 'Media')
    setDatos({ ...VACIA, prioridadId: media?.id ?? null })
    setArchivos([])
    setErrores({})
    setEnviado(false)
    setAviso(null)
    setCreada(null)
  }

  if (creada) return <SolicitudRegistradaCard solicitud={creada} onOtra={reiniciar} />

  const categoria = catalogos?.categorias.find((c) => c.id === datos.categoriaId)
  const prioridad = catalogos?.prioridades.find((p) => p.id === datos.prioridadId)

  return (
    <div className="max-w-[760px]">
      <Link
        to="/solicitudes"
        className="mb-4 inline-flex items-center gap-1 text-[13px] font-semibold text-muted hover:text-ink max-sm:min-h-11"
      >
        <Icon name="chev" className="size-4" />
        Volver a mis solicitudes
      </Link>
      <PageHeader
        title="Nueva solicitud"
        subtitle={
          <>
            Cuéntanos qué necesitas. Los campos con <span className="text-bad">*</span> son obligatorios.
          </>
        }
      />

      <div className="rounded-card border border-line bg-surface p-6 max-sm:p-[18px]">
        {catalogoError ? (
          <div role="alert" className="grid justify-items-center gap-3 py-10 text-center">
            <Icon name="wifi-off" className="size-6 text-muted" />
            <p className="text-muted">{catalogoError}</p>
            <Button
              size="sm"
              onClick={() => {
                setCatalogoError(null)
                setIntento((n) => n + 1)
              }}
            >
              <Icon name="refresh" className="size-4" />
              Reintentar
            </Button>
          </div>
        ) : !catalogos ? (
          <div role="status" className="grid gap-5" aria-label="Cargando formulario">
            {[0, 1, 2].map((i) => (
              <div key={i} className="grid gap-2">
                <span className="h-3.5 w-28 animate-pulse rounded bg-line-soft" />
                <span className={`animate-pulse rounded bg-line-soft ${i === 2 ? 'h-28' : 'h-10'}`} />
              </div>
            ))}
          </div>
        ) : (
          <form noValidate onSubmit={handleSubmit} aria-busy={enviando}>
            {aviso && (
              <div
                role="alert"
                className="mb-5 flex items-start gap-2.5 rounded-md bg-bad-soft px-3 py-2.5 text-[13px] font-medium text-bad"
              >
                <Icon name="alert" className="mt-px size-[18px]" />
                <span>{aviso}</span>
              </div>
            )}

            <div className="grid gap-5">
              <Field
                id={campoId('categoriaId')}
                label="Categoría"
                required
                error={errores.categoriaId}
                hint={categoria && `La atiende: ${categoria.area}.`}
              >
                <select
                  id={campoId('categoriaId')}
                  value={datos.categoriaId ?? ''}
                  onChange={(e) => actualizar('categoriaId', e.target.value ? Number(e.target.value) : null)}
                  aria-invalid={!!errores.categoriaId}
                  aria-describedby={describedBy('categoriaId', errores.categoriaId, !!categoria)}
                  className={`${inputClass(!!errores.categoriaId)} ${datos.categoriaId == null ? 'text-muted' : ''}`}
                >
                  <option value="">Selecciona una categoría</option>
                  {catalogos.categorias.map((c) => (
                    <option key={c.id} value={c.id} className="text-ink">
                      {c.nombre}
                    </option>
                  ))}
                </select>
              </Field>

              <Field
                id={campoId('asunto')}
                label="Asunto"
                required
                error={errores.asunto}
                hint={`${datos.asunto.length}/${LIMITES.asunto} caracteres`}
              >
                <input
                  id={campoId('asunto')}
                  value={datos.asunto}
                  maxLength={LIMITES.asunto}
                  onChange={(e) => actualizar('asunto', e.target.value)}
                  placeholder="Resume el problema en una línea"
                  aria-invalid={!!errores.asunto}
                  aria-describedby={describedBy('asunto', errores.asunto, true)}
                  className={inputClass(!!errores.asunto)}
                />
              </Field>

              <Field
                id={campoId('descripcion')}
                label="Descripción"
                required
                error={errores.descripcion}
                hint={`Mínimo ${LIMITES.descripcionMin} caracteres.`}
              >
                <textarea
                  id={campoId('descripcion')}
                  value={datos.descripcion}
                  maxLength={LIMITES.descripcionMax}
                  onChange={(e) => actualizar('descripcion', e.target.value)}
                  placeholder="¿Qué ocurre? ¿Desde cuándo? ¿A cuántas personas afecta?"
                  aria-invalid={!!errores.descripcion}
                  aria-describedby={describedBy('descripcion', errores.descripcion, true)}
                  className={`${inputClass(!!errores.descripcion)} h-auto min-h-28 resize-y py-2.5`}
                />
              </Field>

              <fieldset>
                <legend className="text-[13px] font-semibold">
                  Ubicación<span className="text-bad"> *</span>
                </legend>
                <div className="mt-1.5 grid gap-2.5 sm:grid-cols-3">
                  {(
                    [
                      ['pabellon', 'Pabellón', 'Ej. Pabellón B', LIMITES.pabellon],
                      ['nivel', 'Piso o nivel', 'Ej. 2.º piso', LIMITES.nivel],
                      ['ambiente', 'Aula o ambiente', 'Ej. Aula 4B', LIMITES.ambiente],
                    ] as const
                  ).map(([campo, etiqueta, placeholder, max]) => (
                    <div key={campo}>
                      <label htmlFor={campoId(campo)} className="sr-only">
                        {etiqueta}
                      </label>
                      <div className="relative">
                        {campo === 'pabellon' && (
                          <Icon name="pin" className="pointer-events-none absolute top-[11px] left-3 size-[18px] text-muted" />
                        )}
                        <input
                          id={campoId(campo)}
                          value={datos[campo]}
                          maxLength={max}
                          onChange={(e) => actualizar(campo, e.target.value)}
                          placeholder={placeholder}
                          aria-invalid={!!errores[campo]}
                          aria-describedby={describedBy(campo, errores[campo])}
                          className={`${inputClass(!!errores[campo])} ${campo === 'pabellon' ? 'pl-[38px]' : ''}`}
                        />
                      </div>
                      <FieldError id={`${campoId(campo)}-error`} message={errores[campo]} />
                    </div>
                  ))}
                </div>
              </fieldset>

              <div>
                <span id={`${campoId('prioridadId')}-label`} className="block text-[13px] font-semibold">
                  Prioridad
                </span>
                <PrioridadSelector
                  id={campoId('prioridadId')}
                  prioridades={catalogos.prioridades}
                  value={datos.prioridadId}
                  onChange={(id) => actualizar('prioridadId', id)}
                  invalid={!!errores.prioridadId}
                  describedBy={describedBy('prioridadId', errores.prioridadId, true)}
                />
                <p id={`${campoId('prioridadId')}-hint`} className="mt-1.5 text-xs text-muted">
                  {prioridad && <>Plazo de atención: {plazoTexto(prioridad.slaDias, prioridad.diasHabiles)}. </>}
                  Usa Urgente solo ante riesgos de seguridad o servicios críticos detenidos.
                </p>
                <FieldError id={`${campoId('prioridadId')}-error`} message={errores.prioridadId} />
              </div>

              <div>
                <span className="block text-[13px] font-semibold">
                  Evidencias <span className="font-normal text-muted">(opcional)</span>
                </span>
                <EvidenciasInput
                  id={campoId('evidencias')}
                  archivos={archivos}
                  disabled={enviando}
                  invalid={!!errores.evidencias}
                  describedBy={describedBy('evidencias', errores.evidencias)}
                  onAgregar={(nuevos) => {
                    const resultado = agregarArchivos(archivos, nuevos)
                    setArchivos(resultado.archivos)
                    setErrores((prev) => ({ ...prev, evidencias: resultado.error }))
                  }}
                  onQuitar={(index) => {
                    setArchivos((prev) => prev.filter((_, i) => i !== index))
                    setErrores((prev) => ({ ...prev, evidencias: undefined }))
                  }}
                />
                <FieldError id={`${campoId('evidencias')}-error`} message={errores.evidencias} />
              </div>
            </div>

            <div className="mt-6 flex justify-end gap-2.5 border-t border-line pt-5 max-sm:flex-col-reverse">
              <Button onClick={() => navigate('/solicitudes')} disabled={enviando}>
                Cancelar
              </Button>
              <Button type="submit" variant="primary" disabled={enviando} aria-busy={enviando}>
                {enviando ? (
                  <span className="size-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                ) : (
                  <Icon name="send" className="size-4" />
                )}
                {enviando ? 'Enviando…' : 'Enviar solicitud'}
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
