using Newtonsoft.Json;
using System;
using System.IO;

namespace ItemisIntegrateEA_Example_AddIn.Utils
{
    public static class FileUtils
    {
        private static string ConfigurationFilePath = GetConfigurationFilePath(); 

        public static bool IsPathValid(string path)
        {
            try
            {
                if (path == null ||
                    !Path.IsPathRooted(path))
                {
                    return false;
                }
                Path.GetFullPath(path);
            }
            catch (Exception)
            {
                return false;

            }

            return true;
        }

        public static bool UpadteOrCreateValidationConfiguration(ValidationConfiguration configuration)
        {
            try
            {
                JsonSerializer serializer = new JsonSerializer();
                serializer.NullValueHandling = NullValueHandling.Ignore;
                StreamWriter sw;
                if (File.Exists(ConfigurationFilePath))
                {
                    sw = new StreamWriter(ConfigurationFilePath);
                }
                else
                {
                    sw = File.CreateText(ConfigurationFilePath);
                }
                using (JsonWriter writer = new JsonTextWriter(sw))
                {
                    serializer.Serialize(writer, configuration);
                    return true;
                }
            }
            catch 
            {
                return false;
            }
        }

        public static ValidationConfiguration ReadValidationConfigurationFile()
        {
            try
            {
                var config = JsonConvert.DeserializeObject<ValidationConfiguration>(File.ReadAllText(ConfigurationFilePath));
                return config;
            }
            catch
            {
                return null;
            }
        }

        private static string GetUserFolder()
        {
            return Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        }

        private static string GetConfigurationFilePath()
        {

            string userPath = GetUserFolder();
            if (!Directory.Exists(Path.Combine(userPath, ".example-cli")))
            {
                // create the config directory if it doesn't exist
                Directory.CreateDirectory(Path.Combine(userPath, ".example-cli"));
            }
            return Path.Combine(GetUserFolder(), ".example-cli", "EA_AddIn_CLI_config.json");

        }

    }
}
