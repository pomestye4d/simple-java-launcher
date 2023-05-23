#include "launcher_data.h"
#include <filesystem>
#include "utils.h"
#include "configuration.h"
#include <regex>

void LauncherData::processRestartScript() const {
    auto fileName = tempDirectory + pathDelimiter + "restart.dat";
    if (std::filesystem::exists(fileName)) {
        auto str = readFile(fileName.c_str());
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
                    std::filesystem::remove_all(std::filesystem::path(file));
                }
                continue;
            }
            if ("sleep" == op) {
                // sleep(std::stoi(getYamlValue(node, "duration")));
                continue;
            }
        }
        std::filesystem::remove(fileName);
    }
}
void LauncherData::init(bool autoCreateTempDir, std::string currentDirectory) {
    #ifdef __unix__
    std::string delimiter="/";
    std::string javaExec = "bin/java";
    #elif defined(_WIN32) || defined(WIN32)
    std::string delimiter="\\";
    std::string javaExec = "bin\\java.exe";
    #endif
    auto workingDirectory = currentDirectory;
    char *workingDirectoryEnv = std::getenv("sjl.workingDirectory");
    if (workingDirectoryEnv != nullptr) {
        workingDirectory = workingDirectoryEnv;
    }
    if (std::filesystem::path(workingDirectory).is_relative()) {
        workingDirectory = currentDirectory + delimiter + workingDirectory;
    }
    if (!std::filesystem::exists(workingDirectory)) {
        throw std::invalid_argument(string_format("Working directory %s does not exist", workingDirectory.c_str()));
    }
    char *configFileEnv = std::getenv("sjl.configFile");
    std::string configFileName;
    if (configFileEnv != nullptr) {
        configFileName = configFileEnv;
    }
    if (configFileName.empty() && std::filesystem::exists(workingDirectory + delimiter+ "config.yml")) {
        configFileName = workingDirectory + delimiter+ "config.yml";
    }
    if (configFileName.empty() && std::filesystem::exists(workingDirectory + delimiter+ "config.yaml")) {
        configFileName = workingDirectory + delimiter+ "config.yaml";
    }
    if (configFileName.empty() && std::filesystem::exists(workingDirectory + delimiter+ "config" + delimiter + "config.yml")) {
        configFileName = "config"+delimiter +"config.yml";
    }
    if (configFileName.empty() && std::filesystem::exists(delimiter+ "config" + delimiter + "config.yaml")) {
        configFileName = "config" + delimiter +"config.yaml";
    }
    if (configFileName.empty() && std::filesystem::exists(workingDirectory + delimiter+"config.properties")) {
        configFileName = "config.properties";
    }
    if (configFileName.empty() && std::filesystem::exists(workingDirectory + delimiter+ "config"+ delimiter +"config.properties")) {
        configFileName = "config"+ delimiter + "config.properties";
    }
    if (configFileName.empty()) {
        throw std::invalid_argument("Unable to determine config file location");
    }
    if (!endsWith(configFileName, ".properties") && !endsWith(configFileName, ".yml") &&
        !endsWith(configFileName, ".yaml")) {
        throw std::invalid_argument(string_format("Config file name %s has wrong extension", configFileName.c_str()));
    }
    if (std::filesystem::path(configFileName).is_relative()) {
        configFileName = workingDirectory + delimiter + configFileName;
    }
    if (!std::filesystem::exists(configFileName)) {
        throw std::invalid_argument(string_format("Config file name %s  does not exist", configFileName.c_str()));
    }
    auto config = Configuration();
    if (endsWith(configFileName, ".properties")) {
        config.readFromProperties(configFileName);
    } else {
        config.readFromYaml(configFileName);
    }
    char *libFolderEnv = std::getenv("sjl.libFolder");
    std::string libFolderName;
    if (libFolderEnv != nullptr) {
        libFolderName = libFolderEnv;
    } else if (!config.libFolder.empty()) {
        libFolderName = config.libFolder;
    } else {
        libFolderName = "lib";
    }
    if (std::filesystem::path(libFolderName).is_relative()) {
        libFolderName = workingDirectory + delimiter + libFolderName;
    }
    if (!std::filesystem::exists(libFolderName)) {
        throw std::invalid_argument(string_format("Lib folder %s does not exist", libFolderName.c_str()));
    }
    char *tempDirEnv = std::getenv("sjl.tempFolder");
    std::string tempDir;
    if (tempDirEnv != nullptr) {
        tempDir = tempDirEnv;
    } else if (!config.tempFolder.empty()) {
        tempDir = config.tempFolder;
    } else {
        tempDir = "temp";
    }

    if (std::filesystem::path(tempDir).is_relative()) {
        tempDir = workingDirectory + delimiter + tempDir;
    }
    if (!std::filesystem::exists(tempDir) && autoCreateTempDir) {
        std::filesystem::create_directories(tempDir);
    }
    tempDirectory = tempDir;
    pathDelimiter = delimiter;
    std::regex launcherExpr(R"(launcher([\-\.0-9]*\.jar))");

    std::string launcherFileName;
    for (const auto &entry: std::filesystem::directory_iterator(libFolderName)) {
        auto fileName = entry.path().filename().string();
        if (std::regex_match(fileName, launcherExpr)) {
            launcherFileName = libFolderName + delimiter + fileName; // NOLINT(performance-inefficient-string-concatenation)
            break;
        }
    }
    if (launcherFileName.empty()) {
        throw std::invalid_argument(
                string_format("Unable to find launcher jar in lib folder %s", libFolderName.c_str()));
    }

    char *javaHome = std::getenv("sjl.javaHome");
    if (javaHome == nullptr) {
        javaHome = std::getenv("JAVA_HOME");
    }
    std::string javaHomeName;
    if (javaHome != nullptr) {
        javaHomeName = javaHome;
    } else if (!config.javaHome.empty()) {
        javaHomeName = config.javaHome;
    }
    if (!javaHomeName.empty() && std::filesystem::path(javaHomeName).is_relative()) {
        javaHomeName = workingDirectory + delimiter + javaHomeName;
    }
    std::string javaCmd;
    if (!javaHomeName.empty()) {
        javaCmd = javaHomeName + delimiter+javaExec;
    } else {
        javaCmd = "java";
    }
    pidFileName = tempDir + delimiter+"sjl.pid";
    std::string command = string_format(R"(cd "%s" && "%s" -cp "%s")", workingDirectory.c_str(), javaCmd.c_str(),
                                        launcherFileName.c_str());
for (const auto& arg: config.commonArgs) {
        command += " " + arg;
    }
    startCommand = command;
    for (const auto& arg: config.startArgs) {
        startCommand += " " + arg;
    }
    startCommand +=" com.vga.sjl.SjlBoot -background";
    stopCommand = command;
    for (const auto& arg: config.stopArgs) {
        stopCommand += " " + arg;
    }
    stopCommand +=" com.vga.sjl.SjlBoot stop";
    statusCommand = command;
    for (const auto& arg: config.statusArgs) {
        statusCommand += " " + arg;
    }
    statusCommand +=" com.vga.sjl.SjlBoot status";
    serviceName = config.serviceName;
    serviceDisplayName = config.serviceDisplayName;
    serviceDescription = config.serviceDescription;
    
}