# ParcTrack Technical Specification

**Version:** 1.0
**Status:** Specification Complete
**Based on:** PRD v1.0 + Detailed Interview
**Target Success Metric:** 90% Reduction in Maintenance Gaps

---

## 1. Executive Summary

ParcTrack is an equipment maintenance tracking system that enables field technicians to verify maintenance eligibility via scanning and provides managers with audit/reporting capabilities. The system uses a stoplight (Green/Yellow/Red) status indicator to communicate maintenance urgency at a glance.

### Key Design Decisions

| Decision Area | Choice | Rationale |
|---------------|--------|-----------|
| Service Verification | Instant single-tap | Minimal friction for field work |
| Agreement Data | ParcTrack as master | No external system dependencies |
| Notifications | None (pull-based) | Simplicity for MVP |
| Mobile Approach | Responsive web | Single codebase, no app store |
| Offline Support | Session caching only | 1-hour cache for connectivity gaps |

---

## 2. Data Model

### 2.1 Entity Hierarchy

```
Organization (implicit, single-tenant)
└── Customer
    └── Site
        └── Equipment
```

**Equipment can transfer between Sites** - the system updates the site reference in place, preserving full maintenance history with the equipment.

### 2.2 Core Entities

#### Organization
Single-tenant deployment for Phase 1. Schema designed to support future multi-tenancy with `tenant_id` column pattern.

#### Customer

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| name | String(255) | NOT NULL | Customer display name |
| agreement_status | Enum | NOT NULL | Covered, Out_of_Scope, Pending_Agreement |
| contract_end_date | Date | NULL | When agreement expires (triggers auto-transition) |
| created_at | Timestamp | NOT NULL | Record creation |
| updated_at | Timestamp | NOT NULL | Last modification |
| deleted_at | Timestamp | NULL | Soft delete marker |

#### Site

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| customer_id | UUID | FK, NOT NULL | Parent customer |
| name | String(255) | NOT NULL | Site display name |
| address | String(500) | NULL | Physical address |
| contact_name | String(255) | NULL | Primary site contact |
| contact_phone | String(50) | NULL | Contact phone number |
| metadata | JSONB | DEFAULT '{}' | Extensible key-value metadata |
| created_at | Timestamp | NOT NULL | Record creation |
| updated_at | Timestamp | NOT NULL | Last modification |
| deleted_at | Timestamp | NULL | Soft delete marker |

#### Equipment

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| serial_number | String(255) | NOT NULL | Manufacturer serial (free-form) |
| qr_code_value | String(255) | NULL | Value encoded in QR/barcode |
| customer_asset_id | String(255) | NULL | Customer's internal ID |
| site_id | UUID | FK, NULL | Current site (NULL = orphaned) |
| equipment_type_id | UUID | FK, NOT NULL | Category reference |
| lifecycle_status | Enum | NOT NULL | Active, Under_Repair, Pending_Installation, Retired |
| maintenance_status | Enum | NOT NULL | Up_to_Date, Overdue, In_Progress |
| service_cycle | Enum | NOT NULL | Monthly, Quarterly, Semesterly, Annually |
| last_service_date | Timestamp | NULL | Date/time of last maintenance |
| next_service_date | Date | COMPUTED | Calculated from last_service + cycle |
| is_provisional | Boolean | DEFAULT false | True for discovered equipment |
| provisional_expires_at | Timestamp | NULL | When provisional record auto-deletes |
| created_at | Timestamp | NOT NULL | Record creation |
| updated_at | Timestamp | NOT NULL | Last modification |
| deleted_at | Timestamp | NULL | Soft delete marker |
| predecessor_id | UUID | FK, NULL | Link to previous record (for asset ID reuse) |

**Lifecycle Status Values:**
- `Active` - Equipment in normal service
- `Under_Repair` - Temporarily out of service for repair
- `Pending_Installation` - Not yet commissioned
- `Retired` - Decommissioned, kept for historical records

**Index Strategy:**
- Unique index on `(serial_number)` where `deleted_at IS NULL`
- Index on `(qr_code_value)` for scan lookups (not unique - duplicates return first match)
- Index on `(customer_asset_id)` where `deleted_at IS NULL`
- Index on `(next_service_date)` for urgency sorting
- Index on `(site_id, lifecycle_status)` for filtered queries

