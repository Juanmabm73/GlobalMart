import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  standalone: false
})
export class RegisterComponent {
  registerForm: FormGroup;
  error: string = '';
  loading: boolean = false;
  selectedFile: File | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      role: ['USER', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(formGroup: FormGroup) {
    const password = formGroup.get('password')?.value;
    const confirmPassword = formGroup.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.selectedFile = input.files[0];
    }
  }

  onSubmit() {
    // Stop here if form is invalid
    if (this.registerForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';

    const { username, email, password, role } = this.registerForm.value;
    // By default, register as 'USER'
    const roles = [role];

    console.log('Enviando datos:', { username, email, password, role: roles });

    this.authService.register(username, email, password, roles).subscribe({
        next: (response: any) => {
            console.log('Registro exitoso:', response);

            // Obtener el ID del usuario registrado
            const userId = response // Asegúrate de que el backend devuelva el ID del usuario registrado
            // Asegúrate de que el backend devuelva el ID del usuario registrado
            console.log('ID del usuario registrado:', userId);
            if (this.selectedFile) {
                const imageFormData = new FormData();
                imageFormData.append('file', this.selectedFile);

                this.authService.createUserImage(userId, imageFormData).subscribe({
                    next: () => {
                        console.log('Imagen del usuario actualizada correctamente');
                        this.router.navigate(['/login'], { queryParams: { registered: true } });
                    },
                    error: (error) => {
                        console.error('Error al actualizar la imagen del usuario:', error);
                        this.router.navigate(['/login'], { queryParams: { registered: true } });
                    }
                });
            } else {
                this.router.navigate(['/login'], { queryParams: { registered: true } });
            }
        },
        error: (error) => {
          console.error('Registration error', error);
          this.error = error.error || 'Registration failed. Please try again.';
          this.loading = false;
        }
      });
  }
}