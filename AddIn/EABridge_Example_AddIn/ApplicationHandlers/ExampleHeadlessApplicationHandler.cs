using EABridge_Example_AddIn.Utils;
using System;
using System.Diagnostics;
using System.IO;
using System.Threading;

namespace EABridge_Example_AddIn.ApplicationHandlers
{
    public class ExampleHeadlessApplicationHandler
    {

        public event OperationCompleteHandler OperationComplete;

        public delegate void OperationCompleteHandler(ExampleHeadlessApplicationHandler sender, ExternApplicationEventArgs e);

        private Process p;

        private Thread thread = null;


        public bool IsThreadRunning()
        {
            return thread != null;
        }

        public void StartExternGenerationOperationAsync(string guid, string modelPath, string outputPath, string language)
        {
            string[] arguments = ComputeGenerationArguments(guid, modelPath, outputPath, language);
            StartExternApplicationAsync(arguments, null);
        }

        private string[] ComputeGenerationArguments(string guid, string modelPath, string outputPath, string language)
        {
            string[] arguments = new string[6];

            arguments[0] = "codegen";
            arguments[1] = @"""" + language + @"""";       // target language
            arguments[2] = @"""" + modelPath + @"""";      // input model path
            arguments[3] = @"""" + outputPath + @"""";     // target folder
            arguments[4] = @"""" + guid + @"""";           // EA element GUID
            arguments[5] = "-v";                           // verbose flag

            string executablePath = HeadlessApplicationUtils.FindExecutable();

            if (!File.Exists(executablePath))
            {
                Debug.WriteLine("Error: " + executablePath + " not found.");
                throw new FileNotFoundException("Error: " + executablePath + " not found.");
            }

            return arguments;
        }


        public void StartExternValidationOperationAsync(string guid, string path)
        {
            string reportFile = ExampleHeadlessApplicationHandler.GetReportFilePath();
            string[] arguments = ComputeArguments(guid, path, reportFile, false);
            StartExternApplicationAsync(arguments, reportFile);
        }

        private string[] ComputeArguments(string guid, string path, string targePath, bool isVerbose)
        {
            string[] arguments = new string[5];

            arguments[0] = "validate";

            arguments[1] = @"""" + path + @"""";
            arguments[2] = @"""" + targePath + @"""";


            if (guid.Length > 0)
            {
                arguments[3] = @"""" + guid + @"""";
            }
            else
            {
                arguments[3] = "";
            }
            arguments[4] = "-v";

            string executablePath = HeadlessApplicationUtils.FindExecutable();

            if (!File.Exists(executablePath))
            {
                Debug.WriteLine("Error: " +  executablePath + " not found.");
                throw new FileNotFoundException("Error: " + executablePath + " not found.");
            }
            return arguments;
        }

        private void StartExternApplicationAsync(string[] arguments,
                                                 string reportFile,
                                                 DataReceivedEventHandler outputActionHandler = null)
        {
            string args = string.Join(" ", arguments);
            args += HeadlessApplicationUtils.GetExecuableInIArguments();
            thread = new Thread(() => StartExternApplication(args, reportFile, outputActionHandler));
            thread.Start();
        }

        private void StartExternApplication(string arguments,
                                            string reportFile,
                                            DataReceivedEventHandler outputActionHandler = null)
        {
            p = new Process();

            try
            {
                Debug.WriteLine("Starting headless application: " + arguments);
                p.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
                p.EnableRaisingEvents = true;
                if (outputActionHandler != null)
                {
                    p.OutputDataReceived -= outputActionHandler;
                    p.ErrorDataReceived -= outputActionHandler;
                    p.OutputDataReceived += outputActionHandler;
                    p.ErrorDataReceived += outputActionHandler;
                }
                p.StartInfo.UseShellExecute = false;
                p.StartInfo.CreateNoWindow = true;
                p.StartInfo.RedirectStandardOutput = true;
                p.StartInfo.RedirectStandardError = false;
                p.StartInfo.FileName = HeadlessApplicationUtils.FindExecutable();
                p.StartInfo.Arguments = arguments;
                DateTime startTime = DateTime.Now;
                p.Start();
                string standardOutput = "";
                if (outputActionHandler == null)
                {
                    // read cli output and save it in a temporary log
                    standardOutput = p.StandardOutput.ReadToEnd();
                } else
                {
                    p.BeginOutputReadLine();
                }

                p.WaitForExit();
                
                DateTime endTime = DateTime.Now;


                

                ExternApplicationEventArgs eventArgs = new ExternApplicationEventArgs();
                if(reportFile != null)
                {
                    eventArgs.ReportFile = reportFile;
                }
                eventArgs.ReturnCode = p.ExitCode;
                eventArgs.Duration = DateTimeUtils.GetTimeDifferenceInMilisec(startTime, endTime);

                if (!string.IsNullOrEmpty(standardOutput))
                {
                    try
                    {
                        string logFilePath = GetReportFilePath();
                        var sw = File.AppendText(logFilePath);
                        sw.Write(standardOutput);
                        sw.Close();
                        eventArgs.LogFile = logFilePath;
                    }
                    catch (Exception)
                    {
                        // do nothing
                    }
                }

                if (this != null)
                {
                    OperationComplete(this, eventArgs);
                    thread = null;
                }
            }
            catch (ThreadAbortException)
            {
                Debug.WriteLine("Validation aborted.");
            }
            finally
            {
                p = null;
            }
        }
        
        public void KillProcess()
        {
            try
            {
                if (thread != null)
                {
                    if (p != null)
                    {

                        p.Kill();
                    }
                    if (thread.IsAlive)
                    {
                        thread.Abort();
                    }
                }
                thread = null;
            }
            catch (Exception)
            {
                // ignored
            }
        }

        public static string GetReportFilePath()
        {
            return Path.Combine(Path.GetTempPath(), Path.GetRandomFileName());
        }
    }


    public class ExternApplicationEventArgs : EventArgs
    {
        public string ReportFile { get; set; }

        public int ReturnCode { get; set; }

        public long Duration { get; set; }
        
        public string LogFile { get; set; }
    }

}
