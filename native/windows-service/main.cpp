#pragma region "Includes"
#include <regex>
#include "ServiceInstaller.h"
#include "utils.h"
#include "SjlService.h"
#include "launcher_data.h"
#include <filesystem>
#include <locale>
#include <string>
#pragma endregion


using namespace std;


int wmain(int argc, wchar_t *argv[])
{
    try {
        // Service parameters
        DWORD dwSvcStartType = SERVICE_START_TYPE;
        PCWSTR wsSvcAccount = SERVICE_ACCOUNT;
        PCWSTR wsSvcPwd = SERVICE_PASSWORD;
        wchar_t wszPath[MAX_PATH];
        GetModuleFileName(NULL, wszPath, _countof(wszPath));
        std::string currentDirectory = toString(wszPath);
        auto idx = currentDirectory.find_last_of("\\");
        currentDirectory = currentDirectory.substr(0, idx);
        auto launcherData = LauncherData();
        launcherData.init(true, currentDirectory);
        auto serviceNameStr = toWString(launcherData.serviceName);
        auto serviceName = serviceNameStr.c_str();
        auto serviceDisplayNameStr = toWString(launcherData.serviceDisplayName);
        auto serviceDisplayName = serviceDisplayNameStr.c_str();
        auto serviceDescriptionStr = toWString(launcherData.serviceDescription);
        auto serviceDescription = serviceDescriptionStr.c_str();
        auto str1 = test(L"lib is %1", L"Èìÿ");
       // auto str1 = vtformat("lib %s is %s.", "Èìÿ", "/home/avramenko/test/home/avramenko/test2");
        //auto str2 = format(L"lib %s is %s.", L"Èìÿ", L"/home/avramenko/test/home/avramenko/test2");
        
        if (launcherData.serviceName.empty()) {
            throw exception("service name is not defined in config");
        }
        if (launcherData.serviceDisplayName.empty()) {
            throw exception("service display name is not defined in config");
        }
        if (launcherData.serviceDescription.empty()) {
            throw exception("service description is not defined in config");
        }
       
        if (argc > 1)
        {
            if (_wcsicmp(L"install", argv[1]) == 0)
            {
                try
                {
                    for (int i = 2; i < argc; i++)
                    {
                        PWSTR arg = argv[i];

                        if (arg[0] == '-')
                        {
                            if (_wcsicmp(L"-start-type", arg) == 0)
                            {
                                if (argc > i)
                                {
                                    PCWSTR wsStartType = argv[++i];

                                    if (regex_match(wsStartType, wregex(L"[2-4]")))
                                    {
                                        dwSvcStartType = _wtol(wsStartType);
                                    }
                                    else
                                    {
                                        throw exception("service start type must be a number between 2 and 4");
                                    }
                                }
                                else
                                {
                                    throw exception("no startup type specified after -start-type");
                                }
                            }
                            else if (_wcsicmp(L"-account", arg) == 0)
                            {
                                if (argc > i)
                                {
                                    wsSvcAccount = argv[++i];
                                }
                                else
                                {
                                    throw exception("no account name after -account");
                                }
                            }
                            else if (_wcsicmp(L"-password", arg) == 0)
                            {
                                if (argc > i)
                                {
                                    wsSvcPwd = argv[++i];
                                }
                                else
                                {
                                    throw exception("no password  after -password");
                                }
                            }
                            else
                            {
                                char errMsg[MAX_PATH];
                                _snprintf_s(errMsg, _countof(errMsg), _TRUNCATE, "unknown parameter: %S", arg);

                                throw exception(errMsg);
                            }
                        }
                    }

                    InstallService(
                        serviceName,               // Name of service
                        serviceDisplayName,          // Display name
                        serviceDescription,               // Description
                        dwSvcStartType,             // Service start type
                        SERVICE_DEPENDENCIES,       // Dependencies
                        wsSvcAccount,               // Service running account
                        wsSvcPwd,                   // Password of the account
                        TRUE,                       // Register with Windows Event Log, so our log messages will be found in Event Viewer
                        1,                          // We have only one event category, "Service"
                        NULL                        // No separate resource file, use resources in main executable for messages (default)
                    );
                }
                catch (exception const& ex)
                {
                    wprintf(L"Couldn't install service: %S", ex.what());
                    return 1;
                }
                catch (...)
                {
                    wprintf(L"Couldn't install service: unexpected error");
                    return 2;
                }
            }
            else if (_wcsicmp(L"uninstall", argv[1]) == 0)
            {
                UninstallService(serviceName);
            }
            else if (_wcsicmp(L"serve", argv[1]) == 0)
            {
                
                SjlService service(serviceName);

                service.SetCommandLine(argc, argv);

                if (!CServiceBase::Run(service))
                {
                    DWORD dwErr = GetLastError();

                    wprintf(L"Service failed to run with error code: 0x%08lx\n", dwErr);

                    return dwErr;
                }
            }
            else if (_wcsicmp(L"test", argv[1]) == 0) {
                SjlService service(serviceName);
                //auto str = toWString(launcherData.serviceDescription);
                //wprintf(L"description is: %s\n", str.c_str());
                return 0;
            }
            else
            {
                wprintf(L"Unknown parameter: %s\n", argv[1]);
            }
        }
        else
        {
            wprintf(L"\n%s\n\n", serviceNameStr);
            wprintf(L"Parameters:\n\n");
            wprintf(L" install [-start-type <2..4> -account <account-name> -password <account-password>]\n  - to install the service.\n");
            wprintf(L"    service start types are:\n");
            wprintf(L"     2 - service started automatically by the service control manager during system startup.\n");
            wprintf(L"     3 - service started manually or by calling StartService function from another process.\n");
            wprintf(L"     4 - service installed in the \"disabled\" state, and cannot be started until enabled.\n");
            wprintf(L" uninstall\n  - to remove the service.\n");
        }

        return 0;
    }
    catch (exception e) {
        printf("exception is %s\n", e.what());
        throw e;
    }
}




