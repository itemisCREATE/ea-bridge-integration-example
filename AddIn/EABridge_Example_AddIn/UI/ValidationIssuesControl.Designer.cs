namespace EABridge_Example_AddIn.UI
{
    partial class ValidationIssuesControl
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

        #region Component Designer generated code

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.ValidationIssuesDataGridView = new System.Windows.Forms.DataGridView();
            this.ElementNameColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.ElementTypeColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.IssueCategoryColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.SeverityColumn = new System.Windows.Forms.DataGridViewImageColumn();
            this.MsgColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.ValidationInProgressLabel = new System.Windows.Forms.Label();
            this.AbortValidationButton = new System.Windows.Forms.Button();
            ((System.ComponentModel.ISupportInitialize)(this.ValidationIssuesDataGridView)).BeginInit();
            this.SuspendLayout();
            // 
            // ValidationIssuesDataGridView
            // 
            this.ValidationIssuesDataGridView.AllowUserToAddRows = false;
            this.ValidationIssuesDataGridView.AllowUserToDeleteRows = false;
            this.ValidationIssuesDataGridView.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.Fill;
            this.ValidationIssuesDataGridView.AutoSizeRowsMode = System.Windows.Forms.DataGridViewAutoSizeRowsMode.AllCellsExceptHeaders;
            this.ValidationIssuesDataGridView.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.ValidationIssuesDataGridView.Columns.AddRange(new System.Windows.Forms.DataGridViewColumn[] {
            this.ElementNameColumn,
            this.ElementTypeColumn,
            this.IssueCategoryColumn,
            this.SeverityColumn,
            this.MsgColumn});
            this.ValidationIssuesDataGridView.Dock = System.Windows.Forms.DockStyle.Fill;
            this.ValidationIssuesDataGridView.Location = new System.Drawing.Point(0, 0);
            this.ValidationIssuesDataGridView.Name = "ValidationIssuesDataGridView";
            this.ValidationIssuesDataGridView.ReadOnly = true;
            this.ValidationIssuesDataGridView.RowTemplate.Resizable = System.Windows.Forms.DataGridViewTriState.False;
            this.ValidationIssuesDataGridView.Size = new System.Drawing.Size(600, 400);
            this.ValidationIssuesDataGridView.TabIndex = 0;
            this.ValidationIssuesDataGridView.CellDoubleClick += new System.Windows.Forms.DataGridViewCellEventHandler(this.ValidationIssuesDataGridView_CellClick);
            this.ValidationIssuesDataGridView.SortCompare += new System.Windows.Forms.DataGridViewSortCompareEventHandler(this.ValidationIssuesDataGridView_SortCompare);
            // 
            // ElementNameColumn
            // 
            this.ElementNameColumn.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.AllCells;
            this.ElementNameColumn.HeaderText = "Element name";
            this.ElementNameColumn.Name = "ElementNameColumn";
            this.ElementNameColumn.ReadOnly = true;
            this.ElementNameColumn.Width = 91;
            // 
            // ElementTypeColumn
            // 
            this.ElementTypeColumn.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.ColumnHeader;
            this.ElementTypeColumn.HeaderText = "Element type";
            this.ElementTypeColumn.Name = "ElementTypeColumn";
            this.ElementTypeColumn.ReadOnly = true;
            this.ElementTypeColumn.Width = 86;
            // 
            // IssueCategoryColumn
            // 
            this.IssueCategoryColumn.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.ColumnHeader;
            this.IssueCategoryColumn.HeaderText = "Issue Category";
            this.IssueCategoryColumn.Name = "IssueCategoryColumn";
            this.IssueCategoryColumn.ReadOnly = true;
            this.IssueCategoryColumn.Width = 94;
            // 
            // SeverityColumn
            // 
            this.SeverityColumn.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.ColumnHeader;
            this.SeverityColumn.HeaderText = "Severity";
            this.SeverityColumn.Name = "SeverityColumn";
            this.SeverityColumn.ReadOnly = true;
            this.SeverityColumn.Resizable = System.Windows.Forms.DataGridViewTriState.True;
            this.SeverityColumn.SortMode = System.Windows.Forms.DataGridViewColumnSortMode.Automatic;
            this.SeverityColumn.Width = 70;
            // 
            // MsgColumn
            // 
            this.MsgColumn.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.Fill;
            this.MsgColumn.HeaderText = "Validation message";
            this.MsgColumn.Name = "MsgColumn";
            this.MsgColumn.ReadOnly = true;
            // 
            // ValidationInProgressLabel
            // 
            this.ValidationInProgressLabel.AutoSize = true;
            this.ValidationInProgressLabel.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.ValidationInProgressLabel.ForeColor = System.Drawing.SystemColors.ActiveCaptionText;
            this.ValidationInProgressLabel.Location = new System.Drawing.Point(3, 0);
            this.ValidationInProgressLabel.Name = "ValidationInProgressLabel";
            this.ValidationInProgressLabel.Size = new System.Drawing.Size(179, 39);
            this.ValidationInProgressLabel.TabIndex = 1;
            this.ValidationInProgressLabel.Text = "Validation of [Name] in progress... \r\n\r\nValidation results will be shown here.\r\n";
            this.ValidationInProgressLabel.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // abortValidationButton
            // 
            this.AbortValidationButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.AbortValidationButton.Location = new System.Drawing.Point(522, 3);
            this.AbortValidationButton.Name = "abortValidationButton";
            this.AbortValidationButton.Size = new System.Drawing.Size(75, 23);
            this.AbortValidationButton.TabIndex = 2;
            this.AbortValidationButton.Text = "Abort";
            this.AbortValidationButton.UseVisualStyleBackColor = true;
            this.AbortValidationButton.Click += new System.EventHandler(this.AbortValidationButton_Click);
            // 
            // ValidationIssuesControl
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.AbortValidationButton);
            this.Controls.Add(this.ValidationInProgressLabel);
            this.Controls.Add(this.ValidationIssuesDataGridView);
            this.Name = "ValidationIssuesControl";
            this.Size = new System.Drawing.Size(600, 400);
            ((System.ComponentModel.ISupportInitialize)(this.ValidationIssuesDataGridView)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.DataGridView ValidationIssuesDataGridView;
        private System.Windows.Forms.DataGridViewTextBoxColumn ElementNameColumn;
        private System.Windows.Forms.DataGridViewTextBoxColumn ElementTypeColumn;
        private System.Windows.Forms.DataGridViewTextBoxColumn IssueCategoryColumn;
        private System.Windows.Forms.DataGridViewImageColumn SeverityColumn;
        private System.Windows.Forms.DataGridViewTextBoxColumn MsgColumn;
        private System.Windows.Forms.Label ValidationInProgressLabel;
        private System.Windows.Forms.Button AbortValidationButton;
    }
}
