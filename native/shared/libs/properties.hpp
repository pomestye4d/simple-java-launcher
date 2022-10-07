/*
 * Copyright (c) 2014 Fredy Wijaya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#include <fstream>
#include <iostream>
/*
 * Copyright (c) 2014 Fredy Wijaya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#ifndef PROPERTIESPARSER_H_
#define PROPERTIESPARSER_H_

#include <string>
#include <exception>
/*
 * Copyright (c) 2014 Fredy Wijaya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#ifndef PROPERTIES_H_
#define PROPERTIES_H_

#include <string>
#include <vector>
#include <map>
/*
 * Copyright (c) 2018 Mario Emmenlauer (mario@emmenlauer.de)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#ifndef PROPERTIESEXCEPTION_H_
#define PROPERTIESEXCEPTION_H_

#include <string>
#include <exception>

namespace cppproperties {

class PropertiesException : public std::exception {
public:
    PropertiesException() {}
    PropertiesException(const std::string& msg) throw() : message(msg) {}

    virtual ~PropertiesException() throw() {}

    const std::string& str() const throw() { return message; }

    virtual const char* what() const throw() { return message.c_str(); }

private:
    std::string message;
};

} /* namespace cppproperties */

#endif /* PROPERTIESEXCEPTION_H_ */
/*
 * Copyright (c) 2018 Mario Emmenlauer (mario@emmenlauer.de)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#ifndef PROPERTYNOTFOUNDEXCEPTION_H_
#define PROPERTYNOTFOUNDEXCEPTION_H_

#include <string>
#include <exception>

namespace cppproperties {

class PropertyNotFoundException : public std::exception {
public:
    PropertyNotFoundException() {}
    PropertyNotFoundException(const std::string& msg) throw() : message(msg) {}

    virtual ~PropertyNotFoundException() throw() {}

    const std::string& str() const throw() { return message; }

    virtual const char* what() const throw() { return message.c_str(); }

private:
    std::string message;
};

} /* namespace cppproperties */

#endif /* PROPERTYNOTFOUNDEXCEPTION_H_ */

namespace cppproperties {

class Properties {
public:
    Properties();
    virtual ~Properties();

    /**
     * Gets the property value from a given key.
     *
     * This method throws a PropertyNotFoundException when a given key does not
     * exist.
     */
    std::string GetProperty(const std::string& key) const;

    /**
     * Gets the property value from a given key. Use a default value if not found.
     */
    std::string GetProperty(const std::string& key, const std::string& defaultValue) const;

    /**
     * Gets the list of property names.
     */
    std::vector<std::string> GetPropertyNames() const;

    /**
     * Adds a new property. If the property already exists, it'll overwrite
     * the old one.
     */
    void AddProperty(const std::string& key, const std::string& value);

    /**
     * Removes the property from a given key.
     *
     * If the property doesn't exist a PropertyNotFoundException will be thrown.
     */
    void RemoveProperty(const std::string& key);
private:
    // to preserve the order
    std::vector<std::string> keys;
    std::map<std::string, std::string> properties;
};

} /* namespace cppproperties */

#endif /* PROPERTIES_H_ */

namespace cppproperties {

class PropertiesParser {
public:
    PropertiesParser();
    virtual ~PropertiesParser();

    /**
     * Reads a properties file and returns a Properties object.
     */
    static Properties Read(const std::string& file);

    /**
     * Writes Properties object to a file.
     */
    static void Write(const std::string& file, const Properties& props);
};

} /* namespace cppproperties */

#endif /* PROPERTIESPARSER_H_ */
/*
 * Copyright (c) 2014 Fredy Wijaya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
#ifndef PROPERTIESUTILS_H_
#define PROPERTIESUTILS_H_

#include <string>
#include <utility>

namespace cppproperties {
namespace PropertiesUtils {

/**
 * Left trims a string.
 * This function doesn't modify the given str.
 */
std::string LeftTrim(const std::string& str);

/**
 * Right trims a string.
 * This function doesn't modify the given str.
 */
std::string RightTrim(const std::string& str);

/**
 * Trims a string (perform a left and right trims).
 * This function doesn't modify the given str.
 */
std::string Trim(const std::string& str);

/**
 * Is a given string a property. A property must have the following format:
 * key=value
 */
bool IsProperty(const std::string& str);

/**
 * Parses a given property into a pair of key and value.
 *
 * ParseProperty assumes a given string has a correct format.
 */
std::pair<std::string, std::string> ParseProperty(const std::string& str);

/**
 * Is a given string a comment? A comment starts with #
 */
bool IsComment(const std::string& str);

/**
 * Is a given string empty?
 */
bool IsEmptyLine(const std::string& str);

} // namespace PropertiesUtils
} // namespace cppproperties

#endif /* PROPERTIESUTILS_H_ */

