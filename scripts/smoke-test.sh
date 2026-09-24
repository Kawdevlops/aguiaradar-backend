#!/usr/bin/env bash
# =====================================================================
#  Smoke test do AguiaRadar - usado no pipeline apos cada deploy
#  Uso: ./scripts/smoke-test.sh <BASE_URL> [VERSAO_ESPERADA]
#  Ex.: ./scripts/smoke-test.sh http://localhost:8080
#       ./scripts/smoke-test.sh https://aguiaradar-staging.onrender.com 3f2a1bc
# =====================================================================
set -euo pipefail

BASE_URL="${1:?informe a URL base, ex: http://localhost:8080}"
EXPECTED_VERSION="${2:-}"
TIMEOUT_SECONDS="${SMOKE_TIMEOUT:-600}"   # 10 min (servicos free "acordam" devagar)
EMAIL="${SMOKE_EMAIL:-operador@aguiaradar.com}"
SENHA="${SMOKE_SENHA:-123456}"

echo "==> Aguardando ${BASE_URL} ficar saudavel (timeout ${TIMEOUT_SECONDS}s)..."
inicio=$(date +%s)
while true; do
  info=$(curl -fsS --max-time 10 "${BASE_URL}/actuator/info" 2>/dev/null || true)
  health=$(curl -fsS --max-time 10 "${BASE_URL}/actuator/health" 2>/dev/null || true)
  if echo "$health" | grep -q '"UP"'; then
    if [ -z "$EXPECTED_VERSION" ] || echo "$info" | grep -q "\"version\":\"${EXPECTED_VERSION}\""; then
      break
    fi
    echo "    API no ar, mas ainda na versao antiga. Aguardando nova versao ${EXPECTED_VERSION}..."
  fi
  if [ $(( $(date +%s) - inicio )) -ge "$TIMEOUT_SECONDS" ]; then
    echo "XX Timeout: a API nao ficou pronta. Ultima resposta:"
    echo "   health: ${health:-<vazio>}"
    echo "   info:   ${info:-<vazio>}"
    exit 1
  fi
  sleep 10
done

echo "OK /actuator/health -> $health"
echo "OK /actuator/info   -> $info"

echo "==> Testando login (${EMAIL})..."
resp=$(curl -fsS -X POST "${BASE_URL}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${EMAIL}\",\"senha\":\"${SENHA}\"}")
TOKEN=$(echo "$resp" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
[ -n "$TOKEN" ] || { echo "XX Login falhou: $resp"; exit 1; }
echo "OK token JWT recebido (${TOKEN:0:25}...)"

echo "==> Testando rota protegida /api/v1/orientacoes-estrategicas..."
code=$(curl -s -o /dev/null -w '%{http_code}' "${BASE_URL}/api/v1/orientacoes-estrategicas" \
  -H "Authorization: Bearer ${TOKEN}")
[ "$code" = "200" ] || { echo "XX Esperado 200, recebido $code"; exit 1; }
echo "OK rota protegida respondeu 200"

echo "==> Testando que rota protegida SEM token e bloqueada..."
code=$(curl -s -o /dev/null -w '%{http_code}' "${BASE_URL}/api/v1/orientacoes-estrategicas")
if [ "$code" = "401" ] || [ "$code" = "403" ]; then
  echo "OK sem token -> $code"
else
  echo "XX Esperado 401/403, recebido $code"; exit 1
fi

echo "==> SMOKE TEST APROVADO em ${BASE_URL}"
