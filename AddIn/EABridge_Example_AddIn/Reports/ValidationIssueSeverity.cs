using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace EABridge_Example_AddIn.Reports
{

    public enum ValidationIssueSeverity
    {
        Error,
        Warning,
        Information,
        None
    }
    public static class ValidationIssueSeverityUtils
    {

        public static ValidationIssueSeverity GetSeverity(string severity)
        {
            switch (severity)
            {
                case "E": return ValidationIssueSeverity.Error;
                case "W": return ValidationIssueSeverity.Warning;
                case "I": return ValidationIssueSeverity.Information;
                default: return ValidationIssueSeverity.None;
            }
        }

        public static int GetSeverityLevel(ValidationIssueSeverity severity)
        {
            switch (severity)
            {
                case ValidationIssueSeverity.Error: return 2;
                case ValidationIssueSeverity.Warning: return 1;
                case ValidationIssueSeverity.Information: return 0;
                default: return -1;
            }
        }

        public static int CompareSeverityLevels(string first, string second)
        {
            var firstSeverity= GetSeverity(first);
            var secondSeverity = GetSeverity(second);
            return GetSeverityLevel(firstSeverity) - GetSeverityLevel(secondSeverity);
        }

    }
}
