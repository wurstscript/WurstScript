# Renews the session that -editor launches sign in with, from the Battle.net desktop app.
#
# The app signs Warcraft III in with a one-time WEB_TOKEN it writes just before it starts the game. A client given
# -editor signs in with EDITOR_TOKEN instead, which only the client itself renews (on each sign-in), so once it has
# lapsed every -editor launch stops at the login panel. This asks the app to start the game, closes that game the
# moment it appears - before it can spend the token - and stores the token as EDITOR_TOKEN.
#
# Prints one line per step and ends with "result <outcome>"; never prints a token.
param(
  [Parameter(Mandatory = $true)][string]$BattleNetExe,
  [int]$TimeoutSeconds = 90,
  [int]$AskAgainSeconds = 25
)
$ErrorActionPreference = "Stop"
$launchKey = "HKCU:\Software\Blizzard Entertainment\Battle.net\Launch Options\W3"
$gameKey = "HKCU:\Software\Blizzard Entertainment\Warcraft III"

function Read-WebToken {
  $value = (Get-ItemProperty -Path $launchKey -Name WEB_TOKEN -ErrorAction SilentlyContinue).WEB_TOKEN
  if ($value -is [byte[]]) { return $value }
  return $null
}

function Test-AppGame([int]$id) {
  # A client the app started: its parent is a Battle.net.exe process. Anything else is someone else's client.
  $process = Get-CimInstance Win32_Process -Filter "ProcessId=$id" -ErrorAction SilentlyContinue
  if (-not $process) { return $false }
  $parent = Get-Process -Id $process.ParentProcessId -ErrorAction SilentlyContinue
  return [bool]($parent -and $parent.ProcessName -eq "Battle.net")
}

function Get-GamePids {
  @(Get-Process -Name "Warcraft III" -ErrorAction SilentlyContinue | ForEach-Object { $_.Id })
}

$before = Read-WebToken
$beforeHex = if ($before) { [BitConverter]::ToString($before) } else { "" }
$existing = Get-GamePids
# The app does not start a second client while its own is running, so it would never write a token.
if (@($existing | Where-Object { Test-AppGame $_ }).Count -gt 0) {
  "result app-game-running"
  exit 3
}

$watch = [Diagnostics.Stopwatch]::StartNew()
$askedAt = -1e9
$token = $null
$tokenAt = 0
$closed = 0
while ($watch.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
  if (-not $token -and $watch.Elapsed.TotalSeconds - $askedAt -ge $AskAgainSeconds) {
    $askedAt = $watch.Elapsed.TotalSeconds
    Start-Process -FilePath $BattleNetExe -ArgumentList '--exec="launch W3"'
    "asked {0:N1}" -f $askedAt
  }
  foreach ($id in Get-GamePids) {
    if ($existing -contains $id) { continue }
    $existing += $id
    if (Test-AppGame $id) {
      Stop-Process -Id $id -Force -ErrorAction SilentlyContinue
      $closed++
      "closed {0} {1:N1}" -f $id, $watch.Elapsed.TotalSeconds
    }
  }
  if (-not $token) {
    $now = Read-WebToken
    if ($now -and [BitConverter]::ToString($now) -ne $beforeHex) {
      $token = $now
      $tokenAt = $watch.Elapsed.TotalSeconds
      "token {0:N1}" -f $tokenAt
    }
  }
  # The app writes the token, then starts the game: wait briefly for that game so it is closed, not left running.
  if ($token -and ($closed -gt 0 -or $watch.Elapsed.TotalSeconds - $tokenAt -ge 10)) { break }
  Start-Sleep -Milliseconds 50
}
if (-not $token -and $closed -gt 0) {
  # The app started the game, so it wrote a token, but not where this script reads: the app keeps a private copy of
  # the registry, as one started from a packaged (MSIX) app does.
  "result token-not-visible"
  exit 4
}
if (-not $token) {
  "result no-token"
  exit 2
}
if (-not (Test-Path $gameKey)) { New-Item -Path $gameKey -Force | Out-Null }
Set-ItemProperty -Path $gameKey -Name EDITOR_TOKEN -Value ([byte[]]$token) -Type Binary
"result refreshed"
exit 0
