/****************************** Module Header ******************************\
* Module Name:  SampleService.cpp
* Project:      sample-service
* Copyright (c) Microsoft Corporation.
* Copyright (c) Tromgy (tromgy@yahoo.com)
*
* Provides a sample service class that derives from the service base class -
* CServiceBase. The sample service logs the service start and stop
* information to the Application event log, and shows how to run the main
* function of the service in a thread pool worker thread.
*
* This source is subject to the Microsoft Public License.
* See http://www.microsoft.com/en-us/openness/resources/licenses.aspx#MPL.
* All other rights reserved.
*
* THIS CODE AND INFORMATION IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
* EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
\***************************************************************************/



#include "SjlService.h"
#include "event_ids.h"


SjlService::SjlService(PCWSTR pszServiceName,
                               BOOL fCanStop,
                               BOOL fCanShutdown,
                               BOOL fCanPauseContinue) :
    CServiceBase(pszServiceName, fCanStop, fCanShutdown, fCanPauseContinue, MSG_SVC_FAILURE, CATEGORY_SERVICE)
{
    m_bIsStopping = FALSE;

    m_hHasStoppedEvent = CreateEvent(NULL, TRUE, FALSE, NULL);

    if (m_hHasStoppedEvent == NULL)
    {
        throw GetLastError();
    }
}

void SjlService::OnStart(DWORD /* useleses */, PWSTR* /* useless */)
{
    // Log a service start message to the Application log.
    WriteLogEntry(L"Sample Service is starting...", EVENTLOG_INFORMATION_TYPE, MSG_STARTUP, CATEGORY_SERVICE);
    // Add the main service function for execution in a worker thread.
    if (!CreateThread(NULL, 0, ServiceRunner, this, 0, NULL))
    {
        WriteLogEntry(L"Sample Service couldn't create worker thread.", EVENTLOG_ERROR_TYPE, MSG_STARTUP, CATEGORY_SERVICE);
    }
}

SjlService::~SjlService()
{
}

void SjlService::Run()
{
    OnStart(0, NULL);
}

DWORD __stdcall SjlService::ServiceRunner(void* self)
{
    SjlService* pService = (SjlService*)self;

    pService->WriteLogEntry(L"Sample Service has started.", EVENTLOG_INFORMATION_TYPE, MSG_STARTUP, CATEGORY_SERVICE);

    // Periodically check if the service is stopping.
    for (bool once = true; !pService->m_bIsStopping; once = false)
    {
        if (once)
        {
            // Log multi-line message
            pService->WriteLogEntry(L"Sample Service is pretending to be working:\nStarting fake job 1...\nStarting fake job 2...\nStarting fake job 3...", EVENTLOG_INFORMATION_TYPE, MSG_OPERATION, CATEGORY_SERVICE);
        }

        // Just pretend to do some work
        Sleep(5000);
    }

    // Signal the stopped event.
    SetEvent(pService->m_hHasStoppedEvent);
    pService->WriteLogEntry(L"Sample Service has stopped.", EVENTLOG_INFORMATION_TYPE, MSG_SHUTDOWN, CATEGORY_SERVICE);

    return 0;
}

void SjlService::OnStop()
{
    // Log a service stop message to the Application log.
    WriteLogEntry(L"Sample Service is stopping", EVENTLOG_INFORMATION_TYPE, MSG_SHUTDOWN, CATEGORY_SERVICE);

    // Indicate that the service is stopping and wait for the finish of the
    // main service function (ServiceWorkerThread).
    m_bIsStopping = TRUE;

    if (WaitForSingleObject(m_hHasStoppedEvent, INFINITE) != WAIT_OBJECT_0)
    {
        throw GetLastError();
    }
}
