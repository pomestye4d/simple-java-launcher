cmake_minimum_required(VERSION 3.23)
project(shared)

set(CMAKE_CXX_STANDARD 17)
include_directories(libs)
#add_executable(shared main.cpp configuration.h configuration.cpp libs/properties.hpp libs/rapidyaml-0.4.1.hpp
#        utils.h utils.cpp launcher_data.cpp launcher_data.h)
add_executable(shared main.cpp utils.h utils.cpp launcher_data.cpp launcher_data.h configuration.h configuration.cpp)