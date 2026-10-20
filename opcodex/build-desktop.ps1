# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$PackagePath,
    [string]$BuildJavaHome = $env:JAVA_HOME,
    [switch]$Install
)
$ErrorActionPreference = 'Stop'
if (-not $IsWindows) { throw 'Native desktop installation currently supports Windows.' }

$opcodexArchive = (Resolve-Path -LiteralPath $PackagePath).Path
$opcodexRepository = Split-Path -Parent $PSScriptRoot
$opcodexChecksumFile = $opcodexArchive + '.sha256'
if (-not (Test-Path -LiteralPath $opcodexChecksumFile)) { throw 'Missing package checksum.' }
$opcodexExpected = ((Get-Content -LiteralPath $opcodexChecksumFile -Raw).Trim() -split '\s+')[0]
$opcodexHash = (Get-FileHash -LiteralPath $opcodexArchive -Algorithm SHA256).Hash.ToLowerInvariant()
if ($opcodexHash -ne $opcodexExpected) { throw 'Package checksum mismatch.' }

Add-Type -AssemblyName System.IO.Compression.FileSystem
$opcodexZip = [System.IO.Compression.ZipFile]::OpenRead($opcodexArchive)
try {
    $opcodexRoots = @($opcodexZip.Entries | ForEach-Object {
        if ($_.FullName -match '(^/|\\|:|(^|/)\.\.(/|$))') { throw 'Unsafe archive path.' }
        ($_.FullName -split '/')[0]
    } | Sort-Object -Unique)
    if ($opcodexRoots.Count -ne 1) { throw 'Expected one application root.' }
    $opcodexEntry = $opcodexZip.GetEntry($opcodexRoots[0] + '/opcodex-release.json')
    if ($null -eq $opcodexEntry) { throw 'Missing product manifest.' }
    $opcodexReader = [System.IO.StreamReader]::new($opcodexEntry.Open())
    try { $opcodexManifest = $opcodexReader.ReadToEnd() | ConvertFrom-Json }
    finally { $opcodexReader.Dispose() }
}
finally { $opcodexZip.Dispose() }
if ($opcodexManifest.product -ne 'Opcodex' -or $opcodexManifest.platform -ne 'win_x86_64') {
    throw 'Expected a native Opcodex Windows x86-64 package.'
}
if ($opcodexManifest.version -notmatch '^\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?$' -or
    $opcodexManifest.sourceRevision -notmatch '^[0-9a-f]{40}$') { throw 'Invalid product provenance.' }

$opcodexBuildRoot = Join-Path $opcodexRepository ('build/desktop/' + $opcodexHash.Substring(0,12))
$opcodexInput = Join-Path $opcodexBuildRoot 'input'
$opcodexImage = Join-Path $opcodexBuildRoot 'Opcodex'
$opcodexRecordPath = Join-Path $opcodexImage 'desktop-build.json'
$opcodexConfigPath = Join-Path $opcodexImage 'app/Opcodex.cfg'
$opcodexBootstrap = 'app.classpath=$APPDIR\Ghidra\Framework\Utility\lib\Utility.jar'
$opcodexJpackagePath = $null
if ($BuildJavaHome) {
    $opcodexCandidate = Join-Path $BuildJavaHome 'bin/jpackage.exe'
    if (Test-Path -LiteralPath $opcodexCandidate) { $opcodexJpackagePath = $opcodexCandidate }
}
else {
    $opcodexCommand = Get-Command jpackage.exe -ErrorAction SilentlyContinue
    if ($null -ne $opcodexCommand) { $opcodexJpackagePath = $opcodexCommand.Source }
}
if ($null -eq $opcodexJpackagePath) { throw 'jpackage.exe is required from the build JDK.' }

