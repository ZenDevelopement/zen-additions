# Build script for all Minecraft versions
# This script builds the mod for Minecraft 1.21.8, 1.21.7, 1.21.6, 1.21.5, and 1.21.4

$versions = @("1.21.8", "1.21.7", "1.21.6", "1.21.5", "1.21.4")
$buildDir = "build/libs"
$outputDir = "builds"

# Create output directory if it doesn't exist
if (!(Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Building Zen Additions for all versions" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$successCount = 0
$failCount = 0

foreach ($version in $versions) {
    Write-Host "Building for Minecraft $version..." -ForegroundColor Green
    
    # Copy the version-specific properties
    Copy-Item "gradle-$version.properties" "gradle.properties" -Force
    
    # Clean and build
    & .\gradlew.bat clean build
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Successfully built for Minecraft $version" -ForegroundColor Green
        $successCount++
        
        # Copy the built JAR to the output directory with version in filename
        $jarFile = Get-ChildItem "$buildDir/*.jar" | Where-Object { $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-dev.jar" } | Select-Object -First 1
        if ($jarFile) {
            $newName = $jarFile.Name -replace "\.jar$", "-mc$version.jar"
            Copy-Item $jarFile.FullName "$outputDir/$newName" -Force
            Write-Host "  Copied to: $outputDir/$newName" -ForegroundColor Gray
        }
    } else {
        Write-Host "✗ Failed to build for Minecraft $version" -ForegroundColor Red
        $failCount++
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Build Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Successful builds: $successCount" -ForegroundColor Green
Write-Host "Failed builds: $failCount" -ForegroundColor Red
Write-Host ""

if ($successCount -gt 0) {
    Write-Host "Built JARs are located in: $outputDir" -ForegroundColor Yellow
}
