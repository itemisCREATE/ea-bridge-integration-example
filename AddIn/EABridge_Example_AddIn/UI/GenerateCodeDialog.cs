using EA;
using EABridge_Example_AddIn.ApplicationHandlers;
using System;
using System.IO;
using System.Windows.Forms;

namespace EABridge_Example_AddIn.UI
{
    public partial class GenerateCodeDialog : UserControl
    {
        private ExampleHeadlessApplicationHandler _handler;

        public GenerateCodeDialog()
        {
            InitializeComponent();
            InitializeUi();
        }

        private void InitializeUi()
        {
            // Populate language dropdown
            cmbLanguage.Items.AddRange(new string[] { "C", "C++", "Csharp", "Java", "Python" });
            cmbLanguage.SelectedIndex = 0;

            lblStatus.Text = string.Empty;
        }
        public void SetSelectedStateMachineName(string name)
        {
            lblStateMachine.Text = $"StateMachine: {name}";
        }

        private void BtnBrowse_Click(object sender, EventArgs e)
        {
            using (var dialog = new FolderBrowserDialog())
            {
                dialog.Description = "Select Output Folder";
                dialog.ShowNewFolderButton = true;

                if (dialog.ShowDialog() == DialogResult.OK)
                {
                    txtOutputPath.Text = dialog.SelectedPath;
                }
            }
        }

        private void BtnGenerate_Click(object sender, EventArgs e)
        {
            lblStatus.Text = string.Empty;

            string language = cmbLanguage?.SelectedItem?.ToString() ?? "C";
            string outputPath = txtOutputPath?.Text ?? "";

            if (string.IsNullOrWhiteSpace(outputPath) || !Directory.Exists(outputPath))
            {
                MessageBox.Show("Please select a valid output path.", "Invalid Path",
                    MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            if (string.IsNullOrEmpty(SelectedElementGuid) || string.IsNullOrEmpty(EARepositoryPath))
            {
                MessageBox.Show("EA element or repository path not set.", "Error",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            _handler = new ExampleHeadlessApplicationHandler();
            _handler.OperationComplete += Handler_OperationComplete;

            try
            {
                lblStatus.Text = "Generating code, please wait...";
                _handler.StartExternGenerationOperationAsync(
                    SelectedElementGuid,
                    EARepositoryPath,
                    outputPath,
                    language
                );
            }
            catch (Exception ex)
            {
                lblStatus.Text = "";
                MessageBox.Show("Error starting code generation: " + ex.Message, "Error",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void Handler_OperationComplete(ExampleHeadlessApplicationHandler sender, ExternApplicationEventArgs e)
        {
            if (InvokeRequired)
            {
                Invoke(new Action(() => Handler_OperationComplete(sender, e)));
                return;
            }

            lblStatus.Text = "";

            string message = e.ReturnCode == 0
                ? $"Code generation completed successfully!\nReport: {e.ReportFile}"
                : $"Code generation failed (exit code {e.ReturnCode})\nCheck log: {e.LogFile}";

            MessageBox.Show(message, "Code Generation Finished",
                MessageBoxButtons.OK, MessageBoxIcon.Information);
        }

        // These are set externally before displaying the dialog
        public string SelectedElementGuid { get; set; }
        public string EARepositoryPath { get; set; }
    }
}