namespace cppproperties {

PropertiesParser::PropertiesParser() {
}

PropertiesParser::~PropertiesParser() {
}

Properties PropertiesParser::Read(const std::string& file) {
    Properties properties;

    std::ifstream is;
    is.open(file.c_str());
    if (!is.is_open()) {
        throw PropertiesException("PropertiesParser::Read(" + file + "): Unable to open file for reading.");
    }

    try {
        size_t linenr = 0;
        std::string line;
        while (getline(is, line)) {
            if (PropertiesUtils::IsEmptyLine(line) || PropertiesUtils::IsComment(line)) {
                // ignore it
            } else if (PropertiesUtils::IsProperty(line)) {
                std::pair<std::string, std::string> prop = PropertiesUtils::ParseProperty(line);
                properties.AddProperty(prop.first, prop.second);
            } else {
                throw PropertiesException("PropertiesParser::Read(" + file + "): Invalid line " + std::to_string(linenr) + ": " + line);
            }
            ++linenr;
        }
        is.close();
    } catch (...) {
        // don't forget to close the ifstream
        is.close();
        throw;
    }

    return properties;
}

void PropertiesParser::Write(const std::string& file, const Properties& props) {
    std::ofstream os;
    os.open(file.c_str());
    if (!os.is_open()) {
        throw PropertiesException("PropertiesParser::Write(" + file + "): Unable to open file for writing.");
    }

    try {
        const std::vector<std::string>& keys = props.GetPropertyNames();
        for (std::vector<std::string>::const_iterator i = keys.begin();
            i != keys.end(); ++i) {
            os << *i << " = " << props.GetProperty(*i) << std::endl;
        }
        os.close();
    } catch (...) {
        // don't forget to close the ofstream
        os.close();
        throw;
    }
}

} /* namespace cppproperties */
/*
 * Copyright (c) 2014 Fredy Wijaya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

namespace cppproperties {
namespace PropertiesUtils {

namespace {
const std::string TRIM_DELIMITERS = " \f\n\r\t\v";
std::string ltrim(const std::string& str) {
    std::string::size_type s = str.find_first_not_of(TRIM_DELIMITERS);
    if (s == std::string::npos) {
        return "";
    }
    return str.substr(s);
}
}

std::string RightTrim(const std::string& str) {
    std::string::size_type s = str.find_last_not_of(TRIM_DELIMITERS);
    if (s == std::string::npos) {
        return "";
    }
    return str.substr(0, s+1);
}

std::string LeftTrim(const std::string& str) {
    std::string rstr = ltrim(str);

    while (rstr != ltrim(rstr)) {
        rstr = ltrim(rstr);
    }

    return rstr;
}

std::string Trim(const std::string& str) {
    return RightTrim(LeftTrim(str));
}

bool IsProperty(const std::string& str) {
    std::string trimmedStr = LeftTrim(str);
    std::string::size_type s = trimmedStr.find_first_of("=");
    if (s == std::string::npos) {
        return false;
    }
    std::string key = Trim(trimmedStr.substr(0, s));
    // key can't be empty
    if (key == "") {
        return false;
    }
    return true;
}

std::pair<std::string, std::string> ParseProperty(const std::string& str) {
    std::string trimmedStr = LeftTrim(str);
    std::string::size_type s = trimmedStr.find_first_of("=");
    std::string key = Trim(trimmedStr.substr(0, s));
    std::string value = LeftTrim(trimmedStr.substr(s+1));

    return std::pair<std::string, std::string>(key, value);
}

bool IsComment(const std::string& str) {
    std::string trimmedStr = LeftTrim(str);
    return trimmedStr[0] == '#';
}

bool IsEmptyLine(const std::string& str) {
    std::string trimmedStr = LeftTrim(str);
    return trimmedStr == "";
}

} // namespace PropertiesUtils
} // namespace cppproperties

/*
 * Copyright (c) 2014 Fredy Wijaya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
#include <algorithm>

namespace cppproperties {

Properties::Properties() {
}

Properties::~Properties() {
}

std::string Properties::GetProperty(const std::string& key) const {
    if (properties.find(key) == properties.end()) {
        throw PropertyNotFoundException(key + " does not exist");
    }
    return properties.at(key);
}

std::string Properties::GetProperty(const std::string& key, const std::string& defaultValue) const {
    if (properties.find(key) == properties.end()) {
        return defaultValue;
    }
    return properties.at(key);
}

std::vector<std::string> Properties::GetPropertyNames() const {
    return keys;
}

void Properties::AddProperty(const std::string& key, const std::string& value) {
    if (properties.find(key) == properties.end()) {
        keys.push_back(key);
    }
    properties[key] = value;
}

void Properties::RemoveProperty(const std::string& key) {
    if (properties.find(key) == properties.end()) {
        throw PropertyNotFoundException(key + " does not exist");
    }
    keys.erase(std::remove(keys.begin(), keys.end(), key), keys.end());
    properties.erase(key);
}

} /* namespace cppproperties */
