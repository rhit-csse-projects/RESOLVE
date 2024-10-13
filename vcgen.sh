#!/bin/bash
#
# vcgen.sh
# ---------------------------------
# Copyright (c) 2024
# RESOLVE Software Research Group
# School of Computing
# Clemson University
# All rights reserved.
# ---------------------------------
# This file is subject to the terms and conditions defined in
# file 'LICENSE.txt', which is part of this source code package.
#


#set "repo_path=%cd%"

REPO_PATH=$PWD

#export REPO_PATH

echo "$REPO_PATH"

#mvn clean package -DskipTests
EXECUTABLE="$REPO_PATH/target/RESOLVE-Summer24a-jar-with-dependencies.jar"

echo "$EXECUTABLE"
#set "executable=%cd%\target\RESOLVE-Summer24a-jar-with-dependencies.jar"
#echo executable: %executable%

cd ..
git clone https://github.com/ClemsonRSRG/RESOLVE-Workspace.git
cd RESOLVE-Workspace
cd RESOLVE
cd Main

RETURN_PATH=$PWD

echo "$RETURN_PATH"

find "$RETURN_PATH" -type f -name "*.rb" | while read -r file; do
    # Get the directory of the file
    dir=$(dirname "$file")
    base_file="${file%.*}"

    echo "Base File: $base_file"
    echo "Directory: $dir"
    echo "File: $file"
    # Output the directory and the file
    cd "$dir"
    java -jar "$EXECUTABLE" -VCs "$file"
    mv "$base_file.asrt" "$RETURN_PATH"


done



#for /r "%return_path%" %%d in (*.rb) do (
#    @REM echo Hello
#    @REM pause
#    rem Extract the directory path
#    echo Full file path: %%d
#    set "dir_path=%%~pd"
#    cd %dir_path%
#    java -jar %executable% -VCs %%d
#    @REM pause
#)
#
#@REM cd %repo_path%
#@REM call mvn test
#@REM pause
