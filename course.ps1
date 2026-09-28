[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet("setup", "demo", "exercise", "validate", "solution", "validate-all", "trace", "help")]
    [string]$Action = "help",

    [Parameter(Position = 1)]
    [ValidatePattern("^(0[1-9]|1[0-2])$")]
    [string]$Module,

    [switch]$Headless,

    [string]$Groups,

    [switch]$Parallel,

    [ValidateSet("off", "on-failure", "always")]
    [string]$Trace = "on-failure",

    [ValidateSet("off", "on-failure", "always")]
    [string]$Screenshot = "on-failure",

    [ValidateSet("off", "on-failure", "always")]
    [string]$Video = "off",

    [string]$TracePath
)

$courseRoot = $PSScriptRoot
$maven = Join-Path $courseRoot "mvn-local.ps1"
$env:PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD = "1"
Write-Host "Download automatico de navegadores: desabilitado"

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
        throw "Informe a aula com dois digitos. Exemplo: .\course.ps1 demo 04"
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

$headlessValue = if ($Headless.IsPresent) { "true" } else { "false" }
$parallelValue = if ($Parallel.IsPresent) { "true" } else { "false" }

function New-TestArguments {
    param(
        [string]$TestClass,
        [switch]$IncludeGroups
    )

    $arguments = @(
        "test",
        "-Dtest=$TestClass",
        "-Dheadless=$headlessValue",
        "-Djunit.jupiter.execution.parallel.enabled=$parallelValue",
        "-Dtrace=$Trace",
        "-Dscreenshot=$Screenshot",
        "-Dvideo=$Video"
    )

    if ($IncludeGroups.IsPresent -and $Groups) {
        $arguments += "-Dgroups=$Groups"
    }

    return $arguments
}

switch ($Action) {
    "setup" {
        java -version
        if ($LASTEXITCODE -ne 0) {
            throw "Java nao foi encontrado."
        }

        Invoke-CourseMaven @("-version")
        Write-Host ""
        Write-Host "Download dos navegadores Playwright desabilitado."
        Write-Host "Os testes utilizarao o Microsoft Edge instalado na maquina."
        Write-Host ""
    }

    "demo" {
        Require-Module
        $arguments = New-TestArguments -TestClass (Demo-Class $Module) -IncludeGroups
        Invoke-CourseMaven $arguments
    }

    "exercise" {
        Require-Module
        $arguments = New-TestArguments -TestClass (Exercise-Class $Module) -IncludeGroups
        Invoke-CourseMaven $arguments
    }

    "validate" {
        Require-Module
        $arguments = New-TestArguments -TestClass (Exercise-Class $Module) -IncludeGroups
        Invoke-CourseMaven $arguments

        Invoke-CourseMaven @(
            "test",
            "-Dtest=CourseSourceValidator",
            "-Dcourse.module=$Module"
        )

        if ([int]$Module -ge 7) {
            Invoke-CourseMaven @("test", "-Dtest=ArchitectureValidator")
        }
    }

    "solution" {
        Require-Module
        Invoke-CourseMaven @(
            "test",
            "-Dgroups=solution-$Module",
            "-Dheadless=$headlessValue",
            "-Djunit.jupiter.execution.parallel.enabled=$parallelValue",
            "-Dtrace=$Trace",
            "-Dscreenshot=$Screenshot",
            "-Dvideo=$Video"
        ) (Join-Path $courseRoot "reference-solutions")
    }

    "validate-all" {
        foreach ($number in 1..12) {
            $selected = $number.ToString("00")
            $arguments = New-TestArguments -TestClass (Exercise-Class $selected)
            Invoke-CourseMaven $arguments

            Invoke-CourseMaven @(
                "test",
                "-Dtest=CourseSourceValidator",
                "-Dcourse.module=$selected"
            )
        }

        Invoke-CourseMaven @("test", "-Dtest=ArchitectureValidator")
    }

    "trace" {
        if (-not $TracePath) {
            throw "Informe o arquivo com -TracePath. Exemplo: .\course.ps1 trace -TracePath artifacts\trace.zip"
        }

        $resolvedTrace = Resolve-Path $TracePath -ErrorAction Stop
        Invoke-CourseMaven @(
            "exec:java",
            "-Dexec.mainClass=com.microsoft.playwright.CLI",
            "-Dexec.args=show-trace $resolvedTrace"
        )
    }

    default {
        Write-Host "Curso Playwright Java"
        Write-Host ""
        Write-Host ".\course.ps1 setup"
        Write-Host ".\course.ps1 demo 04 [-Headless] [-Groups smoke] [-Parallel]"
        Write-Host ".\course.ps1 exercise 04 [-Headless] [-Groups smoke] [-Parallel]"
        Write-Host ".\course.ps1 validate 04 [-Headless] [-Groups smoke] [-Parallel]"
        Write-Host ".\course.ps1 solution 04 [-Headless] [-Parallel]"
        Write-Host ".\course.ps1 validate-all [-Headless] [-Parallel]"
        Write-Host ".\course.ps1 demo 11 [-Trace always] [-Screenshot always] [-Video always]"
        Write-Host ".\course.ps1 trace -TracePath artifacts\trace.zip"
        Write-Host ""
    }
}
