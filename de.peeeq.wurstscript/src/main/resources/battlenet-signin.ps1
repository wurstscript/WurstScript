# Waits, in the registry view it runs in, until the launched client replaces EDITOR_TOKEN, which it does as it signs in.
#
# Prints "ready" once it has read the token the renewal stored, then "signed-in" when that token changes, or "timeout".
# Never prints a token.
param(
  [int]$TimeoutSeconds = 120,
  [string]$Key = "HKCU:\Software\Blizzard Entertainment\Warcraft III"
)
$ErrorActionPreference = "Stop"

function Read-EditorToken {
  $value = (Get-ItemProperty -Path $Key -Name EDITOR_TOKEN -ErrorAction SilentlyContinue).EDITOR_TOKEN
  if ($value -is [byte[]]) { return [BitConverter]::ToString($value) }
  return ""
}

$before = Read-EditorToken
"ready"
$watch = [Diagnostics.Stopwatch]::StartNew()
while ($watch.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
  if ((Read-EditorToken) -ne $before) {
    "signed-in"
    exit 0
  }
  Start-Sleep -Milliseconds 250
}
"timeout"
exit 1
