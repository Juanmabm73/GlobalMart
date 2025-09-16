import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../service/product.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-product-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css'],
  standalone: false
})
export class ProductSearchComponent implements OnInit {
  products: any[] = [];
  loading = false;

  constructor(
    private productService: ProductService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Leer parámetros de búsqueda desde la URL
    this.route.queryParams.subscribe(params => {
      const query = params['search_text'] || '';
      const type = params['type'] || 'All';

      this.searchProducts(query, type);
    });
  }

  searchProducts(query: string, type?: string): void {
    this.loading = true;

    const category = type !== 'All' ? type : undefined;

    this.productService.searchProducts(query, category)
      .subscribe({
        next: (results) => {
          this.products = results;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error fetching products:', error);
          this.loading = false;
        }
      });
  }
}
