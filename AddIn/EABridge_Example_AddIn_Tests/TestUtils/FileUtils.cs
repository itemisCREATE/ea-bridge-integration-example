using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace EABridge_Example_AddIn_Tests.TestUtils
{
    public static class FileUtils
    {

        public static string BackupFile(string testModelFilePath)
        {
            string tmpFolder = Path.Combine(Path.GetTempPath());
            string tempModelFilePath = Path.Combine(tmpFolder, string.Format("Copy-of-{0}", Path.GetFileName(testModelFilePath)));
            return BackupFile(testModelFilePath, tempModelFilePath);
        }

        public static string BackupFile(string testModelFilePath, string targetPath)
        {
            Assert.IsFalse(string.IsNullOrEmpty(testModelFilePath), "File to backup not defined.");
            if (testModelFilePath.StartsWith(Path.DirectorySeparatorChar + ""))
            {
                testModelFilePath = testModelFilePath.Substring(1);
            }
            string assemblyFolder = GetAssemblyFolder();
            string sourcePath = Path.Combine(assemblyFolder, testModelFilePath);
            try
            {
                Assert.IsTrue(File.Exists(sourcePath), "File '" + sourcePath + "' does not exist.");
                File.Copy(sourcePath, targetPath, true);
            }
            catch (Exception e)
            {
                Assert.Fail(e.Message);
            }
            return targetPath;
        }

        public static string GetAssemblyFolder()
        {
            var filePath = new Uri(Assembly.GetExecutingAssembly().CodeBase).LocalPath;
            var folder = Path.GetDirectoryName(filePath);
            return folder;
        }


    }
}
