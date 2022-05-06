using EABridge_Example_AddIn.ApplicationHandlers;
using EABridge_Example_AddIn.Reports;
using EABridge_Example_AddIn.Utils;
using EA;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Drawing;
using System.Runtime.InteropServices;
using System.Windows.Forms;

namespace EABridge_Example_AddIn.UI
{
    [Guid("E4F9A26A-5EDF-44BE-98A0-015E3D7C6E7A")]
    [ComVisible(true)]
    public partial class ValidationIssuesControl : UserControl
    {
        private Repository Repository;
        IDictionary<ValidationIssue, ValidationIssueCategory> Issues = new Dictionary<ValidationIssue, ValidationIssueCategory>();
        private const string validationInProgressText = @"Validation of [Name] in progress... ";
        private ExampleHeadlessApplicationHandler HeadlessApplicationHandler;

        public ValidationIssuesControl()
        {
            InitializeComponent();
        }

        public ValidationIssuesControl(Repository repository, SerializableValidationResult displayResult)
        {
            InitializeComponent();
            this.Issues = displayResult.GetAllValidationIssues();
            this.Repository = repository;
            ValidationInProgressLabel.Visible = false;
            AbortValidationButton.Visible = false;
            ShowValidationResults();
        }

        private void ShowValidationResults()
        {
            foreach (var item in Issues)
            {
                AddValidationIssue(Repository, item);
            }
        }

        public void UpdateValidationViewState(string guid, ExampleHeadlessApplicationHandler handler)
        {
            HeadlessApplicationHandler = handler;
            Issues.Clear();
            ValidationIssuesDataGridView.Rows.Clear();
            ValidationIssuesDataGridView.Visible = false;
            var guidName = EARepositoryUtils.GetEAObjectSimpleNameFromGuid(Repository, guid);
            string validationText = validationInProgressText.Replace("[Name]", guidName);
            ValidationInProgressLabel.Text = validationText;
            ValidationInProgressLabel.Visible = true;
            AbortValidationButton.Visible = true;
            AbortValidationButton.Enabled = true;

        }

        public void UpdateValidationIssues(SerializableValidationResult newResult, Repository repository)
        {
            ValidationInProgressLabel.Visible = false;
            AbortValidationButton.Visible = false;
            ValidationIssuesDataGridView.Visible = true;
            ValidationIssuesDataGridView.Rows.Clear();
            this.Issues.Clear();
            this.Repository = repository ?? throw new ArgumentNullException(nameof(repository));
            this.Issues = newResult.GetAllValidationIssues();
            ShowValidationResults();
        }

        public void AddValidationIssue(Repository repository, KeyValuePair<ValidationIssue, ValidationIssueCategory> issue)
        {
            DataGridViewRow row = new DataGridViewRow();
            row.Tag = issue;
            row.MinimumHeight = row.Height;
            row.CreateCells(ValidationIssuesDataGridView);
            UpdateRowCellValues(row);
            if (ValidationIssuesDataGridView.InvokeRequired)
            {
                ValidationIssuesDataGridView.Invoke(new MethodInvoker(delegate { ValidationIssuesDataGridView.Rows.Add(row); }));
            } else
            {
                ValidationIssuesDataGridView.Rows.Add(row);
            }
        }

        private void UpdateRowHeight(DataGridViewRow row)
        {
            string membersText = row.Cells[MsgColumn.Index].Value as string;
            if (membersText != "")
            {
                int padding = row.MinimumHeight - TextRenderer.MeasureText("|", ValidationIssuesDataGridView.Font).Height;
                Size size = TextRenderer.MeasureText(membersText != "" ? membersText : "|", ValidationIssuesDataGridView.Font);
                row.Height = size.Height + padding;
            }
            else
            {
                row.Height = row.MinimumHeight;
            }
        }

        private void UpdateRowCellValues(DataGridViewRow row)
        {
            KeyValuePair<ValidationIssue, ValidationIssueCategory> issue = (KeyValuePair<ValidationIssue, ValidationIssueCategory>)row.Tag;

            row.Cells[ElementNameColumn.Index].Value = EARepositoryUtils.GetEAElementName(this.Repository, issue.Key.type, issue.Key.id).Value;
            row.Cells[ElementTypeColumn.Index].Value = issue.Key.type;
            string issueSeverity = issue.Key.severity;
            switch (issueSeverity)
            {
                case "E":
                    row.Cells[SeverityColumn.Index].Value = new Bitmap(SystemIcons.Error.ToBitmap(), 16, 16);
                    break;
                case "W":
                    row.Cells[SeverityColumn.Index].Value =  new Bitmap(SystemIcons.Warning.ToBitmap(), 16, 16);
                    break;
                case "I":
                    row.Cells[SeverityColumn.Index].Value =  new Bitmap(SystemIcons.Information.ToBitmap(), 16, 16);
                    break;
                default:
                    row.Cells[SeverityColumn.Index].Value =  new Bitmap(SystemIcons.Question.ToBitmap(), 16, 16);
                    break;
            }
            row.Cells[IssueCategoryColumn.Index].Value = issue.Value;
            row.Cells[MsgColumn.Index].Value = issue.Key.msg;
            UpdateRowHeight(row);
        }

        private void ValidationIssuesDataGridView_CellClick(object sender, DataGridViewCellEventArgs e)
        {
            if (e.RowIndex >= 0 && e.RowIndex < Issues.Count)
            {
                DataGridViewRow row = ValidationIssuesDataGridView.Rows[e.RowIndex];

                if (row != null)
                {
                    KeyValuePair<ValidationIssue, ValidationIssueCategory> issue = (KeyValuePair<ValidationIssue, ValidationIssueCategory>)row.Tag;
                    NavigateToElementInProjectExplorer(issue.Key);
                }

            }

        }

