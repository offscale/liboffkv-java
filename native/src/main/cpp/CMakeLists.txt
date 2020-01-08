cmake_minimum_required (VERSION 3.10)

project (liboffkv VERSION 0.0.1)

set (CMAKE_CXX_STANDARD 17)

find_package(JNI REQUIRED)
find_package(Threads REQUIRED)

add_subdirectory(liboffkv)

add_library(offkv_java_bindings SHARED offkv_bindings.cpp)
target_link_libraries(offkv_java_bindings PUBLIC ${JNI_LIBRARIES} ${CMAKE_THREAD_LIBS_INIT})
target_link_libraries(offkv_java_bindings PUBLIC liboffkv)
target_include_directories(offkv_java_bindings PUBLIC ${JNI_INCLUDE_DIRS})
