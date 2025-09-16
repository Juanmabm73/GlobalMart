import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProductService } from '../../service/product.service';
import { AuthService } from '../../service/auth/auth.service';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css'],
  standalone: false,
})
export class ProductListComponent implements OnInit {
  // Propiedades
  type: string | null = null;
  products: any[] = [];
  filteredProducts: any[] = [];
  hasMoreProducts: boolean = true;
  loading: boolean = false;
  page: number = 0;
  pageSize: number = 5;
  totalElements: number = 0;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private authService: AuthService // Inyectar AuthService
  ) { }

  ngOnInit(): void {
    // Captura el parámetro de la URL para determinar el tipo de productos a mostrar
    this.route.params.subscribe(params => {
      this.type = params['category'] || null;
      this.page = 0; // Resetear la página cuando cambia el tipo
      this.loadProducts();
    });
  }

  loadProducts(): void {
    this.loading = true;

    // Determinar los parámetros según el rol del usuario
    this.authService.user$.subscribe(user => {
      const params: any = {};

      if (this.type) {
        // Si hay un tipo definido, filtrar por tipo
        this.productService.getProductsByType(this.type, this.page, this.pageSize).subscribe(
          response => {
            this.handleProductsResponse(response);
          },
          error => {
            console.error('Error loading products by type', error);
            this.loading = false;
          }
        );
      } else {
        // Caso general: cargar productos sin filtro de tipo
        
        if (this.authService.hasRole("COMPANY")) {
          params.accepted = true;
          params.company = user.username; // Nombre del usuario como compañía
        } else {
          params.accepted = true;
        }

        // Llamar al servicio con los parámetros construidos
        this.productService.getProducts(this.page, this.pageSize, params).subscribe(
          response => {
            this.handleProductsResponse(response);
          },
          error => {
            console.error('Error loading products', error);
            this.loading = false;
          }
        );
      }
    });
  }

  handleProductsResponse(response: any): void {
    if (this.page === 0) {
      this.products = response.content || [];
    } else {
      this.products = [...this.products, ...(response.content || [])];
    }
    this.totalElements = response.totalElements || 0;
    this.hasMoreProducts = !response.last;
    this.filteredProducts = this.products;
    this.loading = false;

    // Añade logs para depuración
    console.log('Productos cargados:', this.filteredProducts);
  }

  loadMore(): void {
    this.page++;
    this.loadProducts();
  }
}