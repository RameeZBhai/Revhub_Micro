# CORS Fix Instructions

## Problem
Your Angular app (localhost:4200) was getting CORS errors when trying to access the backend API (localhost:8080) because the request was being redirected to localhost:8085/login without proper CORS headers.

## Solution Applied
1. **Angular Proxy Configuration**: Created `proxy.conf.json` to route `/api/*` requests to `http://localhost:8080`
2. **Updated Angular Configuration**: Modified `angular.json` to use the proxy
3. **Updated Service URLs**: Changed all hardcoded `http://localhost:8080` URLs to relative `/api` URLs

## How to Run

### Start the Angular app with proxy:
```bash
cd frontend
ng serve
```

The proxy will automatically route:
- `http://localhost:4200/api/notifications` → `http://localhost:8080/notifications`
- `http://localhost:4200/api/auth/login` → `http://localhost:8080/api/auth/login`
- etc.

## Files Modified
- `frontend/proxy.conf.json` (created)
- `frontend/angular.json` (updated serve options)
- `frontend/src/app/core/services/auth.service.ts`
- `frontend/src/app/core/services/notification.service.ts`
- `frontend/src/app/core/services/chat.service.ts`
- `frontend/src/app/core/services/post.service.ts`
- `frontend/src/app/core/services/profile.service.ts`

## Alternative Backend Solution
If you prefer to fix this on the backend instead, add CORS headers to your server (localhost:8085):

```javascript
// Express.js example
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', 'http://localhost:4200');
  res.header('Access-Control-Allow-Credentials', 'true');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  next();
});
```