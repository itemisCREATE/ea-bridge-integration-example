using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using ItemisIntegrateEA_Example_AddIn.Reports;

namespace ItemisIntegrateEA_Example_AddIn_Tests.ReportsTest
{
    [TestFixture]
    public class ValidationResultTest
    {

        private readonly string ExampeleJson = @"
{
   ""resourceIssues"":[
      
   ],
   ""loadIssues"":[
      
   ],
   ""umlIssues"":[
      {
         ""severity"":""W"",
         ""type"":""Model"",
         ""id"":""1"",
         ""message"":""msg1""
      },
      {
         ""severity"":""W"",
         ""type"":""Class"",
         ""id"":""2"",
         ""message"":""msg2""
      },
      {
         ""severity"":""W"",
         ""type"":""Package"",
         ""id"":""3"",
         ""message"":""msg3""
      },
      {
         ""severity"":""W"",
         ""type"":""Class"",
         ""id"":""4"",
         ""message"":""msg4""
      }
   ],
   ""customIssues"":[
      
   ],
}
";

        private readonly string EmptyJson = "{\"resourceIssues\":[],\"loadIssues\":[],\"umlIssues\":[],\"customIssues\":[]}";

        [Test]
        public void ValidationResult_Should_HandleEmptyStrings()
        {
            SerializableValidationResult validationReport = JsonConvert.DeserializeObject<SerializableValidationResult>(EmptyJson);
            Assert.That(validationReport, Is.Not.Null);
            Assert.That(validationReport.GetAllValidationIssues().Count == 0, Is.True, "Validation result of an empty string is not empty!");
        }

        [Test]
        public void ValidationResult_Should_DeserializeStrings()
        {
            SerializableValidationResult validationReport = JsonConvert.DeserializeObject<SerializableValidationResult>(ExampeleJson);
            Assert.That(validationReport, Is.Not.Null);
            Assert.That(validationReport.GetAllValidationIssues().Count == 4, Is.True, "Desrialization result of test json is incorrect!");
            Assert.That(validationReport.umlIssues.Count == 4, Is.True, "Desrialization result of test json is incorrect!");
        }

        [Test]
        public void ValidationResult_Should_HandleMalformedStrings()
        {
            SerializableValidationResult validationReport = JsonConvert.DeserializeObject<SerializableValidationResult>("");
            Assert.That(validationReport, Is.Null);
        }
    }
}
