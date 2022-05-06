using EABridge_Example_AddIn.ApplicationHandlers;
using EA;
using System;
using System.Collections.Generic;
using System.IO;
using System.Xml.Linq;
using File = System.IO.File;

namespace EABridge_Example_AddIn.Utils
{
    public static class EARepositoryUtils
    {

        public const string EAPackageType = "Package";
        public const string EAElementType = "Element";


        public static bool IsPackageGuid(Repository repository, string guid)
        {
            try
            {
                Package package = repository.GetPackageByGuid(guid);
                return package != null ? true : false;
            }
            catch
            {
                return false;
            }
        }

        public static bool IsElementGuid(Repository repository, string guid)
        {
            try
            {
                Element element = repository.GetElementByGuid(guid);
                return element != null ? true : false;
            }
            catch
            {
                return false;
            }
        }

        public static string GetGuidOfPackageOrElementOrDiagram(object cxt)
        {
            if (cxt is Package)
            {
                return ((Package)cxt).PackageGUID;

            }
            else if (cxt is Element)
            {
                return ((Element)cxt).ElementGUID;

            }
            else if (cxt is Diagram)
            {
                return ((Diagram)cxt).DiagramGUID;

            }
            else
            {
                return "";
            }

        }

        public static bool IsCLIOperationForContextObjectAllowed(object cxt)
        {
            if (cxt is Package)
            {
                return true;
            }
            else if (cxt is Element)
            {
                Element cxtCast = (Element)cxt;
                return true;
            }
            else
            {
                return false;
            }
        }
        

        
        public static string GetAllRootPackageNames(this Repository repository)
        {
            var models = repository.Models;
            List<string> names = new List<string>();
            if (models == null || models.Count == 0)
            {
                return "";
            }
            else
            {
                // get the model where the requested package is
                foreach (Package model in models)
                {
                    names.Add(model.Name);
                }

                return string.Join(", ", names.ToArray());
            }

        }

        public static string GetEAObjectSimpleNameFromGuid(this Repository repository, string guid)
        {
            if (IsPackageGuid(repository, guid))
            {
                var package = repository.GetPackageByGuid(guid);
                return package.Name;
            }
            else if (IsElementGuid(repository, guid))
            {
                var element = repository.GetElementByGuid(guid);
                return element.Name;
            }
            else if (string.IsNullOrEmpty(guid))
            {
                return GetAllRootPackageNames(repository);
            }
            else
            {
                return "";
            }
        }


        public static KeyValuePair<string, string> GetEAElementName(Repository repository, string type, string id)
        {
            Element element = null;
            switch (type)
            {
                case "element":
                    element = repository.GetElementByID(int.Parse(id));
                    return new KeyValuePair<string, string>(element.FQName, element.Name);

                case "connector":
                    Connector connector = repository.GetConnectorByID(int.Parse(id));
                    if (connector != null)
                    {
                        int connectorID = connector.ClientID;
                        element = repository.GetElementByID(connectorID);
                        return new KeyValuePair<string, string>(element.FQName, element.Name);
                    }
                    return new KeyValuePair<string, string>(String.Empty, String.Empty);

                case "attribute":
                    EA.Attribute attribute = repository.GetAttributeByID(int.Parse(id));
                    if (attribute != null)
                    {
                        return new KeyValuePair<string, string>(attribute.FQStereotype, attribute.Name);
                    }
                    return new KeyValuePair<string, string>(String.Empty, String.Empty);

                case "operation":
                    Method method = repository.GetMethodByID(int.Parse(id));
                    if (method != null)
                    {
                        return new KeyValuePair<string, string>(method.FQStereotype, method.Name);
                    }
                    return new KeyValuePair<string, string>(String.Empty, String.Empty);

                case "parameter":
                    Method paraMethod = repository.GetMethodByID(int.Parse(id));
                    if (paraMethod != null)
                    {
                        return new KeyValuePair<string, string>(paraMethod.FQStereotype, paraMethod.Name);
                    }
                    return new KeyValuePair<string, string>(String.Empty, String.Empty);

                case "package":
                    Package package = repository.GetPackageByID(int.Parse(id));
                    if (package != null)
                    {
                        return new KeyValuePair<string, string>(package.StereotypeEx, package.Name);
                    }
                    return new KeyValuePair<string, string>(String.Empty, String.Empty);

                default:
                    return new KeyValuePair<string, string>(String.Empty, String.Empty);

            }
        }


    }
}
