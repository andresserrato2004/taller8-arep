// ==================== CONFIGURACIÓN API ====================
const USER_SERVICE_URL = 'http://localhost:8081';
//const POST_SERVICE_URL = 'http://localhost:8083';
const POST_SERVICE_URL = 'https://8amb0dnji4.execute-api.us-east-1.amazonaws.com/dev2';
const STREAM_SERVICE_URL = 'http://localhost:8082';

// ==================== VARIABLES GLOBALES ====================
let currentUser = null;
let accessToken = null;

// ==================== UTILIDAD PARA OBTENER HEADERS CON TOKEN ====================
function getAuthHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`
    };
}

// ==================== LLAMADAS API ====================

async function fetchPosts() {
    try {
        const response = await fetch(`${POST_SERVICE_URL}/api/posts`, {
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('Error al obtener posts');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        showNotification('Error al cargar posts');
        return [];
    }
}

async function createPostAPI(content) {
    try {
        const response = await fetch(`${POST_SERVICE_URL}/api/posts`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ content })
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Error al crear post');
        }
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        showNotification('Error al publicar post: ' + error.message);
        return null;
    }
}

async function deletePostAPI(postId) {
    try {
        const response = await fetch(`${POST_SERVICE_URL}/api/posts/${postId}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        return response.ok;
    } catch (error) {
        console.error('Error:', error);
        showNotification('Error al eliminar post');
        return false;
    }
}

