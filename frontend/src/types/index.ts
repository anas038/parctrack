export type Role = 'TECHNICIAN' | 'MANAGER' | 'ADMIN'
export type AgreementStatus = 'COVERED' | 'OUT_OF_SCOPE' | 'PENDING'
export type ServiceCycle = 'MONTHLY' | 'QUARTERLY' | 'SEMESTERLY' | 'ANNUALLY'
export type StoplightStatus = 'GREEN' | 'YELLOW' | 'RED'

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

export interface Equipment {
  id: string
  serialNumber: string
  custAssetId: string | null
  agreementStatus: AgreementStatus
  serviceCycle: ServiceCycle
  lastService: string | null
  nextService: string | null
  nextServiceOverride: boolean
  stoplightStatus: StoplightStatus
  createdAt: string
  updatedAt: string
}

export interface DashboardSummary {
  totalEquipment: number
  greenCount: number
  yellowCount: number
  redCount: number
  overdueCount: number
  warningCount: number
  compliancePercentage: number
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
