echo ####################################
echo ### Administrator Rights Needed! ###
echo ####################################

set dir=%~dp0
set assembly1=bin\Debug\EABridge_Example_AddIn.dll


"C:\Windows\Microsoft.NET\Framework\v4.0.30319\RegAsm.exe" "%dir%%assembly1%" /codebase

reg ADD "HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\Sparx Systems\EAAddins\Example Validation AddIn Dev" /f /d "EABridge_Example_AddIn.ExampleValidationAddIn"


pause