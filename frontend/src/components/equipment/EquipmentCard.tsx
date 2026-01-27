import { useTranslation } from 'react-i18next'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Stoplight } from './Stoplight'
import { formatDate } from '@/lib/utils'
import type { Equipment } from '@/types'

interface EquipmentCardProps {
  equipment: Equipment
  onMarkServiced?: () => void
  loading?: boolean
}

export function EquipmentCard({ equipment, onMarkServiced, loading }: EquipmentCardProps) {
  const { t } = useTranslation()

  return (
    <Card className="w-full max-w-md">
      <CardHeader className="flex flex-row items-center gap-4">
        <Stoplight status={equipment.stoplightStatus} size="lg" />
        <div>
          <CardTitle className="text-lg">{equipment.serialNumber}</CardTitle>
          {equipment.custAssetId && (
            <p className="text-sm text-muted-foreground">{equipment.custAssetId}</p>
          )}
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-muted-foreground">{t('equipment.agreementStatus')}</p>
            <p className="font-medium">
              {t(`equipment.${equipment.agreementStatus.toLowerCase()}`)}
            </p>
          </div>
          <div>
            <p className="text-muted-foreground">{t('equipment.serviceCycle')}</p>
            <p className="font-medium">
              {t(`equipment.${equipment.serviceCycle.toLowerCase()}`)}
            </p>
          </div>
          <div>
            <p className="text-muted-foreground">{t('equipment.lastService')}</p>
            <p className="font-medium">{formatDate(equipment.lastService)}</p>
          </div>
          <div>
            <p className="text-muted-foreground">{t('equipment.nextService')}</p>
            <p className="font-medium">{formatDate(equipment.nextService)}</p>
          </div>
        </div>

        {onMarkServiced && (
          <Button
            className="w-full"
            size="lg"
            onClick={onMarkServiced}
            disabled={loading}
          >
            {loading ? t('common.loading') : t('scanner.markServiced')}
          </Button>
        )}
      </CardContent>
    </Card>
  )
}
