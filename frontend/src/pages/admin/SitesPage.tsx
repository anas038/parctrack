import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent } from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { useToast } from '@/components/ui/toast'
import { api } from '@/lib/api'
import { formatDate } from '@/lib/utils'
import type { Site, Customer, Page, CreateSiteRequest, UpdateSiteRequest } from '@/types'
import { Plus, Pencil, Trash2 } from 'lucide-react'

export function SitesPage() {
  const { t } = useTranslation()
  const { toast } = useToast()

  const [sites, setSites] = useState<Site[]>([])
  const [customers, setCustomers] = useState<Customer[]>([])
  const [loading, setLoading] = useState(true)
  const [totalPages, setTotalPages] = useState(0)
  const [page, setPage] = useState(0)
  const [customerFilter, setCustomerFilter] = useState<string>('')

  const [dialogOpen, setDialogOpen] = useState(false)
  const [editingSite, setEditingSite] = useState<Site | null>(null)
  const [formData, setFormData] = useState<CreateSiteRequest>({
    customerId: '',
    name: '',
    address: undefined,
    contactName: undefined,
    contactPhone: undefined,
  })

  useEffect(() => {
    loadCustomers()
  }, [])

  useEffect(() => {
    loadSites()
  }, [page, customerFilter])

  const loadCustomers = async () => {
    try {
      const data = await api.get<Customer[]>('/customers/all')
      setCustomers(data)
    } catch (err) {
      console.error('Failed to load customers', err)
    }
  }

  const loadSites = async () => {
    setLoading(true)
    try {
      const params = new URLSearchParams({ page: page.toString(), size: '20' })
      if (customerFilter) params.set('customerId', customerFilter)

      const data = await api.get<Page<Site>>(`/sites?${params}`)
      setSites(data.content)
      setTotalPages(data.totalPages)
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Failed to load sites',
        variant: 'destructive',
      })
    } finally {
      setLoading(false)
    }
  }

  const openCreateDialog = () => {
    setEditingSite(null)
    setFormData({
      customerId: customerFilter || customers[0]?.id || '',
      name: '',
      address: undefined,
      contactName: undefined,
      contactPhone: undefined,
    })
    setDialogOpen(true)
  }

  const openEditDialog = (site: Site) => {
    setEditingSite(site)
    setFormData({
      customerId: site.customerId,
      name: site.name,
      address: site.address || undefined,
      contactName: site.contactName || undefined,
      contactPhone: site.contactPhone || undefined,
    })
    setDialogOpen(true)
  }

  const handleSubmit = async () => {
    try {
      if (editingSite) {
        const updateData: UpdateSiteRequest = {
          name: formData.name,
          address: formData.address,
          contactName: formData.contactName,
          contactPhone: formData.contactPhone,
        }
        if (formData.customerId !== editingSite.customerId) {
          updateData.customerId = formData.customerId
        }
        await api.put<Site, UpdateSiteRequest>(`/sites/${editingSite.id}`, updateData)
        toast({ title: t('common.success'), description: 'Site updated' })
      } else {
        await api.post<Site, CreateSiteRequest>('/sites', formData)
        toast({ title: t('common.success'), description: 'Site created' })
      }
      setDialogOpen(false)
      loadSites()
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Operation failed',
        variant: 'destructive',
      })
    }
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure? This will orphan all equipment at this site.')) return

    try {
      await api.delete(`/sites/${id}`)
      toast({ title: t('common.success'), description: 'Site deleted' })
      loadSites()
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Delete failed',
        variant: 'destructive',
      })
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Sites</h1>
        <Button onClick={openCreateDialog}>
          <Plus className="h-4 w-4 mr-2" />
          Add Site
        </Button>
      </div>

      <Card>
        <CardContent className="p-4">
          <div className="flex gap-4">
            <Select value={customerFilter} onValueChange={setCustomerFilter}>
              <SelectTrigger className="w-[250px]">
                <SelectValue placeholder="Filter by customer" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">All Customers</SelectItem>
                {customers.map((c) => (
                  <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="border-b bg-muted/50">
                <tr>
                  <th className="p-3 text-left">Name</th>
                  <th className="p-3 text-left">Customer</th>
                  <th className="p-3 text-left">Address</th>
                  <th className="p-3 text-left">Contact</th>
                  <th className="p-3 text-left">Created</th>
                  <th className="p-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan={6} className="p-8 text-center">
                      <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary mx-auto" />
                    </td>
                  </tr>
                ) : sites.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="p-8 text-center text-muted-foreground">
                      No sites found
                    </td>
                  </tr>
                ) : (
                  sites.map((site) => (
                    <tr key={site.id} className="border-b hover:bg-muted/50">
                      <td className="p-3 font-medium">{site.name}</td>
                      <td className="p-3">{site.customerName}</td>
                      <td className="p-3">{site.address || '-'}</td>
                      <td className="p-3">
                        {site.contactName && (
                          <div className="text-sm">
                            <div>{site.contactName}</div>
                            {site.contactPhone && (
                              <div className="text-muted-foreground">{site.contactPhone}</div>
                            )}
                          </div>
                        )}
                        {!site.contactName && '-'}
                      </td>
                      <td className="p-3">{formatDate(site.createdAt)}</td>
                      <td className="p-3 text-right">
                        <Button variant="ghost" size="sm" onClick={() => openEditDialog(site)}>
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="sm" onClick={() => handleDelete(site.id)}>
                          <Trash2 className="h-4 w-4 text-destructive" />
                        </Button>
                      </td>
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
          <Button variant="outline" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </Button>
          <span className="flex items-center px-4">
            Page {page + 1} of {totalPages}
          </span>
          <Button variant="outline" disabled={page >= totalPages - 1} onClick={() => setPage((p) => p + 1)}>
            Next
          </Button>
        </div>
      )}

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingSite ? 'Edit Site' : 'New Site'}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="customerId">Customer</Label>
              <Select
                value={formData.customerId}
                onValueChange={(v) => setFormData({ ...formData, customerId: v })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select customer" />
                </SelectTrigger>
                <SelectContent>
                  {customers.map((c) => (
                    <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="address">Address</Label>
              <Input
                id="address"
                value={formData.address || ''}
                onChange={(e) => setFormData({ ...formData, address: e.target.value || undefined })}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="contactName">Contact Name</Label>
                <Input
                  id="contactName"
                  value={formData.contactName || ''}
                  onChange={(e) => setFormData({ ...formData, contactName: e.target.value || undefined })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="contactPhone">Contact Phone</Label>
                <Input
                  id="contactPhone"
                  value={formData.contactPhone || ''}
                  onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value || undefined })}
                />
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button onClick={handleSubmit}>
              {editingSite ? 'Save' : 'Create'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