        public void NavigateToElementInProjectExplorer(ValidationIssue issue)
        {
            Element element = null;
            int id = -1;
            object focus = null;
            string type = issue.type;
            int issueId = -1;
            if (!int.TryParse(issue.id, out issueId))
            {
                MessageBox.Show(Win32Window.GetMainWindowHandle(), "This validation issue is not navigable\n\n", "Reported issue is not navigable");
                return;
            }

            switch (type)
            {
                case "element":
                    element = Repository.GetElementByID(int.Parse(issue.id));
                    id = element.ElementID;
                    focus = element;
                    break;

                case "connector":
                    Connector connector = Repository.GetConnectorByID(int.Parse(issue.id));
                    if (connector != null)
                    {
                        id = connector.ClientID;
                        element = Repository.GetElementByID(id);
                    }
                    focus = element;
                    break;

                case "attribute":
                    EA.Attribute attribute = Repository.GetAttributeByID(int.Parse(issue.id));
                    if (attribute != null)
                    {
                        id = attribute.ParentID;
                        element = Repository.GetElementByID(id);
                    }
                    focus = attribute;
                    break;

                case "operation":
                    Method method = Repository.GetMethodByID(int.Parse(issue.id));
                    if (method != null)
                    {
                        id = method.ParentID;
                        element = Repository.GetElementByID(id);
                    }
                    focus = method;
                    break;

                case "parameter":
                    Method paraMethod = Repository.GetMethodByID(int.Parse(issue.id));
                    if (paraMethod != null)
                    {
                        id = paraMethod.ParentID;
                        element = Repository.GetElementByID(id);
                    }
                    focus = paraMethod;
                    break;

                case "package":
                    Package package = Repository.GetPackageByID(int.Parse(issue.id));
                    if (package != null)
                    {
                        Repository.ShowInProjectView(package);
                    }
                    break;

                default:
                    MessageBox.Show(Win32Window.GetMainWindowHandle(), "The reported issue with severity '" + issue.severity + "' is currently not navigable:\n\n" + issue.msg, "Reported issue is not navigable");
                    return;
            }
            
            if (element != null)
            {
                Repository.ShowInProjectView(element);
                Collection elementDiagrams = null;
                if ("element" == issue.type)
                {
                    Element parentElement = element;
                    if (element.Diagrams.Count < 1)
                    {
                        try
                        {
                            parentElement = Repository.GetElementByID(element.ParentID);
                            elementDiagrams = parentElement.Diagrams;
                        }
                        catch (Exception)
                        {
                            // If the element is not in a diagram and its parent is not found then we ignore navigation to diagram
                            // MessageBox.Show(Win32Window.GetMainWindowHandle(), "Element: " + parentElement.Name + " can not be identified by its ID");
                        }
                    } else
                    {
                        elementDiagrams = element.Diagrams;
                    }
                }

                Diagram diagram = Repository.GetCurrentDiagram();
                if (elementDiagrams != null && elementDiagrams.Count >= 1)
                {
                    // we'll navigate to the first diagram for now
                    diagram = elementDiagrams.GetAt(0);
                    Repository.OpenDiagram(diagram.DiagramID);
                    Repository.ActivateDiagram(diagram.DiagramID);
                }
                if (diagram != null)
                {
                    for (short i = (short)(diagram.SelectedObjects.Count - 1); i >= 0; i--)
                    {
                        diagram.SelectedObjects.DeleteAt(i, false); // There is no need to update the DescriptionsCache, because it is only a change in the UI.
                    }
                    diagram.SelectedObjects.Refresh();
                    diagram.SelectedObjects.AddNew(id.ToString(), element.Type);
                    diagram.SelectedObjects.Refresh();
                    try
                    {
                        if (!diagram.Update())
                        {
                            throw new Exception("Failed to update diagram '" + diagram.Name + "' with ID " + diagram.DiagramID + ".");
                        }
                    }
                    catch (COMException e)
                    {
                        throw new Exception("Failed to update diagram '" + diagram.Name + "' with ID " + diagram.DiagramID + ": " + e.Message, e);
                    }
                }
                else
                {
                    // do nothing for now !!
                }
            }
            if (focus != null)
            {
                Repository.ShowInProjectView(focus);
            }
        }
        
        private void ValidationIssuesDataGridView_SortCompare(object sender, DataGridViewSortCompareEventArgs e)
        {
            if (e.Column.Index == SeverityColumn.Index)
            {
                // sort based on the severity icon
                DataGridViewRow row1 = ValidationIssuesDataGridView.Rows[e.RowIndex1];
                DataGridViewRow row2 = ValidationIssuesDataGridView.Rows[e.RowIndex2];
                KeyValuePair<ValidationIssue, ValidationIssueCategory> issue1Entry = (KeyValuePair<ValidationIssue, ValidationIssueCategory>) row1.Tag;
                KeyValuePair<ValidationIssue, ValidationIssueCategory> issue2Entry = (KeyValuePair<ValidationIssue, ValidationIssueCategory>) row2.Tag;

                string severity1 = issue1Entry.Key.severity;
                string severity2 = issue2Entry.Key.severity;
                e.SortResult = ValidationIssueSeverityUtils.CompareSeverityLevels(severity1, severity2);
                e.Handled = true;
            } else
            {
                // do nothing
            }
        }

        private void AbortValidationButton_Click(object sender, EventArgs e)
        {
            if (HeadlessApplicationHandler != null)
            {
                HeadlessApplicationHandler.KillProcess();
                ValidationInProgressLabel.Text = @"Validtion aborted";
                AbortValidationButton.Enabled = false;
                HeadlessApplicationHandler = null;
            }
        }
    }
}