#### Equipment Type (Admin-Configurable)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| name | String(100) | NOT NULL, UNIQUE | Type name (HVAC, Electrical, etc.) |
| description | String(500) | NULL | Optional description |
| display_order | Integer | DEFAULT 0 | Sorting for dropdowns |
| is_active | Boolean | DEFAULT true | Can be disabled without deletion |
| created_at | Timestamp | NOT NULL | Record creation |

#### Service Record

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| equipment_id | UUID | FK, NOT NULL | Equipment serviced |
| serviced_at | Timestamp | NOT NULL | When service was performed |
| serviced_by_user_id | UUID | FK, NOT NULL | Technician who performed service |
| reason_code | Enum | NULL | Required if equipment was RED status |
| created_at | Timestamp | NOT NULL | Record creation |

**Reason Codes (Fixed System List):**
- `EMERGENCY` - Emergency situation requiring immediate service
- `CUSTOMER_REQUEST` - Customer specifically requested service
- `TECHNICIAN_DISCRETION` - Technician judgment call
- `OTHER` - Other reason (audit trail purposes)

### 2.3 User & Access Control

#### User

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| email | String(255) | NOT NULL, UNIQUE | Login identifier |
| password_hash | String(255) | NULL | Bcrypt hash (NULL for magic-link-only) |
| full_name | String(255) | NOT NULL | Display name |
| is_active | Boolean | DEFAULT true | Account enabled |
| failed_login_count | Integer | DEFAULT 0 | For lockout tracking |
| locked_until | Timestamp | NULL | Account lockout expiration |
| password_changed_at | Timestamp | NULL | For expiration policy |
| created_at | Timestamp | NOT NULL | Record creation |
| updated_at | Timestamp | NOT NULL | Last modification |
| deleted_at | Timestamp | NULL | Soft delete (preserves history) |

**Password History Table** for preventing reuse of last 5 passwords.

#### Role

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| name | String(50) | NOT NULL, UNIQUE | Role identifier |
| display_name | String(100) | NOT NULL | Human-readable name |
| permissions | String[] | NOT NULL | List of permission codes |

**System Roles:**
- `TECHNICIAN` - Search, view equipment, mark service complete
- `MANAGER` - Read-only access to all equipment, reports, audit trail
- `ADMIN` - Full CRUD, user management, organization settings

**Permissions Model:**
Users can have multiple roles. Effective permissions = union of all assigned roles.
Roles are organization-wide (no hierarchical scoping).

#### User Role Assignment

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| user_id | UUID | PK, FK | User reference |
| role_id | UUID | PK, FK | Role reference |
| assigned_at | Timestamp | NOT NULL | When role was granted |
| assigned_by_user_id | UUID | FK | Who granted the role |

### 2.4 Audit Trail

#### Audit Log Entry

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK | Unique identifier |
| user_id | UUID | FK, NULL | Acting user (NULL for system) |
| action_type | String(50) | NOT NULL | CREATE, UPDATE, DELETE, VIEW, etc. |
| resource_type | String(50) | NOT NULL | Entity type affected |
| resource_id | UUID | NOT NULL | Entity ID affected |
| changes | JSONB | NULL | Before/after for updates |
| ip_address | String(45) | NULL | Client IP (IPv6 capable) |
| user_agent | String(500) | NULL | Browser/client identifier |
| timestamp | Timestamp | NOT NULL | When action occurred |
| hmac_signature | String(64) | NOT NULL | HMAC-SHA256 of entry data |

**Integrity:**
- Table has no UPDATE or DELETE permissions at database level
- Each entry signed with HMAC-SHA256 using server secret key
- Signature covers: user_id, action_type, resource_type, resource_id, changes, timestamp

**Retention:** 2 years minimum per GDPR requirements

---

## 3. Business Logic

### 3.1 Stoplight Status Calculation

```
function calculateStoplightStatus(equipment, agreement):
    if agreement.status == "Out_of_Scope" OR agreement.status == "Pending_Agreement":
        return RED

    if equipment.next_service_date < today:
        return RED  // Overdue

    if equipment.next_service_date <= today + 15 days:
        return YELLOW  // Warning window

    return GREEN  // Clear
```

**Note:** The 15-day warning threshold is fixed globally.

### 3.2 Next Service Date Calculation

```
function calculateNextServiceDate(last_service_date, service_cycle):
    cycle_days = {
        "Monthly": 30,
        "Quarterly": 91,
        "Semesterly": 182,
        "Annually": 365
    }

    return last_service_date + cycle_days[service_cycle]
```

**Recalculation:** Immediate upon service completion (synchronous).

### 3.3 Agreement Status Auto-Transition

Daily batch job evaluates all Customers:

