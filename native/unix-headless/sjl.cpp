#include <iostream>
#include <filesystem>

#include <string>
#include <unistd.h>
#include <sys/wait.h>
#include "launcher_data.h"
#include "utils.h"
#include "rapidyaml-0.4.1.hpp"

static LauncherData launcherData;


int stop() {
    auto res = system(launcherData.stopCommand.c_str());
    if (std::filesystem::exists(launcherData.pidFileName)) {
        try {
            pid_t process = std::stoi(readFile(launcherData.pidFileName.c_str()));
            while (kill(process, 0) != 0) {
                sleep(1);
            }
        }
        catch (const char *msg) {
            //noops
        }
        if (std::filesystem::exists(launcherData.pidFileName)) {
            std::filesystem::remove(launcherData.pidFileName);
        }
    }
    return res;
}

void signalHandler(int signum) {
    std::cout << "Interrupt signal (" << signum << " " << ") received.\n";
    stop();
    exit(signum);
}

int main(int argc, char **argv) {
    launcherData = LauncherData();
    launcherData.init(true, argv[0]);
    if (argc == 2) {
        std::string arg = argv[1];
        if (arg == "stop" || arg == "restart") {
            auto res = stop();
            if (res != 0) {
                std::cout << "Unable to stop application";
                return res;
            }
            if (arg == "stop") {
                return 0;
            }
        }
        if (arg == "status") {
            return system(launcherData.statusCommand.c_str());
        }
    }

    writeToFile(launcherData.pidFileName.c_str(), std::to_string(getpid()));
    while (true) {
        signal(SIGINT, signalHandler);
        signal(SIGTERM, signalHandler);
        auto res = system(launcherData.startCommand.c_str());
        if (res == 512) {
            launcherData.processRestartScript();
            launcherData.init(true, argv[0]);
            continue;
        }
        if (std::filesystem::exists(launcherData.pidFileName)) {
            std::filesystem::remove(launcherData.pidFileName);
        }
        break;
    }
    return 0;
}
