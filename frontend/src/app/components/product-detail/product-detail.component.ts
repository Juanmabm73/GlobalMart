// src/app/components/product-detail/product-detail.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../service/product.service';
import { Cart, ShoppingCartService } from '../../service/shopping-cart.service';
import { AuthService } from '../../service/auth/auth.service';
import { finalize } from 'rxjs';
import { NavComponent } from '../nav/nav.component';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';


@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css'],
  standalone: false,
})
export class ProductDetailComponent implements OnInit {
  product: any = null;
  loading = true;
  addingToCart = false;
  showSuccessMessage = false;  // Añadir esta propiedad
  showErrorMessage = false;    // Añadir esta propiedad
  isAdmin = false; // Variable para almacenar si el usuario es administrador
  reviewForm: FormGroup; // Formulario para las reseñas
  isUser = false; // Verificar si el usuario tiene el rol USER
  isCompany = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: ShoppingCartService,
    private authService: AuthService,
    private fb: FormBuilder // Inyectar FormBuilder
  ) {
    // Inicializar el formulario de reseñas
    this.reviewForm = this.fb.group({
      calification: [null, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['', [Validators.required, Validators.maxLength(500)]],
    });
  }

  ngOnInit(): void {
    const productId = this.route.snapshot.paramMap.get('id');
    if (productId) {
      this.loadProduct(+productId);
    }
    
    // Just one subscription to monitor user state
    this.authService.user$.subscribe((user) => {
      if (user) {
        // Use the AuthService's hasRole method which handles different role formats
        this.isUser = this.authService.hasRole('USER');
        this.isAdmin = this.authService.hasRole('ADMIN');
        this.isCompany = this.authService.hasRole('COMPANY');

        console.log('User roles detected:', {
          isUser: this.isUser,
          isAdmin: this.isAdmin,
          isCompany: this.isCompany,
          userObject: user
        });
      } else {
        this.isUser = false;
        this.isAdmin = false;
        this.isCompany = false;
      }
    });
  }

  updateViewsCount(): void {
    if (this.product) {
      this.productService.updateViewsCount(this.product.id).subscribe(
        (response) => {
          console.log('Views count updated successfully:', response);
        },
        (error) => {
          console.error('Error updating views count:', error);
        }
      );
    } else {
      console.error('No product loaded to update views count.');
    }
  }




  loadProduct(id: number): void {
    this.loading = true;

    // Obtener los datos del producto
    this.productService.getProductById(id).subscribe(
      (data) => {
        this.product = data;
        this.updateViewsCount();
        console.log('Product loaded:', this.product);
        // Obtener la imagen del producto por separado
        this.productService.loadProductImage(id).subscribe(
          (imageBase64) => {
            this.product.imageBase64 = imageBase64; // Asignar la imagen al producto
          },
          (error) => {
            console.error('Error loading product image:', error);
          }
        );

        this.loading = false;
      },
      (error) => {
        console.error('Error loading product:', error);
        this.loading = false;
        this.router.navigate(['/products/allProducts']);
      }
    );
    
  }

  addToCart(): void {
    if (!this.authService.getCurrentUser()) {
      // Si el usuario no está autenticado, guardamos la URL actual para redireccionar después del login
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }

    this.addingToCart = true;

    // Usar el nuevo método que combina añadir al carrito y redirigir
    this.cartService.addToCartAndNavigate(this.product.id, 1);

    // La gestión del estado addingToCart se puede hacer con un timeout simple
    // ya que la redirección ocurrirá rápidamente
    setTimeout(() => {
      this.addingToCart = false;
    }, 500);
  }

  editProduct(): void {
    if (this.product) {
      this.productService.setProduct(this.product);
      this.router.navigate(['/newProduct']);
    } else {
      console.error('No product loaded to edit.');
    }
  }

  submitReview(): void {
    if (this.reviewForm.valid) {
      const reviewData = this.reviewForm.value;
      console.log('Submitting review:', reviewData);

      // Llamar al servicio para enviar la reseña
      this.productService.addReview(this.product.id, reviewData).subscribe(
        (response) => {
          console.log('Review submitted successfully:', response);
          // Actualizar las reseñas del producto
          this.product.reviews.push(response);
          this.reviewForm.reset();
        },
        (error) => {
          console.error('Error submitting review:', error);
        }
      );
    }
  }

  deleteProduct(): void {
    if (this.product) {
      this.productService.declineProduct(this.product.id).subscribe(
        () => {
          console.log(`Product ${this.product.id} deleted successfully.`);
          this.router.navigate(['/products']); // Redirigir a la lista de productos
        },
        (error) => {
          console.error(`Error deleting product ${this.product.id}:`, error);
        }
      );
    } else {
      console.error('No product loaded to delete.');
    }
  }

}