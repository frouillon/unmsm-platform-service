import { useEffect, useState } from 'react'

const SLIDES = [
  { id: 's1', mark: 'San Marcos' },
  { id: 's2', mark: 'Servicios' },
  { id: 's3', mark: 'UNMSM' },
] as const

const INTERVAL_MS = 5200

export function LoginVisual() {
  const [active, setActive] = useState(0)
  const [cycle, setCycle] = useState(0)

  useEffect(() => {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
    const timer = window.setInterval(() => setActive((i) => (i + 1) % SLIDES.length), INTERVAL_MS)
    return () => window.clearInterval(timer)
  }, [cycle])

  const select = (index: number) => {
    setActive(index)
    setCycle((c) => c + 1)
  }

  return (
    <div
      aria-label="Identidad San Marcos"
      className="relative hidden min-h-dvh overflow-hidden bg-[#6b0d15] min-[701px]:block"
    >
      <div className="absolute top-[42px] left-[30px] z-[8] flex items-center gap-3 text-white min-[901px]:left-[42px]">
        <img
          src="/escudo-unmsm.png"
          alt="Escudo UNMSM"
          className="size-[54px] rounded-full object-contain shadow-[0_0_0_1px_rgb(255_255_255/0.15)] drop-shadow-[0_4px_10px_rgb(0_0_0/0.12)]"
        />
        <div>
          <b className="block font-serif text-lg leading-none">San Marcos</b>
          <span className="mt-1 block text-[10px] tracking-[.16em] text-gold-soft uppercase">Servicios</span>
        </div>
      </div>

      <div className="absolute inset-0">
        {SLIDES.map((slide, index) => (
          <article
            key={slide.id}
            aria-hidden={index !== active}
            data-active={index === active}
            className={`login-slide ${slide.id}`}
          >
            <div className="pointer-events-none absolute inset-0 opacity-80">
              <i className="slide-line a" />
              <i className="slide-line b" />
              <i className="slide-line c" />
            </div>
            <div className="absolute bottom-[72px] left-[34px] z-[3] font-serif text-[clamp(42px,5.2vw,72px)] font-semibold tracking-[-.04em] text-white/95 min-[901px]:bottom-[78px] min-[901px]:left-12">
              {slide.mark}
            </div>
          </article>
        ))}
      </div>

      <div
        aria-label="Cambiar imagen"
        className="absolute bottom-4 left-[30px] z-[6] flex gap-2 min-[901px]:left-[42px]"
      >
        {SLIDES.map((slide, index) => (
          <button
            key={slide.id}
            type="button"
            aria-label={`Diapositiva ${index + 1}`}
            aria-current={index === active ? 'true' : undefined}
            onClick={() => select(index)}
            className="flex h-11 items-center px-1"
          >
            <span
              className={`block h-[3px] rounded-full transition-[width,background-color] duration-200 ${
                index === active ? 'w-[42px] bg-gold-soft' : 'w-6 bg-white/35'
              }`}
            />
          </button>
        ))}
      </div>
    </div>
  )
}