```
for each customer where agreement_status == "Covered":
    if contract_end_date is not null AND contract_end_date < today:
        customer.agreement_status = "Pending_Agreement"
        log_audit_entry("AUTO_TRANSITION", customer)
```

Managers can manually override any auto-transitioned status.

### 3.4 Provisional Equipment Lifecycle

1. Technician discovers unknown equipment, enters serial number only
2. System creates Equipment record with `is_provisional = true`, `provisional_expires_at = now + 7 days`
3. Nightly cleanup job deletes all records where `provisional_expires_at < now` (silent, no notifications)
4. Manager can "formalize" provisional equipment by completing required fields and setting `is_provisional = false`

### 3.5 Asset ID Collision Handling

When creating equipment with a `customer_asset_id` that exists on another equipment record:

1. Existing record is soft-deleted (`deleted_at = now`)
2. New record is created with `predecessor_id` pointing to the deleted record
3. Historical records remain accessible via predecessor chain

### 3.6 Site/Customer Deletion Behavior

When a Site or Customer is deleted:
- Child Equipment records have their parent reference set to NULL (orphaned)
- Equipment is flagged with a system-generated note indicating orphan status
- Manager dashboard shows "Orphaned Equipment" warning count
- Equipment must be reassigned or deleted separately

---

## 4. API Specification

### 4.1 Equipment Lookup

**Endpoint:** `GET /api/v1/equipment/lookup`

**Query Parameters:**
- `q` (required): Search query - searches across serial_number, customer_asset_id, and qr_code_value simultaneously

**Response:**
```json
{
  "equipment": {
    "id": "uuid",
    "serial_number": "ABC123",
    "customer_asset_id": "CUST-001",
    "qr_code_value": "QR-ABC123",
    "stoplight_status": "GREEN",
    "next_service_date": "2026-03-15",
    "last_service_date": "2025-12-15T10:30:00Z",
    "service_cycle": "QUARTERLY",
    "lifecycle_status": "ACTIVE",
    "maintenance_status": "UP_TO_DATE",
    "equipment_type": {
      "id": "uuid",
      "name": "HVAC"
    },
    "site": {
      "id": "uuid",
      "name": "Building A",
      "customer": {
        "id": "uuid",
        "name": "Acme Corp",
        "agreement_status": "COVERED"
      }
    }
  }
}
```

**Duplicate QR Handling:** If multiple equipment records match the QR value, returns the first match (oldest by created_at). Logs warning for data quality monitoring.

### 4.2 Mark Service Complete

**Endpoint:** `POST /api/v1/equipment/{id}/service`

**Request Body (for RED status equipment only):**
```json
{
  "reason_code": "CUSTOMER_REQUEST"
}
```

**Response:**
```json
{
  "service_record": {
    "id": "uuid",
    "serviced_at": "2026-01-30T14:22:00Z",
    "serviced_by": "John Technician"
  },
  "equipment": {
    "stoplight_status": "GREEN",
    "next_service_date": "2026-05-01",
    "maintenance_status": "UP_TO_DATE"
  }
}
```

**Validation:**
- If current stoplight status is RED, `reason_code` is required
- Updates `last_service_date` immediately
- Recalculates `next_service_date` immediately
- Creates audit log entry

### 4.3 Equipment History

**Endpoint:** `GET /api/v1/equipment/{id}/history`

**Response:**
```json
{
  "recent_services": [
    {
      "serviced_at": "2025-12-15T10:30:00Z",
      "serviced_by": "John Technician"
    }
  ],
  "monthly_summaries": [
    {
      "month": "2024-06",
      "service_count": 3
    }
  ]
}
```

**Tiered Retention:**
- Last 1 year: Full detail for each service record
- Older than 1 year: Monthly aggregates (count per month)

### 4.4 Manager Dashboard

**Endpoint:** `GET /api/v1/dashboard/equipment`

**Query Parameters:**
- `status` (optional): GREEN, YELLOW, RED filter
- `customer_id` (optional): Filter by customer
- `site_id` (optional): Filter by site
- `equipment_type_id` (optional): Filter by type
- `period` (optional): Point-in-time snapshot (e.g., "2025-Q4", "2025-S1", "2025-06")
- `sort` (default: `next_service_date_asc`): Sorting order
- `page`, `page_size`: Pagination

**Response includes:** Equipment list sorted by most urgent first (next_service_date ascending).

**Point-in-Time Calculation:** When `period` is specified, the system calculates what the status WOULD HAVE BEEN at the end of that period by replaying service records against equipment state.

