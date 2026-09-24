#!/bin/bash
BASE=http://localhost:8080

echo "🔑 Fazendo login..."
TOKEN=$(curl -s -X POST $BASE/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"operador@aguiaradar.com","senha":"123456"}' \
  | grep -oP '"token":"\K[^"]+')

if [ -z "$TOKEN" ]; then
  echo "❌ Falha no login"
  exit 1
fi

echo "✅ Token obtido: ${TOKEN:0:40}..."
echo

for endpoint in \
  "dashboard/resumo-geral" \
  "dashboard/por-estrategia" \
  "ideias" \
  "projetos" \
  "orientacoes-estrategicas"
do
  echo "📡 GET /api/v1/$endpoint"
  curl -i -s "$BASE/api/v1/$endpoint" \
    -H "Authorization: Bearer $TOKEN" | head -c 500
  echo; echo "---"
done
