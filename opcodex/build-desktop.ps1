# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$PackagePath,
    [string]$BuildJavaHome = $env:JAVA_HOME,
    [switch]$Install,
    [switch]$Installer,
    [string]$DesktopShellPath = (Join-Path (Split-Path -Parent $PSScriptRoot) 'opcodex/desktop/src-tauri/target/release/opcodex-desktop.exe'),
    [string]$WixDirectory = $env:OPCODEX_WIX_HOME
)
$ErrorActionPreference = 'Stop'
if ([Environment]::OSVersion.Platform -ne [PlatformID]::Win32NT) { throw 'Native desktop installation currently supports Windows.' }
# Runs on PowerShell 7 and on the Windows PowerShell 5.1 that ships with Windows.
function Write-OpcodexText([string]$Path, [string[]]$Lines) {
    [IO.File]::WriteAllLines($Path, $Lines, [Text.UTF8Encoding]::new($false))
}

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
    # A failed earlier run can leave a partial extraction behind; start from an empty folder.
    if (Test-Path -LiteralPath $opcodexInput) { Remove-Item -LiteralPath $opcodexInput -Recurse -Force }
    New-Item -ItemType Directory -Path $opcodexInput -Force | Out-Null
    [System.IO.Compression.ZipFile]::ExtractToDirectory($opcodexArchive,$opcodexInput)
    $opcodexPayload = Join-Path $opcodexInput $opcodexRoots[0]
    # Preserve the runtime's available features, including Java scripting/debugging tools.
    # Hosted Temurin 25 images cannot re-link the build-only jdk.jlink/jdk.jpackage modules.
    $opcodexJava = Join-Path (Split-Path -Parent $opcodexJpackagePath) 'java.exe'
    $opcodexModules = @(& $opcodexJava --list-modules | ForEach-Object { ($_ -split '@')[0] } |
        Where-Object { $_ -notin @('jdk.jlink', 'jdk.jpackage') })
    if ($LASTEXITCODE -ne 0 -or 'java.desktop' -notin $opcodexModules -or 'jdk.compiler' -notin $opcodexModules) {
        throw 'Unable to enumerate required desktop and scripting runtime modules.'
    }
    $opcodexArguments = @(
        '--type','app-image','--name','Opcodex',
        '--app-version',($opcodexManifest.version -split '-')[0],
        '--vendor','Opcodex','--description','Opcodex reverse-engineering workbench',
        '--copyright','Copyright 2026 Opcodex contributors',
        '--input',$opcodexPayload,'--dest',$opcodexBuildRoot,
        '--main-jar','Ghidra/Framework/Utility/lib/Utility.jar',
        '--main-class','ghidra.Ghidra','--arguments','ghidra.GhidraRun',
        '--add-modules',($opcodexModules -join ','),
        '--jlink-options','--strip-debug --no-man-pages --no-header-files',
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
    Write-OpcodexText $opcodexConfigPath @($opcodexConfig | Where-Object {
        -not $_.StartsWith('app.classpath=') -or $_ -eq $opcodexBootstrap
    })
    $opcodexRecord = [ordered]@{
        product='Opcodex'; version=$opcodexManifest.version
        sourceRevision=$opcodexManifest.sourceRevision; portableSha256=$opcodexHash
        runtime='Build JDK runtime, including its original legal notices'
    }
    Write-OpcodexText $opcodexRecordPath ($opcodexRecord | ConvertTo-Json)
}
$opcodexSavedRecord = Get-Content -LiteralPath $opcodexRecordPath -Raw | ConvertFrom-Json
if ($opcodexSavedRecord.portableSha256 -ne $opcodexHash) { throw 'Cached app image does not match this package.' }
$opcodexClasspath = @(Get-Content -LiteralPath $opcodexConfigPath | Where-Object { $_.StartsWith('app.classpath=') })
if ($opcodexClasspath.Count -ne 1 -or $opcodexClasspath[0] -ne $opcodexBootstrap) {
    throw 'Native launcher differs from the supported module bootstrap.'
}
# Windows treats Opcodex.exe and opcodex.exe as the same name. Keep the CLI in app/support.
$opcodexCli = Join-Path $opcodexImage 'app/support/opcodex.exe'
if (-not (Test-Path -LiteralPath $opcodexCli)) { throw 'The package is missing the opcodex CLI.' }
if (-not (Test-Path -LiteralPath (Join-Path $opcodexImage 'runtime/bin/java.exe') -PathType Leaf)) {
    throw 'Native app image is missing the bundled Java command required by the Opcodex CLI.'
}
# The native Opcodex app replaces the Java launcher. It starts the engine from runtime/ and app/.
$opcodexShell = $DesktopShellPath
if (-not (Test-Path -LiteralPath $opcodexShell -PathType Leaf)) {
    throw 'The native Opcodex app was not built. Run the buildOpcodexDesktopShell Gradle task first.'
}
Copy-Item -LiteralPath $opcodexShell -Destination (Join-Path $opcodexImage 'Opcodex.exe') -Force
$opcodexShellHash = (Get-FileHash -LiteralPath $opcodexShell -Algorithm SHA256).Hash.ToLowerInvariant()
$opcodexImageRecord = Get-Content -LiteralPath $opcodexRecordPath -Raw | ConvertFrom-Json
$opcodexImageRecord | Add-Member -NotePropertyName desktopShellSha256 -NotePropertyValue $opcodexShellHash -Force
Write-OpcodexText $opcodexRecordPath ($opcodexImageRecord | ConvertTo-Json)

