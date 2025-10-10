using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace EABridge_Example_AddIn.Reports
{

    public class SerializableValidationResult
    {
        public List<ValidationIssue> resourceIssues { get; set; }

        public List<ValidationIssue> loadIssues { get; set; }

        public List<ValidationIssue> umlIssues { get; set; }

        public List<ValidationIssue> sctIssues { get; set; }

        public List<ValidationIssue> customIssues { get; set; }

        public int Count()
        {
            return resourceIssues.Count + loadIssues.Count + umlIssues.Count + sctIssues.Count + customIssues.Count;
        }

        public IDictionary<ValidationIssue, ValidationIssueCategory> GetAllValidationIssues()
        {
            var result = new Dictionary<ValidationIssue, ValidationIssueCategory>();
            resourceIssues.ForEach(issue => result.Add(issue, ValidationIssueCategory.Resource));
            loadIssues.ForEach(issue => result.Add(issue, ValidationIssueCategory.Load));
            umlIssues.ForEach(issue => result.Add(issue, ValidationIssueCategory.UML));
            sctIssues.ForEach(issue => result.Add(issue, ValidationIssueCategory.SCT));
            customIssues.ForEach(issue => result.Add(issue, ValidationIssueCategory.Custom));
            return result;
        }
    }
}
