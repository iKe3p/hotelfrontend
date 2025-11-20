// Efectos para el sistema Oasis Digital
document.addEventListener('DOMContentLoaded', function() {
    
    // Efecto de splash screen con logo
    function showSplashScreen() {
        const splashScreen = document.createElement('div');
        splashScreen.id = 'oasis-splash';
        splashScreen.innerHTML = `
            <div class="splash-content">
                <div class="logo-container">
                    <i class="fas fa-hotel logo-icon"></i>
                    <h1 class="logo-text">Oasis Digital</h1>
                    <p class="logo-subtitle">Lujo y comodidad</p>
                </div>
                <div class="loading-bar">
                    <div class="loading-progress"></div>
                </div>
            </div>
        `;
        
        splashScreen.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: linear-gradient(135deg, #1c1c1c 0%, #2d2d2d 50%, #4A90E2 100%);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 999999;
            animation: splashFadeOut 3s ease-in-out forwards;
        `;
        
        // Ocultar el contenido del body mientras se muestra el splash
        document.body.style.overflow = 'hidden';
        
        document.body.appendChild(splashScreen);
        
        setTimeout(() => {
            splashScreen.remove();
            document.body.style.overflow = 'auto';
        }, 3000);
    }
    
    // Función para mostrar efecto de bienvenida personalizado
    function showWelcomeEffect(userName, userRole) {
        showOperationEffect('bienvenida', `¡Bienvenido ${userName}!`);
    }
    
    // Función para mostrar efecto de despedida
    function showGoodbyeEffect() {
        showOperationEffect('despedida', '¡Hasta pronto!');
    }
    
    // Agregar estilos CSS para animaciones
    const style = document.createElement('style');
    style.textContent = `
        @keyframes splashFadeOut {
            0% { opacity: 1; }
            80% { opacity: 1; }
            100% { opacity: 0; }
        }
        
        .splash-content {
            text-align: center;
            color: white;
        }
        
        .logo-container {
            animation: logoAnimation 2.5s ease-in-out;
        }
        
        .logo-icon {
            font-size: 4rem;
            color: #4A90E2;
            margin-bottom: 20px;
            animation: iconPulse 2s ease-in-out infinite;
        }
        
        .logo-text {
            font-size: 3rem;
            font-weight: bold;
            margin: 0;
            font-family: 'Playfair Display', serif;
            text-shadow: 0 4px 8px rgba(0,0,0,0.5);
        }
        
        .logo-subtitle {
            font-size: 1.2rem;
            margin: 10px 0 30px 0;
            opacity: 0.9;
        }
        
        .loading-bar {
            width: 200px;
            height: 4px;
            background: rgba(255,255,255,0.3);
            border-radius: 2px;
            margin: 0 auto;
            overflow: hidden;
        }
        
        .loading-progress {
            width: 0;
            height: 100%;
            background: linear-gradient(90deg, #4A90E2, #87CEEB);
            border-radius: 2px;
            animation: loadingProgress 2.5s ease-in-out;
        }
        
        @keyframes logoAnimation {
            0% { transform: scale(0.5) rotate(-10deg); opacity: 0; }
            50% { transform: scale(1.1) rotate(2deg); opacity: 1; }
            100% { transform: scale(1) rotate(0deg); opacity: 1; }
        }
        
        @keyframes iconPulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.1); }
        }
        
        @keyframes loadingProgress {
            0% { width: 0%; }
            100% { width: 100%; }
        }
        
        @keyframes welcomeAnimation {
            0% { 
                opacity: 0; 
                transform: translate(-50%, -50%) scale(0.3) rotate(-10deg); 
            }
            15% { 
                opacity: 1; 
                transform: translate(-50%, -50%) scale(1.15) rotate(2deg); 
            }
            25% { 
                transform: translate(-50%, -50%) scale(0.95) rotate(-1deg); 
            }
            35% { 
                transform: translate(-50%, -50%) scale(1.05) rotate(0.5deg); 
            }
            85% { 
                opacity: 1; 
                transform: translate(-50%, -50%) scale(1) rotate(0deg); 
            }
            100% { 
                opacity: 0; 
                transform: translate(-50%, -50%) scale(0.7) rotate(5deg); 
            }
        }
        
        @keyframes operationSuccess {
            0% { transform: scale(1); }
            50% { transform: scale(1.08); box-shadow: 0 8px 25px rgba(74, 144, 226, 0.4); }
            100% { transform: scale(1); }
        }
        
        @keyframes pulseGlow {
            0%, 100% { box-shadow: 0 0 5px rgba(74, 144, 226, 0.3); }
            50% { box-shadow: 0 0 20px rgba(74, 144, 226, 0.6); }
        }
        
        .operation-effect {
            animation: operationSuccess 0.8s ease-in-out;
        }
        
        .btn:hover {
            animation: pulseGlow 1.5s ease-in-out infinite;
        }
        
        .card:hover {
            animation: pulseGlow 2s ease-in-out infinite;
        }
    `;
    
    document.head.appendChild(style);
    
    // Detectar si el usuario acaba de hacer login exitoso
    const urlParams = new URLSearchParams(window.location.search);
    const isLoginSuccessParam = urlParams.get('loginSuccess') === 'true';
    const isDashboard = window.location.pathname === '/dashboard';
    const isFirstDashboardVisit = !sessionStorage.getItem('dashboardVisited');
    const isLoginSuccess = isLoginSuccessParam || (isDashboard && isFirstDashboardVisit);
    
    console.log('Debug - Login Success Detection:', {
        isLoginSuccessParam,
        isDashboard,
        isFirstDashboardVisit,
        isLoginSuccess,
        currentPath: window.location.pathname,
        urlParams: window.location.search
    });
    
    // Mostrar splash screen si es login exitoso
    if (isLoginSuccess && !sessionStorage.getItem('splashShown')) {
        showSplashScreen();
        sessionStorage.setItem('splashShown', 'true');
    }
    
    if (isLoginSuccess) {
        console.log('Mostrando efectos de bienvenida...');
        // Obtener información del usuario desde el DOM
        const userNameElement = document.querySelector('[sec\\:authentication="name"]');
        const userRoleElement = document.querySelector('.badge.bg-primary');
        
        let userName = 'Usuario';
        let userRole = '';
        
        // Buscar el nombre en el navbar
        const navbarSpans = document.querySelectorAll('.navbar span');
        navbarSpans.forEach(span => {
            const text = span.textContent || span.innerText;
            if (text.includes('Bienvenido')) {
                const match = text.match(/Bienvenido,?\s*(.+)/);
                if (match) {
                    userName = match[1].trim();
                }
            }
        });
        
        // Si no encontramos en navbar, buscar en otros elementos
        if (userName === 'Usuario' && userNameElement) {
            userName = userNameElement.textContent || userNameElement.innerText || 'Usuario';
        }
        
        if (userRoleElement) {
            userRole = userRoleElement.textContent || userRoleElement.innerText;
        }
        
        // Mostrar efecto de bienvenida después del splash
        const delay = isLoginSuccess ? 3500 : 500;
        setTimeout(() => {
            showWelcomeEffect(userName, userRole);
        }, delay);
        
        // Marcar que ya visitó el dashboard en esta sesión
        sessionStorage.setItem('dashboardVisited', 'true');
        
        // Limpiar el parámetro de la URL después de mostrar el efecto
        if (isLoginSuccessParam) {
            const newUrl = window.location.pathname;
            window.history.replaceState({}, document.title, newUrl);
        }
    }
    
    // Detectar botones de cerrar sesión
    const logoutButtons = document.querySelectorAll('button[type="submit"]');
    logoutButtons.forEach(button => {
        const form = button.closest('form');
        if (form && (form.action.includes('logout') || button.textContent.includes('Cerrar'))) {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                showGoodbyeEffect();
                // Limpiar sessionStorage para permitir efectos en próximo login
                sessionStorage.removeItem('dashboardVisited');
                sessionStorage.removeItem('splashShown');
                setTimeout(() => {
                    form.submit();
                }, 1000);
            });
        }
    });
        

    
    // Efecto para alertas de éxito/error
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        alert.classList.add('operation-effect');
    });
    
    // Efecto para botones al hacer clic
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(button => {
        button.addEventListener('click', function(e) {
            // Crear efecto de ondas
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(255, 255, 255, 0.4);
                border-radius: 50%;
                transform: scale(0);
                animation: rippleEffect 0.6s ease-out;
                pointer-events: none;
            `;
            
            // Agregar animación de ondas
            if (!document.querySelector('#ripple-style')) {
                const rippleStyle = document.createElement('style');
                rippleStyle.id = 'ripple-style';
                rippleStyle.textContent = `
                    @keyframes rippleEffect {
                        to {
                            transform: scale(2);
                            opacity: 0;
                        }
                    }
                `;
                document.head.appendChild(rippleStyle);
            }
            
            this.style.position = 'relative';
            this.style.overflow = 'hidden';
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
            
            // Efecto de escala
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = 'scale(1)';
            }, 150);
        });
    });
    
    // Efecto para tarjetas
    const cards = document.querySelectorAll('.card');
    cards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transition = 'all 0.3s ease';
            this.style.transform = 'translateY(-5px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });
});

