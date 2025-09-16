import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../service/product.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css'],
  imports: [CommonModule]
})
export class AdminComponent implements OnInit {
  productsNotAccepted: { content: any[]; page: any } = { content: [], page: {} }; // Inicializar con valores vacíos

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadProductsNotAccepted();
  }

  // Cargar productos no aceptados
  loadProductsNotAccepted(): void {
    this.productService.getProductsNotAccepted().subscribe(
      (response) => {
        console.log('Products not accepted:', response); // Verificar la respuesta del backend
        this.productsNotAccepted = response;

        // Cargar imágenes para cada producto
        this.productsNotAccepted.content.forEach(product => {
          this.productService.loadProductImage(product.id).subscribe(
            (imageBase64) => {
              product.imageBase64 = imageBase64; // Asignar la imagen al producto
            },
            (error) => {
              console.error(`Error loading image for product ${product.id}:`, error);
            }
          );
        });
      },
      (error) => {
        console.error('Error loading products not accepted:', error);
        this.productsNotAccepted = { content: [], page: {} };
      }
    );
  }

  // Aceptar producto
  acceptProduct(productId: number): void {
    this.productService.acceptProduct(productId).subscribe(
      () => {
        console.log(`Product ${productId} accepted successfully.`);
        if (this.productsNotAccepted) {
          this.productsNotAccepted.content = this.productsNotAccepted.content.filter(product => product.id !== productId);
        }
      },
      (error) => {
        console.error(`Error accepting product ${productId}:`, error);
      }
    );
  }

  // Rechazar producto
  declineProduct(productId: number): void {
    this.productService.declineProduct(productId).subscribe(
      () => {
        console.log(`Product ${productId} declined successfully.`);
        if (this.productsNotAccepted) {
          this.productsNotAccepted.content = this.productsNotAccepted.content.filter(product => product.id !== productId);
        }
      },
      (error) => {
        console.error(`Error declining product ${productId}:`, error);
      }
    );
  }
}
