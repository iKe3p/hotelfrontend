import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Subscription, filter } from 'rxjs';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit, OnDestroy {
  isLoggedIn = false;
  isAdmin = false;
  isRecepcionista = false;
  isCliente = false;
  username = '';
  menuOpen = false;
  dropdownOpen = false;
  empleadosDropdownOpen = false;
  private routerSubscription?: Subscription;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

// navbar.component.ts

ngOnInit() {
  this.authService.isLoggedIn$.subscribe(isLogged => {
    this.isLoggedIn = isLogged;

    if (this.isLoggedIn) {
      const user = this.authService.getUser();
      console.log('👤 DATOS DEL USUARIO:', user);

      this.username = user?.sub || '';

      // --- CORRECCIÓN: ---
      // Si el token NO trae roles, pero el usuario es 'admin', forzamos a TRUE.
      if (this.username === 'admin') {
          this.isAdmin = true;
          console.log('✅ Admin detectado por nombre');
      } else {
          // Si no es 'admin', intentamos buscar el rol normalmente
          this.isAdmin = this.authService.hasRole('ROLE_ADMIN');
      }
      // -------------------

      // Hacemos lo mismo para recepcionista si fuera necesario
      this.isRecepcionista = this.authService.hasRole('ROLE_RECEPCIONISTA');
      this.isCliente = this.authService.hasRole('ROLE_CLIENTE');
    } else {
      this.username = '';
      this.isAdmin = false;
      this.isRecepcionista = false;
      this.isCliente = false;
    }
  });
}

  ngOnDestroy() {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  updateAuthState() {
    const isAuth = this.authService.isAuthenticated();
    console.log('Estado de Autenticación:', isAuth); // <--- AGREGA ESTO
    console.log('Token en Storage:', localStorage.getItem('token')); // <--- AGREGA ESTO (asumiendo que usas 'token')

    this.isLoggedIn = this.authService.isAuthenticated();
    if (this.isLoggedIn) {
      const user = this.authService.getUser();
      this.username = user?.sub || '';
      this.isAdmin = this.authService.hasRole('ROLE_ADMIN');
      this.isRecepcionista = this.authService.hasRole('ROLE_RECEPCIONISTA');
      this.isCliente = this.authService.hasRole('ROLE_CLIENTE');
    } else {
      this.username = '';
      this.isAdmin = false;
      this.isRecepcionista = false;
      this.isCliente = false;
    }
  }

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  toggleDropdown(event: Event) {
    event.preventDefault();
    this.dropdownOpen = !this.dropdownOpen;
  }

  toggleEmpleadosDropdown(event: Event) {
    event.preventDefault();
    this.empleadosDropdownOpen = !this.empleadosDropdownOpen;
  }

  closeDropdown() {
    this.dropdownOpen = false;
  }

  closeEmpleadosDropdown() {
    this.empleadosDropdownOpen = false;
  }

  logout() {
    this.authService.logout();
    this.updateAuthState();
    this.router.navigate(['/']).then(() => {
      // Forzar recarga del componente para actualizar la vista
      window.location.reload();
    });
  }
}
