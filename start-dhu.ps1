param(
    [string]$Serial = "192.168.1.232:43487"
)

$ErrorActionPreference = "Stop"

function Resolve-SdkPath {
    $localProps = Join-Path $PSScriptRoot "local.properties"
    if (Test-Path $localProps) {
        $line = Get-Content $localProps | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
        if ($line) {
            $value = $line.Substring("sdk.dir=".Length)
            $value = $value -replace '\\\\', '\\'
            return $value
        }
    }
    if ($env:ANDROID_SDK_ROOT) { return $env:ANDROID_SDK_ROOT }
    if ($env:ANDROID_HOME) { return $env:ANDROID_HOME }
    throw "SDK Android non trovato. Configura local.properties o ANDROID_SDK_ROOT."
}

$sdk = Resolve-SdkPath
$adb = Join-Path $sdk "platform-tools\adb.exe"
$dhu = Join-Path $sdk "extras\google\auto\desktop-head-unit.exe"

if (!(Test-Path $adb)) { throw "adb non trovato in: $adb" }
if (!(Test-Path $dhu)) {
    $alt = Join-Path $sdk "extras\google\auto\desktop-head-unit\desktop-head-unit.exe"
    if (Test-Path $alt) { $dhu = $alt } else { throw "DHU non trovato nello SDK." }
}

Write-Host "[1/5] Connessione ADB a $Serial"
& $adb connect $Serial | Out-Host

Write-Host "[2/5] Verifica device"
$state = (& $adb -s $Serial get-state 2>$null)
if ($state -notmatch "device") {
    throw "Device non disponibile su $Serial (stato: $state)."
}

Write-Host "[3/5] Forward porta 5277"
& $adb -s $Serial forward --remove tcp:5277 2>$null | Out-Null
& $adb -s $Serial forward tcp:5277 tcp:5277 | Out-Host

Write-Host "[4/5] Stop vecchi processi DHU"
Get-Process "desktop-head-unit" -ErrorAction SilentlyContinue | Stop-Process -Force

Write-Host "[5/5] Avvio DHU"
Start-Process -FilePath $dhu

Write-Host ""
Write-Host "DHU avviato. Se resta su 'waiting for phone':"
Write-Host "- Android Auto > Impostazioni sviluppatore > Start head unit server"
