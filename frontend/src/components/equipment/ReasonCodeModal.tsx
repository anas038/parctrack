import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import type { ReasonCode } from '@/types'
import { AlertTriangle } from 'lucide-react'

interface ReasonCodeModalProps {
  open: boolean
  onClose: () => void
  onConfirm: (reasonCode: ReasonCode) => void
  loading?: boolean
}

const REASON_CODES: { value: ReasonCode; label: string; description: string }[] = [
  { value: 'EMERGENCY', label: 'Emergency', description: 'Emergency situation requiring immediate service' },
  { value: 'CUSTOMER_REQUEST', label: 'Customer Request', description: 'Customer specifically requested service' },
  { value: 'TECHNICIAN_DISCRETION', label: 'Technician Discretion', description: 'Technician judgment call' },
  { value: 'OTHER', label: 'Other', description: 'Other reason (audit trail purposes)' },
]

export function ReasonCodeModal({ open, onClose, onConfirm, loading }: ReasonCodeModalProps) {
  const { t } = useTranslation()
  const [selectedCode, setSelectedCode] = useState<ReasonCode | null>(null)

  const handleConfirm = () => {
    if (selectedCode) {
      onConfirm(selectedCode)
    }
  }

  const handleClose = () => {
    setSelectedCode(null)
    onClose()
  }

  return (
    <Dialog open={open} onOpenChange={(isOpen) => !isOpen && handleClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <AlertTriangle className="h-5 w-5 text-red-500" />
            Reason Required
          </DialogTitle>
          <DialogDescription>
            This equipment has RED status. Please select a reason for servicing.
          </DialogDescription>
        </DialogHeader>
        <div className="py-4">
          <RadioGroup
            value={selectedCode || undefined}
            onValueChange={(v) => setSelectedCode(v as ReasonCode)}
          >
            {REASON_CODES.map((code) => (
              <div key={code.value} className="flex items-start space-x-3 p-3 rounded-lg hover:bg-muted/50 cursor-pointer">
                <RadioGroupItem value={code.value} id={code.value} className="mt-1" />
                <Label htmlFor={code.value} className="cursor-pointer flex-1">
                  <div className="font-medium">{code.label}</div>
                  <div className="text-sm text-muted-foreground">{code.description}</div>
                </Label>
              </div>
            ))}
          </RadioGroup>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={handleClose}>
            {t('common.cancel')}
          </Button>
          <Button onClick={handleConfirm} disabled={!selectedCode || loading}>
            {loading ? t('common.loading') : 'Confirm Service'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
