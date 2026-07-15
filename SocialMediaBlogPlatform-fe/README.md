# Social Media Blog Platform Frontend

React + Vite frontend for the Social Media Blog Platform base.

## Commands

```powershell
npm run dev
npm run build
npm run lint
```

The app expects the backend gateway at `http://localhost:8080` by default.
Override with:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080"
npm run dev
```

## Production

For Vercel deployment at https://subtrack.click, configure VITE_API_BASE_URL=https://api.subtrack.click, VITE_WS_BASE_URL=wss://api.subtrack.click, and VITE_GOOGLE_CLIENT_ID. See [PRODUCTION_DEPLOYMENT.md](../PRODUCTION_DEPLOYMENT.md).
