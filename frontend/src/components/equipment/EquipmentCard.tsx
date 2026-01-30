import { useTranslation } from 'react-i18next'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Stoplight } from './Stoplight'
import { formatDate } from '@/lib/utils'
import type { Equipment } from '@/types'
import { MapPin, Building2, Settings } from 'lucide-react'

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
        {/* Site and Customer Info */}
        {equipment.site && (
          <div className="flex flex-col gap-1 p-3 bg-muted/50 rounded-lg">
            <div className="flex items-center gap-2 text-sm">
              <Building2 className="h-4 w-4 text-muted-foreground" />
              <span className="font-medium">{equipment.site.customer.name}</span>
              <span className={`px-2 py-0.5 rounded-full text-xs ${
                equipment.site.customer.agreementStatus === 'COVERED' ? 'bg-green-100 text-green-800' :
                equipment.site.customer.agreementStatus === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                'bg-red-100 text-red-800'
              }`}>
                {equipment.site.customer.agreementStatus.replace('_', ' ')}
              </span>
            </div>
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <MapPin className="h-4 w-4" />
              <span>{equipment.site.name}</span>
            </div>
          </div>
        )}

        {/* Equipment Type */}
        {equipment.equipmentType && (
          <div className="flex items-center gap-2 text-sm">
            <Settings className="h-4 w-4 text-muted-foreground" />
            <span>{equipment.equipmentType.name}</span>
          </div>
        )}

        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-muted-foreground">{t('equipment.serviceCycle')}</p>
            <p className="font-medium">
              {t(`equipment.${equipment.serviceCycle.toLowerCase()}`)}
            </p>
          </div>
          <div>
            <p className="text-muted-foreground">Lifecycle</p>
            <p className="font-medium">
              {equipment.lifecycleStatus.replace('_', ' ')}
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

        {equipment.provisional && (
          <div className="text-sm text-yellow-600 bg-yellow-50 p-2 rounded">
            Provisional equipment - expires {formatDate(equipment.provisionalExpiresAt)}
          </div>
        )}

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
