## zxingcpp

Built from 78c62b2573a72b07760f0ef334ef9ae0193b3f68 with the following cmake options (just add those lines to `wrappers/android/zxingcpp/src/main/cpp/CMakeLists.txt` **directly below** the other `set`s):

```cmake
set(ZXING_C_API OFF)
set(ZXING_EXAMPLES OFF)
set(ZXING_ENABLE_UNICODE OFF)


set(ZXING_ENABLE_1D OFF)
set(ZXING_ENABLE_AZTEC OFF)
set(ZXING_ENABLE_DATAMATRIX OFF)
set(ZXING_ENABLE_MAXICODE OFF)
set(ZXING_ENABLE_PDF417 OFF)
```

All architectures (except arm64) were removed from the aar. In order to be reproducible with F-Droid, you must build it inside the fdroidbuildserver, see [this comment](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49149#note_3850390886) for more details.
