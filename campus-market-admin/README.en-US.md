# campus-market-admin

Admin dashboard for the Campus Market project.

## What It Does

- Admin login
- Goods review and approval workflow
- User list management
- User ban / unban actions

In production, this app is served under `/admin/`.

## Tech Stack

- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Pure Admin Lite

## Local Development

```bash
corepack enable
pnpm install
pnpm dev
```

Default local URL:

```text
http://127.0.0.1:8848
```

## Backend Dependency

During development, Vite proxies:

- `/api`
- `/static`

to:

```text
http://localhost:8080
```

Make sure the backend is already running before opening the admin app.

## Build and Checks

```bash
pnpm test
pnpm typecheck
pnpm build
```

Build output:

```text
dist/
```

## Demo Account

- `admin / admin123`

## Related Docs

- [Root deployment README](../README.md)
- [Backend README](../campus-market-server/README.md)
