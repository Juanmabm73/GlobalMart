import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../service/auth/auth.service';

@Injectable({
    providedIn: 'root'
})
export class AuthGuard implements CanActivate {
    constructor(
        private authService: AuthService,
        private router: Router
    ) { }

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot
    ): boolean {
        // Verifica si el usuario está autenticado
        if (!this.authService.getToken()) {
            this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
            return false;
        }

        // Verifica si el usuario tiene al menos uno de los roles requeridos
        const requiredRoles: string[] = route.data['requiredRoles'];
        if (requiredRoles && requiredRoles.some(role => this.authService.hasRole(role))) {
            return true;
        }

        // Si no tiene los roles requeridos, redirige al usuario
        this.router.navigate(['/']);
        return false;
    }
}