if (-not (Test-Path -LiteralPath (Join-Path $opcodexImage 'Opcodex.exe'))) {
    if (Test-Path -LiteralPath $opcodexImage) { throw 'An incomplete app image already exists; inspect it before rebuilding.' }
    New-Item -ItemType Directory -Path $opcodexInput -Force | Out-Null
    [System.IO.Compression.ZipFile]::ExtractToDirectory($opcodexArchive,$opcodexInput,$true)
    $opcodexPayload = Join-Path $opcodexInput $opcodexRoots[0]
    $opcodexArguments = @(
        '--type','app-image','--name','Opcodex',
        '--app-version',($opcodexManifest.version -split '-')[0],
        '--vendor','Opcodex','--description','Opcodex reverse-engineering workbench',
        '--copyright','Copyright 2026 Opcodex contributors',
        '--input',$opcodexPayload,'--dest',$opcodexBuildRoot,
        '--main-jar','Ghidra/Framework/Utility/lib/Utility.jar',
        '--main-class','ghidra.Ghidra','--arguments','ghidra.GhidraRun',
        '--add-modules','ALL-MODULE-PATH',
        '--icon',(Join-Path $opcodexRepository 'opcodex/branding/opcodex.ico')
    )
    foreach ($opcodexLine in Get-Content -LiteralPath (Join-Path $opcodexPayload 'support/launch.properties')) {
        if ($opcodexLine -match '^VMARGS(?:_WINDOWS)?=(.+)$') {
            $opcodexArguments += @('--java-options',$Matches[1])
        }
    }
    & $opcodexJpackagePath @opcodexArguments
    if ($LASTEXITCODE -ne 0) { throw 'Native app-image creation failed.' }
    # jpackage enumerates every payload JAR. Preserve the original minimal bootstrap instead;
    # the existing module loader selects application and extension dependencies at startup.
    $opcodexConfig = @(Get-Content -LiteralPath $opcodexConfigPath)
    if ($opcodexBootstrap -notin $opcodexConfig) { throw 'Native launcher is missing its bootstrap classpath.' }
    $opcodexConfig | Where-Object {
        -not $_.StartsWith('app.classpath=') -or $_ -eq $opcodexBootstrap
    } | Set-Content -LiteralPath $opcodexConfigPath -Encoding utf8NoBOM
    $opcodexRecord = [ordered]@{
        product='Opcodex'; version=$opcodexManifest.version
        sourceRevision=$opcodexManifest.sourceRevision; portableSha256=$opcodexHash
        runtime='Build JDK runtime, including its original legal notices'
    }
    $opcodexRecord | ConvertTo-Json | Set-Content -LiteralPath $opcodexRecordPath -Encoding utf8NoBOM
}
$opcodexSavedRecord = Get-Content -LiteralPath $opcodexRecordPath -Raw | ConvertFrom-Json
if ($opcodexSavedRecord.portableSha256 -ne $opcodexHash) { throw 'Cached app image does not match this package.' }
$opcodexClasspath = @(Get-Content -LiteralPath $opcodexConfigPath | Where-Object { $_.StartsWith('app.classpath=') })
if ($opcodexClasspath.Count -ne 1 -or $opcodexClasspath[0] -ne $opcodexBootstrap) {
    throw 'Native launcher differs from the supported module bootstrap.'
}

if ($Install) {
    $opcodexInstallRoot = Join-Path $env:LOCALAPPDATA 'Programs/Opcodex/versions'
    $opcodexVersionFolder = $opcodexManifest.version + '-' +
        $opcodexManifest.sourceRevision.Substring(0,10) + '-' + $opcodexHash.Substring(0,8)
    $opcodexTarget = Join-Path $opcodexInstallRoot $opcodexVersionFolder
    $opcodexExe = Join-Path $opcodexTarget 'Opcodex.exe'
    if (-not (Test-Path -LiteralPath $opcodexTarget)) {
        New-Item -ItemType Directory -Path $opcodexInstallRoot -Force | Out-Null
        Copy-Item -LiteralPath $opcodexImage -Destination $opcodexTarget -Recurse
    }
    if (-not (Test-Path -LiteralPath $opcodexExe)) { throw 'Incomplete versioned installation.' }
    $opcodexInstalledRecord = Get-Content -LiteralPath (Join-Path $opcodexTarget 'desktop-build.json') -Raw | ConvertFrom-Json
    if ($opcodexInstalledRecord.portableSha256 -ne $opcodexHash) { throw 'Existing versioned installation differs from this package.' }
    $opcodexShell = New-Object -ComObject WScript.Shell
    $opcodexLocations = @(
        [Environment]::GetFolderPath('Desktop'),
        (Join-Path ([Environment]::GetFolderPath('Programs')) 'Opcodex')
    )
    foreach ($opcodexLocation in $opcodexLocations) {
        New-Item -ItemType Directory -Path $opcodexLocation -Force | Out-Null
        $opcodexShortcut = $opcodexShell.CreateShortcut((Join-Path $opcodexLocation 'Opcodex.lnk'))
        $opcodexShortcut.TargetPath = $opcodexExe
        $opcodexShortcut.WorkingDirectory = $opcodexTarget
        $opcodexShortcut.IconLocation = $opcodexExe + ',0'
        $opcodexShortcut.Description = 'Opcodex reverse-engineering workbench'
        $opcodexShortcut.Save()
    }
    Write-Output ('Installed: ' + $opcodexExe)
}
else { Write-Output ('Built: ' + (Join-Path $opcodexImage 'Opcodex.exe')) }
