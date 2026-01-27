---

# Product Requirement Document (PRD): ParcTrack

**Application Name:** ParcTrack  
**Version:** 1.0  
**Status:** Approved for Development  
**Target Success Metric:** **90% Reduction in Maintenance Gaps**

---

## 1. Project Overview

### 1.1 Problem Statement

The current inventory management ecosystem is hindered by four primary issues:

- **Ghost Equipment:** Assets missing from the registry or physically absent.
- **Contractual Blind Spots:** No clear link between equipment and supplier/customer maintenance agreements.
- **Operational Hesitation:** Technicians cannot verify if maintenance is contractually obligated at the point of service.
- **Poor Tracking:** Manual tracking leads to data decay and expired service windows.

### 1.2 Strategic Goals

- **Eliminate Maintenance Gaps:** Ensure every asset is serviced within its defined cycle.
- **Real‑Time Identification:** Provide instant “Go/No‑Go” status on‑site.
- **Data Centralization:** Create a single source of truth for Serial Numbers, Asset IDs, and Contract Status.

---

## 2. Target Audience & Personas

### 2.1 The Field Technician (The “Action” User)

- **Context:** On‑site, mobile‑first, requiring fast answers.
- **Core Task:** Identify assets via QR/Barcode or Serial Number to verify maintenance eligibility.
- **UI Needs:** Large touch targets, high‑contrast stoplight indicators, native camera integration.

### 2.2 The Parc Manager (The “Audit” User)

- **Context:** Office‑based, responsible for compliance and stakeholder reporting.
- **Core Task:** Access reporting on non‑maintained or out‑of‑scope equipment.
- **UI Needs:** Data‑dense tables, advanced filtering (Quarterly/Semesterly), export functions.

---

## 3. Functional Requirements

### 3.1 Smart Lookup (FR‑01)

Unified search interface supporting:

- **Serial Number**
- **Customer Asset ID**
- **QR/Barcode Scan**

### 3.2 Maintenance “Stoplight” Logic (FR‑02)

