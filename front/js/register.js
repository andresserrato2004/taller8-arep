// Configuración de URLs de los servicios
//const USER_SERVICE_URL = 'http://localhost:8081';
const USER_SERVICE_URL = 'https://9aeuu5z7r0.execute-api.us-east-1.amazonaws.com/v1';

document.getElementById('registerForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    
    // Validaciones
    if (username === '' || email === '' || password === '') {
        showMessage('Por favor, completa todos los campos', 'error');
        return;
    }
    
    if (password.length < 8) {
        showMessage('La contraseña debe tener al menos 8 caracteres', 'error');
        return;
    }
    
    // Validar que la contraseña tenga mayúsculas, números y símbolos
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRegex.test(password)) {
        showMessage('La contraseña debe contener al menos una mayúscula, un número y un símbolo especial', 'error');
        return;
    }
    
    if (password !== confirmPassword) {
        showMessage('Las contraseñas no coinciden', 'error');
        return;
    }
    
    try {
        showMessage('Creando cuenta...', 'info');
        
        const response = await fetch(`${USER_SERVICE_URL}/api/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, email, password })
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            showMessage(data.error || 'Error al crear la cuenta', 'error');
            return;
        }
        
        showMessage('¡Cuenta creada exitosamente! Revisa tu email para obtener el código de verificación.', 'success');
        
        // Redirigir a la página de verificación después de 2 segundos
        setTimeout(() => {
            window.location.href = `verify.html?username=${encodeURIComponent(username)}`;
        }, 2000);
        
    } catch (error) {
        console.error('Error:', error);
        showMessage('Error de conexión con el servidor. Asegúrate de que el User Service esté corriendo en el puerto 8081', 'error');
    }
});

function showMessage(text, type) {
    let messageDiv = document.querySelector('.message');
    if (!messageDiv) {
        messageDiv = document.createElement('div');
        messageDiv.className = 'message';
        const form = document.getElementById('registerForm');
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
document.getElementById('email').addEventListener('input', clearMessage);
document.getElementById('password').addEventListener('input', clearMessage);
document.getElementById('confirmPassword').addEventListener('input', clearMessage);

function clearMessage() {
    const messageDiv = document.querySelector('.message');
    if (messageDiv) {
        messageDiv.classList.remove('show');
    }
}

// Validación en tiempo real de las contraseñas
document.getElementById('confirmPassword').addEventListener('input', function() {
    const password = document.getElementById('password').value;
    const confirmPassword = this.value;
    
    if (confirmPassword && password !== confirmPassword) {
        this.setCustomValidity('Las contraseñas no coinciden');
    } else {
        this.setCustomValidity('');
    }
});
