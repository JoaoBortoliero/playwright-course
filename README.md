# Curso completo de Playwright com Java

Formação prática de Playwright com Java construída sobre o [SauceDemo](https://www.saucedemo.com/) e um laboratório HTTP local.

O curso possui 12 aulas com teoria, demonstrações, exercícios, validação automática, rubricas, soluções de referência e uma pipeline Jenkins com execução diária.

## Pré-requisitos

- Java 17
- Maven 3.9 ou superior
- PowerShell
- Microsoft Edge instalado

> O Microsoft Edge é o navegador padrão do curso. O projeto utiliza o navegador instalado na máquina para evitar downloads automáticos dos navegadores gerenciados pelo Playwright.

## Comece aqui

Depois de clonar o repositório, abra o PowerShell na pasta raiz do projeto e execute:

```powershell
.\course.ps1 setup
.\course.ps1 demo 01
```

Em seguida:

1. Leia [Aula 01 - Fundamentos e primeiro teste](docs/aula-01/README.md).
2. Execute a demonstração.
3. Implemente o exercício sem consultar o gabarito.
4. Execute o exercício e depois a validação.
5. Registre as decisões em [docs/PROGRESSO.md](docs/PROGRESSO.md).
6. Consulte a solução somente depois da sua tentativa.

```powershell
.\course.ps1 exercise 01
.\course.ps1 validate 01
.\course.ps1 solution 01
```

Consulte também:

- [Programa completo](docs/PLANO_DO_CURSO.md)
- [Como funciona a correção](docs/COMO_VALIDAR.md)
- [Contrato didático](docs/CONTRATO_DIDATICO.md)

## Interface do curso

```powershell
.\course.ps1 setup
.\course.ps1 demo 04
.\course.ps1 exercise 04
.\course.ps1 validate 04
.\course.ps1 solution 04
.\course.ps1 validate-all
```

Todas as aulas estão disponíveis, mas os exercícios começam desabilitados para manter o build inicial estável. Avance em ordem e remova `@Disabled` somente do exercício em que estiver trabalhando.

## Execução visível e não visível

Por padrão, os testes são executados com o Microsoft Edge visível:

```powershell
.\course.ps1 demo 03
```

Para executar sem exibir o navegador, utilize o parâmetro `-Headless`:

```powershell
.\course.ps1 demo 03 -Headless
```

O parâmetro `-Headless` também pode ser utilizado com `exercise`, `validate`, `solution` e `validate-all`.

## Configuração

Propriedades `-D` têm precedência sobre variáveis de ambiente, que têm precedência sobre os valores padrão.

| Propriedade | Variável | Padrão |
|---|---|---|
| `baseUrl` | `COURSE_BASE_URL` | `https://www.saucedemo.com/` |
| `apiBaseUrl` | `COURSE_API_BASE_URL` | URL do laboratório |
| `browser` | `COURSE_BROWSER` | `edge` |
| `headless` | `COURSE_HEADLESS` | `false` |
| `timeout` | `COURSE_TIMEOUT` | `10000` |
| `artifactsDir` | `COURSE_ARTIFACTS_DIR` | `artifacts` |
| `trace` | `COURSE_TRACE` | `on-failure` |
| `video` | `COURSE_VIDEO` | `off` |
| `screenshot` | `COURSE_SCREENSHOT` | `on-failure` |
| `tags` | `COURSE_TAGS` | vazio |

## Resultado esperado

Ao concluir o projeto final, a pipeline executará smoke em Chromium, Firefox e WebKit, regressão completa em Chromium e publicará JUnit, Allure, screenshots, vídeos e traces. Os detalhes operacionais são ensinados na Aula 12.

## Regra de estudo

Leia a teoria, execute a demonstração, implemente sem consultar o gabarito, rode `validate` e faça a autoavaliação pela rubrica. O validador encontra problemas objetivos, mas não substitui a revisão crítica. Consulte a solução somente depois de registrar sua tentativa e suas decisões.
