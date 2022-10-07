//
// Created by avramenko on 16.09.2022.
//

#ifndef PG_LAUNCHER_DATA_H
#define PG_LAUNCHER_DATA_H
#include <vector>
#include <map>
class LauncherData  {
public:
    LauncherData() = default;

    virtual ~LauncherData() = default;

    void init(bool autoCreateTempDir, std::string currentDir);

    std::string startCommand;
    std::string stopCommand;
    std::string statusCommand;
    std::string pidFileName;
    std::string pathDelimiter;
    std::string tempDirectory;
};
#endif //PG_LAUNCHER_DATA_H