async function toggleLikeAPI(postId) {
    try {
        const response = await fetch(`${POST_SERVICE_URL}/api/posts/${postId}/like`, {
            method: 'POST',
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('Error al dar like');
        return await response.json();
    } catch (error) {
        console.error('Error:', error);
        return null;
    }
}

// ==================== INICIALIZACIÓN ====================

async function init() {
    const loggedUser = localStorage.getItem('currentUser');
    accessToken = localStorage.getItem('accessToken');
    
    if (!loggedUser || !accessToken) {
        window.location.href = 'login.html';
        return;
    }

    currentUser = JSON.parse(loggedUser);
    updateUserInfo();
    await loadData();
    setupEventListeners();
}

async function loadData() {
    await renderPosts();
}

// ==================== UTILIDADES ====================

function getFormattedTime(timestamp) {
    const now = new Date();
    const postTime = new Date(timestamp);
    const diff = now - postTime;
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (seconds < 60) return 'Ahora';
    if (minutes < 60) return `${minutes}m`;
    if (hours < 24) return `${hours}h`;
    if (days < 7) return `${days}d`;
    return postTime.toLocaleDateString();
}

function getInitials(name) {
    return name ? name.charAt(0).toUpperCase() : 'U';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}


// ==================== UI FUNCTIONS ====================

function updateUserInfo() {
    document.getElementById('currentUserName').textContent = '@' + currentUser.username;
    const avatar = document.getElementById('userAvatar');
    avatar.textContent = getInitials(currentUser.displayName || currentUser.username);
    avatar.style.backgroundColor = '#990000';
}

function setupEventListeners() {
    const postContent = document.getElementById('postContent');
    const btnPost = document.getElementById('btnPost');
    const charCount = document.getElementById('charCount');
    const btnRefresh = document.getElementById('btnRefresh');

    postContent.addEventListener('input', function() {
        const length = this.value.length;
        charCount.textContent = length;
        
        if (length > 120) {
            charCount.style.color = '#990000';
        } else if (length > 100) {
            charCount.style.color = '#ff9800';
        } else {
            charCount.style.color = '#666';
        }
        
        btnPost.disabled = length === 0 || length > 140;
    });

    btnPost.addEventListener('click', createPost);
    
    postContent.addEventListener('keydown', function(e) {
        if (e.ctrlKey && e.key === 'Enter' && !btnPost.disabled) {
            createPost();
        }
    });

    btnRefresh.addEventListener('click', async function() {
        this.classList.add('spinning');
        await loadData();
        setTimeout(() => this.classList.remove('spinning'), 500);
    });
}

async function createPost() {
    const content = document.getElementById('postContent').value.trim();
    
    if (content && content.length <= 140) {
        const result = await createPostAPI(content);
        
        if (result) {
            document.getElementById('postContent').value = '';
            document.getElementById('charCount').textContent = '0';
            document.getElementById('btnPost').disabled = true;
            
            await renderPosts();
            showNotification('Post publicado exitosamente!');
        }
    }
}

async function renderPosts() {
    const container = document.getElementById('postsContainer');
    const posts = await fetchPosts();
    
    if (posts.length === 0) {
        container.innerHTML = '<div class="no-posts"><p>No hay posts aún. ¡Sé el primero en publicar!</p></div>';
        return;
    }
    
    container.innerHTML = posts.map(post => createPostHTML(post)).join('');
    
    posts.forEach(post => {
        const likeBtn = document.getElementById(`like-${post.id}`);
        const deleteBtn = document.getElementById(`delete-${post.id}`);
        
        if (likeBtn) {
            likeBtn.addEventListener('click', () => toggleLike(post.id));
        }
        
        if (deleteBtn) {
            deleteBtn.addEventListener('click', () => deletePost(post.id));
        }
    });
}

function createPostHTML(post) {
    const isOwnPost = post.username === currentUser.username;
    
    return `
        <div class="post" data-post-id="${post.id}">
            <div class="post-avatar">
                <div class="avatar-circle" style="background-color: #990000">
                    ${getInitials(post.username)}
                </div>
            </div>
            <div class="post-content">
                <div class="post-header">
                    <span class="post-author">${post.username}</span>
                    <span class="post-username">@${post.username}</span>
                    <span class="post-time">· ${getFormattedTime(post.createdAt)}</span>
                </div>
                <div class="post-text">${escapeHtml(post.content)}</div>
                <div class="post-actions">
                    <button class="action-btn like-btn" id="like-${post.id}">
                        <svg viewBox="0 0 24 24" class="action-icon">
                            <g><path d="M16.697 5.5c-1.222-.06-2.679.51-3.89 2.16l-.805 1.09-.806-1.09C9.984 6.01 8.526 5.44 7.304 5.5c-1.243.07-2.349.78-2.91 1.91-.552 1.12-.633 2.78.479 4.82 1.074 1.97 3.257 4.27 7.129 6.61 3.87-2.34 6.052-4.64 7.126-6.61 1.111-2.04 1.03-3.7.477-4.82-.561-1.13-1.666-1.84-2.908-1.91zm4.187 7.69c-1.351 2.48-4.001 5.12-8.379 7.67l-.503.3-.504-.3c-4.379-2.55-7.029-5.19-8.382-7.67-1.36-2.5-1.41-4.86-.514-6.67.887-1.79 2.647-2.91 4.601-3.01 1.651-.09 3.368.56 4.798 2.01 1.429-1.45 3.146-2.1 4.796-2.01 1.954.1 3.714 1.22 4.601 3.01.896 1.81.846 4.17-.514 6.67z"></path></g>
                        </svg>
                        ${post.likeCount > 0 ? post.likeCount : ''}
                    </button>
                    ${isOwnPost ? `
                        <button class="action-btn delete-btn" id="delete-${post.id}">
                            <svg viewBox="0 0 24 24" class="action-icon">
                                <g><path d="M16 6V4.5C16 3.12 14.88 2 13.5 2h-3C9.11 2 8 3.12 8 4.5V6H3v2h1.06l.81 11.21C4.98 20.78 6.28 22 7.86 22h8.27c1.58 0 2.88-1.22 3.00-2.79L19.93 8H21V6h-5zm-6-1.5c0-.28.22-.5.5-.5h3c.27 0 .5.22.5.5V6h-4V4.5zm7.13 14.57c-.04.52-.47.93-1.00.93H7.86c-.53 0-.96-.41-1.00-.93L6.07 8h11.85l-.79 11.07zM9 17v-6h2v6H9zm4 0v-6h2v6h-2z"></path></g>
                            </svg>
                        </button>
                    ` : ''}
                </div>
            </div>
        </div>
    `;
}

async function toggleLike(postId) {
    const result = await toggleLikeAPI(postId);
    if (result) {
        await renderPosts();
    }
}

async function deletePost(postId) {
    if (confirm('¿Estás seguro de que quieres eliminar este post?')) {
        const success = await deletePostAPI(postId);
        if (success) {
            await renderPosts();
            showNotification('Post eliminado');
        }
    }
}

function showNotification(message) {
    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.textContent = message;
    document.body.appendChild(notification);
    
    setTimeout(() => notification.classList.add('show'), 10);
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

function logout() {
    if (confirm('¿Estás seguro de que quieres cerrar sesión?')) {
        localStorage.removeItem('currentUser');
        localStorage.removeItem('accessToken');
        localStorage.removeItem('idToken');
        localStorage.removeItem('refreshToken');
        window.location.href = 'login.html';
    }
}

// ==================== INICIO ====================
document.addEventListener('DOMContentLoaded', init);

// Actualizar posts cada 30 segundos
setInterval(() => {
    if (currentUser) loadData();
}, 30000);

