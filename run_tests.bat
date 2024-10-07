@REM
@REM run_tests.bat
@REM ---------------------------------
@REM Copyright (c) 2024
@REM RESOLVE Software Research Group
@REM School of Computing
@REM Clemson University
@REM All rights reserved.
@REM ---------------------------------
@REM This file is subject to the terms and conditions defined in
@REM file 'LICENSE.txt', which is part of this source code package.
@REM

@echo off


REM This script will display a message and pause
set "repo_path=%cd%"
call mvn clean package -DskipTests
set "executable=%cd%\target\RESOLVE-Summer24a-jar-with-dependencies.jar"
echo executable: %executable%
cd ..
cd RESOLVE-Workspace
cd RESOLVE
cd Main
set "return_path=%cd%"
for /r "%return_path%" %%d in (*.rb) do (
    @REM echo Hello
    @REM pause
    rem Extract the directory path
    echo Full file path: %%d
    set "dir_path=%%~pd"
    cd %dir_path%
    java -jar %executable% -VCs %%d
    @REM pause
)
cd %repo_path%
call mvn test
pause