| Status              | Color  | Logic / Condition                                                 |
| ------------------- | ------ | ----------------------------------------------------------------- |
| **Clear**           | Green  | Agreement is **Covered** AND Today < (next_service − 15 days)     |
| **Warning**         | Yellow | Agreement is **Covered** AND next_service is within 15 days       |
| **Action Required** | Red    | Agreement is **Out of Scope**/**Pending** OR Today > next_service |

### 3.3 Cycle-Based Filtering (FR‑03)

Manager Dashboard must support:

- **Monthly:** `2026-05`
- **Quarterly:** `2024-Q1`, `2026-Q2`
- **Semesterly:** `2025-S1`, `2025-S2`

### 3.4 Data Export (FR‑04)

Export filtered lists to:

- **Excel (.xlsx)**
- **PDF**

---

## 4. Data Schema

### 4.1 Equipment Inventory Object

| Field Name       | Data Type         | Description                              |
| ---------------- | ----------------- | ---------------------------------------- |
| serial_number    | String (Unique)   | Manufacturer unique ID                   |
| qr_code_val      | String            | Value encoded in QR/Barcode              |
| cust_asset_id    | String (Unique)   | Customer internal identifier             |
| maint_status     | Enum              | Up‑to‑date, Overdue, In‑Progress         |
| agreement_status | Enum              | Covered, Out of Scope, Pending Agreement |
| service_cycle    | Dropdown          | Monthly, Quarterly, Semesterly, Annually |
| last_service     | Timestamp         | Date/Time of last maintenance            |
| next_service     | Date (Calculated) | Based on cycle logic                     |

### 4.2 Maintenance Calculation Logic

    NextServiceDue = LastServiceDate + C

Where **C** = cycle duration:

- **Monthly:** 30 days
- **Quarterly:** 91 days
- **Semesterly:** 182 days
- **Annually:** 365 days

---

## 5. Non‑Functional Requirements

- **Responsiveness:** Mobile ↔ Desktop seamless adaptation.
- **Connectivity:** Requires active internet (real‑time DB query).
- **Performance:** Lookup + stoplight UI must load in **< 500 ms**.
- **Security:** RBAC
  - **Technicians:** View status, update last_service
  - **Managers:** Full CRUD on all fields

---

## 6. Out of Scope (Phase 1)

- **Offline Mode:** No local caching.
- **GPS Tracking:** Not required for MVP.
- **Procurement:** No spare‑part ordering or automated billing.

---

## 7. Technical Requirements

### Architecture

**Deployment Model:** Self-hosted, containerized (Docker)

**Technology Stack:**

- **Backend:** Java Spring Boot 3.2+ with Java 23
- **Database:** PostgreSQL 15
- **Frontend:** JavaScript React 18 with Vite
- **Reverse Proxy:** Nginx
- **Storage:** File-based, self-hosted (local filesystem)
- **Observability:** Spring Boot Actuator with Prometheus/Grafana
- **Deployment:** JAR deployment for Java backend

**Architecture Pattern:** Domain-Driven Design (DDD) with Hexagonal Architecture

- Domain Layer: Pure business logic, entities, repository interfaces
- Application Layer: Use cases, DTOs, input/output ports
- Infrastructure Layer: JPA adapters, REST controllers, file storage, email

### Infrastructure

**Server Requirements:**

- Docker & Docker Compose installed
- PostgreSQL 15 (Docker container)
- Nginx reverse proxy (Docker container)
- Java Spring Boot 3.2+ with Java 23 (Docker container)
- Mailhog (Docker container)

**File Storage:**

- Directory structure: `/var/parctrack/documents/`
- File types: PDF, XLSX
- Storage capacity: 500GB initial, expandable to 5TB

**Database:**

- PostgreSQL 15 with connection pooling (HikariCP)
- Database name: `parctrack_db`
- Schema migrations: Liquibase
- Backup strategy: Daily pg_dump + rsync to backup location
- Retention: 30 daily backups, 12 monthly backups

### Security & Compliance

**Authentication & Authorization:**

- **Primary Authentication Methods:**
  - Username/Password with strong password requirements
  - Magic Link (passwordless for employees, 24-hour expiration)
- **Password Security:**
  - Minimum requirements: 8 characters, 1 uppercase letter, 1 number, 1 special character
  - Real-time password strength validation
  - Password history: Prevent reuse of last 5 passwords
  - Password expiration policy (optional: force reset every 90 days)
  - Bcrypt hashing with cost factor 12
  - Account lockout after 5 failed login attempts (15-minute lockout)
- **JWT Token Management:**
- JWT RS256 tokens (asymmetric signing)
- Access token expiration: 1 hour
- Refresh token expiration: 7 days
- Magic link authentication for employees (24-hour expiration)

**Data Security:**

- Encryption at rest: File system encryption (LUKS or eCryptfs)
- Encryption in transit: TLS 1.3 for all HTTPS connections
- PostgreSQL SSL connections enforced
- Security headers: HSTS, CSP, X-Frame-Options, X-Content-Type-Options
- SQL injection prevention: JPA parameterized queries
- XSS protection: React default escaping + Content Security Policy
- CSRF protection: Spring Security CSRF tokens

**RBAC (Role-Based Access Control):**

- **Field Technician:** Search and View scanned equipement, equipement maintenance actions
- **Manager:** Read-only access to all equipements, audit trail, reports
- **Admin:** Full system access, user management, organization settings

**Audit Logging:**

- Capture: Equipement created/updated/deleted/viewed/downloaded, user actions, equipement maintenance actions
- Log fields: User ID, action type, resource, timestamp, IP address, user agent
- Tamper-proof: Immutable audit trail with cryptographic signatures
- Retention: 2 years minimum (GDPR compliance)
- Export: PDF, Excel, CSV for regulatory audits

**GDPR Compliance:**

- Data subject rights: Access, rectification, erasure, portability
- Data residency: All data stored on self-hosted infrastructure (customer-controlled)
- Right to erasure: Implement data deletion workflows
- Data retention policies: Configurable per organization
- Consent management: Track acknowledgment timestamps and IP addresses

### Performance Requirements

**Response Times:**

- API response time: <500ms for 95th percentile requests
- Dashboard load time: <2 seconds
- Report generation: <10 seconds for 1,000 employee records

**Scalability:**

- Support 50-500 employees per organization
- Handle 500 concurrent users
- Storage: 500GB initial, expandable to 5TB
- Horizontal scaling: Add application servers behind Nginx load balancer
- Database scaling: PostgreSQL read replicas for reporting queries

**Availability:**

- Uptime target: 99.5% (approximately 3.6 hours downtime per month)
- Scheduled maintenance: Sunday 2-4 AM local time
- Health checks: Every 30 seconds
- Graceful degradation: System remains partially functional during component failures

### Integrations

**Email Platforms:**

- SMTP configuration: Gmail, Office 365, custom SMTP server
- JavaMail API with Spring Boot Starter Mail
- HTML email templates: Thymeleaf
- Email tracking: Sent, delivered, opened, bounced, failed
- Attachment support: Signed policy PDFs

### Observability & Monitoring

**Logging:**

- Framework: Log4j2 with JSON structured logging
- Log levels: ERROR, WARN, INFO, DEBUG, TRACE
- Log aggregation: ELK stack or Loki
- Log retention: 90 days

**Metrics:**

- Spring Boot Actuator endpoints: /actuator/health, /actuator/metrics, /actuator/prometheus
- Prometheus for metrics collection
- Grafana dashboards for visualization
- Key metrics: API response times, error rates, database query performance, file upload success rates

**Alerting:**

- Alert rules: High CPU usage (>80%), memory exhaustion (>90%), error rate spikes (>5%), disk space warnings (<10% free)
- Notification channels: Email, Slack
- On-call rotation for production alerts

### API Documentation

**OpenAPI Specification:**

- OpenAPI 3.1.0 format
- SpringDoc OpenAPI auto-generation from code annotations
- Separate API documentation server (not embedded in main app)
- Interactive API explorer for development/testing
- Complete API reference: policyhub-openapi.yaml

### Testing Requirements

**Test Coverage:**

- Minimum 80% code coverage (enforced by JaCoCo)
- Unit tests: JUnit 5 + Mockito
- Integration tests: TestContainers for database tests
- API tests: RestAssured or MockMvc
- E2E tests: Cypress for critical user flows

**Testing Strategy:**

- Unit tests: Domain entities, use cases, business logic
- Integration tests: Repository implementations, database migrations
- API tests: All REST endpoints with authentication
- E2E tests: Complete user flows (policy upload → acknowledgment → report generation)

---
