import { cn } from '@/lib/utils'
import type { StoplightStatus } from '@/types'

interface StoplightProps {
  status: StoplightStatus
  size?: 'sm' | 'md' | 'lg'
}

const sizeClasses = {
  sm: 'h-4 w-4',
  md: 'h-8 w-8',
  lg: 'h-16 w-16',
}

const colorClasses = {
  GREEN: 'bg-stoplight-green',
  YELLOW: 'bg-stoplight-yellow',
  RED: 'bg-stoplight-red',
}

export function Stoplight({ status, size = 'md' }: StoplightProps) {
  return (
    <div
      className={cn(
        'rounded-full',
        sizeClasses[size],
        colorClasses[status]
      )}
    />
  )
}
