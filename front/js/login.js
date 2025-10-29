// Configuración de URLs de los servicios
//const USER_SERVICE_URL = 'http://localhost:8081';
const USER_SERVICE_URL = 'https://9aeuu5z7r0.execute-api.us-east-1.amazonaws.com/v1';

document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const identifier = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    
    if (identifier === '' || password === '') {
        showMessage('Por favor, completa todos los campos', 'error');
        return;
    }
    
    try {
        showMessage('Verificando usuario...', 'info');
        
        // Paso 1: Verificar si el usuario existe
        const checkResponse = await fetch(`${USER_SERVICE_URL}/api/auth/login/check`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ identifier })
        });
        
        const checkData = await checkResponse.json();
        
        if (!checkData.exists) {
            showMessage('Usuario no encontrado', 'error');
            return;
        }
        
        showMessage('Autenticando...', 'info');
        
        // Paso 2: Autenticar con contraseña
        const loginResponse = await fetch(`${USER_SERVICE_URL}/api/auth/login/authenticate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ identifier, password })
        });
        
        if (!loginResponse.ok) {
            const errorData = await loginResponse.json();
            showMessage(errorData.message || 'Error al iniciar sesión', 'error');
            return;
        }
        
        const loginData = await loginResponse.json();
        
        // Guardar tokens y datos del usuario
        localStorage.setItem('accessToken', loginData.accessToken);
        localStorage.setItem('idToken', loginData.idToken);
        localStorage.setItem('refreshToken', loginData.refreshToken);
        
        const userData = {
            username: loginData.username,
            userId: parseJwt(loginData.accessToken).sub
        };
        localStorage.setItem('currentUser', JSON.stringify(userData));
        
        showMessage('¡Inicio de sesión exitoso!', 'success');
        
        setTimeout(() => {
            window.location.href = 'home.html';
        }, 1000);
        
    } catch (error) {
        console.error('Error:', error);
        showMessage('Error de conexión con el servidor. Asegúrate de que el User Service esté corriendo en el puerto 8081', 'error');
    }
});

// Función para decodificar JWT
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        return {};
    }
}


function showMessage(text, type) {
    let messageDiv = document.querySelector('.message');
    if (!messageDiv) {
        messageDiv = document.createElement('div');
        messageDiv.className = 'message';
        const form = document.getElementById('loginForm');
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


document.getElementById('username').addEventListener('input', clearMessage);
document.getElementById('password').addEventListener('input', clearMessage);

function clearMessage() {
    const messageDiv = document.querySelector('.message');
    if (messageDiv) {
        messageDiv.classList.remove('show');
    }
}
