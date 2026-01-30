import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { api } from '@/lib/api'
import type { DashboardSummary, Customer, Site, EquipmentType } from '@/types'
import { AlertTriangle } from 'lucide-react'

export function DashboardPage() {
  const { t } = useTranslation()
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [orphanedCount, setOrphanedCount] = useState(0)

  // Filters
  const [customers, setCustomers] = useState<Customer[]>([])
  const [sites, setSites] = useState<Site[]>([])
  const [equipmentTypes, setEquipmentTypes] = useState<EquipmentType[]>([])
  const [selectedCustomer, setSelectedCustomer] = useState<string>('')
  const [selectedSite, setSelectedSite] = useState<string>('')
  const [selectedType, setSelectedType] = useState<string>('')

  useEffect(() => {
    loadFilters()
    loadSummary()
    loadOrphanedCount()
  }, [])

  useEffect(() => {
    loadSummary()
  }, [selectedCustomer, selectedSite, selectedType])

  useEffect(() => {
    if (selectedCustomer) {
      loadSites(selectedCustomer)
      setSelectedSite('')
    } else {
      setSites([])
      setSelectedSite('')
    }
  }, [selectedCustomer])

  const loadFilters = async () => {
    try {
      const [customersData, typesData] = await Promise.all([
        api.get<Customer[]>('/customers/all'),
        api.get<EquipmentType[]>('/equipment-types/active'),
      ])
      setCustomers(customersData)
      setEquipmentTypes(typesData)
    } catch (err) {
      console.error('Failed to load filters', err)
    }
  }

  const loadSites = async (customerId: string) => {
    try {
      const data = await api.get<Site[]>(`/sites/all?customerId=${customerId}`)
      setSites(data)
    } catch (err) {
      console.error('Failed to load sites', err)
    }
  }

  const loadSummary = async () => {
    try {
      const params = new URLSearchParams()
      if (selectedCustomer) params.set('customerId', selectedCustomer)
      if (selectedSite) params.set('siteId', selectedSite)
      if (selectedType) params.set('equipmentTypeId', selectedType)

      const url = params.toString() ? `/dashboard/summary?${params}` : '/dashboard/summary'
      const data = await api.get<DashboardSummary>(url)
      setSummary(data)
    } catch (err) {
      console.error('Failed to load dashboard', err)
    } finally {
      setLoading(false)
    }
  }

  const loadOrphanedCount = async () => {
    try {
      const count = await api.get<number>('/equipment/orphaned/count')
      setOrphanedCount(count)
    } catch (err) {
      console.error('Failed to load orphaned count', err)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    )
  }

  if (!summary) {
    return <div>{t('common.error')}</div>
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">{t('dashboard.title')}</h1>

      {/* Orphaned Equipment Warning */}
      {orphanedCount > 0 && (
        <Alert variant="destructive">
          <AlertTriangle className="h-4 w-4" />
          <AlertTitle>Orphaned Equipment</AlertTitle>
          <AlertDescription>
            {orphanedCount} equipment {orphanedCount === 1 ? 'item has' : 'items have'} no assigned site.{' '}
            <Link to="/equipment?orphaned=true" className="underline font-medium">
              View orphaned equipment
            </Link>
          </AlertDescription>
        </Alert>
      )}

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-wrap gap-4">
            <Select value={selectedCustomer} onValueChange={setSelectedCustomer}>
              <SelectTrigger className="w-[200px]">
                <SelectValue placeholder="All Customers" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">All Customers</SelectItem>
                {customers.map((c) => (
                  <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Select value={selectedSite} onValueChange={setSelectedSite} disabled={!selectedCustomer}>
              <SelectTrigger className="w-[200px]">
                <SelectValue placeholder="All Sites" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">All Sites</SelectItem>
                {sites.map((s) => (
                  <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Select value={selectedType} onValueChange={setSelectedType}>
              <SelectTrigger className="w-[200px]">
                <SelectValue placeholder="All Types" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">All Types</SelectItem>
                {equipmentTypes.map((et) => (
                  <SelectItem key={et.id} value={et.id}>{et.name}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              {t('dashboard.totalEquipment')}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{summary.totalEquipment}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              {t('dashboard.upToDate')}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-stoplight-green">{summary.greenCount}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              {t('dashboard.needsAttention')}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-stoplight-yellow">{summary.yellowCount}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              {t('dashboard.overdue')}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold text-stoplight-red">{summary.redCount}</div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{t('dashboard.compliance')}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-5xl font-bold text-center py-8">
            {summary.compliancePercentage.toFixed(1)}%
          </div>
          <div className="w-full bg-muted rounded-full h-4 overflow-hidden">
            <div
              className="bg-stoplight-green h-full transition-all"
              style={{ width: `${summary.compliancePercentage}%` }}
            />
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">{t('dashboard.overdueEquipment')}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold text-stoplight-red">{summary.overdueCount}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg">{t('dashboard.warningEquipment')}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-4xl font-bold text-stoplight-yellow">{summary.warningCount}</div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
