using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ItemisIntegrateEA_Example_AddIn.Reports
{
    public class ValidationIssue
    {
        public string severity { get; set; }

        public string type { get; set; }

        public string id { get; set; }

        public string msg { get; set; }

        public override string ToString()
        {
            return msg + " | " + severity + " | " + type + " | " + id; 
        }
    }
}
