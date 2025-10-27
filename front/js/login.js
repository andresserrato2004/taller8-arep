document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    

    if (username === '' || password === '') {
        showMessage('Por favor, completa todos los campos', 'error');
        return;
    }
    

    console.log('Intentando iniciar sesión con:', { username, password });

    if (validateLogin(username, password)) {
        showMessage('¡Inicio de sesión exitoso!', 'success');
        
        // Guardar usuario en localStorage
        const userData = {
            username: username,
            email: username.includes('@') ? username : username + '@eci.edu.co',
            displayName: username.split('@')[0]
        };
        localStorage.setItem('currentUser', JSON.stringify(userData));

        setTimeout(() => {
            window.location.href = 'home.html';
        }, 1500);
    } else {
        showMessage('Usuario o contraseña incorrectos', 'error');
    }
});


function validateLogin(username, password) {

    const validUsers = {
        'usuario@eci.edu.co': '123456',
        'admin': 'admin123',
        'demo': 'demo'
    };
    
    return validUsers[username] === password;
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
