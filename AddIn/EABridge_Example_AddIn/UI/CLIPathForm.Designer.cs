namespace EABridge_Example_AddIn.UI
{
    partial class CLIPathForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(CLIPathForm));
            this.cliPathTextBox = new System.Windows.Forms.TextBox();
            this.browseButton = new System.Windows.Forms.Button();
            this.okButton = new System.Windows.Forms.Button();
            this.cancelButton = new System.Windows.Forms.Button();
            this.label = new System.Windows.Forms.Label();
            this.pathNotValidLabel = new System.Windows.Forms.Label();
            this.chooseFolderDialog = new System.Windows.Forms.FolderBrowserDialog();
            this.SuspendLayout();
            // 
            // cliPathTextBox
            // 
            this.cliPathTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.cliPathTextBox.Location = new System.Drawing.Point(12, 38);
            this.cliPathTextBox.Name = "cliPathTextBox";
            this.cliPathTextBox.Size = new System.Drawing.Size(635, 20);
            this.cliPathTextBox.TabIndex = 0;
            this.cliPathTextBox.TextChanged += new System.EventHandler(this.cliPathTextBox_TextChanged);
            // 
            // browseButton
            // 
            this.browseButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.browseButton.Location = new System.Drawing.Point(664, 36);
            this.browseButton.Name = "browseButton";
            this.browseButton.Size = new System.Drawing.Size(108, 23);
            this.browseButton.TabIndex = 1;
            this.browseButton.Text = "Browse";
            this.browseButton.UseVisualStyleBackColor = true;
            this.browseButton.Click += new System.EventHandler(this.browseButton_Click);
            // 
            // okButton
            // 
            this.okButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.okButton.Location = new System.Drawing.Point(550, 117);
            this.okButton.Name = "okButton";
            this.okButton.Size = new System.Drawing.Size(108, 23);
            this.okButton.TabIndex = 2;
            this.okButton.Text = "OK";
            this.okButton.UseVisualStyleBackColor = true;
            this.okButton.Click += new System.EventHandler(this.okButton_Click);
            // 
            // cancelButton
            // 
            this.cancelButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.cancelButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.cancelButton.Location = new System.Drawing.Point(664, 117);
            this.cancelButton.Name = "cancelButton";
            this.cancelButton.Size = new System.Drawing.Size(108, 23);
            this.cancelButton.TabIndex = 3;
            this.cancelButton.Text = "Cancel";
            this.cancelButton.UseVisualStyleBackColor = true;
            // 
            // label
            // 
            this.label.AutoSize = true;
            this.label.Location = new System.Drawing.Point(12, 13);
            this.label.Name = "label";
            this.label.Size = new System.Drawing.Size(493, 13);
            this.label.TabIndex = 4;
            this.label.Text = "Select the path to the YAKINDU EA-Bridge CLI Application which executes the actua" +
    "l model validation.";
            // 
            // pathNotValidLabel
            // 
            this.pathNotValidLabel.AutoSize = true;
            this.pathNotValidLabel.ForeColor = System.Drawing.Color.Red;
            this.pathNotValidLabel.Location = new System.Drawing.Point(12, 72);
            this.pathNotValidLabel.Name = "pathNotValidLabel";
            this.pathNotValidLabel.Size = new System.Drawing.Size(405, 13);
            this.pathNotValidLabel.TabIndex = 5;
            this.pathNotValidLabel.Text = "This path does not contain the YAKINDU EA-Bridge CLI. Please choose a valid path";
            // 
            // CLIPathForm
            // 
            this.AcceptButton = this.okButton;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.AutoSize = true;
            this.CancelButton = this.cancelButton;
            this.ClientSize = new System.Drawing.Size(784, 161);
            this.Controls.Add(this.pathNotValidLabel);
            this.Controls.Add(this.label);
            this.Controls.Add(this.cancelButton);
            this.Controls.Add(this.okButton);
            this.Controls.Add(this.browseButton);
            this.Controls.Add(this.cliPathTextBox);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.MaximumSize = new System.Drawing.Size(800, 200);
            this.MinimizeBox = false;
            this.MinimumSize = new System.Drawing.Size(800, 200);
            this.Name = "CLIPathForm";
            this.Text = "Select Path to YAKINDU EA-Bridge CLI Application";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox cliPathTextBox;
        private System.Windows.Forms.Button browseButton;
        private System.Windows.Forms.Button okButton;
        private System.Windows.Forms.Button cancelButton;
        private System.Windows.Forms.Label label;
        private System.Windows.Forms.Label pathNotValidLabel;
        private System.Windows.Forms.FolderBrowserDialog chooseFolderDialog;
    }
}