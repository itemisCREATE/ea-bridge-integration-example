using ItemisIntegrateEA_Example_AddIn.Utils;
using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;

namespace ItemisIntegrateEA_Example_AddIn.Utils
{

    public static class HeadlessApplicationUtils
    {

        public const string INI_FILE_NAME = "IntegrateEAExampleCLI.ini";
        public const string EXECUTABLE_FILE_NAME = "IntegrateEAExampleCLI.exe";
        public const string CONSOLE_EXECUTABLE_FILE_NAME = "IntegrateEAExampleCLIc.exe";

        public static string GetExecuableInIArguments()
        {
            string iniFileLocation = Path.Combine(FindHeadlessApplication(), INI_FILE_NAME);
            return " --launcher.ini \"" + iniFileLocation + "\"";


        }

         static string FindHeadlessApplication()
        {
            var config = FileUtils.ReadValidationConfigurationFile();
            if (config == null)
            {
                return "";
            }
            string storedPath = config.ValidationCLIPath;
            if (!string.IsNullOrEmpty(storedPath) && IsHeadlessApplicationDirectory(storedPath))
            {
                return storedPath;
            } else
            {
                return "";
            }
        }



        public static string FindExecutable()
        {
            string path = Path.Combine(FindHeadlessApplication(), CONSOLE_EXECUTABLE_FILE_NAME);
            if (File.Exists(path))
            {
                return path;
            }
            else
            {
                return Path.Combine(FindHeadlessApplication(), EXECUTABLE_FILE_NAME);
            }
        }

        public static bool IsHeadlessApplicationDirectory(string directory)
        {
            if (Directory.Exists(directory))
            {
                string exeFile = Path.Combine(directory, EXECUTABLE_FILE_NAME);
                return File.Exists(exeFile);
            }
            else
            {
                return false;
            }
        }
    }
}
