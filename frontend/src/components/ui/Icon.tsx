export type IconName =
  | 'home' | 'list' | 'plus' | 'bell' | 'user' | 'search' | 'menu' | 'layers' | 'clock' | 'loader' | 'check'
  | 'arrow' | 'wrench' | 'moon' | 'mail' | 'lock' | 'eye' | 'eye-off' | 'alert' | 'logout' | 'pin' | 'upload'
  | 'file' | 'image' | 'x' | 'chev' | 'clip' | 'users' | 'chart' | 'shield' | 'tag' | 'flag' | 'building'
  | 'sliders' | 'edit' | 'tick' | 'send' | 'wifi-off' | 'server' | 'inbox' | 'refresh'

type IconProps = {
  name: IconName
  className?: string
}

export function Icon({ name, className = 'size-[18px]' }: IconProps) {
  return (
    <svg
      aria-hidden
      className={`flex-none fill-none stroke-current [stroke-linecap:round] [stroke-linejoin:round] [stroke-width:1.75] ${className}`}
    >
      <use href={`/icons.svg#${name}`} />
    </svg>
  )
}
