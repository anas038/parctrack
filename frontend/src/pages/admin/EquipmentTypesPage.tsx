import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent } from '@/components/ui/card'
import { Switch } from '@/components/ui/switch'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { useToast } from '@/components/ui/toast'
import { api } from '@/lib/api'
import type { EquipmentType, CreateEquipmentTypeRequest, UpdateEquipmentTypeRequest } from '@/types'
import { Plus, Pencil, Trash2, GripVertical } from 'lucide-react'

export function EquipmentTypesPage() {
  const { t } = useTranslation()
  const { toast } = useToast()

  const [equipmentTypes, setEquipmentTypes] = useState<EquipmentType[]>([])
  const [loading, setLoading] = useState(true)
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null)

  const [dialogOpen, setDialogOpen] = useState(false)
  const [editingType, setEditingType] = useState<EquipmentType | null>(null)
  const [formData, setFormData] = useState<CreateEquipmentTypeRequest>({
    name: '',
    description: undefined,
  })

  useEffect(() => {
    loadEquipmentTypes()
  }, [])

  const loadEquipmentTypes = async () => {
    setLoading(true)
    try {
      const data = await api.get<EquipmentType[]>('/equipment-types/all')
      setEquipmentTypes(data)
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Failed to load equipment types',
        variant: 'destructive',
      })
    } finally {
      setLoading(false)
    }
  }

  const openCreateDialog = () => {
    setEditingType(null)
    setFormData({ name: '', description: undefined })
    setDialogOpen(true)
  }

  const openEditDialog = (type: EquipmentType) => {
    setEditingType(type)
    setFormData({
      name: type.name,
      description: type.description || undefined,
    })
    setDialogOpen(true)
  }

  const handleSubmit = async () => {
    try {
      if (editingType) {
        await api.put<EquipmentType, UpdateEquipmentTypeRequest>(`/equipment-types/${editingType.id}`, formData)
        toast({ title: t('common.success'), description: 'Equipment type updated' })
      } else {
        await api.post<EquipmentType, CreateEquipmentTypeRequest>('/equipment-types', formData)
        toast({ title: t('common.success'), description: 'Equipment type created' })
      }
      setDialogOpen(false)
      loadEquipmentTypes()
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Operation failed',
        variant: 'destructive',
      })
    }
  }

  const handleToggleActive = async (type: EquipmentType) => {
    try {
      await api.put<EquipmentType, UpdateEquipmentTypeRequest>(`/equipment-types/${type.id}`, {
        active: !type.active,
      })
      loadEquipmentTypes()
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Failed to update',
        variant: 'destructive',
      })
    }
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this equipment type?')) return

    try {
      await api.delete(`/equipment-types/${id}`)
      toast({ title: t('common.success'), description: 'Equipment type deleted' })
      loadEquipmentTypes()
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Delete failed',
        variant: 'destructive',
      })
    }
  }

  const handleDragStart = (index: number) => {
    setDraggedIndex(index)
  }

  const handleDragOver = (e: React.DragEvent, index: number) => {
    e.preventDefault()
    if (draggedIndex === null || draggedIndex === index) return

    const newList = [...equipmentTypes]
    const [draggedItem] = newList.splice(draggedIndex, 1)
    newList.splice(index, 0, draggedItem)
    setEquipmentTypes(newList)
    setDraggedIndex(index)
  }

  const handleDragEnd = async () => {
    if (draggedIndex === null) return

    try {
      await api.post('/equipment-types/reorder', {
        orderedIds: equipmentTypes.map((t) => t.id),
      })
      toast({ title: t('common.success'), description: 'Order saved' })
    } catch (err) {
      toast({
        title: t('common.error'),
        description: 'Failed to save order',
        variant: 'destructive',
      })
      loadEquipmentTypes()
    }
    setDraggedIndex(null)
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Equipment Types</h1>
        <Button onClick={openCreateDialog}>
          <Plus className="h-4 w-4 mr-2" />
          Add Type
        </Button>
      </div>

      <p className="text-sm text-muted-foreground">
        Drag and drop to reorder. Order affects dropdown menus.
      </p>

      <Card>
        <CardContent className="p-0">
          {loading ? (
            <div className="p-8 text-center">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary mx-auto" />
            </div>
          ) : equipmentTypes.length === 0 ? (
            <div className="p-8 text-center text-muted-foreground">
              No equipment types found
            </div>
          ) : (
            <ul className="divide-y">
              {equipmentTypes.map((type, index) => (
                <li
                  key={type.id}
                  draggable
                  onDragStart={() => handleDragStart(index)}
                  onDragOver={(e) => handleDragOver(e, index)}
                  onDragEnd={handleDragEnd}
                  className={`flex items-center justify-between p-4 cursor-move hover:bg-muted/50 ${
                    draggedIndex === index ? 'opacity-50' : ''
                  }`}
                >
                  <div className="flex items-center gap-4">
                    <GripVertical className="h-5 w-5 text-muted-foreground" />
                    <div>
                      <div className="font-medium">{type.name}</div>
                      {type.description && (
                        <div className="text-sm text-muted-foreground">{type.description}</div>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="flex items-center gap-2">
                      <span className="text-sm text-muted-foreground">Active</span>
                      <Switch
                        checked={type.active}
                        onCheckedChange={() => handleToggleActive(type)}
                      />
                    </div>
                    <Button variant="ghost" size="sm" onClick={() => openEditDialog(type)}>
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => handleDelete(type.id)}>
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingType ? 'Edit Equipment Type' : 'New Equipment Type'}</DialogTitle>
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
              <Label htmlFor="description">Description</Label>
              <Input
                id="description"
                value={formData.description || ''}
                onChange={(e) => setFormData({ ...formData, description: e.target.value || undefined })}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button onClick={handleSubmit}>
              {editingType ? 'Save' : 'Create'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
