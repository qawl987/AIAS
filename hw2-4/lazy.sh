#!/bin/bash
cp ../t.cpp ./a.cc
clear
cd ./build
# cmake -DCMAKE_PREFIX_PATH=./libtorch ..
# cmake --build . --config Release -j ${nproc}
cmake -DCMAKE_PREFIX_PATH=./libtorch -DCMAKE_BUILD_TYPE=Debug ..
cmake --build . --config Debug -j ${nproc}