[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet("setup", "demo", "exercise", "validate", "solution", "validate-all", "help")]
    [string]$Action = "help",

    [Parameter(Position = 1)]
    [ValidatePattern("^(0[1-9]|1[0-2])$")]
    [string]$Module,

    [switch]$Headed,

    [ValidateSet("chromium", "firefox", "webkit")]
    [string]$Browser = "chromium",

    [switch]$SkipBrowserInstall
)

$courseRoot = $PSScriptRoot
$maven = Join-Path $courseRoot "mvn-local.ps1"
$env:PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD = "1"
Write-Host "Download automático de navegadores: desabilitado"

function Invoke-CourseMaven {
    param(
        [string[]]$Arguments,
        [string]$Project = $courseRoot
    )

    Push-Location $Project

    try {
        & $maven @Arguments

        if ($LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }
    }
    finally {
        Pop-Location
    }
}

function Require-Module {
    if (-not $Module) {
        throw "Informe a aula com dois dígitos. Exemplo: .\course.ps1 demo 04"
    }
}

function Demo-Class([string]$Number) {
    return @{
        "01" = "PrimeiroContatoPlaywrightTest"
        "02" = "LocatorsEAutoWaitDemonstracaoTest"
        "03" = "LifecycleJUnitDemonstracaoTest"
        "04" = "CatalogoEParametrizacaoDemonstracao"
        "05" = "CheckoutEDinheiroDemonstracao"
        "06" = "InteracoesAvancadasDemonstracao"
        "07" = "ArquiteturaDemonstracao"
        "08" = "ApiEStorageStateDemonstracao"
        "09" = "RedeEMockingDemonstracao"
        "10" = "PortabilidadeEParalelismoDemonstracao"
        "11" = "EvidenciasDemonstracao"
        "12" = "PipelineEProjetoFinalDemonstracao"
    }[$Number]
}

function Exercise-Class([string]$Number) {
    return @{
        "01" = "LoginSauceDemoExercicioTest"
        "02" = "LoginNegativoExercicioTest"
        "03" = "LifecycleEConfiguracaoExercicioTest"
        "04" = "CatalogoEOrdenacaoExercicioTest"
        "05" = "CheckoutExercicioTest"
        "06" = "InteracoesAvancadasExercicioTest"
        "07" = "ArquiteturaSuiteExercicioTest"
        "08" = "ApiEStorageStateExercicioTest"
        "09" = "RedeEMockingExercicioTest"
        "10" = "PortabilidadeEParalelismoExercicioTest"
        "11" = "EvidenciasExercicioTest"
        "12" = "ProjetoFinalExercicioTest"
    }[$Number]
}

$headless = "false"

switch ($Action) {

    "setup" {

        java -version

        if ($LASTEXITCODE -ne 0) {
            throw "Java 17 não foi encontrado."
        }

        Invoke-CourseMaven @("-version")

        Write-Host ""
        Write-Host "Download dos navegadores Playwright desabilitado."
        Write-Host "Os testes utilizarão o navegador instalado na máquina."
        Write-Host ""
    }

    "demo" {
        Require-Module

        Invoke-CourseMaven @(
            "test",
            "-Dtest=$(Demo-Class $Module)",
            "-Dbrowser=$Browser",
            "-Dheadless=$headless"
        )
    }

    "exercise" {
        Require-Module

        Invoke-CourseMaven @(
            "test",
            "-Dtest=$(Exercise-Class $Module)",
            "-Dbrowser=$Browser",
            "-Dheadless=$headless"
        )
    }

    "validate" {
        Require-Module

        Invoke-CourseMaven @(
            "test",
            "-Dtest=$(Exercise-Class $Module)",
            "-Dbrowser=$Browser",
            "-Dheadless=$headless"
        )

        Invoke-CourseMaven @(
            "test",
            "-Dtest=CourseSourceValidator",
            "-Dcourse.module=$Module"
        )

        if ([int]$Module -ge 7) {
            Invoke-CourseMaven @(
                "test",
                "-Dtest=ArchitectureValidator"
            )
        }
    }

    "solution" {
        Require-Module

        Invoke-CourseMaven @(
            "test",
            "-Dgroups=solution-$Module",
            "-Dbrowser=$Browser",
            "-Dheadless=$headless"
        ) (Join-Path $courseRoot "reference-solutions")
    }

    "validate-all" {

        foreach ($number in 1..12) {

            $selected = $number.ToString("00")

            Invoke-CourseMaven @(
                "test",
                "-Dtest=$(Exercise-Class $selected)",
                "-Dbrowser=$Browser",
                "-Dheadless=$headless"
            )

            Invoke-CourseMaven @(
                "test",
                "-Dtest=CourseSourceValidator",
                "-Dcourse.module=$selected"
            )
        }

        Invoke-CourseMaven @(
            "test",
            "-Dtest=ArchitectureValidator"
        )
    }

    default {
        Write-Host "Curso Playwright Java"
        Write-Host ""
        Write-Host ".\course.ps1 setup"
        Write-Host ".\course.ps1 setup -SkipBrowserInstall"
        Write-Host ".\course.ps1 demo 04 [-Headed] [-Browser firefox]"
        Write-Host ".\course.ps1 exercise 04"
        Write-Host ".\course.ps1 validate 04"
        Write-Host ".\course.ps1 solution 04"
        Write-Host ".\course.ps1 validate-all"
        Write-Host ""
    }
}