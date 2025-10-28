#!/bin/bash

# Script para probar el API del Post Service

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
USER_SERVICE_URL="${USER_SERVICE_URL:-http://localhost:8081}"
POST_SERVICE_URL="${POST_SERVICE_URL:-http://localhost:8083}"

echo "========================================="
echo "Post Service API Tests"
echo "========================================="
echo ""

# Función para imprimir sección
print_section() {
    echo ""
    echo -e "${BLUE}=========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}=========================================${NC}"
    echo ""
}

# Función para hacer peticiones
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local token=$4
    
    if [ -n "$token" ]; then
        if [ -n "$data" ]; then
            curl -s -X $method "$url" \
                -H "Authorization: Bearer $token" \
                -H "Content-Type: application/json" \
                -d "$data"
        else
            curl -s -X $method "$url" \
                -H "Authorization: Bearer $token"
        fi
    else
        if [ -n "$data" ]; then
            curl -s -X $method "$url" \
                -H "Content-Type: application/json" \
                -d "$data"
        else
            curl -s -X $method "$url"
        fi
    fi
}

# Test 1: Health Check
print_section "Test 1: Health Check"
echo "GET $POST_SERVICE_URL/health"
RESPONSE=$(make_request "GET" "$POST_SERVICE_URL/health")
echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"

# Test 2: Login (User Service) - Flujo de dos pasos
print_section "Test 2: Login para obtener token (Flujo de dos pasos)"
echo "Ingresa tus credenciales:"
read -p "Username o Email: " USER_IDENTIFIER
read -p "Password: " USER_PASSWORD
echo ""
echo ""

# Paso 1: Verificar si el usuario existe
echo "Paso 1: Verificando usuario..."
CHECK_DATA="{\"identifier\":\"$USER_IDENTIFIER\"}"
echo "POST $USER_SERVICE_URL/api/auth/login/check"
CHECK_RESPONSE=$(make_request "POST" "$USER_SERVICE_URL/api/auth/login/check" "$CHECK_DATA")
echo "$CHECK_RESPONSE" | python3 -m json.tool || echo "$CHECK_RESPONSE"

USER_EXISTS=$(echo "$CHECK_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('exists', False))" 2>/dev/null || echo "false")

if [ "$USER_EXISTS" != "True" ] && [ "$USER_EXISTS" != "true" ]; then
    echo -e "${RED}Error: Usuario no encontrado${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Usuario encontrado${NC}"
echo ""

# Paso 2: Autenticar con contraseña
echo "Paso 2: Autenticando..."
LOGIN_DATA="{\"identifier\":\"$USER_IDENTIFIER\",\"password\":\"$USER_PASSWORD\"}"
echo "POST $USER_SERVICE_URL/api/auth/login/authenticate"
LOGIN_RESPONSE=$(make_request "POST" "$USER_SERVICE_URL/api/auth/login/authenticate" "$LOGIN_DATA")
echo "$LOGIN_RESPONSE" | python3 -m json.tool || echo "$LOGIN_RESPONSE"

# Extraer token
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('accessToken', ''))" 2>/dev/null || echo "")

if [ -z "$ACCESS_TOKEN" ]; then
    echo -e "${RED}Error: No se pudo obtener el token de acceso${NC}"
    echo "Por favor verifica tus credenciales y que el user-service esté ejecutándose"
    exit 1
fi

echo -e "${GREEN}✓ Token obtenido exitosamente${NC}"

# Test 3: Crear Post
print_section "Test 3: Crear Post"
POST_DATA='{"content":"Este es mi primer post de prueba! 🚀"}'
echo "POST $POST_SERVICE_URL/api/posts"
echo "Data: $POST_DATA"
RESPONSE=$(make_request "POST" "$POST_SERVICE_URL/api/posts" "$POST_DATA" "$ACCESS_TOKEN")
echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"

POST_ID=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))" 2>/dev/null || echo "")

# Test 4: Obtener todos los posts (público)
print_section "Test 4: Obtener todos los posts (público)"
echo "GET $POST_SERVICE_URL/api/posts/public"
RESPONSE=$(make_request "GET" "$POST_SERVICE_URL/api/posts/public")
echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"

