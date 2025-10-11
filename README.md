# Zen Additions

A Meteor Client addon that adds various utility features and enhancements.

**Version:** 1.0.0 (First stable release)

## Multi-Version Support

This mod supports multiple Minecraft versions:
- **1.21.8**
- **1.21.7**
- **1.21.6**
- **1.21.5**
- **1.21.4**

See [BUILD.md](BUILD.md) for detailed instructions on building for specific versions.

### Quick Build

#### Building for a specific version (Windows):
```powershell
Copy-Item gradle-1.21.8.properties gradle.properties
.\gradlew build
```

#### Building all versions at once:
```powershell
# Windows
.\build-all.ps1

# Linux/macOS
./build-all.sh
```

Built JARs will be placed in the `builds/` directory.

### Automated Builds

The project includes a GitHub Action that automatically builds all versions on every push to the main/master branch. The built JARs can be downloaded from the Actions tab in GitHub.

## Features

- **Accurate Block Placement** - Precision block placement utilities
- **Easy Bedrock Breaker** - Simplified bedrock breaking mechanics
- **Projectile Trails** - Visual trails for projectiles
- **No Gravity** - Movement module for removing gravity effects
- **Moses** - World manipulation features
- **Freeze** - Ghost mode freezing capabilities
- **Ghost Block Fly** - Flying with ghost blocks

### Commands

- `/entitynbt` - View entity NBT data
- `/ip` - Display current server IP
- `/ping` - Check your connection latency
- `/stats` - View player statistics
- `/uuid` - Display entity UUIDs
- `/equip` - Equipment management utilities
- `/worldborder` - World border information

## Installation

1. Download the appropriate version JAR from releases
2. Install [Meteor Client](https://meteorclient.com/)
3. Place the JAR in your `.minecraft/mods` folder
4. Launch Minecraft with Fabric

## Development

### How to use

### Project structure

```text
.
│── .github
│   ╰── workflows
│       │── dev_build.yml
│       ╰── pull_request.yml
│── gradle
│   ╰── wrapper
│       │── gradle-wrapper.jar
│       ╰── gradle-wrapper.properties
│── src
│   ╰── main
│       │── java
│       │   ╰── com
│       │       ╰── example
│       │           ╰── addon
│       │               │── commands
│       │               │   ╰── CommandExample
│       │               │── hud
│       │               │   ╰── HudExample
│       │               │── modules
│       │               │   ╰── ModuleExample
│       │               ╰── AddonTemplate
│       ╰── resources
│           │── assets
│           │   ╰── template
│           │       ╰── icon.png
│           │── addon-template.mixins.json
│           ╰── fabric.mod.json
│── .editorconfig
│── .gitignore
│── build.gradle
│── gradle.properties
│── gradlew
│── gradlew.bat
│── LICENSE
│── README.md
╰── settings.gradle
```

This is the default project structure. Each folder/file has a specific purpose.  
Here is a brief explanation of the ones you might need to modify:

- `.github/workflows`: Contains the GitHub Actions configuration files.
- `gradle`: Contains the Gradle wrapper files.  
  Edit the `gradle.properties` file to change the version of the Gradle wrapper.
- `src/main/java/com/example/addon`: Contains the main class of the addon.  
  Here you can register your custom commands, modules, and HUDs.  
  Edit the `getPackage` method to reflect the package of your addon.
- `src/main/resources`: Contains the resources of the addon.
    - `assets`: Contains the assets of the addon.  
      You can add your own assets here, separated in subfolders.
        - `template`: Contains the assets of the template.  
          You can replace the `icon.png` file with your own addon icon.  
          Also, rename this folder to reflect the name of your addon.
    - `addon-template.mixins.json`: Contains the Mixin configuration for the addon.  
      You can add your own mixins in the `client` array.
    - `fabric.mod.json`: Contains the metadata of the addon.  
      Edit the various fields to reflect the metadata of your addon.
- `build.gradle.kts`: Contains the Gradle build script.  
  You can manage the dependencies of the addon here.  
  Remember to keep the `fabric-loom` version up-to-date.
- `gradle.properties.kts`: Contains the properties of the Gradle build.  
  These will be used by the build script.
- `LICENSE`: Contains the license of the addon.  
  You can edit this file to change the license of your addon.
- `README.md`: Contains the documentation of the addon.  
  You can edit this file to reflect the documentation of your addon, and showcase its features.

## License

This template is available under the CC0 license. Feel free to use it for your own projects.
