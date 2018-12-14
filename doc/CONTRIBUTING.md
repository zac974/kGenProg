# kGenProg Contribution Guide
## Introduction
Thank you

## Requirements
- JDK 1.8
- Gradle

## Getting setup
1. Get the repository
```shell
$ git clone https://github.com/kusumotolab/kGenProg
$ cd kGenProg
```
2. Build the project
```shell
$ ./gradlew build
```

### for Eclipse developers
1. Create eclipse configuration
```shell
$ ./gradlew eclipse
```

2. Import the project
```
File
 -> Import
 -> Existing projects into Workspace
 -> Next
 -> Specify "PATH_TO_REPO/"
 -> Finish
```

3. Follow the coding style
```
Package Explorer
 -> Right click
 -> Properties
 -> Java Code Style
 -> Formatter
 -> Enable project specific settings
 -> Import
 -> Specify "PATH_TO_REPO/settings/eclipse-coding-style.xml"
 -> Apply
```

### for Intellij developers
```shell
$ ./gradlew idea
```


## Coding style
Contributors must follow our defined coding style.
This style is based on [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

Please use LF as line ending.


https://github.com/openwrt/packages/blob/master/CONTRIBUTING.md
https://github.com/cefsharp/CefSharp/blob/master/CONTRIBUTING.md
https://github.com/GoogleCloudPlatform/Template/blob/master/CONTRIBUTING.md
https://github.com/openwrt/packages/blob/master/CONTRIBUTING.md
https://github.com/TheDevPath/Navi/blob/development/CONTRIBUTING.md
https://github.com/WeAllJS/weallcontribute/blob/latest/CONTRIBUTING.md
