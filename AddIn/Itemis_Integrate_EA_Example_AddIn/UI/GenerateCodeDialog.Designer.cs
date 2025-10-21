using System.Windows.Forms;

namespace ItemisIntegrateEA_Example_AddIn.UI
{
    partial class GenerateCodeDialog
    {
        private System.ComponentModel.IContainer components = null;
        private System.Windows.Forms.Label lblStateMachine;
        private System.Windows.Forms.ComboBox cmbLanguage;
        private System.Windows.Forms.Label lblLanguage;
        private System.Windows.Forms.Label lblOutputPath;
        private System.Windows.Forms.TextBox txtOutputPath;
        private System.Windows.Forms.Button btnBrowse;
        private System.Windows.Forms.Button btnGenerate;
        private System.Windows.Forms.Label lblStatus;

        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        private void InitializeComponent()
        {
            this.lblStateMachine = new System.Windows.Forms.Label();
            this.cmbLanguage = new System.Windows.Forms.ComboBox();
            this.lblLanguage = new System.Windows.Forms.Label();
            this.lblOutputPath = new System.Windows.Forms.Label();
            this.txtOutputPath = new System.Windows.Forms.TextBox();
            this.btnBrowse = new System.Windows.Forms.Button();
            this.btnGenerate = new System.Windows.Forms.Button();
            this.lblStatus = new System.Windows.Forms.Label();
            this.codegenProgressBar = new System.Windows.Forms.ProgressBar();
            this.SuspendLayout();
            // 
            // lblStateMachine
            // 
            this.lblStateMachine.AutoSize = true;
            this.lblStateMachine.Location = new System.Drawing.Point(30, 19);
            this.lblStateMachine.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.lblStateMachine.Name = "lblStateMachine";
            this.lblStateMachine.Size = new System.Drawing.Size(150, 25);
            this.lblStateMachine.TabIndex = 0;
            this.lblStateMachine.Text = "StateMachine:";
            // 
            // cmbLanguage
            // 
            this.cmbLanguage.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cmbLanguage.Location = new System.Drawing.Point(180, 65);
            this.cmbLanguage.Margin = new System.Windows.Forms.Padding(4);
            this.cmbLanguage.Name = "cmbLanguage";
            this.cmbLanguage.Size = new System.Drawing.Size(298, 33);
            this.cmbLanguage.TabIndex = 2;
            // 
            // lblLanguage
            // 
            this.lblLanguage.AutoSize = true;
            this.lblLanguage.Location = new System.Drawing.Point(30, 69);
            this.lblLanguage.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.lblLanguage.Name = "lblLanguage";
            this.lblLanguage.Size = new System.Drawing.Size(114, 25);
            this.lblLanguage.TabIndex = 1;
            this.lblLanguage.Text = "Language:";
            // 
            // lblOutputPath
            // 
            this.lblOutputPath.AutoSize = true;
            this.lblOutputPath.Location = new System.Drawing.Point(30, 119);
            this.lblOutputPath.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.lblOutputPath.Name = "lblOutputPath";
            this.lblOutputPath.Size = new System.Drawing.Size(130, 25);
            this.lblOutputPath.TabIndex = 3;
            this.lblOutputPath.Text = "Output path:";
            // 
            // txtOutputPath
            // 
            this.txtOutputPath.Location = new System.Drawing.Point(180, 115);
            this.txtOutputPath.Margin = new System.Windows.Forms.Padding(4);
            this.txtOutputPath.Name = "txtOutputPath";
            this.txtOutputPath.Size = new System.Drawing.Size(448, 31);
            this.txtOutputPath.TabIndex = 4;
            // 
            // btnBrowse
            // 
            this.btnBrowse.Location = new System.Drawing.Point(645, 114);
            this.btnBrowse.Margin = new System.Windows.Forms.Padding(4);
            this.btnBrowse.Name = "btnBrowse";
            this.btnBrowse.Size = new System.Drawing.Size(112, 36);
            this.btnBrowse.TabIndex = 5;
            this.btnBrowse.Text = "Browse…";
            this.btnBrowse.UseVisualStyleBackColor = true;
            this.btnBrowse.Click += new System.EventHandler(this.BtnBrowse_Click);
            // 
            // btnGenerate
            // 
            this.btnGenerate.Location = new System.Drawing.Point(30, 177);
            this.btnGenerate.Margin = new System.Windows.Forms.Padding(4);
            this.btnGenerate.Name = "btnGenerate";
            this.btnGenerate.Size = new System.Drawing.Size(150, 44);
            this.btnGenerate.TabIndex = 6;
            this.btnGenerate.Text = "Generate";
            this.btnGenerate.UseVisualStyleBackColor = true;
            this.btnGenerate.Click += new System.EventHandler(this.BtnGenerate_Click);
            // 
            // lblStatus
            // 
            this.lblStatus.AutoSize = true;
            this.lblStatus.Location = new System.Drawing.Point(200, 195);
            this.lblStatus.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.lblStatus.Name = "lblStatus";
            this.lblStatus.Size = new System.Drawing.Size(0, 25);
            this.lblStatus.TabIndex = 7;
            // 
            // codegenProgressBar
            // 
            this.codegenProgressBar.Location = new System.Drawing.Point(30, 241);
            this.codegenProgressBar.MarqueeAnimationSpeed = 30;
            this.codegenProgressBar.Name = "codegenProgressBar";
            this.codegenProgressBar.Size = new System.Drawing.Size(598, 23);
            this.codegenProgressBar.Style = System.Windows.Forms.ProgressBarStyle.Marquee;
            this.codegenProgressBar.TabIndex = 8;
            this.codegenProgressBar.Visible = false;
            // 
            // GenerateCodeDialog
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(192F, 192F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
            this.AutoSize = true;
            this.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.Controls.Add(this.codegenProgressBar);
            this.Controls.Add(this.lblStateMachine);
            this.Controls.Add(this.lblLanguage);
            this.Controls.Add(this.cmbLanguage);
            this.Controls.Add(this.lblOutputPath);
            this.Controls.Add(this.txtOutputPath);
            this.Controls.Add(this.btnBrowse);
            this.Controls.Add(this.btnGenerate);
            this.Controls.Add(this.lblStatus);
            this.Margin = new System.Windows.Forms.Padding(4);
            this.Name = "GenerateCodeDialog";
            this.Size = new System.Drawing.Size(761, 267);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        private System.Windows.Forms.ProgressBar codegenProgressBar;
    }
}
