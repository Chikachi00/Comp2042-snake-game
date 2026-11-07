$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$pom = [xml](Get-Content -LiteralPath (Join-Path $projectRoot 'pom.xml') -Raw)
$javafxVersion = $pom.project.properties.'javafx.version'
$javafxCache = Join-Path $env:USERPROFILE '.m2\repository\org\openjfx'
if (-not (Test-Path -LiteralPath $javafxCache)) {
    throw 'JavaFX dependencies are missing. Run mvnw.cmd compile from the project folder first.'
}
$jars = @(Get-ChildItem -LiteralPath $javafxCache -Recurse -Filter "*-$javafxVersion*.jar" | ForEach-Object FullName)
if ($jars.Count -eq 0) {
    throw "JavaFX $javafxVersion dependencies are missing. Run mvnw.cmd compile first."
}
$javadoc = (Get-Command javadoc -ErrorAction Stop).Source
$arguments = @(
    '-locale', 'en',
    '-J-Duser.language=en', '-J-Duser.country=GB',
    '-quiet', '-notimestamp', '-private', '-Xdoclint:all,-missing', '-Werror',
    '-encoding', 'UTF-8', '-docencoding', 'UTF-8', '-charset', 'UTF-8',
    '-doctitle', 'COMP2042 Snake Game', '-windowtitle', 'COMP2042 Snake Game',
    '-d', (Join-Path $projectRoot 'Javadoc'),
    '-classpath', ($jars -join [System.IO.Path]::PathSeparator),
    '-sourcepath', (Join-Path $projectRoot 'src\main\java'),
    '-subpackages', 'com.comp2042'
)
# The two compiler-generated default constructors have no source comment.
# Explicitly declared methods/constructors are documented in the source.
& $javadoc @arguments
if ($LASTEXITCODE -ne 0) { throw "Javadoc generation failed with exit code $LASTEXITCODE." }
Write-Output 'Generated Javadoc/index.html successfully.'
