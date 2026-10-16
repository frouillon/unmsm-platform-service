import type { ButtonHTMLAttributes } from 'react'

type Variant = 'primary' | 'secondary' | 'ghost'
type Size = 'md' | 'sm'

const base =
  'inline-flex items-center justify-center gap-2 rounded border font-semibold whitespace-nowrap transition-colors ' +
  'disabled:cursor-not-allowed disabled:opacity-60 max-sm:min-h-11'

const variants: Record<Variant, string> = {
  primary: 'border-primary bg-primary text-white hover:bg-primary-hover hover:border-primary-hover',
  secondary: 'border-line bg-surface text-ink hover:bg-line-soft',
  ghost: 'border-transparent bg-transparent text-primary-hover hover:bg-primary-soft',
}

const sizes: Record<Size, string> = {
  md: 'h-10 px-4 text-sm',
  sm: 'h-8 px-2.5 text-[13px]',
}

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: Variant
  size?: Size
}

export function Button({ variant = 'secondary', size = 'md', className = '', type = 'button', ...props }: ButtonProps) {
  return <button type={type} className={`${base} ${variants[variant]} ${sizes[size]} ${className}`} {...props} />
}
