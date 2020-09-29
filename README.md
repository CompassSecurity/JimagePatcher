# JImagePatcher  
The JDK tool `jimage`  supports only the extraction of jimage files but not the recreation of them. If you need to patch code or resources in a jimage file, you can use this tool to do so.

First extract the modules from the jimage file to a folder with the JDK tool:

    jimage extract --dir extracted-modules jimagefile
Then patch the files in the extracted folder as needed.

At least recreate the jimage file with this tool:

    java --add-opens jdk.jlink/jdk.tools.jlink.internal=jimagePatcher --module-path jimagePatcher.jar -m jimagePatcher/jimagePatcher.Run extracted-modules new-jimagefile

## Usage
    java --add-opens jdk.jlink/jdk.tools.jlink.internal=jimagePatcher --module-path jimagePatcher.jar -m jimagePatcher/jimagePatcher.Run input-folder output-file
## Infos
Stiched together with the help of 
http://hg.openjdk.java.net/jdk9/jdk9/jdk/rev/78a06bc11975
and
http://jar.fyicenter.com/3245_JDK_11_jdk_jlink_jmod-JLink_Tool.html

Because the jlink stuff is not exported in the module, we have to use reflection to access all that stuff. That reflection can access all that stuff, the runtime needs to be started with `--add-opens jdk.jlink/jdk.tools.jlink.internal=jimagePatcher`

## TODO
### Testing
This tool was only used for one project. It was only tested with OpenJDK 11.0.8
### Support for Plugins (e.g. Compressions)
The creation of jimages with the JDK tool `jlink` supports plugin options used for compression and other stuff... Currently the tool does not support any plugins.
