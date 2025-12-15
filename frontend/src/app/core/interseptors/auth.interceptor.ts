import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token && !req.url.includes('/auth/')) {
    const user = authService.getCurrentUser();
    let headers = req.headers.set('Authorization', `Bearer ${token}`);

    if (user && user.username) {
      headers = headers.set('X-User-Name', user.username);
    }

    const authReq = req.clone({ headers });
    return next(authReq);
  }


  return next(req);
};