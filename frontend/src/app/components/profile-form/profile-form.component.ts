import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserProfileService } from '../../service/user-profile.service';
import { AuthService } from '../../service/auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile-form',
  templateUrl: './profile-form.component.html',
  styleUrls: ['./profile-form.component.css'],
  standalone: false
})
export class ProfileFormComponent implements OnInit {
  profileForm!: FormGroup;
  successMessage: string = '';
  errorMessage: string = '';
  originalUsername: string = '';
  isLoading: boolean = false;
  showPassword: boolean = false;
  formChanges: any = {};
  originalValues: any = {};
  showConfirmDialog: boolean = false; // Para controlar la visualización del diálogo

  constructor(
    private fb: FormBuilder,
    private userProfileService: UserProfileService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required]],
      username: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: [''] // Nuevo campo opcional para la contraseña
    });

    // Cargar los datos del usuario
    this.loadUserProfile();

    // Escuchar cambios en el formulario para detectar modificaciones
    this.profileForm.valueChanges.subscribe(() => {
      this.detectChanges();
    });
  }

  loadUserProfile(): void {
    this.isLoading = true;
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.originalUsername = currentUser.username;

      this.profileForm.patchValue({
        name: currentUser.name || '',
        username: currentUser.username || '',
        email: currentUser.email || '',
        password: '' // No cargamos la contraseña actual por seguridad
      });

      // Guardar valores originales para comparar después
      this.originalValues = {
        name: currentUser.name || '',
        username: currentUser.username || '',
        email: currentUser.email || ''
      };
    }
    this.isLoading = false;
  }

  // Método para detectar qué campos han cambiado
  detectChanges(): void {
    const formValue = this.profileForm.value;
    this.formChanges = {
      name: formValue.name !== this.originalValues.name,
      username: formValue.username !== this.originalValues.username,
      email: formValue.email !== this.originalValues.email,
      password: formValue.password && formValue.password.trim() !== ''
    };
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  showSuccessMessage(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    setTimeout(() => {
      this.successMessage = '';
    }, 5000);
  }

  showErrorMessage(message: string): void {
    this.errorMessage = message;
    this.successMessage = '';
    setTimeout(() => {
      this.errorMessage = '';
    }, 5000);
  }

  onSubmit(): void {
    if (this.profileForm.valid) {
      // Mostrar diálogo de confirmación
      this.showConfirmDialog = true;
    } else {
      this.showErrorMessage('Por favor, completa correctamente el formulario');
    }
  }

  cancelUpdate(): void {
    this.showConfirmDialog = false;
  }

  confirmUpdate(): void {
    this.showConfirmDialog = false;
    this.updateProfile();
  }

  updateProfile(): void {
    if (this.profileForm.valid) {
      this.isLoading = true;
      const formData = this.profileForm.value;
      const usernameChanged = formData.username !== this.originalUsername;

      // Crear objeto de datos del formulario (omitir password si está vacío)
      const userData = {
        name: formData.name,
        username: formData.username,
        email: formData.email
      };

      // Solo incluir la contraseña si se ha proporcionado una nueva
      if (formData.password && formData.password.trim() !== '') {
        (userData as any).password = formData.password;
      }

      this.userProfileService.editProfile(userData).subscribe({
        next: (response) => {
          if (usernameChanged) {
            this.showSuccessMessage('Perfil actualizado. Necesitas iniciar sesión nuevamente con tu nuevo nombre de usuario.');

            setTimeout(() => {
              this.authService.logout().subscribe({
                next: () => {
                  this.router.navigate(['/login']);
                }
              });
            }, 2000);
          } else {
            this.showSuccessMessage('Perfil actualizado correctamente');

            // Refrescar token para actualizar la información de sesión
            this.authService.refreshToken().subscribe();
          }
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error al actualizar perfil:', error);
          this.showErrorMessage('Error al actualizar el perfil');
          this.isLoading = false;
        }
      });
    } else {
      this.showErrorMessage('Por favor, completa correctamente el formulario');
    }
  }
}