// Configuración de URLs de los servicios
//const USER_SERVICE_URL = 'http://localhost:8081';
const USER_SERVICE_URL = 'https://9aeuu5z7r0.execute-api.us-east-1.amazonaws.com/v1';

// Obtener datos del registro si vienen de la página anterior
const urlParams = new URLSearchParams(window.location.search);
const prefilledUsername = urlParams.get('username');
if (prefilledUsername) {
    document.getElementById('username').value = prefilledUsername;
}

document.getElementById('verifyForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value.trim();
    const code = document.getElementById('code').value.trim();
    
    if (username === '' || code === '') {
        showMessage('Por favor, completa todos los campos', 'error');
        return;
    }
    
    if (code.length !== 6) {
        showMessage('El código debe tener 6 dígitos', 'error');
        return;
    }
    
    try {
        showMessage('Verificando código...', 'info');
        
        const response = await fetch(`${USER_SERVICE_URL}/api/auth/verify`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, confirmationCode: code })
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            showMessage(data.error || 'Código de verificación inválido', 'error');
            return;
        }
        
        showMessage('¡Cuenta verificada exitosamente! Redirigiendo al login...', 'success');
        
        // Redirigir al login después de 2 segundos
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
        
    } catch (error) {
        console.error('Error:', error);
        showMessage('Error de conexión con el servidor. Asegúrate de que el User Service esté corriendo en el puerto 8081', 'error');
    }
});

// Reenviar código
document.getElementById('resendCode').addEventListener('click', async function(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value.trim();
    
    if (username === '') {
        showMessage('Por favor, ingresa tu nombre de usuario primero', 'error');
        return;
    }
    
    try {
        showMessage('Reenviando código...', 'info');
        
        const response = await fetch(`${USER_SERVICE_URL}/api/auth/resend-code`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username })
        });
        
        if (!response.ok) {
            const data = await response.json();
            showMessage(data.error || 'Error al reenviar el código', 'error');
            return;
        }
        
        showMessage('Código reenviado. Revisa tu email.', 'success');
        
    } catch (error) {
        console.error('Error:', error);
        showMessage('Error de conexión con el servidor', 'error');
    }
});

function showMessage(text, type) {
    let messageDiv = document.querySelector('.message');
    if (!messageDiv) {
        messageDiv = document.createElement('div');
        messageDiv.className = 'message';
        const form = document.getElementById('verifyForm');
        form.insertBefore(messageDiv, form.firstChild);
    }
    
    messageDiv.textContent = text;
    messageDiv.className = 'message ' + type + ' show';

    if (type === 'error') {
        setTimeout(() => {
            messageDiv.classList.remove('show');
        }, 5000);
    }
}

// Limpiar mensajes cuando el usuario escribe
document.getElementById('username').addEventListener('input', clearMessage);
document.getElementById('code').addEventListener('input', clearMessage);

function clearMessage() {
    const messageDiv = document.querySelector('.message');
    if (messageDiv) {
        messageDiv.classList.remove('show');
    }
}

// Solo permitir números en el código
document.getElementById('code').addEventListener('input', function(e) {
    this.value = this.value.replace(/[^0-9]/g, '');
});
