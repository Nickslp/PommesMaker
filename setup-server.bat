@echo off
if not exist server\ (
    mkdir server
)
cd server
if not exist spigot-1.17.1.jar (
    curl -o "spigot-1.17.1.jar" "https://download.getbukkit.org/spigot/spigot-1.17.1.jar"
)

IF EXIST eula.txt (
  goto CHECKEULA
) ELSE (
  goto ASKEULA
)
IF %errlevel% EQU 1 goto END
:CHECKEULA
>nul find "eula=false" eula.txt && (
  goto ASKEULA
) || (
  goto END
)
:ASKEULA
echo "Do you agree to the Mojang EULA available at https://account.mojang.com/documents/minecraft_eula ?"
set /p EULA=[y/n]
IF /I "%EULA%" NEQ "y" GOTO END
echo eula=true>eula.txt
:END
pause