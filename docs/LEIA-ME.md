# Prints de evidência

Execução #2 do pipeline (commit `e73e8ab`).

| Arquivo | O que mostra |
|---|---|
| `01-pipeline-visao-geral.png` | Os 4 jobs do pipeline concluídos com sucesso |
| `02-build-testes.png` | Job 1: build Maven e testes automatizados |
| `03-docker-smoke.png` | Job 2: imagem Docker, compose, smoke test e push no GHCR |
| `04-deploy-staging.png` | Job 3: deploy em staging com smoke test |
| `05-aprovacao-producao.png` | Aprovação manual do environment `production` e resumo dos testes |
| `06-deploy-producao.png` | Job 4: deploy em produção com smoke test |
| `07-staging-info.png` | `/actuator/info` de staging (`environment: staging`, versão `e73e8ab`) |
| `08-producao-info.png` | `/actuator/info` de produção (`environment: production`, versão `e73e8ab`) |
| `09-resumo-deploys.png` | Resumo dos deploys e registro da aprovação |
| `10-render-staging.png` | Serviço de staging Live no Render |
| `11-ghcr-imagem.png` | Tags da imagem no GitHub Container Registry |
| `12-render-producao.png` | Serviço de produção Live no Render |