# Test 5: Obtener todos los posts (autenticado)
print_section "Test 5: Obtener todos los posts (autenticado)"
echo "GET $POST_SERVICE_URL/api/posts"
RESPONSE=$(make_request "GET" "$POST_SERVICE_URL/api/posts" "" "$ACCESS_TOKEN")
echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"

# Test 6: Obtener mis posts
print_section "Test 6: Obtener mis posts"
echo "GET $POST_SERVICE_URL/api/posts/my-posts"
RESPONSE=$(make_request "GET" "$POST_SERVICE_URL/api/posts/my-posts" "" "$ACCESS_TOKEN")
echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"

# Test 7: Crear múltiples posts
print_section "Test 7: Crear múltiples posts"
for i in {1..3}; do
    POST_DATA="{\"content\":\"Post de prueba número $i - $(date +%T)\"}"
    echo "POST $POST_SERVICE_URL/api/posts (Post $i)"
    RESPONSE=$(make_request "POST" "$POST_SERVICE_URL/api/posts" "$POST_DATA" "$ACCESS_TOKEN")
    echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"
    sleep 1
done

# Test 8: Dar like a un post
if [ -n "$POST_ID" ]; then
    print_section "Test 8: Dar like a un post"
    echo "POST $POST_SERVICE_URL/api/posts/$POST_ID/like"
    RESPONSE=$(make_request "POST" "$POST_SERVICE_URL/api/posts/$POST_ID/like")
    echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"
fi

# Test 9: Actualizar post
if [ -n "$POST_ID" ]; then
    print_section "Test 9: Actualizar post"
    UPDATE_DATA='{"content":"Post actualizado! ✨"}'
    echo "PUT $POST_SERVICE_URL/api/posts/$POST_ID"
    RESPONSE=$(make_request "PUT" "$POST_SERVICE_URL/api/posts/$POST_ID" "$UPDATE_DATA" "$ACCESS_TOKEN")
    echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"
fi

# Test 10: Obtener post por ID
if [ -n "$POST_ID" ]; then
    print_section "Test 10: Obtener post por ID"
    echo "GET $POST_SERVICE_URL/api/posts/$POST_ID"
    RESPONSE=$(make_request "GET" "$POST_SERVICE_URL/api/posts/$POST_ID" "" "$ACCESS_TOKEN")
    echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"
fi

# Test 11: Crear post con más de 140 caracteres (debe fallar)
print_section "Test 11: Validación de 140 caracteres"
LONG_POST='{"content":"Este es un post muy largo que debería fallar porque excede el límite de 140 caracteres. Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."}'
echo "POST $POST_SERVICE_URL/api/posts (debe fallar)"
RESPONSE=$(make_request "POST" "$POST_SERVICE_URL/api/posts" "$LONG_POST" "$ACCESS_TOKEN")
echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"

# Test 12: Contar posts del usuario
print_section "Test 12: Contar posts del usuario"
USER_ID=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('userId', ''))" 2>/dev/null || echo "")
if [ -n "$USER_ID" ]; then
    echo "GET $POST_SERVICE_URL/api/posts/count/user/$USER_ID"
    RESPONSE=$(make_request "GET" "$POST_SERVICE_URL/api/posts/count/user/$USER_ID")
    echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"
fi

# Test 13: Eliminar post
if [ -n "$POST_ID" ]; then
    print_section "Test 13: Eliminar post"
    read -p "¿Deseas eliminar el post $POST_ID? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "DELETE $POST_SERVICE_URL/api/posts/$POST_ID"
        RESPONSE=$(make_request "DELETE" "$POST_SERVICE_URL/api/posts/$POST_ID" "" "$ACCESS_TOKEN")
        echo "$RESPONSE" | python3 -m json.tool || echo "$RESPONSE"
    fi
fi

# Resumen
print_section "Resumen de Tests"
echo -e "${GREEN}✓ Tests completados${NC}"
echo ""
echo "Servicios probados:"
echo "  - User Service: $USER_SERVICE_URL"
echo "  - Post Service: $POST_SERVICE_URL"
echo ""
echo "Token guardado en: ACCESS_TOKEN"
echo "Para usar el token en otros tests:"
echo "  export ACCESS_TOKEN='$ACCESS_TOKEN'"
echo ""
