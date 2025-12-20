@echo off
REM Script de lancement de la configuration automatique
REM ====================================================

echo.
echo =====================================================
echo   Configuration Automatique de l'Application
echo =====================================================
echo.
echo Ce script va executer setup.ps1 avec les permissions necessaires
echo.

REM Vérifier si PowerShell est disponible
where powershell >nul 2>nul
if %errorlevel% neq 0 (
    echo ERREUR: PowerShell n'est pas installe ou pas dans le PATH
    pause
    exit /b 1
)

REM Exécuter le script PowerShell avec politique d'exécution temporaire
powershell -ExecutionPolicy Bypass -File "%~dp0setup.ps1"

if %errorlevel% equ 0 (
    echo.
    echo Configuration terminee avec succes!
) else (
    echo.
    echo Une erreur s'est produite lors de la configuration.
    echo Consultez les messages ci-dessus pour plus de details.
)

echo.
pause
