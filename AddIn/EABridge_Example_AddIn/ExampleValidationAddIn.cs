using System;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using System.IO;
using System.Threading;
using System.Windows.Forms;
using File = System.IO.File;
using EA;
using EABridge_Example_AddIn.UI;
using EABridge_Example_AddIn.Utils;
using EABridge_Example_AddIn.Reports;
using Newtonsoft.Json;
using EABridge_Example_AddIn.ApplicationHandlers;

namespace EABridge_Example_AddIn
{
    [Guid("0212EA66-C960-473A-B7FB-0BB36729DF25")]
    [ComVisible(true)]
    public class ExampleValidationAddIn
    {
        const string AddInHeader = "-&YAKINDU EA-Bridge Integration";
        const string ValidationMenuEntry = "&Model Validation";
        const string ConfigurationMenuEntry = "&Configure CLI Path.. ";
        public const string RESULTS_TAB = "Results";
        public const string VALIDATION_TAB = "Model Validation Results";

        private readonly ExampleHeadlessApplicationHandler applicationHandler = new ExampleHeadlessApplicationHandler();

        public delegate void ShowValidationViewHandler(System.Runtime.Remoting.Contexts.Context sender, ExternRefreshValidationViewArgs e);
        public event ShowValidationViewHandler ShowValidationView;

        private Repository repository;

        private SerializableValidationResult ValidationReport;

        private ValidationIssuesControl validationIssuesControl;

        public ExampleValidationAddIn()
        {
            ShowValidationView += ShowValidationViewMethod;
            applicationHandler.OperationComplete += OperationCompleteMethod;
        }

        public String EA_Connect(Repository Repository)
        {
            this.repository = Repository;
            return "EA repository connected";
        }

        public object EA_GetMenuItems(Repository Repository, string Location, string MenuName)
        {
            if (MenuName == "")
            {
                    return AddInHeader;
            }
            if ("MainMenu" == Location)
            {
                return new string[] {ValidationMenuEntry, ConfigurationMenuEntry};
            } else
            {
                return new string[] { ValidationMenuEntry};
            }

        }

        bool IsProjectOpen(Repository Repository)
        {
            try
            {
                Collection c = Repository.Models;
                return true;
            }
            catch
            {
                return false;
            }
        }

        public void EA_GetMenuState(Repository Repository, string Location, string MenuName, string ItemName, ref bool IsEnabled, ref bool IsChecked)
        {
            if (IsProjectOpen(Repository))
            {
                if ("MainMenu" == Location || EARepositoryUtils.IsCLIOperationForContextObjectAllowed(Repository.GetContextObject()))
                {
                    IsChecked = false;
                    switch (ItemName)
                    {
                        case ValidationMenuEntry:
                            IsEnabled = true;
                            break;
                        default:
                            IsEnabled = true;
                            break;
                    }
                } else
                {
                    // not a valid choice for operations
                    IsEnabled = false;
                }
            }
            else
            {
                // If no open project, disable all menu options
                IsEnabled = false;
            }
        }

        public void EA_MenuClick(Repository Repository, string Location, string MenuName, string ItemName)
        {
            string operationGuid = "";
            bool isValidationOperation = true;
            this.repository = Repository;
            if (applicationHandler.IsThreadRunning())
            {
                if (DialogResult.Yes == MessageBox.Show(Win32Window.GetMainWindowHandle(), "The CLI application is still running. Do you want to abort the current operation ?" + Environment.NewLine + Environment.NewLine + "All changes will be lost.", "Abort current operation ?", MessageBoxButtons.YesNo, MessageBoxIcon.Warning, MessageBoxDefaultButton.Button2))
                {
                    this.applicationHandler.KillProcess();
                } else
                {
                    return;
                }
            }
            if (ValidationMenuEntry != ItemName)
            {
                isValidationOperation = false;
            } else
            {
                isValidationOperation = true;
            } 
            if ("MainMenu" != Location)
            {
                // get the context element
                object cxt = repository.GetContextObject();
                operationGuid = EARepositoryUtils.GetGuidOfPackageOrElementOrDiagram(cxt);
            }
            try
            {
                PerformOperation(operationGuid, isValidationOperation);
            } catch (FileNotFoundException)
            {
                MessageBox.Show(Win32Window.GetMainWindowHandle(),
                            string.Format("The CLI application could not be found at the expected location '{0}'. ", HeadlessApplicationUtils.FindExecutable()),
                            "CLI application not found",
                            MessageBoxButtons.OK,
                            MessageBoxIcon.Error);
            }
        }


