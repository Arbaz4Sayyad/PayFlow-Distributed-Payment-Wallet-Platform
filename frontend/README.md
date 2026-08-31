# PayFlow Frontend — Production-Grade Fintech Dashboard

Production-grade web application and operations dashboard for **PayFlow**, a distributed payment and digital wallet platform backed by Java/Spring Boot microservices.

---

## 1. Overview & Architectural Boundary

PayFlow's frontend is designed following **strict fintech software engineering principles** inspired by platforms such as Stripe Dashboard, Mercury, and Adyen.

### ⚠️ Financial Authority Invariant
* **The backend is the single source of truth** for financial state: wallet balances, ledger double-entry journal postings, transaction lifecycles, and fraud rules.
* **The frontend does not perform financial calculations** using client-side floating-point logic. All financial figures are transferred and maintained in integer minor units (`amountMinor`) and formatted strictly for display via native `Intl.NumberFormat`.
* **Idempotency Protection**: All money-moving requests (transfers, deposits, withdrawals, merchant authorizations) generate unique `Idempotency-Key` headers to guarantee safety against accidental double-clicks and network retries.

---

## 2. Technology Stack

* **Core**: React 18, TypeScript (Strict Mode)
* **Build Tool**: Vite
* **Styling**: Tailwind CSS (Restrained Fintech Design Tokens)
* **Routing**: React Router DOM v6
* **Server State**: TanStack Query v5
* **HTTP Client**: Axios with JWT Interceptors & Session Handling
* **Forms & Validation**: React Hook Form, Zod
* **Icons**: Lucide React

---

## 3. Design System & Anti-AI Philosophy

The user interface explicitly eliminates generic AI-generated templates:
* **No purple/neon gradients or floating glassmorphism blobs.**
* **High Information Density**: Compact tables, 1px crisp borders (`slate-200`), tabular numerals (`font-variant-numeric: tabular-nums`).
* **Semantic Status System**: Color is reserved solely for status indicators (Success/Credit, In-Review, Debit/Error).

---

## 4. Project Structure

```text
frontend/
├── public/
│   └── favicon.svg           # Minimal geometric PayFlow mark
├── src/
│   ├── api/                  # Centralized Axios client & typed REST modules
│   │   ├── client.ts
│   │   ├── auth.ts
│   │   ├── wallet.ts
│   │   ├── payments.ts
│   │   ├── transfers.ts
│   │   ├── transactions.ts
│   │   ├── notifications.ts
│   │   └── admin.ts
│   │
│   ├── components/
│   │   ├── ui/               # Headless primitives: Button, Input, Select, Badge, Table, Dialog, MoneyAmount
│   │   └── layout/           # AppLayout, AdminLayout, Sidebar, Header, MobileNav
│   │
│   ├── context/              # AuthContext & ToastContext
│   ├── hooks/                # useAuth, useIdempotency, useDebounce, useMoney
│   ├── mocks/                # Isolated realistic fallback data
│   ├── router/               # AppRouter with RequireAuth & RequireAdmin guards
│   ├── types/                # Domain models, transaction enums, and API responses
│   ├── utils/                # Currency formatters, date helpers, error normalization
│   └── pages/
│       ├── auth/             # LoginPage, RegisterPage
│       ├── app/              # DashboardPage, WalletPage, TransfersPage, TransactionsPage, ...
│       └── admin/            # AdminDashboardPage, AdminUsersPage, AdminFraudPage, ...
│
├── .env.example
├── package.json
├── tailwind.config.js
└── vite.config.ts
```

---

## 5. Running Locally

### Prerequisites
* Node.js `>= 18.0.0`
* npm `>= 9.0.0`

### Setup & Launch
```bash
# 1. Navigate to frontend folder
cd frontend

# 2. Install dependencies
npm install

# 3. Start development server
npm run dev
```

The frontend will run on `http://localhost:3001` (or next available port) and automatically proxy `/api` calls to the Spring Boot API Gateway on `http://localhost:8080`.

---

## 6. Environment Variables

Create `.env` based on `.env.example`:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 7. Operational Roles & Portals

1. **Customer Workspace** (`/app/dashboard`, `/app/wallet`, `/app/transfers`, `/app/transactions`):
   * Real-time balance and transaction history.
   * Send Money with live idempotency shield and confirmation dialogs.
2. **Operations Portal** (`/admin`, `/admin/fraud`, `/admin/events`, `/admin/wallets`):
   * Information-dense system KPIs (volume, success/failure rate).
   * Live Fraud Review Queue with release/freeze actions.
   * Transactional Outbox and Kafka event monitoring.
