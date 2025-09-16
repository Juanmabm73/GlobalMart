import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError, forkJoin } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { AuthService } from './auth/auth.service';
import { Router } from '@angular/router';

export interface CartItem {
    id: number;
    productId: number;
    quantity: number;
    product: any;
}

export interface Cart {
    id?: number;
    userId?: number;
    items: CartItem[];
    cartProducts?: CartItem[];
    totalPrice: number;
}

@Injectable({
    providedIn: 'root'
})
export class ShoppingCartService {
    private apiUrl = '/api/v1';
    private cartSubject = new BehaviorSubject<Cart | null>(null);
    cart$ = this.cartSubject.asObservable();

    constructor(
        private http: HttpClient,
        private authService: AuthService,
        private router: Router
    ) {
        this.loadCart();

        this.authService.isLoggedIn$.subscribe(isLoggedIn => {
            if (isLoggedIn) {
                this.loadCart();
            } else {
                this.cartSubject.next(null);
            }
        });
    }

    loadCart(): void {
        console.log('Intentando cargar carrito...');
        this.authService.isLoggedIn$.pipe(
            tap(isLoggedIn => {
                if (!isLoggedIn) {
                    console.log('No hay usuario logueado, no se cargará el carrito');
                    this.cartSubject.next(null);
                }
            }),
            switchMap(isLoggedIn => {
                if (!isLoggedIn) {
                    return of(null);
                }
                return this.authService.getCurrentUserId();
            }),
            switchMap(userId => {
                if (!userId) {
                    console.log('No se pudo obtener el ID del usuario');
                    return of({ items: [], totalPrice: 0 } as Cart);
                }
                const params = { id: userId.toString() };
                console.log('Cargando carrito para usuario con ID:', userId);
                return this.http.get<Cart>(`${this.apiUrl}/users/shoppingcarts`,{ params })
                    .pipe(
                        catchError(error => {
                            console.error('Error al cargar el carrito:', error);
                            return of({ items: [], totalPrice: 0 } as Cart);
                        })
                    );
            })
        ).subscribe({
            next: (cart) => {
                if (cart) {
                    console.log('Carrito cargado:', cart);
                    this.cartSubject.next(cart);
                }
            },
            error: (err) => {
                console.error('Error en proceso de carga de carrito:', err);
                this.cartSubject.next({ items: [], totalPrice: 0 } as Cart);
            }
        });
    }

    getShoppingCart(): Observable<Cart> {
        return this.getCart();
    }

    getCart(): Observable<any> {
        return this.authService.getCurrentUserId().pipe(
            switchMap(userId => {
                if (!userId) {
                    return throwError(() => new Error('Usuario no autenticado'));
                }
                const params = { id: userId.toString() };
                return this.http.get<any>(`${this.apiUrl}/users/shoppingcarts`,{params})
                    .pipe(
                        tap(cart => console.log('Carrito recibido del servidor:', cart)),
                        switchMap(cart => {
                            const cartItems = cart.cartProducts || cart.items || [];
                            const normalizedCart = {
                                ...cart,
                                items: cartItems,
                                cartProducts: cartItems
                            };

                            if (cartItems.length === 0) {
                                return of(normalizedCart);
                            }

                            console.log('Carrito items:', cartItems);

                            const productObservables = cartItems.map((item: any) => {
                                const productId = item.id;

                                console.log('Processing item with ID:', productId);

                                if (productId && !item.imageBase64) {
                                    return this.http.get(`${this.apiUrl}/products/${productId}/image`, { responseType: 'blob' })
                                        .pipe(
                                            switchMap(blob => this.blobToBase64(blob)),
                                            map(base64 => {
                                                return {
                                                    ...item,
                                                    imageBase64: base64
                                                };
                                            }),
                                            catchError(error => {
                                                console.error(`Error loading image for product ${productId}:`, error);
                                                return of(item); // Return item without image
                                            })
                                        );
                                }
                                return of(item);
                            });

                            return forkJoin(productObservables).pipe(
                                map(updatedItems => {
                                    return {
                                        ...normalizedCart,
                                        items: updatedItems,
                                        cartProducts: updatedItems
                                    };
                                })
                            );
                        }),
                        catchError(error => {
                            console.error('Error getting cart:', error);
                            return of({ items: [], cartProducts: [], totalPrice: 0 });
                        })
                    );
            })
        );
    }

