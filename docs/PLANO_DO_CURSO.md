# Plano completo do curso

Carga estimada: 45 a 60 horas. Todo o conteúdo está disponível. A recomendação é avançar em ordem, pois os conceitos e validadores são cumulativos.

## Progressão de dificuldade

- **Fundamentos, Aulas 1 a 3:** objetos do Playwright, locators, esperas e isolamento.
- **Aplicação, Aulas 4 a 6:** regras de negócio, coleções, dinheiro, jornadas e eventos do browser.
- **Engenharia da suíte, Aulas 7 a 9:** arquitetura, API, autenticação e controle de rede.
- **Nível profissional, Aulas 10 a 12:** mobile, acessibilidade, paralelismo, evidências, Docker, Jenkins e operação diária.

Cada faixa depende da anterior. Se um checkpoint não puder ser explicado com suas próprias palavras, repita a demonstração antes de avançar.

| Aula | Tema | Entrega | Estado inicial |
|---:|---|---|---|
| 1 | Fundamentos | Primeiro login | Disponível |
| 2 | Locators e esperas | Login negativo e locator composto | Disponível |
| 3 | JUnit e isolamento | Fixture manual confiável | Disponível |
| 4 | Estratégia e dados | Catálogo parametrizado e ordenação | Disponível |
| 5 | Jornada de negócio | Carrinho e checkout | Disponível |
| 6 | Interações avançadas | Upload, download, iframe e popup | Disponível |
| 7 | Arquitetura | Page Objects, componentes e extensão | Disponível |
| 8 | API e autenticação | APIRequestContext e storage state | Disponível |
| 9 | Rede e mocking | Observação, fulfill e abort | Disponível |
| 10 | Mobile e paralelismo | Emulação, acessibilidade e execução paralela no Edge | Disponível |
| 11 | Diagnóstico | Trace, vídeo, screenshot e Allure | Disponível |
| 12 | CI e projeto final | Docker, Jenkins, agenda diária e suíte profissional | Disponível |

## Método

Cada aula possui objetivos, teoria, contraste com Selenium, demonstração, exercício guiado, desafio, validador, rubrica, reflexão e leituras oficiais.

Uma aula é concluída quando:

1. o exercício passa isoladamente;
2. o validador passa;
3. as decisões da rubrica podem ser justificadas;
4. a evidência foi registrada em `docs/PROGRESSO.md`.

## Padrão de execução

- somente Microsoft Edge instalado;
- navegador visível por padrão;
- `-Headless` apenas quando necessário;
- comandos executados pelo `course.ps1`;
- `mvn-local.ps1` usado somente como implementação interna;
- download automático de navegadores Playwright desabilitado.

## Resultado final

A regressão cobre autenticação, catálogo, detalhes, ordenação, carrinho, checkout, logout e reset. Os contextos são isolados, o paralelismo é seguro e as falhas geram evidências úteis no Jenkins.

A smoke e a regressão executam somente no Microsoft Edge. A pipeline fica versionada com a suíte e pode ser executada manualmente ou diariamente pelo Jenkins.
