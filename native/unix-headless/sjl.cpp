#include <iostream>
#include <filesystem>

#include <string>
#include <unistd.h>
#include <sys/wait.h>
#include "launcher_data.h"
#include "utils.h"
#include "rapidyaml-0.4.1.hpp"
#include "configuration.h"

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
            std::string fileName = launcherData.tempDirectory + launcherData.pathDelimiter + "restart.dat";
            if (std::filesystem::exists(fileName)) {
                std::string str = readFile(fileName.c_str());
                ryml::Tree tree = ryml::parse_in_arena(ryml::to_csubstr(str));
                for (int n = 0; n < tree.num_children(tree.root_id()); n++) {
                    ryml::NodeRef node = tree[n];
                    auto op = getYamlValue(node, "operation");
                    if ("move" == op) {
                        auto to = getYamlValue(node, "to");
                        auto from = getYamlValue(node, "from");
                        if (std::filesystem::exists(from)) {
                            if (std::filesystem::exists(to)) {
                                std::filesystem::remove(std::filesystem::path(to));
                            }
                            std::filesystem::rename(std::filesystem::path(from), std::filesystem::path(to));
                        }
                        continue;
                    }
                    if ("delete" == op) {
                        auto file = getYamlValue(node, "file");
                        if (std::filesystem::exists(file)) {
                            std::filesystem::remove(std::filesystem::path(file));
                        }
                        continue;
                    }
                    if ("sleep" == op) {
                        sleep(std::stoi(getYamlValue(node, "duration")));
                        continue;
                    }
                }
                std::filesystem::remove(fileName);
            }
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
