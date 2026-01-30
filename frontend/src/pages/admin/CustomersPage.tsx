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
import type { Customer, Page, AgreementStatus, CreateCustomerRequest, UpdateCustomerRequest } from '@/types'
import { Plus, Pencil, Trash2 } from 'lucide-react'

export function CustomersPage() {
  const { t } = useTranslation()
  const { toast } = useToast()

  const [customers, setCustomers] = useState<Customer[]>([])
  const [loading, setLoading] = useState(true)
  const [totalPages, setTotalPages] = useState(0)
  const [page, setPage] = useState(0)

  const [dialogOpen, setDialogOpen] = useState(false)
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null)
  const [formData, setFormData] = useState<CreateCustomerRequest>({
    name: '',
    agreementStatus: 'COVERED',
    contractEndDate: undefined,
  })

  useEffect(() => {
    loadCustomers()
  }, [page])

  const loadCustomers = async () => {
    setLoading(true)
    try {
      const data = await api.get<Page<Customer>>(`/customers?page=${page}&size=20`)
      setCustomers(data.content)
      setTotalPages(data.totalPages)
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Failed to load customers',
        variant: 'destructive',
      })
    } finally {
      setLoading(false)
    }
  }

  const openCreateDialog = () => {
    setEditingCustomer(null)
    setFormData({ name: '', agreementStatus: 'COVERED', contractEndDate: undefined })
    setDialogOpen(true)
  }

  const openEditDialog = (customer: Customer) => {
    setEditingCustomer(customer)
    setFormData({
      name: customer.name,
      agreementStatus: customer.agreementStatus,
      contractEndDate: customer.contractEndDate || undefined,
    })
    setDialogOpen(true)
  }

  const handleSubmit = async () => {
    try {
      if (editingCustomer) {
        await api.put<Customer, UpdateCustomerRequest>(`/customers/${editingCustomer.id}`, formData)
        toast({ title: t('common.success'), description: 'Customer updated' })
      } else {
        await api.post<Customer, CreateCustomerRequest>('/customers', formData)
        toast({ title: t('common.success'), description: 'Customer created' })
      }
      setDialogOpen(false)
      loadCustomers()
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Operation failed',
        variant: 'destructive',
      })
    }
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure? This will cascade to all sites and orphan equipment.')) return

    try {
      await api.delete(`/customers/${id}`)
      toast({ title: t('common.success'), description: 'Customer deleted' })
      loadCustomers()
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Delete failed',
        variant: 'destructive',
      })
    }
  }

  const getStatusBadgeClass = (status: AgreementStatus) => {
    switch (status) {
      case 'COVERED':
        return 'bg-green-100 text-green-800'
      case 'OUT_OF_SCOPE':
        return 'bg-red-100 text-red-800'
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800'
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Customers</h1>
        <Button onClick={openCreateDialog}>
          <Plus className="h-4 w-4 mr-2" />
          Add Customer
        </Button>
      </div>

      <Card>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="border-b bg-muted/50">
                <tr>
                  <th className="p-3 text-left">Name</th>
                  <th className="p-3 text-left">Agreement Status</th>
                  <th className="p-3 text-left">Contract End Date</th>
                  <th className="p-3 text-left">Created</th>
                  <th className="p-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan={5} className="p-8 text-center">
                      <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary mx-auto" />
                    </td>
                  </tr>
                ) : customers.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="p-8 text-center text-muted-foreground">
                      No customers found
                    </td>
                  </tr>
                ) : (
                  customers.map((customer) => (
                    <tr key={customer.id} className="border-b hover:bg-muted/50">
                      <td className="p-3 font-medium">{customer.name}</td>
                      <td className="p-3">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusBadgeClass(customer.agreementStatus)}`}>
                          {customer.agreementStatus.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="p-3">{formatDate(customer.contractEndDate)}</td>
                      <td className="p-3">{formatDate(customer.createdAt)}</td>
                      <td className="p-3 text-right">
                        <Button variant="ghost" size="sm" onClick={() => openEditDialog(customer)}>
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="sm" onClick={() => handleDelete(customer.id)}>
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
            <DialogTitle>{editingCustomer ? 'Edit Customer' : 'New Customer'}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="name">Name</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="agreementStatus">Agreement Status</Label>
              <Select
                value={formData.agreementStatus}
                onValueChange={(v) => setFormData({ ...formData, agreementStatus: v as AgreementStatus })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="COVERED">Covered</SelectItem>
                  <SelectItem value="OUT_OF_SCOPE">Out of Scope</SelectItem>
                  <SelectItem value="PENDING">Pending</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="contractEndDate">Contract End Date</Label>
              <Input
                id="contractEndDate"
                type="date"
                value={formData.contractEndDate || ''}
                onChange={(e) => setFormData({ ...formData, contractEndDate: e.target.value || undefined })}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button onClick={handleSubmit}>
              {editingCustomer ? 'Save' : 'Create'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
