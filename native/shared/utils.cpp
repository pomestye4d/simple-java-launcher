#include "utils.h"
#include <sstream>
#include <codecvt>
#include <locale>
#include <fstream>
#include <cstring>
#include <stdarg.h>
#include <memory>
#include <vector>

namespace utf8 {

    std::string  convert(const std::wstring& s)
    {
        typedef std::codecvt_utf8<wchar_t>
            convert_typeX;
        std::wstring_convert<convert_typeX, wchar_t>
            converterX;
        return converterX.to_bytes(s);
    }
    std::wstring convert(const std::string& s)
    {
        typedef std::codecvt_utf8<wchar_t>
            convert_typeX;
        std::wstring_convert<convert_typeX, wchar_t>
            converterX;
        return converterX.from_bytes(s);
    }

}//namespace utf8 

namespace stdlocal {

    std::wstring convert(const char* first, const size_t len, const std::locale& loc)
    {
        if (len == 0)
            return std::wstring();
        const std::ctype<wchar_t>& facet =
            std::use_facet< std::ctype<wchar_t> >(loc);
        const char* last = first + len;
        std::wstring result(len, L'\0');
        facet.widen(first, last, &result[0]);
        return result;
    }

    std::string convert(
        const wchar_t* first,
        const size_t len,
        const std::locale& loc,
        const char default_char
    )
    {
        if (len == 0)
            return std::string();
        const std::ctype<wchar_t>& facet =
            std::use_facet<std::ctype<wchar_t> >(loc);
        const wchar_t* last = first + len;
        std::string result(len, default_char);
        facet.narrow(first, last, default_char, &result[0]);
        return result;
    }

    std::string  convert(const wchar_t* s, const std::locale& loc, const char default_char)
    {
        return convert(s, std::wcslen(s), loc, default_char);
    }
    std::string  convert(const std::wstring& s, const std::locale& loc, const char default_char)
    {
        return convert(s.c_str(), s.length(), loc, default_char);
    }
    std::wstring convert(const char* s, const std::locale& loc)
    {
        return convert(s, std::strlen(s), loc);
    }
    std::wstring convert(const std::string& s, const std::locale& loc)
    {
        return convert(s.c_str(), s.length(), loc);
    }

}

std::string toString(std::wstring data) {
#if defined(__GNUC__) || defined(__MINGW32__) || defined(__MINGW__)
    return utf8::convert(data);
#else
    return stdlocal::convert(data, std::locale(""), '?');
#endif
}

std::wstring toWString(std::string data) {
#if defined(__GNUC__) || defined(__MINGW32__) || defined(__MINGW__)
    return utf8::convert(data);
#else
    return stdlocal::convert(data, std::locale(""));
#endif
}

std::string readFile(const char* filename)
{
    std::wifstream wif(filename);
    wif.imbue(std::locale("en_US.UTF-8"));
    std::wstringstream wss;
    wss << wif.rdbuf();
    return toString(wss.str());
}

void writeToFile(const char* filename, const std::string& content){
    std::ofstream file;
    file.open(filename);
    file << content;
    file.close();
}

std::wstring test(const wchar_t* fmt, ...)
{
    std::wstring ret;

    va_list va;
    va_start(va, fmt);

    int size = vswprintf(nullptr, 0, fmt, va);
    if (size > 0)
    {
        ret.resize(size + 1);
        vswprintf(&ret[0], size + 1, fmt, va);
    }

    va_end(va);
    return ret;
}

std::string format(const char* fmt, ...)
{
    va_list args;
    va_start(args, fmt);
    std::vector<char> v(1024);
    while (true)
    {
        va_list args2;
        va_copy(args2, args);
        int res = vsnprintf(v.data(), v.size(), fmt, args2);
        if ((res >= 0) && (res < static_cast<int>(v.size())))
        {
            va_end(args);
            va_end(args2);
            return std::string(v.data());
        }
        size_t size;
        if (res < 0)
            size = v.size() * 2;
        else
            size = static_cast<size_t>(res) + 1;
        v.clear();
        v.resize(size);
        va_end(args2);
    }
}
std::wstring format(const wchar_t* fmt,  ...)
{
    va_list args;
    va_start(args, fmt);
    wchar_t wszFullCommand[2024];
    va_list args2;
    va_copy(args2, args);
    _snwprintf_s(wszFullCommand, _countof(wszFullCommand), _countof(wszFullCommand) - 1, fmt);
    return wszFullCommand;
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

