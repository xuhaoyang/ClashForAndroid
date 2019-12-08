## Clash for Android

A GUI for [clash](https://github.com/Dreamacro/clash) on Android

> NOTICE: Early testing currently



### Feature

Fully feature of [clash](https://github.com/Dreamacro/clash) ~~(Exclude `external-controller`~~



### Requirement

* Android 7.0+
* `arm64` or `x86_64` architecture



### License

See also [LICENSE](./LICENSE) and [NOTICE](./NOTICE)



### Build

1. Update submodules

   ```bash
   git submodule update --init --recursive
   ```

2. Install `Android SDK` ,`Android NDK` and `Golang`

3. Configure `local.properties` 

   ```properties
   sdk.dir=/path/to/android-sdk
   ndk.dir=/path/to/android-ndk
   ```

4. Get `google-service.json` from [Firebase](https://firebase.google.com) and copy it to `app/google-service.json`

5. Build

   on Linux

   ```bash
   ./gradlew build
   ```

   on Windows

   ```bash
   .\gradlew.bat build
   ```

   