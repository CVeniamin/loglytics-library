# loglytics-library

[![Release](https://jitpack.io/v/CVeniamin/loglytics-library.svg)](https://jitpack.io/#CVeniamin/loglytics-library)

Loglytics Android library project that works with jitpack.io.

For more details check out the [documentation](https://github.com/jitpack/jitpack.io/blob/master/ANDROID.md)

https://jitpack.io/#CVeniamin/loglytics-library

Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and:

```gradle
dependencies {
    compile 'com.github.CVeniamin:loglytics-library:{latest version}'
}
```

## Adding a sample app 

If you add a sample app to the same repo then your app needs to have a dependency on the library. To do this in your app/build.gradle add:

```gradle
    dependencies {
        compile project(':library')
    }
```


This Library only Supports SDK Version >= 16