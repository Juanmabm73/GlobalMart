import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, from, tap, switchMap, map, catchError, throwError } from 'rxjs'; // Añadir 'map' a las importaciones
import { environment } from '../enviroments/enviroment';
import { AuthService } from './auth/auth.service'; // Asegúrate de que la ruta es correcta

@Injectable({
    providedIn: 'root'
})
export class ProductService {
    private apiUrl = `${environment.apiUrl}/products`;
    private product: any; // Variable para almacenar el producto

    constructor(private http: HttpClient, private authService: AuthService) { }

    getProducts(page: number = 0, size: number = 5, params?: { accepted?: boolean; company?: string }): Observable<any> {
        let queryParams: any = {
            page: page.toString(),
            size: size.toString(),
        };

        // Agregar parámetros opcionales si están presentes
        if (params?.accepted !== undefined) {
            queryParams.accepted = params.accepted.toString();
        }
        if (params?.company) {
            queryParams.company = params.company;
        }

        return this.http.get<any>(`${this.apiUrl}/`, { params: queryParams }).pipe(
            map((response: any) => this.processProductsResponse(response)) // Procesar las imágenes
        );
    }

    getProductsByType(type: string, page: number = 0, size: number = 5): Observable<any> {
        const params = new HttpParams()
            .set('type', type)
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get(`${this.apiUrl}/type`, { params })
            .pipe(
                map((response: any) => this.processProductsResponse(response)) // Procesar las imágenes
            );
    }

    getProductById(id: number): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/${id}`);
    }

    getMostViewedProducts(): Observable<any[]> {
        return this.http.get(`${this.apiUrl}/mostViewedProducts`)
            .pipe(
                map((response: any) => this.processProductList(response))
            );

    }

    getLastProducts(): Observable<any[]> {
        return this.http.get(`${this.apiUrl}/lastProducts`)
            .pipe(
                map((response: any) => this.processProductList(response))
            );

    }

    createProduct(product: any): Observable<any> {
        console.log('Creando producto:', product);

        // Obtener el token JWT actualizado
        const token = this.authService.getToken();
        console.log('Token JWT actual:', token?.substring(0, 20) + '...'); // Solo mostrar parte del token por seguridad

        if (!token) {
            console.error('No hay token disponible');
            return throwError(() => new Error('No hay token de autenticación disponible'));
        }

        // Obtener información del usuario actual
        const currentUser = this.authService.getCurrentUser();
        if (!currentUser || !this.authService.hasRole('COMPANY')) {
            console.error('Usuario no tiene permisos para crear productos');
            return throwError(() => new Error('No tienes permisos para crear productos'));
        }

        // Añadir información de la compañía al producto si es necesario
        if (currentUser && currentUser.id) {
            product.userId = currentUser.id; // Asignar ID del usuario/compañía al producto
        }

        // Crear headers con el token de autenticación
        const headers = new HttpHeaders({
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        });

        console.log('Enviando solicitud con encabezado de autorización y datos de usuario');

        return this.http.post<any>(`${this.apiUrl}/`, product, {
            headers: headers,
            withCredentials: true
        }).pipe(
            tap(response => console.log('Respuesta de creación de producto:', response)),
            catchError(error => {
                console.error('Error creando producto:', error);

                if (error.status === 401) {
                    console.error('Error de autenticación o autorización. Verifica que tu cuenta tenga los permisos necesarios.');
                }

                throw error;
            })
        );
    }

    updateProduct(id: number, product: any): Observable<any> {
        return this.http.put<any>(`${this.apiUrl}/${id}`, product);
    }

    deleteProduct(id: number): Observable<any> {
        return this.http.delete<any>(`${this.apiUrl}/${id}`);
    }
    setProduct(product: any): void {
        this.product = product;
    }

    getProduct(): any {
        const producto = this.product;
        this.setProduct(null); // Limpiar la variable después de obtener el producto
        return producto;
    }


    /**
     * Procesa la respuesta de la API con múltiples productos
     * y carga las imágenes para cada uno de ellos
     */
    private processProductsResponse(response: any): any {
        if (response && response.content) {
            // Para cada producto en la respuesta, cargar su imagen
            response.content = response.content.map((product: any) => {
                return this.processProductResponse(product);
            });
        }
        return response;
    }

    /**
     * Procesa un único producto y carga su imagen
     */
    private processProductResponse(product: any): any {
        if (product && product.id && !product.imageBase64) {
            // Si el producto no tiene ya una imagen en base64, cargarla
            this.loadProductImage(product.id)
                .subscribe(imageBase64 => {
                    product.imageBase64 = imageBase64;
                });
        }
        return product;
    }


    /**
     * Procesa una lista de productos y carga sus imágenes
     */
    processProductList(products: any[]): any[] {
        return products.map(product => this.processProductResponse(product));
    }

    /**
     * Carga la imagen de un producto por su ID
     */
    loadProductImage(productId: number): Observable<string> {
        return this.http.get(`${this.apiUrl}/${productId}/image`, { responseType: 'blob' }).pipe(
            switchMap((blob) => this.convertBlobToBase64(blob))
        );
    }



    private convertBlobToBase64(blob: Blob): Observable<string> {
        return new Observable((observer) => {
            const reader = new FileReader();
            reader.onloadend = () => {
                observer.next(reader.result as string);
                observer.complete();
            };
            reader.onerror = (error) => {
                observer.error(error);
            };
            reader.readAsDataURL(blob);
        });
    }

    searchProducts(search_text: string, type?: string): Observable<any[]> {
        let params = new HttpParams();

        if (search_text) {
            params = params.append('search_text', search_text);
        }

        if (type && type !== 'All') {
            params = params.append('type', type);
        }

        return this.http.get<any[]>(`${this.apiUrl}/search`, { params }).pipe(
            map(products => {
                // Procesar cada producto para cargar su imagen
                products.forEach(product => this.processProductResponse(product));
                return products;
            }),
            catchError(error => {
                console.error('Error searching products:', error);
                throw error;
            })
        );
    }


    addReview(productId: number, review: { rating: number; comment: string }): Observable<any> {
        return this.http.post<any>(`${environment.apiUrl}/reviews/${productId}`, review);
    }

    getProductsNotAccepted(): Observable<{ content: any[]; page: any }> {
        return this.http.get<{ content: any[]; page: any }>(`${this.apiUrl}/?accepted=false`);
    }

    acceptProduct(productId: number): Observable<any> {
        return this.http.put(`${this.apiUrl}/accept`, null, {
            params: { id: productId.toString() }
        });
    }

    declineProduct(productId: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/delete`, {
            params: { id: productId.toString() }
        });
    }

    updateProductImage(productId: number, imageFormData: FormData): Observable<any> {
        return this.http.put(`${this.apiUrl}/${productId}/image`, imageFormData);

    }

    createProductImage(productId: number, imageFormData: FormData): Observable<any> {
        return this.http.post(`${this.apiUrl}/${productId}/image`, imageFormData);
    }

    updateViewsCount(productId: number): Observable<any> {
        const params = new HttpParams().set('id', productId.toString());
        return this.http.put(`${this.apiUrl}/addViewsCount`, null, { params });
    }
}
