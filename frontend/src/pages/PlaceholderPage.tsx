export function PlaceholderPage() {
  return (
    <main className="grid min-h-dvh place-items-center px-6">
      <div className="flex items-center gap-3">
        <img src="/escudo-unmsm.png" alt="" className="size-[58px] rounded-full border border-primary/20 object-cover" />
        <div>
          <b className="block font-serif text-[21px] leading-[1.05]">San Marcos Servicios</b>
          <span className="mt-[3px] block text-[10px] font-semibold tracking-[.16em] text-primary uppercase">
            Universidad Nacional Mayor de San Marcos
          </span>
        </div>
      </div>
    </main>
  )
}