### 4.5 Data Export

**Endpoint:** `GET /api/v1/dashboard/export`

**Query Parameters:** Same as dashboard endpoint, plus:
- `format`: `xlsx` or `pdf`

**Response:** Binary file download

**Excel Export:** Includes exactly the columns currently visible in the dashboard view (passed as parameter).

**PDF Export:** Basic data table with column headers. No branding or formatting.

### 4.6 CSV Import

**Endpoint:** `POST /api/v1/equipment/import`

**Request:** Multipart form with CSV file

**Response (Validation Phase):**
```json
{
  "import_id": "uuid",
  "total_rows": 150,
  "valid_rows": 142,
  "error_rows": 8,
  "errors": [
    {"row": 5, "field": "service_cycle", "message": "Invalid value 'Weekly'"},
    {"row": 12, "field": "serial_number", "message": "Required field missing"}
  ]
}
```

**Endpoint:** `POST /api/v1/equipment/import/{import_id}/commit`

**Behavior:**
- Imports all valid rows
- Creates "pending review" equipment records for problematic rows (flagged for manager attention)
- Column mapping UI in frontend allows matching CSV columns to equipment fields

---

## 5. User Interface Specification

### 5.1 Technician View (Mobile-Optimized)

**Primary Screen: Scan/Search**
- Embedded camera scanner supporting all standard barcode formats (QR, Code 128, Code 39, EAN-13, UPC-A, etc.)
- Manual serial number entry field as fallback if scan fails
- Large touch targets (minimum 44x44px)

**Equipment Result Screen:**
- Large stoplight indicator (color only, no icons/patterns)
- Equipment identifier prominently displayed
- Next service date shown (date only, no calculation breakdown)
- Site and customer name
- "Mark Serviced" button (primary action)
- "View History" expandable section

**Mark Serviced Flow:**
1. If status is GREEN or YELLOW: Single tap completes service
2. If status is RED: Modal appears requiring reason code selection before completion
3. Confirmation shown with new next_service_date

**History View (Tap to Expand):**
- Last 12 months: Individual service entries with date
- Older: Monthly summary counts

### 5.2 Manager Dashboard (Desktop-Optimized)

**Main View: Equipment Table**
- Columns: Status (stoplight), Serial Number, Asset ID, Site, Customer, Type, Next Service, Last Service
- Column visibility is user-configurable
- Default sort: Most urgent first (next_service_date ascending)

**Filters:**
- Stoplight Status (multi-select)
- Customer (dropdown)
- Site (dropdown, filtered by selected customer)
- Equipment Type (dropdown)
- Period (date picker with presets: Q1-Q4, S1-S2, Monthly)

**Export:**
- Excel and PDF buttons export current filtered view
- Exports match visible columns exactly

**Orphaned Equipment Warning:**
- Banner appears when orphaned equipment exists
- Links to filtered view of orphaned records

### 5.3 Admin Screens

**User Management:**
- Create user (admin only, no self-registration)
- Assign multiple roles per user
- Deactivate user (soft delete)

**Equipment Types:**
- CRUD for organization's equipment categories
- Drag-to-reorder for dropdown display order

**Site Metadata:**
- Configure custom metadata fields available for sites

**CSV Import:**
- Upload interface with column mapping
- Preview valid/invalid rows before commit
- View pending review queue after import

### 5.4 Session Caching

- Recently viewed equipment cached in browser localStorage
- Cache valid for 1 hour
- UI shows "Last updated: X minutes ago" indicator
- When offline, cached data displayed with "Offline - data may be outdated" banner
- Automatic retry when connection restores

---

## 6. Authentication & Security

### 6.1 Authentication Methods

**Password Authentication:**
- Minimum: 8 characters, 1 uppercase, 1 number, 1 special character
- Bcrypt with cost factor 12
- Account lockout: 5 failed attempts = 15-minute lockout
- Password history: Cannot reuse last 5 passwords

**Magic Link Authentication:**
- Available to all users
- 24-hour link expiration
- Single use
- Email contains secure token

### 6.2 JWT Token Configuration

- Algorithm: RS256 (asymmetric)
- Access token: 1 hour expiration
- Refresh token: 7 days expiration
- Tokens include: user_id, roles[], issued_at, expires_at

### 6.3 Role-Based Access Control

