//
// Created by avramenko on 16.09.2022.
//
#define _CRT_SECURE_NO_WARNINGS
#ifndef PG_LAUNCHER_DATA_H
#define PG_LAUNCHER_DATA_H
#include <vector>
#include <string>
#include <map>
class LauncherData  {
public:
    LauncherData() = default;

    virtual ~LauncherData() = default;

    void init(bool autoCreateTempDir, std::string currentDirectory);

    void processRestartScript() const;

    std::string startCommand;
    std::string stopCommand;
    std::string statusCommand;
    std::string pidFileName;
    std::string pathDelimiter;
    std::string tempDirectory;
    std::string serviceName;
    std::string serviceDisplayName;
    std::string serviceDescription;
};
#endif //PG_LAUNCHER_DATA_H
