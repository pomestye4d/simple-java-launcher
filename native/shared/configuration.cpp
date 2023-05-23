#include "configuration.h"
#define RYML_SINGLE_HDR_DEFINE_NOW
#include "rapidyaml-0.4.1.hpp"
#include "properties.hpp"
#include "utils.h"


void Configuration::readFromProperties(const std::string& fileName) {
    auto props = cppproperties::PropertiesParser::Read(fileName);
    auto names = props.GetPropertyNames();
    for (auto & name : names) {
        if(name.find("sjl.javaHome") != std::string::npos){
            javaHome = props.GetProperty(name);
        } else if(name.find("sjl.libFolder") != std::string::npos){
            libFolder = props.GetProperty(name);
        }else if(name.find("sjl.tempFolder") != std::string::npos){
            tempFolder = props.GetProperty(name);
        } else if(name.find("sjl.jvmOptions") != std::string::npos){
            commonArgs.push_back("-"+ props.GetProperty(name));
        }else if(name.find("sjl.arguments.common") != std::string::npos){
            commonArgs.push_back(props.GetProperty(name));
        }else if(name.find("sjl.arguments.start") != std::string::npos){
            startArgs.push_back(props.GetProperty(name));
        }else if(name.find("sjl.arguments.stop") != std::string::npos){
            stopArgs.push_back(props.GetProperty(name));
        }else if(name.find("sjl.arguments.status") != std::string::npos){
            statusArgs.push_back(props.GetProperty(name));
        }else if (name.find("sjl.service.name") != std::string::npos) {
            serviceName = props.GetProperty(name);
        }else if (name.find("sjl.service.displayName") != std::string::npos) {
            serviceDisplayName = props.GetProperty(name);
        }else if (name.find("sjl.service.description") != std::string::npos) {
            serviceDescription = props.GetProperty(name);
        }
    }
}

std::string toString(ryml::basic_substring<const char> substring){
    if(substring == (c4::substr) nullptr){
        return "";
    }
    std::string data;
    c4::from_chars(substring, &data);
    return data;
}

std::string getYamlValue(c4::yml::NodeRef node, std::string key){
    c4::substr ckey = c4::to_substr(key);
    c4::yml::NodeRef child = node[ckey];
    if(child == nullptr){
        return "";
    }
    return toString(child.val());
}

void Configuration::readFromYaml(const std::string& fileName) {
    auto str = readFile(fileName.c_str());
    ryml::Tree tree = ryml::parse_in_arena(ryml::to_csubstr(str));
    /*auto tree = ryml::parse_in_arena(R"(
sjl:
  service:
    name: sjl - service
    displayName : Sjl Service
    description : Пример службы
)");*/
    ryml::NodeRef sjlNode = tree["sjl"];
    if(sjlNode == nullptr) {
        return;
    }
    libFolder = getYamlValue(sjlNode, "libFolder");
    tempFolder = getYamlValue(sjlNode, "tempFolder");
    javaHome = getYamlValue(sjlNode, "javaHome");
    auto serviceNode = sjlNode["service"];
    if (serviceNode != nullptr) {
        serviceName = getYamlValue(serviceNode, "name");
        serviceDisplayName = getYamlValue(serviceNode, "displayName");
        serviceDescription = getYamlValue(serviceNode, "description");
    }
    ryml::NodeRef vmOptionsNode = sjlNode["jvmOptions"];
    if(vmOptionsNode != nullptr){
        for(ryml::NodeRef const& child : vmOptionsNode.children()){
            std::string value = toString(child.val());
            if(value == "NULL"){
                commonArgs.push_back("-"+toString(child.key()));
            } else {
                commonArgs.push_back("-"+toString(child.key())+"="+value);
            }
        }
    }
    ryml::NodeRef argsNode = sjlNode["arguments"];
    if(argsNode != nullptr){
        ryml::NodeRef startArgsNode = argsNode["start"];
        if(startArgsNode != nullptr) {
            for (unsigned int i = 0; i < startArgsNode.num_children(); ++i) {
                startArgs.push_back(toString(startArgsNode[i].val()));
            }
        }
        ryml::NodeRef commonArgsNode = argsNode["common"];
        if(commonArgsNode != nullptr) {
            for (unsigned int i = 0; i < commonArgsNode.num_children(); ++i) {
                commonArgs.push_back(toString(commonArgsNode[i].val()));
            }
        }
        ryml::NodeRef stopArgsNode = argsNode["stop"];
        if(stopArgsNode != nullptr) {
            for (unsigned int i = 0; i < stopArgsNode.num_children(); ++i) {
                stopArgs.push_back(toString(stopArgsNode[i].val()));
            }
        }
        ryml::NodeRef statusArgsNode = argsNode["status"];
        if(statusArgsNode != nullptr) {
            for (unsigned int i = 0; i < statusArgsNode.num_children(); ++i) {
                statusArgs.push_back(toString(statusArgsNode[i].val()));
            }
        }
    }
}


