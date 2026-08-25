# Curso completo de Playwright com Java

Formação prática de Playwright Java construída sobre o
[SauceDemo](https://www.saucedemo.com/) e um laboratório HTTP local. O curso
possui 12 aulas, teoria, demonstrações, exercícios, validação automática,
rubricas, soluções de referência e uma pipeline Jenkins com execução diária.

## Comece aqui

Pré-requisitos: Java 17, Maven 3.9+ e PowerShell.

Depois de clonar o repositório, entre na pasta do projeto e execute:

```powershell
.\course.ps1 setup
.\course.ps1 demo 01
```

O `setup` verifica Java e Maven, baixa as dependências e instala Chromium,
Firefox e WebKit. Ele deve ser executado uma vez em cada máquina nova.

Em seguida:

1. leia [Aula 01 — Fundamentos e primeiro teste](docs/aula-01/README.md);
2. execute a demonstração;
3. implemente o arquivo de exercício sem consultar o gabarito;
4. rode `exercise` e depois `validate`;
5. registre as decisões em [docs/PROGRESSO.md](docs/PROGRESSO.md);
6. consulte `solution` somente depois da sua tentativa.

```powershell
.\course.ps1 exercise 01
.\course.ps1 validate 01
.\course.ps1 solution 01
```

Para visualizar o browser:

```powershell
.\course.ps1 demo 01 -Headed
```

Programa completo: [docs/PLANO_DO_CURSO.md](docs/PLANO_DO_CURSO.md). Como
funciona a correção: [docs/COMO_VALIDAR.md](docs/COMO_VALIDAR.md). Leia também
o [contrato didático](docs/CONTRATO_DIDATICO.md), que define o que cada aula
deve ensinar antes de cobrar na prática.

## Interface do curso

```powershell
.\course.ps1 setup
.\course.ps1 demo 04
.\course.ps1 exercise 04
.\course.ps1 validate 04
.\course.ps1 solution 04
.\course.ps1 validate-all
```

Todas as aulas estão disponíveis, mas os exercícios começam desabilitados para
que o build inicial seja estável. Avance em ordem e remova `@Disabled` somente
do exercício em que estiver trabalhando. O `mvn-local.ps1` permanece disponível
para comandos Maven livres.

## Configuração

Propriedades `-D` têm precedência sobre variáveis de ambiente, que têm
precedência sobre os padrões:

| Propriedade | Variável | Padrão |
|---|---|---|
| `baseUrl` | `COURSE_BASE_URL` | `https://www.saucedemo.com/` |
| `apiBaseUrl` | `COURSE_API_BASE_URL` | URL do laboratório |
| `browser` | `COURSE_BROWSER` | `chromium` |
| `headless` | `COURSE_HEADLESS` | `true` |
| `timeout` | `COURSE_TIMEOUT` | `10000` |
| `artifactsDir` | `COURSE_ARTIFACTS_DIR` | `artifacts` |
| `trace` | `COURSE_TRACE` | `on-failure` |
| `video` | `COURSE_VIDEO` | `off` |
| `screenshot` | `COURSE_SCREENSHOT` | `on-failure` |
| `tags` | `COURSE_TAGS` | vazio |

## Resultado esperado

Ao concluir o projeto final, a pipeline executará smoke em Chromium, Firefox e
WebKit, regressão completa em Chromium e publicará JUnit, Allure, screenshots,
vídeos e traces. O `Jenkinsfile` agenda essa execução uma vez por dia durante a
madrugada; os detalhes operacionais são ensinados na Aula 12.

## Regra de estudo

Leia a teoria, execute a demonstração, implemente sem consultar o gabarito,
rode `validate` e faça a autoavaliação pela rubrica. O validador encontra
problemas objetivos; ele não substitui revisão crítica. Consulte a solução
somente depois de registrar sua tentativa e suas decisões.