if ($Installer) {
    $opcodexInstallerConfig = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'windows-installer.json') -Raw | ConvertFrom-Json
    if ($opcodexInstallerConfig.productVersion -ne $opcodexManifest.version -or
        $opcodexInstallerConfig.installerVersion -notmatch '^\d+\.\d+\.\d+$' -or
        $opcodexInstallerConfig.upgradeUuid -notmatch '^[0-9a-f-]{36}$') { throw 'Installer identity must match this product release.' }
    $opcodexSavedPath = $env:PATH
    try {
        if ($WixDirectory) { $env:PATH = $WixDirectory + [IO.Path]::PathSeparator + $env:PATH }
        if (-not (Get-Command wix.exe -ErrorAction SilentlyContinue)) { throw 'Install the pinned WiX 5.0.2 build tool and UI/Util extensions, or provide -WixDirectory.' }
        $opcodexInstallerOutput = Join-Path $opcodexBuildRoot 'installer'
        New-Item -ItemType Directory -Path $opcodexInstallerOutput -Force | Out-Null
        $opcodexInstallerArgs = @('--type','msi','--name','Opcodex','--app-image',$opcodexImage,
            '--app-version',$opcodexInstallerConfig.installerVersion,'--dest',$opcodexInstallerOutput,
            '--vendor','Opcodex','--description',('Opcodex ' + $opcodexManifest.version),
            '--about-url','https://opcodex.xyz','--win-per-user-install','--win-dir-chooser',
            '--win-menu','--win-menu-group','Opcodex','--win-shortcut','--install-dir','Programs\Opcodex\Application',
            '--win-upgrade-uuid',$opcodexInstallerConfig.upgradeUuid)
        & $opcodexJpackagePath @opcodexInstallerArgs
        if ($LASTEXITCODE -ne 0) { throw 'Windows installer creation failed.' }
        $opcodexBuiltMsi = Join-Path $opcodexInstallerOutput ('Opcodex-' + $opcodexInstallerConfig.installerVersion + '.msi')
        if (-not (Test-Path -LiteralPath $opcodexBuiltMsi)) { throw 'Expected Windows installer was not created.' }
        $opcodexPublishedMsi = Join-Path (Split-Path -Parent $opcodexArchive) ('opcodex_' + $opcodexManifest.version + '_win_x86_64_setup.msi')
        Copy-Item -LiteralPath $opcodexBuiltMsi -Destination $opcodexPublishedMsi -Force
        $opcodexInstallerHash = (Get-FileHash -LiteralPath $opcodexPublishedMsi -Algorithm SHA256).Hash.ToLowerInvariant()
        Write-OpcodexText ($opcodexPublishedMsi + '.sha256') ($opcodexInstallerHash + '  ' + (Split-Path -Leaf $opcodexPublishedMsi))
        $opcodexInstallerRecord = [ordered]@{product='Opcodex'; version=$opcodexManifest.version; platform='win_x86_64';
            sourceRevision=$opcodexManifest.sourceRevision; installerVersion=$opcodexInstallerConfig.installerVersion;
            installerSha256=$opcodexInstallerHash; portableSha256=$opcodexHash; bundledRuntime=$true;
            installScope='current-user'; installSubdirectory='Programs\Opcodex\Application';
            upgradeUuid=$opcodexInstallerConfig.upgradeUuid; signed=$false}
        Write-OpcodexText ($opcodexPublishedMsi + '.json') ($opcodexInstallerRecord | ConvertTo-Json)
        Write-Output ('Installer: ' + $opcodexPublishedMsi)
    }
    finally { $env:PATH = $opcodexSavedPath }
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
