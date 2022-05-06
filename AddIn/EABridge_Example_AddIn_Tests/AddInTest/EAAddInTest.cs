using EABridge_Example_AddIn_Tests.TestUtils;
using EABridge_Example_AddIn;
using EABridge_Example_AddIn.Utils;
using EA;
using NUnit.Framework;
using FileUtils = EABridge_Example_AddIn_Tests.TestUtils.FileUtils;

namespace EABridge_Example_AddIn_Tests.AddInTest
{
    [TestFixture]
    public class EAAddInTest
    {

        protected Repository Repository;
        protected ExampleValidationAddIn ValidationAddIn;
        protected App App;

        [SetUp]
        public void SetUp()
        {
            LoadRepository(AddInTestConstants.ReferenceTestModelPath, true);
            ValidationAddIn = new ExampleValidationAddIn(); 
        }

        private void LoadRepository(string testModelPath, bool loadAddins)
        {
            App = new App();
            Repository = App.Repository;
            string repoLocation = FileUtils.BackupFile(testModelPath);
            Repository.OpenFile(repoLocation);
            if (loadAddins)
            {
                Repository.LoadAddins();
            }
        }

        
        [TearDown]
        public void TearDown()
        {
            if (Repository != null)
            {
                Repository.CloseFile();
                Repository.CloseAddins();
                Repository.Exit();
                Repository = null;
                App = null;
            }

        }
        
    }
}
