import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Cart, ShoppingCartService } from '../../service/shopping-cart.service';
import { AuthService } from '../../service/auth/auth.service';

@Component({
  selector: 'app-shopping-cart',
  templateUrl: './shopping-cart.component.html',
  styleUrls: ['./shopping-cart.component.css'],
  standalone: false,
})
export class ShoppingCartComponent implements OnInit {
  cart: any = { cartProducts: [] };
  loading = true;
  loadError = false;
  isEmpty = true;
  processingPayment = false;
  checkoutMessage = '';

  constructor(
    private shoppingCartService: ShoppingCartService,
    private router: Router,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.authService.isLoggedIn$.subscribe(isLoggedIn => {
      if (isLoggedIn) {
        this.loadCart();
      } else {
        this.isEmpty = true;
        this.loading = false;
      }
    });
  }

  loadCart(): void {
    this.shoppingCartService.getCart().subscribe(
      (data: any) => {
        console.log('Datos recibidos del carrito:', data);

        if (data.cartProducts) {
          this.cart = data;
        }

        this.cart.cartProducts = this.cart.cartProducts.map((item: any) => {
          if (!item.product) {
            item.product = { id: item.productId };
          }
          return item;
        });
        console.log("Carrito después de la adaptación:", this.cart);

        this.isEmpty = !this.cart.cartProducts || this.cart.cartProducts.length === 0;
        this.loading = false;
      },
      (error: any) => {
        console.error('Error al cargar el carrito:', error);
        this.loadError = true;
        this.loading = false;
      }
    );
  }

  removeProduct(productId: number): void {
    this.shoppingCartService.removeFromCart(productId).subscribe(
      (updated: Cart) => {
        this.cart = {
          ...updated,
          cartProducts: updated.items
        };
      },
      (error: any) => console.error('Error al eliminar producto:', error)
    );
  }

  checkout(): void {
    this.loading = true;
    this.checkoutMessage = 'Procesando pago...';

    this.shoppingCartService.processPayment().subscribe({
      next: () => {
        this.checkoutMessage = 'Pago procesado correctamente. Redirigiendo...';
        setTimeout(() => {
          this.router.navigate(['/']);
        }, 2000);
      },
      error: (error) => {
        console.error('Error al procesar el pago:', error);
        this.checkoutMessage = 'Error al procesar el pago. Inténtalo de nuevo.';
        this.loading = false;
      }
    });
  }
}