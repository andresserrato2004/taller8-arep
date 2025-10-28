#!/bin/bash

# Script para probar el User Service

BASE_URL="http://localhost:8081"

echo "🧪 Testing User Service API"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Test 1: Health Check
echo "1️⃣ Testing Health Check..."
curl -s "$BASE_URL/health" | jq .
echo ""

# Test 2: Register User
echo "2️⃣ Registering new user..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "email": "andresserratocamero@gmail.com",
    "password": "TestPass123!"
  }')

echo $REGISTER_RESPONSE | jq .
echo ""

# Solicitar código de confirmación
read -p "Ingresa el código de confirmación enviado al email: " CONFIRMATION_CODE

# Test 3: Verify User
echo "3️⃣ Verifying user..."
VERIFY_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"testuser1\",
    \"confirmationCode\": \"$CONFIRMATION_CODE\"
  }")

echo $VERIFY_RESPONSE | jq .
echo ""

# Test 4: Login - Paso 1 (verificar usuario)
echo "4️⃣ (Paso 1) Checking if user exists..."
CHECK_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login/check" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser1"
  }')

echo $CHECK_RESPONSE | jq .
echo ""

# Test 5: Login - Paso 2 (autenticar con contraseña)
# Test 4: Login - Paso 1 (verificar usuario)
echo "4️⃣ (Paso 1) Checking if user exists..."
CHECK_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login/check" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser1"
  }')

echo $CHECK_RESPONSE | jq .
echo ""

# Test 5: Login - Paso 2 (autenticar con contraseña)
echo "5️⃣ (Paso 2) Logging in with password..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login/authenticate" \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "testuser1",
    "password": "TestPass123!"
  }')

echo $LOGIN_RESPONSE | jq .

# Extraer token
ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
echo ""
echo "Access Token: ${ACCESS_TOKEN:0:50}..."
echo ""

# Test 6: Get Current User
echo "6️⃣ Getting current user info..."
curl -s -X GET "$BASE_URL/api/users/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
echo ""

# Test 7: Get All Users
echo "7️⃣ Getting all users..."
curl -s -X GET "$BASE_URL/api/users" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
echo ""

# Test 8: Update Profile
echo "8️⃣ Updating profile..."
curl -s -X PUT "$BASE_URL/api/users/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bio": "Software Developer and Coffee Lover ☕",
    "profilePicture": "https://avatars.githubusercontent.com/u/1234567"
  }' | jq .
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Tests completados!"
