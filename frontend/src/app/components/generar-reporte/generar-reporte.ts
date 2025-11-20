import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../navbar/navbar.component';
import { ReporteService } from '../../services/reporte';

@Component({
  selector: 'app-generar-reporte',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './generar-reporte.html',
  styleUrl: './generar-reporte.scss',
})
export class GenerarReporte implements OnInit {
  reportType: string = '';
  fechaInicio: string = '';
  fechaFin: string = '';
  reportData: any[] = [];
  showResults: boolean = false;
  reportTitle: string = '';
  errorMessage: string = '';

  constructor(private reporteService: ReporteService) {}

  ngOnInit() {
    const today = new Date().toISOString().split('T')[0];
    this.fechaInicio = today;
    this.fechaFin = today;
  }

  generarReporte() {
    if (!this.reportType || !this.fechaInicio || !this.fechaFin) {
      this.errorMessage = 'Por favor, complete todos los campos.';
      return;
    }

    if (this.fechaInicio > this.fechaFin) {
      this.errorMessage = 'La fecha de inicio no puede ser posterior a la fecha de fin.';
      return;
    }

    this.errorMessage = '';

    if (this.reportType === 'ingresos') {
      this.reporteService.obtenerIngresosPorPeriodo(this.fechaInicio, this.fechaFin).subscribe({
        next: (data) => {
          this.reportData = data;
          this.reportTitle = 'Ingresos por Período';
          this.showResults = true;
        },
        error: (err) => {
          this.errorMessage = 'Error al generar el reporte de ingresos';
          console.error(err);
        }
      });
    } else if (this.reportType === 'movimiento') {
      this.reporteService.obtenerMovimientoPorPeriodo(this.fechaInicio, this.fechaFin).subscribe({
        next: (data) => {
          this.reportData = data;
          this.reportTitle = 'Movimiento (Check-ins/Check-outs) por Período';
          this.showResults = true;
        },
        error: (err) => {
          this.errorMessage = 'Error al generar el reporte de movimiento';
          console.error(err);
        }
      });
    }
  }

  getTableHeaders(): string[] {
    if (this.reportType === 'ingresos') {
      return ['Fecha', 'Ingresos (S/.)'];
    } else if (this.reportType === 'movimiento') {
      return ['Fecha', 'Check-ins', 'Check-outs'];
    }
    return [];
  }

  getTableRows(): any[][] {
    return this.reportData.map(item => {
      if (this.reportType === 'ingresos') {
        return [item.fecha, item.ingresos?.toFixed(2) || '0.00'];
      } else if (this.reportType === 'movimiento') {
        return [item.fecha, item.checkIns || 0, item.checkOuts || 0];
      }
      return [];
    });
  }
}
