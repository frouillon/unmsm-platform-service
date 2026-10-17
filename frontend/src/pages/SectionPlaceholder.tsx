import { EmptyState } from '../components/ui/EmptyState'
import type { IconName } from '../components/ui/Icon'
import { PageHeader } from '../components/ui/PageHeader'
import { useAuth } from '../features/auth/useAuth'

type SectionPlaceholderProps = {
  /** Si se omite, se saluda al usuario ("Hola, Carlos"), como en los dashboards del Figma. */
  title?: string
  subtitle: string
  icon: IconName
}

/** Sección navegable cuyo contenido llega en una fase posterior del MVP. */
export function SectionPlaceholder({ title, subtitle, icon }: SectionPlaceholderProps) {
  const { usuario } = useAuth()
  const heading = title ?? `Hola, ${usuario?.nombres.split(' ')[0] ?? ''}`

  return (
    <>
      <PageHeader title={heading} subtitle={subtitle} />
      <div className="rounded-card border border-line bg-surface">
        <EmptyState
          icon={icon}
          title="Sección en construcción"
          description="Esta pantalla se habilitará en una próxima fase del MVP."
        />
      </div>
    </>
  )
}