    private blobToBase64(blob: Blob): Observable<string> {
        return new Observable(observer => {
            const reader = new FileReader();
            reader.onloadend = () => {
                observer.next(reader.result as string);
                observer.complete();
            };
            reader.onerror = error => {
                observer.error(error);
            };
            reader.readAsDataURL(blob);
        });
    }

    addProductToCart(productId: number, quantity: number = 1): Observable<Cart> {
        return this.addToCart(productId, quantity);
    }

    addToCart(productId: number, quantity: number = 1): Observable<Cart> {
        return this.authService.getCurrentUserId().pipe(
            switchMap(userId => {
                if (!userId) {
                    return throwError(() => new Error('Usuario no autenticado'));
                }
                const params = { id: userId.toString() };

                return this.http.post<Cart>(`${this.apiUrl}/users/shoppingcarts/${productId}`,
                    { productId, quantity },{params})
                    .pipe(
                        tap(cart => this.cartSubject.next(cart)),
                        catchError(error => {
                            console.error('Error al añadir al carrito:', error);
                            return throwError(() => error);
                        })
                    );
            })
        );
    }

    addToCartAndNavigate(productId: number, quantity: number = 1): void {
        this.addProductToCart(productId, quantity).subscribe({
            next: (cart) => {
                console.log('Producto añadido al carrito, redirigiendo a la página del carrito');
                this.router.navigate(['/shoppingcart']);
            },
            error: (error) => {
                console.error('Error al añadir al carrito:', error);
                // Opcionalmente, puedes manejar el error aquí
            }
        });
    }

    removeProductFromCart(productId: number): Observable<Cart> {
        return this.removeFromCart(productId);
    }

    removeFromCart(itemId: number): Observable<Cart> {
        return this.authService.getCurrentUserId().pipe(
            switchMap(userId => {
                if (!userId) {
                    return throwError(() => new Error('Usuario no autenticado'));
                }
                const params = { id: userId.toString() };
                return this.http.delete(`${this.apiUrl}/users/shoppingcarts/${itemId}`,{ params })
                    .pipe(
                        // After deleting, fetch the updated cart
                        switchMap(() => this.getCart()),
                        tap(cart => this.cartSubject.next(cart)),
                        catchError(error => {
                            console.error('Error al eliminar del carrito:', error);
                            return throwError(() => error);
                        })
                    );
            })
        );
    }

    updateQuantity(itemId: number, quantity: number): Observable<Cart> {
        return this.authService.getCurrentUserId().pipe(
            switchMap(userId => {
                if (!userId) {
                    return throwError(() => new Error('Usuario no autenticado'));
                }
                const params = { id: userId.toString() };
                return this.http.put<Cart>(`${this.apiUrl}/users/shoppingcarts/update/${itemId}`,
                    { quantity },{ params })
                    .pipe(
                        tap(cart => this.cartSubject.next(cart)),
                        catchError(error => {
                            console.error('Error al actualizar cantidad:', error);
                            return throwError(() => error);
                        })
                    );
            })
        );
    }

    processPayment(): Observable<any> {
        return this.authService.getCurrentUserId().pipe(
            switchMap(userId => {
                if (!userId) {
                    return throwError(() => new Error('Usuario no autenticado'));
                }
                const params = { id: userId.toString() };
                return this.http.post<any>(`${this.apiUrl}/users/shoppingcarts/payment`, {params})
                    .pipe(
                        tap(() => {
                            // Limpiar el carrito después del pago exitoso
                            this.cartSubject.next({ items: [], totalPrice: 0 });
                        }),
                        catchError(error => {
                            console.error('Error al procesar el pago:', error);
                            return throwError(() => error);
                        })
                    );
            })
        );
    }
}