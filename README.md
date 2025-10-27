# Enterprise Architect - Itemis CREATE Integration Example

The example contained in this repository demonstrate the integration of the Enterprise Architect (EA) modeling tool with a command-line Java application for **model validation** and **code-generation** for state-machines.
A simple EA Add-in calls a command-line application (CLI) which loads the `.eap/.eapx` file, performs validation / code-generation on that model and returns the result.
For the validation use-case, the Add-in displays the validation issues in a custom UI view including the possibility to navigate to the corresponding elements in the EA project browser or in existing diagrams. 
The following diagram illustrates how this example works:

```mermaid
flowchart TB
    user[(User)]
    user -- - validate model<br>- generate code --> Action
    user -- navigate to elements --> View

    code[["C/C++/C#/Java/Python files"]]
    CREATE -- generate --> code

    eap[[".eap(x)/.qea(x) File"]]
    EA -- query model --> eap
    INTEGRATE -- load model --> eap

    subgraph EA["Enterprise Architect"]
        subgraph AddIn["EA AddIn"]
            View["Validation View"]
            Action["Actions"]
            CLIHandler["CLI Handler"]
        end
        Browser["Project Browser"]
        Diagram
    end
    Action --> CLIHandler
    CLIHandler -- update--> View
    View -- select element --> Browser
    View -- select element --> Diagram

    subgraph CLI["CLI Application"]
        Logic["CLI Logic"]
        INTEGRATE["Itemis INTEGRATE-EA"]
        CREATE["Itemis CREATE"]
        Validator
        UML["UML"]
        Rules["Custom Rules"]
    end


    Logic -- load UML Model --> INTEGRATE
    Logic -- call --> Validator
    Logic -- call --> CREATE
    CLIHandler -- call --> Logic
    Logic -- Validation Report (JSON) --> CLIHandler
    Validator -- uses --> UML
    Validator -- uses --> CREATE
    Validator -- uses --> Rules
```

This example may also serve as a template for further use cases such as custom model validation or code generation, or any kind reporting.

**Advantages** of this approach are:

* **Performance**: the CLI application loads and processes large EA models *much faster* (up to 50x) than using EA's API.
* **Standard compliance**: the model is loaded as a *standard UML model* and can be processed with *powerful languages* such as Java, Kotlin, or Xtend.
* **Seamless integration**: the EA Add-in *integrates* powerful model processing into the UI of Enterprise Architect.

*This example is tested with Enterprise Architect 17.*


## How to run this example 

This repository consists of two codebases:
    
* The command-line interface (CLI) is a headless Java application containing an [EA-integration adapter](https://www.itemis.com/en/yakindu/ea-bridge/) for loading UML models from eap-files and [itemis CREATE](https://www.itemis.com/en/products/itemis-create/) for code-generation; it further performs UML syntax validation and runs an exemplary custom validation rule.
The result of this validation is stored in a json-file for further use with other tools such as the EA Add-in below.

* The EA Add-in allows the user to validate the model using a UI integrated in Enterprise Architect; it further lists the validation results in a custom view inside EA without the need to use the command line.
A *double click* on a validation result in EA selects the corresponding element in the Project Browser and opens the diagram containing that element (if such a diagram exists).

Please follow these steps to try out the pre-built example on your local machine:
1. Download the [zip-package](https://github.com/itemisCREATE/ea-bridge-integration-example/releases/latest) and extract it to some location on your hard-drive.
2. Download the [Add-in installer](https://github.com/itemisCREATE/ea-bridge-integration-example/releases/latest) and install it (EA must not be running).
3. Start EA, go to *Specialize* &rarr; *Itemis Integrate-EA* &rarr; *Configure CLI Path* &rarr; and set the path to the folder into which you extracted the zip-package.
4. Validate your model via the context-menu on a package in the Project Browser and select *Specialize* %rarr; *Itemis Integrate-EA* &rarr; *Example Model Validation* &rarr; it will take a few seconds, then check the Add-in window for the result.
5. Generate code via the context-menu on a state-machine and select *Specialize* &rarr; *Itemis Integrate-EA* &rarr; *Generate Code...* $rarr; a dialog will popup to configure and finally run the code generation.

![EA demo](./EA_demo.gif)


## How to build this example

* CLI application:

    The CLI is an OSGi application written in Java. Simply run the following maven command to build it.  

    `cd CLI`

    `mvn clean verify`

* EA Add-in:
    The Add-in is written in C# and uses the [automation API provided by Enterprise Architect](https://sparxsystems.com/enterprise_architect_user_guide/17.0/add-ins___scripting/addins_2.html) to access the model and add custom views to the UI.
    The UI extensions are simple Windows controls and forms. The entire Add-in is a .NET-based COM application.
    To build the Add-in you need the following tools:
    * Visual Studio 2022 or newer. In the IDE, the following extensions are required:
        * NUnit 4 Test Adapter: required to build and run tests. It can be installed using the extension manager in Visual Studio.
        * Wix Toolkit Visual Studio 2022 extensions: The Wix toolset is required to build the Add-in installer package. It can be installed using the extension manager in Visual Studio. However, it must be accompanied by the installation of the Wix tool which can be downloaded [here](https://wixtoolset.org/releases/).
    * [Enterprise Architect version 15.2](https://sparxsystems.com/products/ea/downloads.html) (or newer)

    After installing these tools, open the solution [`AddIn/Itemis_Integrate_EA_Example_AddIn.sln`](./AddIn/Itemis_Integrate_EA_Example_AddIn.sln) and build all projects contained in it. To register the built Add-in `DLL` with Enterprise Architect, run the script [`AddIn/Itemis_Integrate_EA_Example_AddIn\Install.bat`](./AddIn/Itemis_Integrate_EA_Example_AddIn/Install.bat). This script registers the DLL built by the IDE with Enterprise Architect as the Add-in. It needs to be run only once. When the Add-in installer is used, the DLL in the specified installation location will be used instead.   


# Contribute

We are always happy to see suggestions, bug reports, feature requests as well as pull requests.
Please feel free to raise an issue or contribute with a pull request. 


# Further information

The [Itemis INTEGRATE-EA](https://www.itemis.com/en/yakindu/ea-bridge/) adapter has a free 30-day trial period which can be used with models containing up to 1000 elements.<br/>
The [Itemis CREATE](https://www.itemis.com/en/products/itemis-create/) code generator also has a free 30-day trial period.
Please use the Contact forms on the Websites for specific questions related to these components.
