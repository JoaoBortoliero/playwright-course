$courseRoot = $PSScriptRoot
$localRepository = Join-Path $courseRoot ".m2\repository"
$browserDirectory = Join-Path $courseRoot ".playwright-browsers"
$previousBrowserDirectory = $env:PLAYWRIGHT_BROWSERS_PATH
$mavenExitCode = 1

$mavenOnPath = Get-Command mvn -ErrorAction SilentlyContinue
$mavenCandidates = @(
    if ($null -ne $mavenOnPath) { $mavenOnPath.Source }
    if ($env:MAVEN_HOME) { Join-Path $env:MAVEN_HOME "bin\mvn.cmd" }
    if ($env:M2_HOME) { Join-Path $env:M2_HOME "bin\mvn.cmd" }
    "C:\maven\bin\mvn.cmd"
    "C:\apache-maven-3.9.10\bin\mvn.cmd"
)
$mavenExecutable = $mavenCandidates |
    Where-Object { $_ -and (Test-Path $_) } |
    Select-Object -First 1

if (-not $mavenExecutable) {
    throw "Maven nao encontrado. Instale o Maven ou configure MAVEN_HOME."
}

try {
    $env:PLAYWRIGHT_BROWSERS_PATH = $browserDirectory
    # Ensure arguments are strings (avoid System.Char elements)
    if ($args.Count -eq 0) {
        $mavenArguments = @("test")
    }
    else {
        $mavenArguments = @()
        foreach ($a in $args) { $mavenArguments += [string]$a }
    }

    & $mavenExecutable "-Dmaven.repo.local=$localRepository" @mavenArguments
    $mavenExitCode = $LASTEXITCODE
}
finally {
    if ($null -eq $previousBrowserDirectory) {
        Remove-Item Env:PLAYWRIGHT_BROWSERS_PATH -ErrorAction SilentlyContinue
    }
    else {
        $env:PLAYWRIGHT_BROWSERS_PATH = $previousBrowserDirectory
    }
}

exit $mavenExitCode
