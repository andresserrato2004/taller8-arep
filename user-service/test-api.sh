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
    "username": "testuser",
    "email": "test@example.com",
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
    \"username\": \"testuser\",
    \"confirmationCode\": \"$CONFIRMATION_CODE\"
  }")

echo $VERIFY_RESPONSE | jq .
echo ""

# Test 4: Login
echo "4️⃣ Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPass123!"
  }')

echo $LOGIN_RESPONSE | jq .

# Extraer token
ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
echo ""
echo "Access Token: ${ACCESS_TOKEN:0:50}..."
echo ""

# Test 5: Get Current User
echo "5️⃣ Getting current user info..."
curl -s -X GET "$BASE_URL/api/users/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
echo ""

# Test 6: Get All Users
echo "6️⃣ Getting all users..."
curl -s -X GET "$BASE_URL/api/users" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
echo ""

# Test 7: Update Profile
echo "7️⃣ Updating profile..."
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
