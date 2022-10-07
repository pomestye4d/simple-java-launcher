//
// Created by avramenko on 16.09.2022.
//

#ifndef PG_UTILS_H
#define PG_UTILS_H
#include <string>
std::string readFile(const char* filename);
void writeToFile(const char* filename, const std::string& content);
std::string string_format(std::string  fmt, ... );
bool endsWith(const std::string& str, const std::string& ending);
#endif //PG_UTILS_H