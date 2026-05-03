param(
    [string]$Target = "all"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$SafeTarget = ($Target -replace '[^a-zA-Z0-9#_-]', '_') -replace '#', '_'
if ([string]::IsNullOrWhiteSpace($SafeTarget)) {
    $SafeTarget = "all"
}
$OutDir = Join-Path $ProjectRoot ("out-test-" + $SafeTarget + "-" + $PID)

New-Item -ItemType Directory -Path $OutDir | Out-Null

$SourceFiles = Get-ChildItem -Recurse -Filter *.java (Join-Path $ProjectRoot "src") | ForEach-Object { $_.FullName }
$TestFiles = Get-ChildItem -Recurse -Filter *.java (Join-Path $ProjectRoot "test") | ForEach-Object { $_.FullName }
$AllFiles = @($SourceFiles + $TestFiles)

javac --release 8 -Xlint:-options -encoding UTF-8 -d $OutDir $AllFiles

if ($Target -eq "all") {
    java -cp $OutDir com.hotel.tests.TestLauncher all
} else {
    java -cp $OutDir com.hotel.tests.TestLauncher $Target
}
