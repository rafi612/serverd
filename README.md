# serverd
![License](https://img.shields.io/github/license/rafi612/serverd)
![Repo size](https://img.shields.io/github/repo-size/rafi612/serverd)

ServerD is TCP and UDP server which allows to communicate between clients and manage them.

# Building
## Requirements
- JDK 11+
- Maven or Ant

To build run the command: `mvn package`

If you don't have Maven you can use Maven wrapper: `./mvnw package`

Jar file can be found in `./target`

You can also build using Ant by command: `ant`

# Plugins
ServerD have plugin API which allows to create advanced plugins to extends program functionality.

**API includes:**
- Events (Connection and Disconnection event etc.) support
- Custom commands support
- Debugger
- Plugin workspaces

And much more.

**Example plugin:**

```java
package example;

import com.serverd.plugin.Plugin;
import com.serverd.plugin.Plugin.Info;
import com.serverd.plugin.ServerdPlugin;

public class Example implements ServerdPlugin {

	@Override
	public String init(Plugin plugin) {
		//init plugin
		return null;
	}

	@Override
	public void metadata(Info info) {
		info.name = "Example";
		info.author = "Author";
		info.decription = "Example Plugin";
		info.version = "1.0";
	}

	@Override
	public void stop(Plugin plugin) {
		// stop plugin
	}

	@Override
	public void work(Plugin plugin) {
		// plugin main method
	}
}
```

Plugin main class must be marked like this: `Plugin-Main-Class: main.class.Name` in **MANIFEST.MF** file

Plugins can be created using all Java-compatible languages (Kotlin, Scala etc.)

For more info look for **Documentation**.

# Documentation

To generate Javadoc run command: `./mwnw javadoc:javadoc`

Javadoc can be found in `./target/apidocs/index.html` or `./target/ServerD-<version>-javadoc.jar` as jar file.



