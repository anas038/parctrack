export type Role = 'TECHNICIAN' | 'MANAGER' | 'ADMIN'
export type AgreementStatus = 'COVERED' | 'OUT_OF_SCOPE' | 'PENDING'
export type ServiceCycle = 'MONTHLY' | 'QUARTERLY' | 'SEMESTERLY' | 'ANNUALLY'
export type StoplightStatus = 'GREEN' | 'YELLOW' | 'RED'
export type LifecycleStatus = 'ACTIVE' | 'UNDER_REPAIR' | 'PENDING_INSTALLATION' | 'RETIRED'
export type ReasonCode = 'EMERGENCY' | 'CUSTOMER_REQUEST' | 'TECHNICIAN_DISCRETION' | 'OTHER'

export interface User {
  id: string
  email: string
  username: string
  role: Role
  organizationId: string
  organizationName: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

export interface Customer {
  id: string
  name: string
  agreementStatus: AgreementStatus
  contractEndDate: string | null
  createdAt: string
  updatedAt: string
}

export interface Site {
  id: string
  customerId: string
  customerName: string
  name: string
  address: string | null
  contactName: string | null
  contactPhone: string | null
  metadata: Record<string, unknown>
  createdAt: string
  updatedAt: string
}

export interface EquipmentType {
  id: string
  name: string
  description: string | null
  displayOrder: number
  active: boolean
  createdAt: string
}

export interface ServiceRecord {
  id: string
  equipmentId: string
  servicedAt: string
  servicedByUserId: string
  servicedByName: string
  reasonCode: ReasonCode | null
  createdAt: string
}

export interface EquipmentSiteInfo {
  id: string
  name: string
  customer: {
    id: string
    name: string
    agreementStatus: AgreementStatus
  }
}

export interface EquipmentTypeInfo {
  id: string
  name: string
}

export interface Equipment {
  id: string
  serialNumber: string
  custAssetId: string | null
  qrCodeValue: string | null
  agreementStatus: AgreementStatus
  lifecycleStatus: LifecycleStatus
  serviceCycle: ServiceCycle
  lastService: string | null
  nextService: string | null
  nextServiceOverride: boolean
  stoplightStatus: StoplightStatus
  provisional: boolean
  provisionalExpiresAt: string | null
  predecessorId: string | null
  site: EquipmentSiteInfo | null
  equipmentType: EquipmentTypeInfo | null
  createdAt: string
  updatedAt: string
}

export interface EquipmentHistory {
  recentServices: ServiceRecord[]
  monthlySummaries: Array<{
    month: string
    serviceCount: number
  }>
}

export interface DashboardSummary {
  totalEquipment: number
  greenCount: number
  yellowCount: number
  redCount: number
  overdueCount: number
  warningCount: number
  compliancePercentage: number
  orphanedCount?: number
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface ImportResult {
  totalRows: number
  successCount: number
  errorCount: number
  errors: Array<{
    row: number
    field: string | null
    message: string
  }>
}

export interface BulkOperationResult {
  successCount: number
  failureCount: number
  message: string
}

// Request types
export interface CreateCustomerRequest {
  name: string
  agreementStatus: AgreementStatus
  contractEndDate?: string
}

export interface UpdateCustomerRequest {
  name?: string
  agreementStatus?: AgreementStatus
  contractEndDate?: string
}

export interface CreateSiteRequest {
  customerId: string
  name: string
  address?: string
  contactName?: string
  contactPhone?: string
  metadata?: Record<string, unknown>
}

export interface UpdateSiteRequest {
  customerId?: string
  name?: string
  address?: string
  contactName?: string
  contactPhone?: string
  metadata?: Record<string, unknown>
}

export interface CreateEquipmentTypeRequest {
  name: string
  description?: string
  displayOrder?: number
}

export interface UpdateEquipmentTypeRequest {
  name?: string
  description?: string
  displayOrder?: number
  active?: boolean
}

export interface MarkServicedRequest {
  reasonCode?: ReasonCode
}
