import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  registerForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      nombres: ['', [Validators.required, Validators.minLength(2)]],
      apellidos: ['', [Validators.required, Validators.minLength(2)]],
      dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      nacionalidad: [''],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.pattern(/^\d{9}$/)]],
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit() {
    console.log('1. Función onSubmit() llamada.');
    this.isLoading = true; // Iniciar carga inmediatamente
    this.errorMessage = '';
    this.successMessage = '';

    if (this.registerForm.valid) {
      console.log('2. Formulario es VÁLIDO. Enviando datos...');

      this.authService.register(this.registerForm.value).subscribe({
        next: () => {
          console.log("✅ Registro exitoso. Iniciando redirección.");
          this.successMessage = 'Registro exitoso. Redirigiendo...';
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 1500);
        },
        error: (err) => {
          // Mejoramos la extracción del mensaje de error del backend
          this.errorMessage = err.error?.message || err.error?.error || 'Error en el registro. Verifique sus datos.';
          console.error("❌ Error del Backend:", err);
          this.isLoading = false;
        }
      });
    } else {
      console.log('2. Formulario es INVÁLIDO. Mostrando errores de validación.');

      // Marcar todos los campos como "tocados" para que el HTML muestre los errores
      this.registerForm.markAllAsTouched();
      this.errorMessage = 'Por favor, complete todos los campos requeridos correctamente.';
      this.isLoading = false; // Detener la carga si la validación falla

      // LOGS detallados para depuración
      Object.keys(this.registerForm.controls).forEach(key => {
        const control = this.registerForm.get(key);
        if (control?.invalid) {
          console.log(`Campo inválido: ${key}, Errores: `, control.errors);
        }
      });
    }
  }
}
