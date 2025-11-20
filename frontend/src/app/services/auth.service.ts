import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, BehaviorSubject } from 'rxjs'; // <--- AGREGAR BehaviorSubject
import { LoginRequest, RegisterRequest, AuthResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8084/api/auth';
  private tokenKey = 'jwt_token';
  private userKey = 'current_user';

  // 1. Creamos el notificador de estado
  // Revisa si ya existe un token al iniciar la aplicación para poner true o false
  private loggedIn = new BehaviorSubject<boolean>(this.hasToken());

  // 2. Exponemos el observable para que el Navbar se suscriba
  public isLoggedIn$ = this.loggedIn.asObservable();

  constructor(private http: HttpClient) {}

  // Helper privado para verificar existencia de token
  private hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem(this.tokenKey, response.token);
          this.decodeAndStoreUser(response.token);

          // 3. AVISAMOS QUE EL USUARIO ENTRÓ
          this.loggedIn.next(true); // <--- IMPORTANTE
        }
      })
    );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem(this.tokenKey, response.token);
          this.decodeAndStoreUser(response.token);

          // 3. AVISAMOS QUE EL USUARIO ENTRÓ
          this.loggedIn.next(true); // <--- IMPORTANTE
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);

    // 4. AVISAMOS QUE EL USUARIO SALIÓ
    this.loggedIn.next(false); // <--- IMPORTANTE
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return this.hasToken();
  }

  getUser(): any {
    const userStr = localStorage.getItem(this.userKey);
    return userStr ? JSON.parse(userStr) : null;
  }

  hasRole(role: string): boolean {
    const user = this.getUser();
    if (!user) return false;
    return user.rol === role || user.authorities?.some((auth: any) => auth.authority === role);
  }

  hasAnyRole(...roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  private decodeAndStoreUser(token: string): void {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      localStorage.setItem(this.userKey, JSON.stringify(payload));
    } catch (e) {
      console.error('Error decoding token', e);
    }
  }
}
