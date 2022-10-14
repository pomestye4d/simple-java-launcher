cmake_minimum_required(VERSION 3.23)
project(gui)

set(CMAKE_CXX_STANDARD 17)

include_directories(libs ../shared ../shared/libs)

add_executable(sjl sjl.cpp
        ../shared/libs/rapidyaml-0.4.1.hpp ../shared/libs/properties.hpp
        ../shared/launcher_data.h ../shared/launcher_data.cpp
        ../shared/utils.cpp ../shared/utils.h
        ../shared/configuration.cpp ../shared/configuration.h
        )
