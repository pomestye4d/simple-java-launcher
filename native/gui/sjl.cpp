#include <filesystem>

#include <string>
#include "launcher_data.h"
#include "rapidyaml-0.4.1.hpp"

static LauncherData launcherData;


int main(int argc, char **argv) {
    launcherData = LauncherData();
    launcherData.init(true, argv[0]);
    while (true) {
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
