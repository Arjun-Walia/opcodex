# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$AppImagePath,
    [Parameter(Mandatory)][string]$PackageVersion,
    [string]$OutputDirectory,
    [string]$MakeAppxPath,
    [switch]$LocalTest
)

$ErrorActionPreference = 'Stop'
if (-not $IsWindows) { throw 'Build the MSIX on Windows with the Windows SDK installed.' }
if ($PackageVersion -notmatch '^[1-9]\d*\.(0|[1-9]\d*)\.(0|[1-9]\d*)\.0$' -or
    @($PackageVersion.Split('.') | Where-Object { [long]$_ -gt 65535 }).Count) {
    throw 'Use a four-part Store version such as 1.0.0.0. The last part is reserved for Microsoft.'
}

$repo = Split-Path -Parent $PSScriptRoot
$image = (Resolve-Path -LiteralPath $AppImagePath).Path
$record = Get-Content -LiteralPath (Join-Path $image 'desktop-build.json') -Raw | ConvertFrom-Json
if ($record.product -ne 'Opcodex' -or $record.sourceRevision -notmatch '^[0-9a-f]{40}$' -or
    $record.portableSha256 -notmatch '^[0-9a-f]{64}$') {
    throw 'Expected an Opcodex Windows app image with build provenance.'
}
foreach ($file in 'Opcodex.exe', 'app/Opcodex.cfg', 'app/opcodex-release.json',
                  'runtime/bin/server/jvm.dll', 'runtime/lib/modules') {
    if (-not (Test-Path -LiteralPath (Join-Path $image $file) -PathType Leaf)) {
        throw "App image is missing $file."
    }
}
$release = Get-Content -LiteralPath (Join-Path $image 'app/opcodex-release.json') -Raw | ConvertFrom-Json
if ($release.product -ne 'Opcodex' -or $release.platform -ne 'win_x86_64' -or
    $release.sourceRevision -ne $record.sourceRevision -or $release.version -ne $record.version) {
    throw 'App image and native package provenance disagree.'
}
$launcher = @(Get-Content -LiteralPath (Join-Path $image 'app/Opcodex.cfg'))
$classpath = @($launcher | Where-Object { $_.StartsWith('app.classpath=') })
if ($classpath.Count -ne 1 -or
    $classpath[0] -ne 'app.classpath=$APPDIR\Ghidra\Framework\Utility\lib\Utility.jar' -or
    ($launcher -match '^-?java-options=.*-D(user\.home|application\.settingsdir)=') -or
    ($launcher -match '^-?java-options=.*-javaagent:')) {
    throw 'Use a clean native app image rather than a desktop test overlay.'
}

if (-not $MakeAppxPath) {
    $kits = Join-Path ${env:ProgramFiles(x86)} 'Windows Kits/10/bin'
    $MakeAppxPath = Get-ChildItem -LiteralPath $kits -Directory -ErrorAction SilentlyContinue |
        Where-Object Name -Match '^\d+\.\d+\.\d+\.\d+$' |
        Sort-Object { [version]$_.Name } -Descending |
        ForEach-Object { Join-Path $_.FullName 'x64/makeappx.exe' } |
        Where-Object { Test-Path -LiteralPath $_ -PathType Leaf } |
        Select-Object -First 1
}
if (-not $MakeAppxPath -or -not (Test-Path -LiteralPath $MakeAppxPath -PathType Leaf)) {
    throw 'MakeAppx.exe was not found. Install the Windows SDK or pass -MakeAppxPath.'
}

$identity = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'windows-store.json') -Raw | ConvertFrom-Json
$packageLabel = 'Store'
if ($LocalTest) {
    $identity.identityName += '.LocalTest'
    $identity.publisher = 'CN=Opcodex Local Test, OID.2.25.311729368913984317654407730594956997722=1'
    $identity.publisherDisplayName = 'Opcodex Local Test'
    $identity.displayName = 'Opcodex MSIX Test'
    $packageLabel = 'LocalTest'
}
$name = [Security.SecurityElement]::Escape($identity.identityName)
$publisher = [Security.SecurityElement]::Escape($identity.publisher)
$publisherDisplay = [Security.SecurityElement]::Escape($identity.publisherDisplayName)
$display = [Security.SecurityElement]::Escape($identity.displayName)

if (-not $OutputDirectory) { $OutputDirectory = Join-Path $repo 'build/store' }
$output = (New-Item -ItemType Directory -Force -Path $OutputDirectory).FullName
$final = Join-Path $output "Opcodex-$packageLabel-$PackageVersion-x64.msix"
if (Test-Path -LiteralPath $final) {
    throw 'This package already exists. Choose a new output directory or use the next Store version.'
}
$stageName = 'msix-staging-' + [guid]::NewGuid().ToString('N')
$stage = (New-Item -ItemType Directory -Path (Join-Path $output $stageName)).FullName
$assetPath = Join-Path $repo 'Ghidra/Features/Base/src/main/resources/images/opcodex/app-dark-256.png'

