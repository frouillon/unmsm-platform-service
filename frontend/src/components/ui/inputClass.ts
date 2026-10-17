export const inputClass = (invalid: boolean) =>
  'block h-10 w-full rounded border bg-field px-3 font-normal text-ink placeholder:text-muted/80 ' +
  'focus:border-primary focus:outline-2 focus:outline-offset-1 focus:outline-primary ' +
  'max-sm:min-h-11 max-sm:text-base ' +
  (invalid ? 'border-bad' : 'border-line')