| Permission | Technician | Manager | Admin |
|------------|------------|---------|-------|
| Search equipment | Yes | Yes | Yes |
| View equipment details | Yes | Yes | Yes |
| View maintenance history | Yes | Yes | Yes |
| Mark service complete | Yes | No | Yes |
| View dashboard | No | Yes | Yes |
| Export reports | No | Yes | Yes |
| View audit logs | No | Yes | Yes |
| Manage equipment | No | No | Yes |
| Manage users | No | No | Yes |
| Manage equipment types | No | No | Yes |
| Import equipment | No | No | Yes |

**Role Assignment:**
- Users can have multiple roles
- Effective permissions = union of all roles
- Roles are organization-wide (no hierarchy scoping)

---

## 7. Performance Requirements

### 7.1 Response Time Targets

| Operation | Target | Condition |
|-----------|--------|-----------|
| Equipment lookup | 500ms | Good network (4G+) |
| Dashboard load | 2 seconds | Initial page load |
| Mark service | 500ms | Good network |
| Export (1000 records) | 10 seconds | Background generation |

**Adaptive Performance:**
- 500ms target on good network conditions
- Graceful degradation with loading states on slow connections
- No hard enforcement - targets are design goals

### 7.2 Scalability Targets

- 50-500 concurrent ParcTrack users
- 500 concurrent sessions (design target, not hard limit)
- No hard rate limiting - system should scale
- Horizontal scaling via additional app servers behind Nginx

### 7.3 Caching Strategy

**Server-Side:**
- Equipment lookup results: Redis cache, 5-minute TTL
- Dashboard aggregations: Invalidate on equipment update

**Client-Side:**
- Session cache: 1 hour validity
- Static assets: Aggressive browser caching

---

## 8. Deployment & Infrastructure

### 8.1 Container Architecture

```
┌─────────────────────────────────────────────────────────┐
│                         Nginx                            │
│                    (Reverse Proxy)                       │
└─────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│  Spring Boot  │  │  Spring Boot  │  │    React      │
│   App (1)     │  │   App (N)     │  │   Static      │
└───────────────┘  └───────────────┘  └───────────────┘
        │                   │
        └─────────┬─────────┘
                  ▼
        ┌───────────────┐
        │  PostgreSQL   │
        │      15       │
        └───────────────┘
                  │
        ┌───────────────┐
        │    Redis      │
        │   (Cache)     │
        └───────────────┘
```

### 8.2 Environment Configuration

| Environment | Purpose | Database |
|-------------|---------|----------|
| Development | Local dev | Docker PostgreSQL |
| Testing | CI/CD | TestContainers |
| Staging | Pre-production | Isolated PostgreSQL |
| Production | Live system | PostgreSQL with replicas |

### 8.3 Backup Strategy

- Daily: pg_dump full backup
- Retention: 30 daily + 12 monthly backups
- Storage: rsync to backup location

---

## 9. Testing Strategy

### 9.1 Test Coverage Requirements

| Test Type | Coverage Target | Tools |
|-----------|-----------------|-------|
| Unit Tests | 80% | JUnit 5, Mockito |
| Integration Tests | Critical paths | TestContainers |
| API Tests | All endpoints | RestAssured |
| E2E Tests | Core flows | Cypress |

### 9.2 Critical Test Scenarios

**Equipment Lookup:**
- Search by serial number
- Search by QR code value
- Search by customer asset ID
- Duplicate QR returns first match
- No match returns empty result

**Service Completion:**
- Green status: instant completion
- Yellow status: instant completion
- Red status: requires reason code
- next_service_date recalculates correctly

**Stoplight Calculation:**
- Green: Covered + >15 days remaining
- Yellow: Covered + <=15 days remaining
- Red: Out of scope
- Red: Overdue

---

## 10. Out of Scope (Phase 1)

- Offline mode (beyond session caching)
- GPS tracking
- Procurement/spare parts ordering
- Push notifications
- Native mobile apps
- Multi-tenancy
- Technician assignment/scheduling
- Colorblind accessibility patterns (color-only stoplight)
- PWA capabilities

---

## 11. Glossary

| Term | Definition |
|------|------------|
| Equipment | A physical asset tracked for maintenance |
| Stoplight Status | Visual indicator (Green/Yellow/Red) of maintenance urgency |
| Service Cycle | Frequency of required maintenance (Monthly, Quarterly, etc.) |
| Agreement Status | Contract coverage state (Covered, Out of Scope, Pending) |
| Provisional Equipment | Temporarily created equipment record pending manager review |
| Ghost Equipment | Equipment discovered in the field not in the database |
| Parc | French term for equipment fleet/inventory (origin of "ParcTrack") |
