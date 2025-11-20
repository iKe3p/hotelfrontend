import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./components/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'habitaciones',
    loadComponent: () => import('./components/habitaciones/habitaciones.component').then(m => m.HabitacionesComponent),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'])]
  },
  {
    path: 'habitaciones/nueva',
    loadComponent: () => import('./components/habitacion-form/habitacion-form').then(m => m.HabitacionForm),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN'])]
  },
  {
    path: 'habitaciones/editar/:id',
    loadComponent: () => import('./components/habitacion-form/habitacion-form').then(m => m.HabitacionForm),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN'])]
  },
  {
    path: 'habitaciones/publico',
    loadComponent: () => import('./components/habitaciones-publico/habitaciones-publico').then(m => m.HabitacionesPublico)
  },
  {
    path: 'clientes',
    loadComponent: () => import('./components/clientes/clientes.component').then(m => m.ClientesComponent),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'])]
  },
  {
    path: 'clientes/historial',
    loadComponent: () => import('./components/historial-cliente/historial-cliente').then(m => m.HistorialCliente),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'])]
  },
  {
    path: 'clientes/editar/:id',
    loadComponent: () => import('./components/editar-cliente/editar-cliente').then(m => m.EditarCliente),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'])]
  },
  {
    path: 'clientes/registro',
    loadComponent: () => import('./components/registro-cliente/registro-cliente').then(m => m.RegistroCliente),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'])]
  },
  {
    path: 'reservas',
    loadComponent: () => import('./components/reservas/reservas.component').then(m => m.ReservasComponent),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA', 'ROLE_CLIENTE'])]
  },
  {
    path: 'reservas/crear',
    loadComponent: () => import('./components/generar-reserva/generar-reserva').then(m => m.GenerarReserva),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA'])]
  },
  {
    path: 'reservas/:id/servicios',
    loadComponent: () => import('./components/seleccionar-servicios/seleccionar-servicios').then(m => m.SeleccionarServicios),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA', 'ROLE_CLIENTE'])]
  },
  {
    path: 'reservas/:id/pago',
    loadComponent: () => import('./components/pago-reserva/pago-reserva').then(m => m.PagoReserva),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_RECEPCIONISTA', 'ROLE_CLIENTE'])]
  },
  {
    path: 'empleados/lista',
    loadComponent: () => import('./components/lista-empleados/lista-empleados').then(m => m.ListaEmpleados),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN'])]
  },
  {
    path: 'empleados/registrar',
    loadComponent: () => import('./components/registrar-empleado/registrar-empleado').then(m => m.RegistrarEmpleado),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN'])]
  },
  {
    path: 'empleados/editar/:id',
    loadComponent: () => import('./components/editar-empleado/editar-empleado').then(m => m.EditarEmpleado),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN'])]
  },
  {
    path: 'reportes/generar',
    loadComponent: () => import('./components/generar-reporte/generar-reporte').then(m => m.GenerarReporte),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN'])]
  },
  {
    path: 'auditoria/logs',
    loadComponent: () => import('./components/lista-logs/lista-logs').then(m => m.ListaLogs),
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN'])]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
