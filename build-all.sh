#!/bin/bash
# Build script for all Minecraft versions
# This script builds the mod for Minecraft 1.21.8, 1.21.7, 1.21.6, 1.21.5, and 1.21.4

versions=("1.21.8" "1.21.7" "1.21.6" "1.21.5" "1.21.4")
build_dir="build/libs"
output_dir="builds"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
GRAY='\033[0;90m'
NC='\033[0m' # No Color

# Create output directory if it doesn't exist
mkdir -p "$output_dir"

echo -e "${CYAN}========================================"
echo -e "Building Zen Additions for all versions"
echo -e "========================================${NC}"
echo ""

success_count=0
fail_count=0

for version in "${versions[@]}"; do
    echo -e "${GREEN}Building for Minecraft $version...${NC}"
    
    # Copy the version-specific properties
    cp "gradle-$version.properties" "gradle.properties"
    
    # Clean and build
    ./gradlew clean build
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Successfully built for Minecraft $version${NC}"
        ((success_count++))
        
        # Copy the built JAR to the output directory with version in filename
        jar_file=$(find "$build_dir" -name "*.jar" ! -name "*-sources.jar" ! -name "*-dev.jar" -type f | head -n 1)
        if [ -n "$jar_file" ]; then
            base_name=$(basename "$jar_file" .jar)
            new_name="${base_name}-mc${version}.jar"
            cp "$jar_file" "$output_dir/$new_name"
            echo -e "${GRAY}  Copied to: $output_dir/$new_name${NC}"
        fi
    else
        echo -e "${RED}✗ Failed to build for Minecraft $version${NC}"
        ((fail_count++))
    fi
    echo ""
done

echo -e "${CYAN}========================================"
echo -e "Build Summary"
echo -e "========================================${NC}"
echo -e "${GREEN}Successful builds: $success_count${NC}"
echo -e "${RED}Failed builds: $fail_count${NC}"
echo ""

if [ $success_count -gt 0 ]; then
    echo -e "${YELLOW}Built JARs are located in: $output_dir${NC}"
fi