// Efectos para operaciones específicas
function showOperationEffect(type, message) {
    const effectDiv = document.createElement('div');
    effectDiv.className = 'operation-notification';
    
    let icon = '';
    let color = '';
    
    switch(type) {
        case 'bienvenida':
            icon = 'fas fa-star';
            color = '#4A90E2';
            break;
        case 'despedida':
            icon = 'fas fa-hand-wave';
            color = '#dc3545';
            break;
        case 'reserva':
            icon = 'fas fa-calendar-check';
            color = '#4A90E2';
            break;
        case 'cancelar':
            icon = 'fas fa-times-circle';
            color = '#dc3545';
            break;
        case 'finalizar':
            icon = 'fas fa-check-circle';
            color = '#28a745';
            break;
        case 'habitacion':
            icon = 'fas fa-bed';
            color = '#17a2b8';
            break;
        case 'reporte':
            icon = 'fas fa-chart-bar';
            color = '#6f42c1';
            break;
        case 'historial':
            icon = 'fas fa-history';
            color = '#fd7e14';
            break;
        default:
            icon = 'fas fa-check';
            color = '#4A90E2';
    }
    
    effectDiv.innerHTML = `
        <div class="operation-content">
            <i class="${icon} operation-icon"></i>
            <p class="operation-message">${message}</p>
        </div>
    `;
    
    effectDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: linear-gradient(135deg, ${color}, ${color}dd);
        color: white;
        padding: 20px 25px;
        border-radius: 15px;
        z-index: 9998;
        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        animation: slideInRight 0.5s ease-out, slideOutRight 0.5s ease-in 3s forwards;
        min-width: 300px;
        border: 2px solid rgba(255,255,255,0.2);
    `;
    
    // Agregar estilos de animación si no existen
    if (!document.querySelector('#operation-animations')) {
        const animationStyle = document.createElement('style');
        animationStyle.id = 'operation-animations';
        animationStyle.textContent = `
            @keyframes slideInRight {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            
            @keyframes slideOutRight {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(100%); opacity: 0; }
            }
            
            .operation-content {
                display: flex;
                align-items: center;
                gap: 15px;
            }
            
            .operation-icon {
                font-size: 1.5rem;
                animation: bounce 1s ease-in-out;
            }
            
            .operation-message {
                margin: 0;
                font-weight: 600;
                font-size: 1rem;
            }
            
            @keyframes bounce {
                0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
                40% { transform: translateY(-10px); }
                60% { transform: translateY(-5px); }
            }
        `;
        document.head.appendChild(animationStyle);
    }
    
    document.body.appendChild(effectDiv);
    
    setTimeout(() => {
        if (effectDiv.parentNode) {
            effectDiv.remove();
        }
    }, 4000);
}

// Detectar operaciones automáticamente
document.addEventListener('DOMContentLoaded', function() {
    // Detectar formularios de reserva
    const reservaForms = document.querySelectorAll('form[action*="reservas"], form[action*="guardar"]');
    reservaForms.forEach(form => {
        form.addEventListener('submit', function() {
            setTimeout(() => {
                showOperationEffect('reserva', '¡Procesando reserva!');
            }, 100);
        });
    });
    
    // Detectar botones de cancelar
    const cancelarBtns = document.querySelectorAll('button:contains("Cancelar"), input[value*="cancelar"]');
    cancelarBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            showOperationEffect('cancelar', 'Cancelando operación...');
        });
    });
    
    // Detectar botones de finalizar
    const finalizarBtns = document.querySelectorAll('button:contains("Finalizar"), input[value*="finalizar"]');
    finalizarBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            showOperationEffect('finalizar', 'Finalizando proceso...');
        });
    });
    
    // Detectar formularios de habitaciones
    const habitacionForms = document.querySelectorAll('form[action*="habitacion"]');
    habitacionForms.forEach(form => {
        form.addEventListener('submit', function() {
            showOperationEffect('habitacion', 'Gestionando habitación...');
        });
    });
    
    // Detectar enlaces de reportes
    const reporteLinks = document.querySelectorAll('a[href*="reporte"], a[href*="generar"]');
    reporteLinks.forEach(link => {
        link.addEventListener('click', function() {
            showOperationEffect('reporte', 'Generando reporte...');
        });
    });
    
    // Detectar enlaces de historial
    const historialLinks = document.querySelectorAll('a[href*="historial"]');
    historialLinks.forEach(link => {
        link.addEventListener('click', function() {
            showOperationEffect('historial', 'Cargando historial...');
        });
    });
});