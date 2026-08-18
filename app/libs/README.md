## zxingcpp

Build from 7e51e7b16ce14e8cd9399e84fd2d854172e9d933 with the following cmake options (just add those lines to `wrappers/android/zxingcpp/src/main/cpp/CMakeLists.txt` **directly below** the other `set`s):

```cmake
set(ZXING_C_API OFF)
set(ZXING_EXAMPLES OFF)


set(ZXING_ENABLE_1D OFF)
set(ZXING_ENABLE_AZTEC OFF)
set(ZXING_ENABLE_DATAMATRIX OFF)
set(ZXING_ENABLE_MAXICODE OFF)
set(ZXING_ENABLE_PDF417 OFF)
```

And you must replace all `#if 0` with `#if 1` inside `core/src/TextEncoder.cpp` and `core/src/TextDecoder.cpp`. This will be another cmake option in the future, see [Discussion 1155](https://github.com/zxing-cpp/zxing-cpp/discussions/1155).

All architectures (except arm64) were removed from the aar.
