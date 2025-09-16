import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { tap, catchError, map, switchMap } from 'rxjs/operators';



@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = '/api/v1'; // Asegúrate de que coincida con tu backend
    private tokenKey = 'auth_token';
    // Cambiar a público para permitir acceso desde el nav component
    public userSubject: BehaviorSubject<any>;
    public user: Observable<any>;
    public user$: Observable<any>; // Alias para compatibilidad
    // Cambiar a público para permitir acceso desde el nav component
    public isLoggedInSubject = new BehaviorSubject<boolean>(false);
    public isLoggedIn$ = this.isLoggedInSubject.asObservable();
    private tempUser: any;

    constructor(private http: HttpClient) {
        // Comprobar si hay información de sesión guardada
        const storedToken = localStorage.getItem(this.tokenKey);
        const storedUser = localStorage.getItem('user');

        this.userSubject = new BehaviorSubject<any>(storedUser ? JSON.parse(storedUser) : null);
        this.user = this.userSubject.asObservable();
        this.user$ = this.user; // Compatibilidad

        // Actualizar el estado de inicio de sesión basado en la existencia del token
        this.isLoggedInSubject.next(!!storedToken);

        // Log para depurar el estado inicial
        console.log('Estado inicial de autenticación:', {
            token: !!storedToken,
            user: storedUser ? JSON.parse(storedUser) : null,
            isLoggedIn: this.isLoggedInSubject.value
        });
    }

    login(username: string, password: string): Observable<any> {
        console.log('Intentando login con:', username);

        // Add withCredentials: true to handle cookies
        return this.http.post<any>(`${this.apiUrl}/auth/login`, { username, password }, { withCredentials: true })
            .pipe(
                tap(response => {
                    console.log('Respuesta de login:', response);

                    // Check for successful authentication by response status
                    if (response && response.status === 'SUCCESS') {
                        // Store token if provided
                        if (response.token) {
                            localStorage.setItem(this.tokenKey, response.token);
                        } else {
                            // Si no hay token explícito, usar un marcador
                            localStorage.setItem(this.tokenKey, 'authenticated');
                        }

                        // Store user information from response
                        if (response.user) {
                            localStorage.setItem('user', JSON.stringify(response.user));
                            this.userSubject.next(response.user);
                        }

                        this.isLoggedInSubject.next(true);
                        console.log('Login exitoso, sesión almacenada');
                    } else {
                        console.error('Respuesta de login sin éxito', response);
                        throw new Error('Login failed');
                    }
                }),
                catchError(error => {
                    console.error('Error en la solicitud de login:', error);
                    throw error;
                })
            );
    }

    refreshToken(): Observable<any> {
        console.log('Intentando refrescar el token de autenticación...');

        return this.http.post<any>(`${this.apiUrl}/auth/refresh`, {}, { withCredentials: true })
            .pipe(
                tap(response => {
                    console.log('Respuesta de refresh token:', response);

                    if (response && response.status === 'SUCCESS') {
                        // Actualizar el token si se proporciona
                        if (response.token) {
                            localStorage.setItem(this.tokenKey, response.token);
                            console.log('Token actualizado en localStorage');
                        }

                        // Si también viene usuario en la respuesta, actualizarlo
                        if (response.user) {
                            localStorage.setItem('user', JSON.stringify(response.user));
                            this.userSubject.next(response.user);
                            console.log('Usuario actualizado después del refresh');
                        }

                        return response;
                    } else {
                        console.warn('Respuesta inesperada en refresh token:', response);
                        return null;
                    }
                }),
                catchError(error => {
                    console.error('Error al refrescar token:', error);
                    // En caso de error, no limpiar automáticamente para permitir 
                    // que el consumidor decida qué hacer
                    return throwError(() => error);
                })
            );
    }

    setToken(token: string): void {
        localStorage.setItem(this.tokenKey, token);
    }

    getUserProfile(): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/users/profile`)
            .pipe(
                tap(userData => {
                    console.log('Datos de usuario recibidos del perfil:', userData);

                    // Normalizar formato de roles (convertir 'role' a 'roles' si es necesario)
                    if (userData) {
                        // Si existe role pero no roles, usar role como roles
                        if (userData.role && !userData.roles) {
                            console.log('Normalizando formato de roles: role → roles');
                            userData.roles = userData.role;
                        }

                        // Asegurar que roles siempre sea un array para consistencia
                        if (userData.roles && !Array.isArray(userData.roles)) {
                            userData.roles = [userData.roles];
                        }

                        console.log('Datos de usuario normalizados:', userData);
                    }

                    // Guardar datos del usuario en localStorage
                    localStorage.setItem('user', JSON.stringify(userData));
                    this.userSubject.next(userData);

                    // Asegurarse de que isLoggedIn es true cuando tenemos perfil válido
                    this.isLoggedInSubject.next(true);
                }),
                catchError(error => {
                    console.error('Error obteniendo perfil de usuario:', error);

                    // Si hay un error 401, probablemente el token expiró
                    if (error.status === 401) {
                        this.handleUnauthorized();
                    }

                    throw error;
                })
            );
    }

    // Método para manejar respuestas 401 (no autorizado)
    private handleUnauthorized(): void {
        console.warn('Sesión no autorizada (401), limpiando datos locales');
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem('user');
        this.userSubject.next(null);
        this.isLoggedInSubject.next(false);
    }

    // Métodos necesarios para compatibilidad
    isLoggedIn(): Observable<boolean> {
        return this.isLoggedIn$;
    }

    getUserIdByUsername(username: string): Observable<number> {
        console.log('Solicitando ID para el usuario:', username);
        return this.http.get<number>(`${this.apiUrl}/users/by-username/${username}`)
            .pipe(
                tap(id => console.log('ID obtenido del backend:', id)),
                catchError(error => {
                    console.error('Error al obtener el ID del usuario:', error);
                    throw error;
                })
            );
    }

    getCurrentUserId(): Observable<number | null> {
        const user = this.getCurrentUser(); // obtengo el usuario actual
        if (!user) return of(null);

        if (user.id) return of(user.id);

        if (user.username) { // en caso de solo tener el username
            return this.getUserIdByUsername(user.username).pipe( // obtengo el id
                tap(id => {
                    user.id = id;
                    localStorage.setItem('user', JSON.stringify(user));
                    this.userSubject.next({ ...user });
                })
            );
        }

        return of(null);
    }

    hasRole(role: string): boolean {
        // Obtener usuario directamente del localStorage como respaldo
        let user = this.userSubject.value;

        if (!user) {
            // Intento obtener del localStorage si userSubject está vacío
            const userStr = localStorage.getItem('user');
            if (userStr) {
                try {
                    user = JSON.parse(userStr);
                    console.log('Usuario recuperado de localStorage:', user);
                } catch (e) {
                    console.error('Error al parsear usuario de localStorage:', e);
                }
            }
        }

        if (!user) {
            console.log('No hay usuario definido');
            return false;
        }

        console.log('Verificando rol:', role, 'en usuario:', user);

        // Comprobar roles (plural)
        if (user.roles) {
            if (Array.isArray(user.roles)) {
                return user.roles.some((r: any) =>
                    typeof r === 'string' && r.toLowerCase() === role.toLowerCase()
                );
            } else if (typeof user.roles === 'string') {
                return user.roles.toLowerCase() === role.toLowerCase();
            }
        }

        // Comprobar role (singular) como respaldo
        if (user.role) {
            if (Array.isArray(user.role)) {
                return user.role.some((r: any) =>
                    typeof r === 'string' && r.toLowerCase() === role.toLowerCase()
                );
            } else if (typeof user.role === 'string') {
                return user.role.toLowerCase() === role.toLowerCase();
            }
        }

        console.log('No se encontraron roles en el usuario');
        return false;
    }

    // Agregar o modificar este método para asegurar que obtenemos el usuario correctamente
    getCurrentUser(): any {
        const user = this.userSubject.value;
        if (user) return user;

        // Respaldo desde localStorage
        const userStr = localStorage.getItem('user');
        if (userStr) {
            try {
                return JSON.parse(userStr);
            } catch (e) {
                console.error('Error al parsear usuario de localStorage:', e);
            }
        }
        return null;
    }

    register(username: string, email: string, password: string, role: string[]): Observable<number> {
        return this.http.post<any>(`${this.apiUrl}/users/`, {
            username,
            email,
            password,
            role
        }).pipe(
            map(response => response.id) // Extraer solo el campo `id` de la respuesta
        );
    }

    logout(): Observable<void> {
        return new Observable<void>((observer) => {
            console.log('Cerrando sesión...');

            // Opcional: hacer una petición al servidor para invalidar la sesión
            // this.http.post(`${this.apiUrl}/auth/logout`, {}, { withCredentials: true })
            //   .subscribe({
            //     complete: () => {
            //       // Continuar con la limpieza local
            //     }
            //   });

            // Limpiar datos de autenticación
            localStorage.removeItem(this.tokenKey);
            localStorage.removeItem('user');

            // Asegurarse de que el estado se actualiza correctamente
            this.userSubject.next(null);
            this.isLoggedInSubject.next(false);

            console.log('Sesión cerrada');
            observer.next();
            observer.complete();
        });
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    createUserImage(userId: number, imageFormData: FormData): Observable<any> {
        return this.http.post(`${this.apiUrl}/users/${userId}/image`, imageFormData);
    }

    setUser(tempUser: any): void {
        this.tempUser = tempUser;
    }

    getProduct(): any {
        const tempUser = this.tempUser;
        this.setUser(null); // Limpiar la variable después de obtener el producto
        return tempUser;
    }

    // Método para verificar la validez del token actual
    verifySession(): Observable<boolean> {
        if (!this.getToken()) {
            return of(false);
        }

        return this.refreshToken().pipe(
            map(response => {
                return response && response.status === 'SUCCESS';
            }),
            catchError(() => {
                // Si falla, la sesión no es válida
                return of(false);
            })
        );
    }
}