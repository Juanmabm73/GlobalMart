import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../service/auth/auth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: false
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  error = '';
  returnUrl = '/';

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    // Obtener URL de retorno si existe
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/';
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';

    const { username, password } = this.loginForm.value;

    console.log('Enviando solicitud de login:', { username });

    this.authService.login(username, password)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (response) => {
          console.log('Login exitoso:', response);

          // Verificar explícitamente si se ha iniciado sesión
          if (this.authService.getToken()) {
            console.log('Token almacenado correctamente, redirigiendo...');
            setTimeout(() => {
              this.router.navigate([this.returnUrl]);
            }, 100); // Pequeño retraso para asegurar que todos los observables se han actualizado
          } else {
            console.error('Error: No se pudo almacenar el token');
            this.error = 'Error al iniciar sesión: no se pudo almacenar la sesión';
          }
        },
        error: (err: HttpErrorResponse) => {
          console.error('Error de login completo:', err);

          if (err.status === 401) {
            this.error = 'Usuario o contraseña incorrectos';
          } else if (err.status === 0) {
            this.error = 'No se pudo conectar con el servidor';
          } else {
            this.error = err.error?.message || 'Error al iniciar sesión';
          }
        }
      });
  }
}