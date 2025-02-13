cmake_minimum_required(VERSION 3.22.1)

project("facex")

# Add yuv_converter library
add_library(
        yuv_converter
        SHARED
        image_converter.cpp)

# Find required libraries
find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)

# Link libraries
target_link_libraries(
        yuv_converter
        ${log-lib}
        ${jnigraphics-lib}
        android)