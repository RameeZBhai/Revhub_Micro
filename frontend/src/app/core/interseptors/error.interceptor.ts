import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 200 && !error.ok) {
        console.error('Response parsing error:', error);
        return throwError(() => new Error('Invalid response format'));
      }
      return throwError(() => error);
    })
  );
};