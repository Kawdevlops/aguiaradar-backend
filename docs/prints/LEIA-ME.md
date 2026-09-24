# Prints de evidência

Salve aqui as capturas com exatamente estes nomes (o README.md já aponta para eles):

| Arquivo | O que capturar |
|---|---|
| `01-pipeline-visao-geral.png` | Aba **Actions** → execução verde mostrando os 4 jobs encadeados |
| `02-build-testes.png` | Job "1. Build e testes" aberto + resumo "🧪 Testes automatizados" |
| `03-docker-smoke.png` | Job "2. Imagem Docker" no passo "Smoke test no container" |
| `04-deploy-staging.png` | Job "3. Deploy STAGING" com o smoke test aprovado |
| `05-aprovacao-producao.png` | Tela "Review deployments" pedindo aprovação para production |
| `06-deploy-producao.png` | Job "4. Deploy PRODUCAO" concluído |
| `07-staging-info.png` | Navegador em `https://<staging>/actuator/info` (mostra environment=staging e a versão) |
| `08-producao-info.png` | Navegador em `https://<producao>/actuator/info` (environment=production) |
| `09-login-producao.png` | Postman/Insomnia fazendo login na URL de produção e recebendo o token |
| `10-docker-compose-local.png` | Terminal com `docker compose ps` mostrando backend e mongodb **healthy** |
| `11-ghcr-imagem.png` | Página do pacote no GitHub (Packages) com as tags sha-xxxx, staging, production |
| `12-render-servicos.png` | Dashboard do Render com os serviços staging e produção "Live" |
