# Plano completo do curso

Carga estimada: 45–60 horas. Todo o conteúdo está disponível; a recomendação é
avançar em ordem porque os conceitos e validadores são cumulativos.

## Progressão de dificuldade

- **Fundamentos — Aulas 1 a 3:** aprender os objetos do Playwright, localizar,
  esperar e isolar testes. Nenhum conhecimento prévio da ferramenta é assumido.
- **Aplicação — Aulas 4 a 6:** transformar regras de negócio em oráculos,
  trabalhar com coleções, dinheiro, jornadas e eventos do browser.
- **Engenharia da suíte — Aulas 7 a 9:** organizar responsabilidades, integrar
  API e controlar a rede sem transformar mocks em falsos testes de integração.
- **Nível profissional — Aulas 10 a 12:** portabilidade, paralelismo,
  diagnóstico, evidências, Docker, Jenkins e operação diária da regressão.

Cada faixa depende da anterior. Se um checkpoint não puder ser explicado com
suas próprias palavras, repita a demonstração antes de avançar.

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
| 10 | Portabilidade | Browsers, mobile e paralelismo | Disponível |
| 11 | Diagnóstico | Trace, vídeo, screenshot e Allure | Disponível |
| 12 | CI e projeto final | Docker, Jenkins, agendamento diário e suíte profissional | Disponível |

## Método

Cada aula possui objetivos, teoria, contraste com Selenium, demonstração,
exercício guiado, desafio, validador, rubrica, reflexão e leituras oficiais.
Uma aula é concluída quando o exercício passa isoladamente, o validador passa e
você consegue justificar as decisões da rubrica.

## Resultado final

A regressão cobre autenticação, catálogo, detalhes, ordenação, carrinho,
checkout, logout e reset. A smoke roda nos três motores; a regressão completa
roda em Chromium. Contextos são isolados, o paralelismo é seguro e falhas geram
evidências úteis no Jenkins. A pipeline fica versionada com a suíte e pode ser
executada manualmente, por mudança no repositório ou diariamente de madrugada.
