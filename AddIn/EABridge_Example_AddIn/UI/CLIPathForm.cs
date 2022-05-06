using EABridge_Example_AddIn.ApplicationHandlers;
using EABridge_Example_AddIn.Utils;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace EABridge_Example_AddIn.UI
{
    public partial class CLIPathForm : Form
    {
        private string CliPath = "";
        private ValidationConfiguration validationConfiguration;
        public CLIPathForm()
        {
            InitializeComponent();
            validationConfiguration = FileUtils.ReadValidationConfigurationFile();
            if (validationConfiguration != null)
            {
                cliPathTextBox.Text = validationConfiguration.ValidationCLIPath;
                CliPath = validationConfiguration.ValidationCLIPath;
            }
            ValidateContent();
        }

        private void ValidateContent()
        {
            if (HeadlessApplicationUtils.IsHeadlessApplicationDirectory(CliPath))
            {
                okButton.Enabled = true;
                pathNotValidLabel.Visible = false;
            } else
            {
                okButton.Enabled = false;
                pathNotValidLabel.Visible = true;
            }
        }

        private void browseButton_Click(object sender, EventArgs e)
        {
            if (DialogResult.OK == chooseFolderDialog.ShowDialog())
            {
                CliPath = chooseFolderDialog.SelectedPath;
                cliPathTextBox.Text = chooseFolderDialog.SelectedPath;
            }
            ValidateContent(); 
        }

        private void cliPathTextBox_TextChanged(object sender, EventArgs e)
        {
            CliPath = cliPathTextBox.Text;
            ValidateContent();  
        }

        private void okButton_Click(object sender, EventArgs e)
        {
            var configuration = new ValidationConfiguration
            {
                ValidationCLIPath = CliPath
            };
            FileUtils.UpadteOrCreateValidationConfiguration(configuration);
            this.DialogResult = DialogResult.OK;
            this.Close();
        }
    }
}
