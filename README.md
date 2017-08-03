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

This Library only supports SDK Version >= 16

#Using Loglytics Platform
After library installation you need to create an Intent in order to start the service.
Create this intent inside your ``MainActivity.java``

```java
    Intent intentService = new Intent(this, LoglyticsService.class);
```

Then you need to pass a token and url to created Intent.
You can get this token after signup at https://loglytics.herokuapp.com
```java
   intentService.putExtra("token", "<YOUR_LIBRARY_TOKEN_HERE>");
   intentService.putExtra("serverURL", "https://loglytics.herokuapp.com");
   startService(intentService);
```

#Test Locally
If you wish you can test locally by having socketIO server with a nodeJS
Add following this to your ```MainActivity.java```
```java
   Intent intentService = new Intent(this, LoglyticsService.class);
   intentService.putExtra("token", "THIS_SERVES_AS_A_TOKEN");
   startService(intentService);
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
```
node app.js
```

