# Opcodex for the Microsoft Store

The Store package uses the TwinChiefs identity from Partner Center and contains the x64 desktop application and bundled Java runtime. The product version appears in About. The four-part MSIX version controls Store updates and must increase for subsequent submissions. Microsoft reserves the fourth component.

## Build the package

On Windows, install the Windows SDK so `MakeAppx.exe` is available. Start with the native app image from the exact Windows build under review. If you are building the app image locally, use the checksummed Windows portable archive and the release JDK.

```powershell
pwsh -NoProfile -File opcodex/build-desktop.ps1 -PackagePath <portable-zip> -BuildJavaHome <jdk>
pwsh -NoProfile -File opcodex/build-msix.ps1 -AppImagePath <native-app-image> -PackageVersion 1.0.0.0
```

The second command writes `Opcodex-Store-1.0.0.0-x64.msix` under `build/store` with a checksum and source record. To rebuild the same version, use `-OutputDirectory` with a fresh directory. Verify the checksum and check the manifest against **Product management → Product identity** before uploading.

## Test and submit

Use `-LocalTest` to produce a separate package for an unsigned installation test on Windows 11. Run this command in an elevated PowerShell window on the test machine.

```powershell
Add-AppxPackage -Path 'build/store/Opcodex-LocalTest-1.0.0.0-x64.msix' -AllowUnsigned
```

Open **Opcodex MSIX Test** from Start. Check startup, project creation and reopening, binary import, analysis and decompiler output. Verify that the app and its subprocesses can read user-selected files and write settings outside the protected installation directory. Use a disposable Windows profile for test projects.

The manifest declares `runFullTrust` for the Win32 desktop app. Opcodex reads binaries selected by the user and starts the bundled native analysis and decompiler executables. Debugger launch actions also start or attach to a process when the user requests it.

Upload the **Store** package after the release checks pass. Microsoft signs the Store package after certification.

[Microsoft's MSIX submission requirements](https://learn.microsoft.com/en-us/windows/apps/publish/publish-your-app/msix/app-package-requirements) · [Unsigned MSIX testing](https://learn.microsoft.com/en-us/windows/msix/package/unsigned-package)
