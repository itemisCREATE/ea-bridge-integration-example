﻿using EABridge_Example_AddIn_Tests.TestUtils;
using EA;
using File = System.IO.File;
using NUnit.Framework;
using System;
using Newtonsoft.Json;
using EABridge_Example_AddIn.Reports;
using EABridge_Example_AddIn.ApplicationHandlers;

namespace EABridge_Example_AddIn_Tests.AddInTest
{
    [TestFixture]
    public class HeadlessApplicationHandlerTest : EAAddInTest
    {
        [Test]
        public void HeadlessOperation_Should_UseSeparateThread()
        {
            ExampleHeadlessApplicationHandler applicationHandler = new ExampleHeadlessApplicationHandler();
            applicationHandler.StartExternValidationOperationAsync("", Repository.ConnectionString);
            Assert.IsTrue(applicationHandler.IsThreadRunning(), "Thread could not be started");
        }

        [Test]
        public void HeadlessOperation_Should_TerminateWhenPrompted()
        {
            ExampleHeadlessApplicationHandler applicationHandler = new ExampleHeadlessApplicationHandler();
            applicationHandler.StartExternValidationOperationAsync("", Repository.ConnectionString);
            Assert.IsTrue(applicationHandler.IsThreadRunning(), "Thread could not be started");
            applicationHandler.KillProcess();
            Assert.IsFalse(applicationHandler.IsThreadRunning(), "Thread did not terminate successfully");
        }

        [Test]
        public void ValidationOperationOutcome_Should_BeValidJson()
        {
            ExampleHeadlessApplicationHandler applicationHandler = new ExampleHeadlessApplicationHandler();
            applicationHandler.OperationComplete += AssertHeadlessOperationOutcome; 
            applicationHandler.StartExternValidationOperationAsync("", Repository.ConnectionString);
            Assert.IsTrue(applicationHandler.IsThreadRunning(), "Thread could not be started");
        }

        private void AssertHeadlessOperationOutcome(ExampleHeadlessApplicationHandler sender, ExternApplicationEventArgs e)
        {
            string reportFilePath = e.ReportFile;
            Assert.IsTrue(e.ReturnCode == 0, "Return code of the CLI application is not successful");
            Assert.IsTrue(File.Exists(reportFilePath), "Report file path does not exist");
            try
            {
                SerializableValidationResult ValidationReport = JsonConvert.DeserializeObject<SerializableValidationResult>(File.ReadAllText(reportFilePath));
            }
            catch (Exception ex)
            {
                Assert.Fail(ex.Message);
            }
            File.Delete(reportFilePath);
        }
    }
}
