## zxingcpp

Built from 816a455c70091d160cef6129c7dd1f7dbbb311dc with the following cmake options (just add those lines to `wrappers/android/zxingcpp/src/main/cpp/CMakeLists.txt` **directly below** the other `set`s):

```cmake
set(ZXING_C_API OFF)
set(ZXING_EXAMPLES OFF)
set(ZXING_ENABLE_UNICODE OFF)


set(ZXING_ENABLE_1D OFF)
set(ZXING_ENABLE_AZTEC OFF)
set(ZXING_ENABLE_DATAMATRIX OFF)
set(ZXING_ENABLE_MAXICODE OFF)
set(ZXING_ENABLE_PDF417 OFF)

add_link_options("LINKER:--build-id=none")
```

All architectures (except arm64) were removed from the aar.
