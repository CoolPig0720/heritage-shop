param(
  [string]$JavaHome,
  [string]$MavenHome
)

$ErrorActionPreference = "Stop"

function Prepend-Path([string]$dir) {
  if (-not $dir) { return }
  if (-not (Test-Path $dir)) { return }
  $parts = @($env:Path -split ";") | Where-Object { $_ -and ($_ -ne $dir) }
  $env:Path = ($dir + ";" + ($parts -join ";")).TrimEnd(";")
}

function Append-Path([string]$dir) {
  if (-not $dir) { return }
  if (-not (Test-Path $dir)) { return }
  if ((@($env:Path -split ";") -contains $dir)) { return }
  $env:Path = ($env:Path.TrimEnd(";") + ";" + $dir).TrimEnd(";")
}

if (-not $JavaHome) {
  $javaCandidates = @()
  $javaCandidatePatterns = @(
    "C:\Program Files\Eclipse Adoptium\jdk-17*",
    "C:\Program Files\Java\jdk-17*",
    "C:\Program Files\Microsoft\jdk-17*",
    "D:\Software\Java\jdk-17*",
    "D:\Java\jdk-17*"
  )

  foreach ($pattern in $javaCandidatePatterns) {
    $javaCandidates += (Get-ChildItem $pattern -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName)
  }

  if ($env:JAVA_HOME -and ($env:JAVA_HOME -match "jdk-?17")) { $javaCandidates += $env:JAVA_HOME }

  foreach ($candidate in $javaCandidates) {
    if (-not $candidate) { continue }
    $javac = Join-Path $candidate "bin\javac.exe"
    if (Test-Path $javac) {
      $JavaHome = $candidate
      break
    }
  }
}

if (-not $JavaHome) {
  Write-Host "JDK not found. Install JDK 17 and rerun, or pass -JavaHome." -ForegroundColor Red
  exit 1
}

$env:JAVA_HOME = $JavaHome
Prepend-Path (Join-Path $JavaHome "bin")

if (-not $MavenHome) {
  $mavenCandidates = @()
  if ($env:MAVEN_HOME) { $mavenCandidates += $env:MAVEN_HOME }
  if ($env:M2_HOME) { $mavenCandidates += $env:M2_HOME }
  $mavenCandidates += @(
    "D:\maven\apache-maven-3.9.12",
    "C:\Program Files\apache-maven-3.9.4",
    "C:\Program Files\Java\apache-maven-3.9.12",
    "C:\Program Files\Java\apache-maven-3.9.12"
  )

  foreach ($candidate in $mavenCandidates) {
    if (-not $candidate) { continue }
    $mvnCmd = Join-Path $candidate "bin\mvn.cmd"
    if (Test-Path $mvnCmd) {
      $MavenHome = $candidate
      break
    }
  }
}

if (-not $MavenHome) {
  Write-Host "Maven not found. Install Maven and rerun, or pass -MavenHome." -ForegroundColor Red
  exit 1
}

$env:MAVEN_HOME = $MavenHome
$env:M2_HOME = $MavenHome
Append-Path (Join-Path $MavenHome "bin")

Write-Host "JAVA_HOME=$env:JAVA_HOME" -ForegroundColor Green
Write-Host "MAVEN_HOME=$env:MAVEN_HOME" -ForegroundColor Green
Write-Host "--- java -version" -ForegroundColor Cyan
java -version
Write-Host "--- mvn -v" -ForegroundColor Cyan
mvn -v
