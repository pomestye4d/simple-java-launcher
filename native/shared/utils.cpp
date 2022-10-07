#include "utils.h"
#include <sstream>
#include <codecvt>
#include <locale>
#include <fstream>
#include <cstring>
#include <stdarg.h>
#include <memory>

std::string readFile(const char* filename)
{
    std::wifstream wif(filename);
    wif.imbue(std::locale("en_US.UTF-8"));
    std::wstringstream wss;
    wss << wif.rdbuf();
    using convert_typeX = std::codecvt_utf8<wchar_t>;
    std::wstring_convert<convert_typeX, wchar_t> converterX;
    std::string str = converterX.to_bytes(wss.str());
    return str;
}

void writeToFile(const char* filename, const std::string& content){
    std::ofstream file;
    file.open(filename);
    file << content;
    file.close();
}


std::string string_format(const std::string fmt_str, ...) {
    int final_n, n = ((int)fmt_str.size()) * 2; /* Reserve two times as much as the length of the fmt_str */
    std::unique_ptr<char[]> formatted;
    va_list ap;
    while(1) {
        formatted.reset(new char[n]); /* Wrap the plain char array into the unique_ptr */
        strcpy(&formatted[0], fmt_str.c_str());
        va_start(ap, fmt_str);
        final_n = vsnprintf(&formatted[0], n, fmt_str.c_str(), ap);
        va_end(ap);
        if (final_n < 0 || final_n >= n)
            n += abs(final_n - n + 1);
        else
            break;
    }
    return std::string(formatted.get());
}

bool endsWith(const std::string& str, const std::string& ending) {
    return str.compare(str.length() - ending.length(), ending.length(), ending) == 0;
}

