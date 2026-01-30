import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { EquipmentCard } from '@/components/equipment/EquipmentCard'
import { ReasonCodeModal } from '@/components/equipment/ReasonCodeModal'
import { useScanner } from '@/hooks/useScanner'
import { useToast } from '@/components/ui/toast'
import { api } from '@/lib/api'
import type { Equipment, ReasonCode, ServiceRecord, MarkServicedRequest } from '@/types'
import { QrCode, Keyboard } from 'lucide-react'

export function ScannerPage() {
  const { t } = useTranslation()
  const { toast } = useToast()

  const [mode, setMode] = useState<'scan' | 'manual'>('scan')
  const [manualInput, setManualInput] = useState('')
  const [equipment, setEquipment] = useState<Equipment | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [reasonModalOpen, setReasonModalOpen] = useState(false)

  const handleLookup = async (query: string) => {
    if (!query.trim()) return

    setLoading(true)
    setError(null)
    setEquipment(null)

    try {
      const result = await api.get<Equipment>(`/equipment/lookup?q=${encodeURIComponent(query)}`)
      setEquipment(result)
    } catch (err) {
      setError(t('scanner.equipmentNotFound'))
    } finally {
      setLoading(false)
    }
  }

  const { elementId, isScanning, hasCamera, startScanning, stopScanning } = useScanner({
    onScan: (code) => {
      stopScanning()
      handleLookup(code)
    },
  })

  const handleMarkServiced = async (reasonCode?: ReasonCode) => {
    if (!equipment) return

    // If equipment is RED and no reason code provided, show modal
    if (equipment.stoplightStatus === 'RED' && !reasonCode) {
      setReasonModalOpen(true)
      return
    }

    setLoading(true)
    try {
      const request: MarkServicedRequest | undefined = reasonCode ? { reasonCode } : undefined
      await api.post<ServiceRecord, MarkServicedRequest | undefined>(`/equipment/${equipment.id}/service`, request)
      toast({
        title: t('common.success'),
        description: t('scanner.serviced'),
      })
      setReasonModalOpen(false)
      handleLookup(equipment.serialNumber)
    } catch (err) {
      toast({
        title: t('common.error'),
        description: err instanceof Error ? err.message : 'Failed to mark as serviced',
        variant: 'destructive',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleManualSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    handleLookup(manualInput)
    setManualInput('')
  }

  return (
    <div className="space-y-6 max-w-md mx-auto">
      <h1 className="text-2xl font-bold">{t('scanner.title')}</h1>

      <div className="flex gap-2">
        <Button
          variant={mode === 'scan' ? 'default' : 'outline'}
          onClick={() => setMode('scan')}
          className="flex-1 gap-2"
        >
          <QrCode className="h-4 w-4" />
          {t('scanner.scanBarcode')}
        </Button>
        <Button
          variant={mode === 'manual' ? 'default' : 'outline'}
          onClick={() => {
            setMode('manual')
            stopScanning()
          }}
          className="flex-1 gap-2"
        >
          <Keyboard className="h-4 w-4" />
          {t('scanner.manualEntry')}
        </Button>
      </div>

      {mode === 'scan' && hasCamera && (
        <Card>
          <CardContent className="p-4">
            <div id={elementId} className="w-full" />
            {!isScanning && (
              <Button onClick={startScanning} className="w-full mt-4">
                {t('scanner.scanBarcode')}
              </Button>
            )}
            {isScanning && (
              <Button onClick={stopScanning} variant="outline" className="w-full mt-4">
                Stop
              </Button>
            )}
          </CardContent>
        </Card>
      )}

      {(mode === 'manual' || !hasCamera) && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">{t('scanner.manualEntry')}</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleManualSubmit} className="flex gap-2">
              <Input
                value={manualInput}
                onChange={(e) => setManualInput(e.target.value)}
                placeholder={t('scanner.enterSerial')}
              />
              <Button type="submit" disabled={loading}>
                {t('scanner.lookup')}
              </Button>
            </form>
          </CardContent>
        </Card>
      )}

      {loading && (
        <div className="flex justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
        </div>
      )}

      {error && (
        <Card className="border-destructive">
          <CardContent className="py-4 text-center text-destructive">
            {error}
          </CardContent>
        </Card>
      )}

      {equipment && (
        <EquipmentCard
          equipment={equipment}
          onMarkServiced={() => handleMarkServiced()}
          loading={loading}
        />
      )}

      <ReasonCodeModal
        open={reasonModalOpen}
        onClose={() => setReasonModalOpen(false)}
        onConfirm={(reasonCode) => handleMarkServiced(reasonCode)}
        loading={loading}
      />
    </div>
  )
}
