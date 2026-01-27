import { useEffect, useState, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent } from '@/components/ui/card'
import { Checkbox } from '@/components/ui/checkbox'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Stoplight } from '@/components/equipment/Stoplight'
import { useToast } from '@/components/ui/toast'
import { api } from '@/lib/api'
import { formatDate } from '@/lib/utils'
import type { Equipment, Page, AgreementStatus, ServiceCycle } from '@/types'
import { Search, Download, Upload, Trash2 } from 'lucide-react'

export function EquipmentPage() {
  const { t } = useTranslation()
  const { toast } = useToast()
  const fileInputRef = useRef<HTMLInputElement>(null)

  const [equipment, setEquipment] = useState<Equipment[]>([])
  const [loading, setLoading] = useState(true)
  const [totalPages, setTotalPages] = useState(0)
  const [page, setPage] = useState(0)
  const [searchQuery, setSearchQuery] = useState('')
  const [agreementFilter, setAgreementFilter] = useState<AgreementStatus | ''>('')
  const [cycleFilter, setCycleFilter] = useState<ServiceCycle | ''>('')
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set())
  const [importDialogOpen, setImportDialogOpen] = useState(false)
  const [upsertMode, setUpsertMode] = useState(false)

  useEffect(() => {
    loadEquipment()
  }, [page, agreementFilter, cycleFilter])

  const loadEquipment = async () => {
    setLoading(true)
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: '20',
      })
      if (searchQuery) params.set('searchQuery', searchQuery)
      if (agreementFilter) params.set('agreementStatus', agreementFilter)
      if (cycleFilter) params.set('serviceCycle', cycleFilter)

      const data = await api.get<Page<Equipment>>(`/equipment?${params}`)
      setEquipment(data.content)
      setTotalPages(data.totalPages)
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Failed to load equipment',
        variant: 'destructive',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setPage(0)
    loadEquipment()
  }

  const toggleSelect = (id: string) => {
    const newSet = new Set(selectedIds)
    if (newSet.has(id)) {
      newSet.delete(id)
    } else {
      newSet.add(id)
    }
    setSelectedIds(newSet)
  }

  const toggleSelectAll = () => {
    if (selectedIds.size === equipment.length) {
      setSelectedIds(new Set())
    } else {
      setSelectedIds(new Set(equipment.map((e) => e.id)))
    }
  }

  const handleBulkDelete = async () => {
    if (selectedIds.size === 0) return

    try {
      await api.post('/equipment/bulk/delete', { ids: Array.from(selectedIds) })
      toast({
        title: t('common.success'),
        description: `Deleted ${selectedIds.size} items`,
      })
      setSelectedIds(new Set())
      loadEquipment()
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Failed to delete equipment',
        variant: 'destructive',
      })
    }
  }

  const handleExport = async (format: 'xlsx' | 'pdf') => {
    try {
      const params: Record<string, string> = { format }
      if (agreementFilter) params.agreementStatus = agreementFilter
      if (cycleFilter) params.serviceCycle = cycleFilter
      if (searchQuery) params.searchQuery = searchQuery

      const blob = await api.download('/equipment/export', params)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `equipment.${format}`
      a.click()
      window.URL.revokeObjectURL(url)
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Export failed',
        variant: 'destructive',
      })
    }
  }

  const handleImport = async (file: File) => {
    try {
      const result = await api.upload('/equipment/import', file, { upsert: upsertMode.toString() })
      toast({
        title: t('common.success'),
        description: `Imported ${(result as { successCount: number }).successCount} items`,
      })
      setImportDialogOpen(false)
      loadEquipment()
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Import failed',
        variant: 'destructive',
      })
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">{t('equipment.title')}</h1>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => setImportDialogOpen(true)}>
            <Upload className="h-4 w-4 mr-2" />
            {t('common.import')}
          </Button>
          <Button variant="outline" onClick={() => handleExport('xlsx')}>
            <Download className="h-4 w-4 mr-2" />
            Excel
          </Button>
          <Button variant="outline" onClick={() => handleExport('pdf')}>
            <Download className="h-4 w-4 mr-2" />
            PDF
          </Button>
        </div>
      </div>

      <Card>
        <CardContent className="p-4">
          <form onSubmit={handleSearch} className="flex flex-wrap gap-4">
            <div className="flex-1 min-w-[200px]">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder={t('common.search')}
                  className="pl-9"
                />
              </div>
            </div>
            <Select value={agreementFilter} onValueChange={(v) => setAgreementFilter(v as AgreementStatus | '')}>
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder={t('equipment.agreementStatus')} />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">All</SelectItem>
                <SelectItem value="COVERED">{t('equipment.covered')}</SelectItem>
                <SelectItem value="OUT_OF_SCOPE">{t('equipment.outOfScope')}</SelectItem>
                <SelectItem value="PENDING">{t('equipment.pending')}</SelectItem>
              </SelectContent>
            </Select>
            <Select value={cycleFilter} onValueChange={(v) => setCycleFilter(v as ServiceCycle | '')}>
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder={t('equipment.serviceCycle')} />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">All</SelectItem>
                <SelectItem value="MONTHLY">{t('equipment.monthly')}</SelectItem>
                <SelectItem value="QUARTERLY">{t('equipment.quarterly')}</SelectItem>
                <SelectItem value="SEMESTERLY">{t('equipment.semesterly')}</SelectItem>
                <SelectItem value="ANNUALLY">{t('equipment.annually')}</SelectItem>
              </SelectContent>
            </Select>
            <Button type="submit">{t('common.search')}</Button>
          </form>
        </CardContent>
      </Card>

      {selectedIds.size > 0 && (
        <div className="flex gap-2 items-center">
          <span className="text-sm text-muted-foreground">
            {selectedIds.size} selected
          </span>
          <Button variant="destructive" size="sm" onClick={handleBulkDelete}>
            <Trash2 className="h-4 w-4 mr-2" />
            {t('equipment.bulkDelete')}
          </Button>
        </div>
      )}

      <Card>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="border-b bg-muted/50">
                <tr>
                  <th className="p-3 text-left">
                    <Checkbox
                      checked={selectedIds.size === equipment.length && equipment.length > 0}
                      onCheckedChange={toggleSelectAll}
                    />
                  </th>
                  <th className="p-3 text-left">{t('equipment.status')}</th>
                  <th className="p-3 text-left">{t('equipment.serialNumber')}</th>
                  <th className="p-3 text-left">{t('equipment.assetId')}</th>
                  <th className="p-3 text-left">{t('equipment.agreementStatus')}</th>
                  <th className="p-3 text-left">{t('equipment.serviceCycle')}</th>
                  <th className="p-3 text-left">{t('equipment.lastService')}</th>
                  <th className="p-3 text-left">{t('equipment.nextService')}</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan={8} className="p-8 text-center">
                      <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary mx-auto" />
                    </td>
                  </tr>
                ) : equipment.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="p-8 text-center text-muted-foreground">
                      No equipment found
                    </td>
                  </tr>
                ) : (
                  equipment.map((item) => (
                    <tr key={item.id} className="border-b hover:bg-muted/50">
                      <td className="p-3">
                        <Checkbox
                          checked={selectedIds.has(item.id)}
                          onCheckedChange={() => toggleSelect(item.id)}
                        />
                      </td>
                      <td className="p-3">
                        <Stoplight status={item.stoplightStatus} size="sm" />
                      </td>
                      <td className="p-3 font-medium">{item.serialNumber}</td>
                      <td className="p-3">{item.custAssetId || '-'}</td>
                      <td className="p-3">{t(`equipment.${item.agreementStatus.toLowerCase()}`)}</td>
                      <td className="p-3">{t(`equipment.${item.serviceCycle.toLowerCase()}`)}</td>
                      <td className="p-3">{formatDate(item.lastService)}</td>
                      <td className="p-3">{formatDate(item.nextService)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>

      {totalPages > 1 && (
        <div className="flex justify-center gap-2">
          <Button
            variant="outline"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
          >
            Previous
          </Button>
          <span className="flex items-center px-4">
            Page {page + 1} of {totalPages}
          </span>
          <Button
            variant="outline"
            disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
          >
            Next
          </Button>
        </div>
      )}

      <Dialog open={importDialogOpen} onOpenChange={setImportDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t('equipment.importEquipment')}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="flex items-center gap-2">
              <Checkbox
                id="upsert"
                checked={upsertMode}
                onCheckedChange={(c) => setUpsertMode(c as boolean)}
              />
              <label htmlFor="upsert" className="text-sm">
                {t('equipment.upsert')}
              </label>
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept=".xlsx,.xls,.csv"
              className="hidden"
              onChange={(e) => {
                const file = e.target.files?.[0]
                if (file) handleImport(file)
              }}
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setImportDialogOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button onClick={() => fileInputRef.current?.click()}>
              {t('common.import')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
