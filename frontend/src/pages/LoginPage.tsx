import { useRef, useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { Field } from '../components/ui/Field'
import { inputClass } from '../components/ui/inputClass'
import { Icon } from '../components/ui/Icon'
import { LoginVisual } from '../features/auth/components/LoginVisual'
import { homePath } from '../features/auth/homePath'
import { useAuth } from '../features/auth/useAuth'
import { ApiError } from '../lib/api'

const DOMINIO = /^[^@\s]+@unmsm\.edu\.pe$/i

type Errors = { correo?: string; password?: string }
type Notice = { tone: 'error' | 'info'; text: string }

function validate(correo: string, password: string): Errors {
  const errors: Errors = {}
  if (!correo) errors.correo = 'Ingresa tu correo institucional.'
  else if (!DOMINIO.test(correo)) errors.correo = 'Usa tu correo institucional @unmsm.edu.pe.'
  if (!password) errors.password = 'Ingresa tu contraseña.'
  return errors
}

export function LoginPage() {
  const { login, sessionExpired } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from

  const [correo, setCorreo] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [errors, setErrors] = useState<Errors>({})
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(false)
  const [notice, setNotice] = useState<Notice | null>(
    sessionExpired ? { tone: 'info', text: 'Tu sesión expiró. Inicia sesión nuevamente.' } : null,
  )
  const correoRef = useRef<HTMLInputElement>(null)
  const passwordRef = useRef<HTMLInputElement>(null)

  const revalidate = (nextCorreo: string, nextPassword: string) => {
    if (submitted) setErrors(validate(nextCorreo.trim(), nextPassword))
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitted(true)
    setNotice(null)
    const found = validate(correo.trim(), password)
    setErrors(found)
    if (found.correo) return correoRef.current?.focus()
    if (found.password) return passwordRef.current?.focus()

    setLoading(true)
    try {
      const usuario = await login(correo.trim(), password)
      navigate(from && from !== '/login' ? from : homePath(usuario.rol), { replace: true })
    } catch (error) {
      const apiError = error instanceof ApiError ? error : null
      if (apiError?.status === 400 && Object.keys(apiError.fieldErrors).length > 0) {
        setErrors(apiError.fieldErrors)
      } else {
        setNotice({ tone: 'error', text: apiError?.message ?? 'Ocurrió un error inesperado. Inténtalo nuevamente.' })
      }
      setLoading(false)
    }
  }

  return (
    <section className="grid min-h-dvh grid-cols-1 bg-canvas min-[701px]:grid-cols-[42%_58%] min-[901px]:grid-cols-[minmax(420px,48%)_1fr]">
      <LoginVisual />

      <div className="grid min-h-dvh place-items-center px-5 py-7 min-[701px]:px-[42px] min-[701px]:py-10 min-[901px]:px-16 min-[901px]:py-11">
        <div className="w-full max-w-[390px]">
          <div className="mb-6 flex items-center gap-2.5 min-[701px]:hidden">
            <img src="/escudo-unmsm.png" alt="Escudo UNMSM" className="size-11 rounded-full object-cover" />
            <div>
              <b className="block font-serif text-lg">San Marcos</b>
              <span className="mt-[3px] block text-[10px] tracking-[.14em] text-primary uppercase">Servicios</span>
            </div>
          </div>

          <h1 className="mb-1.5 text-[31px] leading-tight font-bold min-[701px]:text-[34px]">Bienvenido</h1>
          <p className="mb-6 text-muted min-[701px]:mb-[26px]">Ingresa con tu cuenta institucional.</p>

          {notice && (
            <div
              role="alert"
              className={`mb-4 flex items-start gap-2.5 rounded-md px-3 py-2.5 text-[13px] font-medium ${
                notice.tone === 'error' ? 'bg-bad-soft text-bad' : 'bg-subtle-soft text-subtle'
              }`}
            >
              <Icon name="alert" className="mt-px size-[18px]" />
              <span>{notice.text}</span>
            </div>
          )}

          <form noValidate onSubmit={handleSubmit}>
            <Field id="login-correo" label="Correo institucional" error={errors.correo} className="mt-[18px]">
              <div className="relative">
                <Icon name="mail" className="pointer-events-none absolute top-[11px] left-3 size-[18px] text-muted" />
                <input
                  ref={correoRef}
                  id="login-correo"
                  type="email"
                  autoComplete="username"
                  inputMode="email"
                  placeholder="nombre@unmsm.edu.pe"
                  value={correo}
                  onChange={(e) => {
                    setCorreo(e.target.value)
                    revalidate(e.target.value, password)
                  }}
                  aria-invalid={!!errors.correo}
                  aria-describedby={errors.correo ? 'login-correo-error' : undefined}
                  className={`${inputClass(!!errors.correo)} pl-[38px]`}
                />
              </div>
            </Field>

            <Field id="login-password" label="Contraseña" error={errors.password} className="mt-[18px]">
              <div className="relative">
                <Icon name="lock" className="pointer-events-none absolute top-[11px] left-3 size-[18px] text-muted" />
                <input
                  ref={passwordRef}
                  id="login-password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  placeholder="Tu contraseña"
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value)
                    revalidate(correo, e.target.value)
                  }}
                  aria-invalid={!!errors.password}
                  aria-describedby={errors.password ? 'login-password-error' : undefined}
                  className={`${inputClass(!!errors.password)} pr-[42px] pl-[38px]`}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                  aria-pressed={showPassword}
                  className="absolute top-1 right-1 grid size-8 place-items-center rounded-lg text-muted hover:text-ink max-sm:top-0.5 max-sm:size-10"
                >
                  <Icon name={showPassword ? 'eye-off' : 'eye'} />
                </button>
              </div>
            </Field>

            <div className="mt-4 mb-[22px] flex items-center text-[13px]">
              <button
                type="button"
                onClick={() =>
                  setNotice({
                    tone: 'info',
                    text: 'Para restablecer tu contraseña, comunícate con el administrador del sistema.',
                  })
                }
                className="ml-auto text-right font-semibold text-primary-hover hover:underline max-sm:min-h-11"
              >
                ¿Olvidaste tu contraseña?
              </button>
            </div>

            <button
              type="submit"
              disabled={loading}
              aria-busy={loading}
              className="inline-flex h-[46px] w-full items-center justify-center gap-2 rounded-md border border-primary bg-primary text-sm font-semibold text-white transition-colors hover:border-primary-hover hover:bg-primary-hover disabled:cursor-wait disabled:opacity-80"
            >
              {loading && <span className="size-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />}
              {loading ? 'Iniciando sesión…' : 'Iniciar sesión'}
            </button>
          </form>
        </div>
      </div>
    </section>
  )
}