        private void PerformOperation(string contextGuid, bool isValidation)
        {
            try
            {
                if (!isValidation)
                {
                    var cliPathDialog = new CLIPathForm();
                    cliPathDialog.ShowDialog();
                }
                else 
                {
                    repository.CreateOutputTab(RESULTS_TAB);
                    repository.ClearOutput(RESULTS_TAB);
                    string path = repository.ConnectionString;
                    repository.WriteOutput(RESULTS_TAB, "Starting Validation of " + path, 0);
                    applicationHandler.StartExternValidationOperationAsync(contextGuid, path);
                    if (validationIssuesControl != null)
                    {
                        // TODO: lock on this instance of the add in 
                        // because validationIssuesControl has a weak identity
                        // c.f. https://rules.sonarsource.com/csharp/RSPEC-3998
                        lock (validationIssuesControl)
                        {
                            validationIssuesControl.UpdateValidationViewState(contextGuid, applicationHandler);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                if (e is FileNotFoundException)
                {
                    repository.WriteOutput(RESULTS_TAB, e.Message, -1);
                    MessageBox.Show(Win32Window.GetMainWindowHandle(),
                                    "Please choose a valid path to the CLI validator first! (from the ribbon menu)",
                                    "Unexpected Error",
                                    MessageBoxButtons.OK,
                                    MessageBoxIcon.Error);
                }
                else
                {
                    repository.WriteOutput(RESULTS_TAB, e.Message, -1);
                    MessageBox.Show(Win32Window.GetMainWindowHandle(),
                                    string.Format("Unexpected Error:\r\n {0}\r\n {1} ", e.Message, e.StackTrace),
                                    "Unexpected Error",
                                    MessageBoxButtons.OK,
                                    MessageBoxIcon.Error);
                }
            }
            finally
            {
                repository.EnsureOutputVisible(RESULTS_TAB);
            }
        }

        private void OpenValidationIssue()
        {
            try
            {
                repository.HideAddinWindow();
                if (this.validationIssuesControl == null)
                {
                    this.validationIssuesControl = this.repository.AddWindow(VALIDATION_TAB, typeof(ValidationIssuesControl).FullName) as ValidationIssuesControl;
                }
                this.validationIssuesControl.UpdateValidationIssues(this.ValidationReport, repository);
                this.repository.ShowAddinWindow(validationIssuesControl);
            }
            catch (Exception e)
            {
                MessageBox.Show(Win32Window.GetMainWindowHandle(),
                        string.Format("The validation issues view could not be shown. \r\n{0} \r\n{1} ", e.Message, e.StackTrace),
                        "Validation view could not be shown",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error);
            }
        }


        private void OperationCompleteMethod(ExampleHeadlessApplicationHandler sender, ExternApplicationEventArgs e)
        {
            repository.CreateOutputTab(RESULTS_TAB);
            string logFilePath = "";
            try
            {
                
                string reportFilePath = e.ReportFile;
                logFilePath = e.LogFile;
                if (e.ReturnCode == 0 && !File.Exists(reportFilePath))
                {
                    repository.WriteOutput(RESULTS_TAB, "Validation finished in " + DateTimeUtils.GetHumanReadableDuration(e.Duration) + ", 0 issue(s)", -2);
                    repository.EnsureOutputVisible(RESULTS_TAB);
                    return;
                }
                if (e.ReturnCode != 0 && !File.Exists(reportFilePath))
                {
                    throw new Exception("Error: Validation incomplete, view log for more information");
                }
                this.ValidationReport = JsonConvert.DeserializeObject<SerializableValidationResult>(File.ReadAllText(reportFilePath));
                repository.WriteOutput(RESULTS_TAB, "========== Validation finished in " + DateTimeUtils.GetHumanReadableDuration(e.Duration) + ", " + this.ValidationReport.Count().ToString() + " issue(s) ========== ", -2);
                File.Delete(reportFilePath);
                repository.EnsureOutputVisible(RESULTS_TAB);
                ExternRefreshValidationViewArgs validationViewEventArgs = new ExternRefreshValidationViewArgs();
                ShowValidationView(Thread.CurrentContext, validationViewEventArgs);
            }
            catch (Exception ex)
            {
                if (!string.IsNullOrEmpty(logFilePath))
                {
                repository.WriteOutput(RESULTS_TAB, "Validation failed. Check the log file for more information", -1);
                repository.WriteOutput(RESULTS_TAB, logFilePath , -1);
                }
            }
        }


        private void ShowValidationViewMethod(System.Runtime.Remoting.Contexts.Context sender, ExternRefreshValidationViewArgs e){
            OpenValidationIssue();
        }

        public void EA_Disconnect()
        {
            GC.WaitForPendingFinalizers();
        }

        public class ExternRefreshValidationViewArgs : EventArgs
        {
            public IList<ValidationIssue> issues { get; set; }
        }
    }
}
