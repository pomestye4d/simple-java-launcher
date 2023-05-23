//
// Created by avramenko on 16.09.2022.
//

#ifndef PG_CONFIGURATION_H
#define PG_CONFIGURATION_H
#include <vector>
#include <map>
#include "rapidyaml-0.4.1.hpp"
std::string getYamlValue(c4::yml::NodeRef node, std::string key);
class Configuration  {
public:
    Configuration() {}

    virtual ~Configuration() {}

    void readFromYaml(const std::string& fileName);

    void readFromProperties(const std::string& fileName);

    std::vector<std::string> commonArgs;
    std::vector<std::string> startArgs;
    std::vector<std::string> stopArgs;
    std::vector<std::string> statusArgs;
    std::string libFolder;
    std::string tempFolder;
    std::string javaHome;
    std::string serviceName;
    std::string serviceDisplayName;
    std::string serviceDescription;
};
#endif //PG_CONFIGURATION_H