try {
    $manifest = Join-Path $stage 'AppxManifest.xml'
    @"
<?xml version="1.0" encoding="utf-8"?>
<Package xmlns="http://schemas.microsoft.com/appx/manifest/foundation/windows10"
         xmlns:uap="http://schemas.microsoft.com/appx/manifest/uap/windows10"
         xmlns:uap10="http://schemas.microsoft.com/appx/manifest/uap/windows10/10"
         xmlns:rescap="http://schemas.microsoft.com/appx/manifest/foundation/windows10/restrictedcapabilities"
         IgnorableNamespaces="uap uap10 rescap">
  <Identity Name="$name" Publisher="$publisher" Version="$PackageVersion" ProcessorArchitecture="x64" />
  <Properties>
    <DisplayName>$display</DisplayName>
    <PublisherDisplayName>$publisherDisplay</PublisherDisplayName>
    <Logo>StoreAssets\StoreLogo.png</Logo>
  </Properties>
  <Dependencies>
    <TargetDeviceFamily Name="Windows.Desktop" MinVersion="10.0.19041.0" MaxVersionTested="10.0.26100.0" />
  </Dependencies>
  <Resources>
    <Resource Language="en-us" />
  </Resources>
  <Applications>
    <Application Id="Opcodex" Executable="Opcodex.exe" uap10:RuntimeBehavior="win32App" uap10:TrustLevel="mediumIL">
      <uap:VisualElements DisplayName="$display" Description="Opcodex reverse-engineering workbench"
        BackgroundColor="transparent" Square150x150Logo="StoreAssets\Square150x150Logo.png"
        Square44x44Logo="StoreAssets\Square44x44Logo.png" />
    </Application>
  </Applications>
  <Capabilities>
    <rescap:Capability Name="runFullTrust" />
  </Capabilities>
</Package>
"@ | Set-Content -LiteralPath $manifest -Encoding utf8NoBOM

    $map = [Collections.Generic.List[string]]::new()
    $map.Add('[Files]')
    $map.Add(('"{0}" "AppxManifest.xml"' -f $manifest))

    Add-Type -AssemblyName System.Drawing
    $sourceIcon = [Drawing.Image]::FromFile($assetPath)
    try {
        foreach ($asset in @{ StoreLogo = 50; Square44x44Logo = 44; Square150x150Logo = 150 }.GetEnumerator()) {
            $logoPath = Join-Path $stage ($asset.Key + '.png')
            $bitmap = [Drawing.Bitmap]::new($asset.Value, $asset.Value)
            try {
                $graphics = [Drawing.Graphics]::FromImage($bitmap)
                try {
                    $graphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
                    $graphics.DrawImage($sourceIcon, 0, 0, $asset.Value, $asset.Value)
                    $bitmap.Save($logoPath, [Drawing.Imaging.ImageFormat]::Png)
                }
                finally { $graphics.Dispose() }
            }
            finally { $bitmap.Dispose() }
            $map.Add(('"{0}" "StoreAssets\{1}.png"' -f $logoPath, $asset.Key))
        }
    }
    finally { $sourceIcon.Dispose() }

    foreach ($file in Get-ChildItem -LiteralPath $image -File -Recurse -Force) {
        $relative = [IO.Path]::GetRelativePath($image, $file.FullName)
        $map.Add(('"{0}" "{1}"' -f $file.FullName, $relative))
    }
    $mappingFile = Join-Path $stage 'files.txt'
    $map | Set-Content -LiteralPath $mappingFile -Encoding utf8BOM
    & $MakeAppxPath pack /no /f $mappingFile /p $final
    if ($LASTEXITCODE -ne 0) { throw 'MakeAppx could not create the MSIX.' }

    $checksum = (Get-FileHash -LiteralPath $final -Algorithm SHA256).Hash.ToLowerInvariant()
    "$checksum  $(Split-Path -Leaf $final)" | Set-Content -LiteralPath "$final.sha256" -Encoding utf8NoBOM
    [ordered]@{
        product = $record.product
        version = $record.version
        sourceRevision = $record.sourceRevision
        portableSha256 = $record.portableSha256
        msixSha256 = $checksum
        identityName = $identity.identityName
        packageVersion = $PackageVersion
        platform = 'win_x86_64'
        localTest = $LocalTest.IsPresent
    } | ConvertTo-Json | Set-Content -LiteralPath "$final.json" -Encoding utf8NoBOM
    Write-Output "Built: $final"
}
finally {
    $resolvedStage = (Resolve-Path -LiteralPath $stage).Path
    if ((Split-Path -Parent $resolvedStage) -eq $output -and
        (Split-Path -Leaf $resolvedStage) -eq $stageName) {
        Remove-Item -LiteralPath $resolvedStage -Recurse -Force
    }
}
