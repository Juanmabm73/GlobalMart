import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductService } from '../../service/product.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-form',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.css'],
  standalone: false
})
export class FormComponent implements OnInit {
  productForm: FormGroup;
  product: any = null;
  productTypes: string[] = ['Technology', 'Books', 'Education', 'Appliances', 'Sports', 'Home', 'Music', 'Cinema'];
  form_title: string = 'Create New Product';
  selectedFile: File | null = null; // Añadir esta propiedad para el archivo seleccionado

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private router: Router
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required]],
      price: [0, [Validators.required, Validators.min(0)]],
      description: ['', Validators.required],
      type: ['', Validators.required],
      stock: [0, [Validators.required, Validators.min(0)]],
      image: [null],
    });
  }

  ngOnInit(): void {
   
      this.product = this.productService.getProduct();
    

    if (this.product) {
      this.form_title = 'Edit Product';
      this.productForm.patchValue({
        name: this.product.name,
        price: this.product.price,
        description: this.product.description,
        type: this.product.type,
        stock: this.product.stock,
      });
    } else {
      this.form_title = 'Create New Product';
    }
  }

 
  // Manejar el envío del formulario
  onSubmit(): void {
    if (this.productForm.valid) {
      const formData = this.productForm.value;

      if (this.product) {
        // Actualizar producto existente
        this.productService.updateProduct(this.product.id, formData).subscribe({
          next: () => {
            console.log('Product updated successfully');

            // Si hay una imagen seleccionada, actualizar la imagen
            if (this.selectedFile) {
              const imageFormData = new FormData();
              imageFormData.append('file', this.selectedFile); // Adjuntar el archivo seleccionado

              this.productService.updateProductImage(this.product.id, imageFormData).subscribe({
                next: () => {
                  console.log('Product image updated successfully');
                  this.router.navigate(['/products']); // Redirigir después de actualizar
                },
                error: (error) => {
                  console.error('Error updating product image:', error);
                }
              });
            } else {
              this.router.navigate(['/products']); // Redirigir si no hay imagen
            }
          },
          error: (error) => {
            console.error('Error updating product:', error);
          }
        });
      } else {
        // Crear un nuevo producto
        this.productService.createProduct(formData).subscribe({
          next: (createdProduct) => {
            console.log('Product created successfully');

            // Si hay una imagen seleccionada, subir la imagen
            if (this.selectedFile) {
              const imageFormData = new FormData();
              imageFormData.append('file', this.selectedFile); // Adjuntar el archivo seleccionado

              this.productService.createProductImage(createdProduct.id, imageFormData).subscribe({
                next: () => {
                  console.log('Product image uploaded successfully');
                  this.router.navigate(['/products']); // Redirigir después de crear
                },
                error: (error) => {
                  console.error('Error uploading product image:', error);
                }
              });
            } else {
              this.router.navigate(['/products']); // Redirigir si no hay imagen
            }
          },
          error: (error) => {
            console.error('Error creating product:', error);
          }
        });
      }
    } else {
      // Si no hay imagen, envía los datos sin imagen
      const productData = {
        name: this.productForm.value.name,
        price: this.productForm.value.price,
        description: this.productForm.value.description,
        type: this.productForm.value.type,
        stock: this.productForm.value.stock
      };

      this.sendProduct(productData);
    }
  }

  // Método auxiliar para enviar los datos
  sendProduct(productData: any) {
    this.productService.createProduct(productData).subscribe({
      next: (response) => {
        console.log('Producto creado con éxito:', response);
        this.router.navigate(['/']);
      },
      error: (error) => {
        console.error('Error al crear producto:', error);
      }
    });
  }

  // Manejar el cambio de imagen - Versión corregida
  onImageChange(event: any): void {
    const files = event.target.files;
    if (files && files.length > 0) {
      const selectedFile = files[0];

      // Verificar explícitamente el tipo de archivo y logearlo
      console.log('Tipo de archivo seleccionado:', selectedFile.type);

      // Aceptar solo tipos de imagen comunes
      const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/jpg'];

      if (!allowedTypes.includes(selectedFile.type)) {
        console.error('Tipo de archivo no soportado:', selectedFile.type);
        alert('Solo se permiten archivos de imagen (JPG, PNG, GIF)');
        return;
      }

      // Si el tipo es válido, continuar
      this.selectedFile = selectedFile;
      console.log('Archivo seleccionado:', this.selectedFile?.name, 'Tipo:', this.selectedFile?.type);
    }
  }
}