//
// Created by avramenko on 16.09.2022.
//
#define _SILENCE_ALL_CXX17_DEPRECATION_WARNINGS
#define _CRT_SECURE_NO_WARNINGS

#ifndef PG_UTILS_H
#define PG_UTILS_H
#include <string>
std::string readFile(const char* filename);
std::string toString(const std::wstring data);
std::wstring toWString(const std::string data);
void writeToFile(const char* filename, const std::string& content);
std::string string_format(std::string  fmt, ... );
std::wstring test(const wchar_t* fmt, ...);
std::string format(const char*  fmt, ...);
std::wstring format(const wchar_t* fmt, ...);
bool endsWith(const std::string& str, const std::string& ending);
#endif //PG_UTILS_H