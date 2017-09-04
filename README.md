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
Where `{latest version}` corresponds to tag version, e.g., ``V1.16``

## Adding a sample app 

If you add a sample app to the same repo then your app needs to have a dependency on the library. <br/>
To do this in your app/build.gradle add:

```gradle
    dependencies {
        compile project(':library')
    }
```

This Library only supports SDK Version >= 16

### Using Loglytics Platform
After library installation you need to create an Intent in order to start the service.
Create this intent inside your ``MainActivity.java``

```java
    import io.loglytics.LoglyticsService;
    ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        String serverURL = "https://loglytics.herokuapp.com";
        LoglyticsService.start(this, serverURL);
        ...
    }
```

Make sure that you stop the service when destroying your Activity. 
For that add following code inside `onDestroy()` method.
```java
    ...
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ...
        LoglyticsService.stop(this);
        
    }
```

Then you need to create <meta-data> tag and pass a token to it, <meta-data> must be nested inside <application> tag on AndroidManifest.xml as follows. <br/>
You can get this token after signup at https://loglytics.herokuapp.com
```xml
    <application
        ...
        <meta-data
            android:name="io.loglytics.token"
            android:value="YOUR_TOKEN_GOES_HERE"
            />
        ...
    </application>
```

### Test Locally

If you wish you can test locally by having socketIO server with a nodeJS.
Add following this to your ```MainActivity.java```. <br/>
```LoglyticsService.start()``` will use by default the following `http://10.0.2.2:8080` address which represents localhost on android emulator.
```java
   import io.loglytics.LoglyticsService;
       ...
       @Override
       protected void onCreate(Bundle savedInstanceState) {
           ...
           LoglyticsService.start(this);
           ...
       }
````

And having app.js as follows:
```node
var http = require('http'),
    io   = require('socket.io');

var app = http.createServer();
app.listen(8080);

// Socket.IO server
var io = io.listen(app);

io.on('connection', function (socket) {

    socket.on('log', function (data, fn) {
        console.log(data);
    });

    socket.on('disconnect', function () {
        console.log("disconnected");
    });
});
```

Run it with:
```cmd
node app.js
```

