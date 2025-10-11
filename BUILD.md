# Multi-Version Build Instructions

This project supports building for multiple Minecraft versions: 1.21.8, 1.21.7, 1.21.6, 1.21.5, and 1.21.4.

## Overview

Each Minecraft version has its own gradle properties file that contains the correct mappings and dependencies for that version. You switch between versions by copying the appropriate properties file to `gradle.properties` before building.

## Version-Specific Properties Files

- `gradle-1.21.8.properties` - Minecraft 1.21.8
- `gradle-1.21.7.properties` - Minecraft 1.21.7
- `gradle-1.21.6.properties` - Minecraft 1.21.6
- `gradle-1.21.5.properties` - Minecraft 1.21.5
- `gradle-1.21.4.properties` - Minecraft 1.21.4

## Building for a Specific Version

### Windows PowerShell

To build for a specific version, copy the appropriate properties file and then run the build:

```powershell
# For Minecraft 1.21.8
Copy-Item gradle-1.21.8.properties gradle.properties
.\gradlew clean build

# For Minecraft 1.21.7
Copy-Item gradle-1.21.7.properties gradle.properties
.\gradlew clean build

# For Minecraft 1.21.6
Copy-Item gradle-1.21.6.properties gradle.properties
.\gradlew clean build

# For Minecraft 1.21.5
Copy-Item gradle-1.21.5.properties gradle.properties
.\gradlew clean build

# For Minecraft 1.21.4
Copy-Item gradle-1.21.4.properties gradle.properties
.\gradlew clean build
```

### Linux/macOS

```bash
# For Minecraft 1.21.8
cp gradle-1.21.8.properties gradle.properties
./gradlew clean build

# For Minecraft 1.21.7
cp gradle-1.21.7.properties gradle.properties
./gradlew clean build

# For Minecraft 1.21.6
cp gradle-1.21.6.properties gradle.properties
./gradlew clean build

# For Minecraft 1.21.5
cp gradle-1.21.5.properties gradle.properties
./gradlew clean build

# For Minecraft 1.21.4
cp gradle-1.21.4.properties gradle.properties
./gradlew clean build
```

## Building All Versions

### Windows PowerShell Script

Create a PowerShell script to build all versions:

```powershell
# build-all.ps1
$versions = @("1.21.8", "1.21.7", "1.21.6", "1.21.5", "1.21.4")

foreach ($version in $versions) {
    Write-Host "Building for Minecraft $version..." -ForegroundColor Green
    Copy-Item "gradle-$version.properties" "gradle.properties"
    .\gradlew clean build
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Successfully built for Minecraft $version" -ForegroundColor Green
    } else {
        Write-Host "Failed to build for Minecraft $version" -ForegroundColor Red
    }
    Write-Host ""
}
```

### Linux/macOS Shell Script

Create a bash script to build all versions:

```bash
#!/bin/bash
# build-all.sh

versions=("1.21.8" "1.21.7" "1.21.6" "1.21.5" "1.21.4")

for version in "${versions[@]}"; do
    echo "Building for Minecraft $version..."
    cp "gradle-$version.properties" "gradle.properties"
    ./gradlew clean build
    if [ $? -eq 0 ]; then
        echo "Successfully built for Minecraft $version"
    else
        echo "Failed to build for Minecraft $version"
    fi
    echo ""
done
```

Don't forget to make the script executable on Linux/macOS:
```bash
chmod +x build-all.sh
```

## Output Location

Built JAR files will be located in:
```
build/libs/zen-additions-1.0.0.jar
```

After building for each version, you should rename or move the JAR file to avoid overwriting it when building for the next version.

## Important Notes

1. **DO NOT commit `gradle.properties`** - This file should remain as-is for the default version. The version-specific files are for building only.

2. **Clean builds recommended** - Always use `gradlew clean build` to ensure a fresh build for each version.

3. **Version naming** - Consider adding the Minecraft version to the JAR filename by updating `mod_version` in each properties file:
   - Example: `mod_version=0.0.1-1.21.8`

4. **Testing** - Test each built version in the corresponding Minecraft version before release.

5. **Mappings updates** - The mappings versions in the properties files are based on the latest available at the time of creation. Check https://fabricmc.net/develop for updated mappings if needed.

## Updating Mappings

Keep your project up-to-date with the correct Yarn mappings using the automatic migration command. This is especially useful when new mappings are released for your Minecraft version.

### How to Update Mappings

1. **Check for new mappings** at [fabricmc.net/develop](https://fabricmc.net/develop)
2. **Run the migration command** for the version you want to update:

```powershell
# Windows - Example for Minecraft 1.21.8
Copy-Item gradle-1.21.8.properties gradle.properties
.\gradlew migrateMappings --mappings "1.21.8+build.2"
Copy-Item gradle.properties gradle-1.21.8.properties
```

```bash
# Linux/macOS - Example for Minecraft 1.21.8
cp gradle-1.21.8.properties gradle.properties
./gradlew migrateMappings --mappings "1.21.8+build.2"
cp gradle.properties gradle-1.21.8.properties
```

### What This Does

The `migrateMappings` command will:
- Update your code to use the new mapping names
- Automatically refactor method/field references that have changed
- Update the `yarn_mappings` version in gradle.properties

For more information, see the [Fabric Wiki: Updating Yarn Mappings](https://fabricmc.net/wiki/tutorial:migratemappings)

## Troubleshooting

### Build fails with "Could not resolve dependencies"
- Ensure you're using the correct mappings version for your Minecraft version
- Check https://fabricmc.net/develop for compatible versions
- Clear your Gradle cache: `.\gradlew clean --refresh-dependencies`

### Wrong Minecraft version in-game
- Verify you copied the correct properties file before building
- Check the `minecraft_version` value in `gradle.properties`
- Perform a clean build: `.\gradlew clean build`
