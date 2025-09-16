import { Injectable } from '@angular/core';
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor,
    HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { AuthService } from '../service/auth/auth.service';
import { catchError, switchMap } from 'rxjs/operators';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    constructor(private authService: AuthService) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // No interceptar peticiones a login, register, refresh
        if (request.url.includes('/auth/login') ||
            request.url.includes('/auth/register') ||
            request.url.includes('/auth/refresh')) {
            console.log('No interceptando petición de autenticación');
            return next.handle(request);
        }

        // Obtener el token del localStorage
        const token = this.authService.getToken();

        if (token) {
            // Añadir el token a la solicitud Y mantener withCredentials
            const authRequest = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                },
                withCredentials: true // Asegurar que withCredentials se mantiene
            });

            console.log(`Interceptando petición a ${request.url} - Agregando token`);
            return next.handle(authRequest).pipe(/* resto del código */);
        }

        // Si no hay token...
        return next.handle(request);
    }
}