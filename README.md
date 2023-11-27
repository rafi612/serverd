# serverd

[![Build](https://img.shields.io/github/actions/workflow/status/rafi612/serverd/build.yml?branch=main)](https://github.com/rafi612/serverd/actions/workflows/maven.yml)
[![Release](https://img.shields.io/github/v/release/rafi612/serverd)](https://github.com/rafi612/serverd/releases)
[![License](https://img.shields.io/github/license/rafi612/serverd)](https://github.com/rafi612/serverd/blob/main/LICENSE)
[![Repo size](https://img.shields.io/github/repo-size/rafi612/serverd)](https://github.com/rafi612/serverd)

**ServerD** is a framework that allows creating network applications implementing either 
a known protocol or a custom one on the _TCP_, _UDP_, or other layer. ServerD has huge API
which can be used to implement our own protocol on transport layer (e.g. TCP) 
or use some existing protocols (e.g. _HTTP_). 

ServerD currently have only implementation of _TCP_ and _UDP_
transport layer and simple command protocol to show how program work. 
Other protocols can be implemented using plugins created by other developers.
ServerD may receive official implementations of certain protocols in the future.

# Getting started
ServerD have API which can be used both to create a standalone application and as a plugin. More information below.

### Plugins
ServerD have plugin API which allows to create advanced plugins to extends program functionality.

**API includes:**
- Events (Connection and Disconnection event etc.) support
- Simple protocol (that can be replaced by your own)
- Custom transport layer support.
- Custom protocol implementation support.
- Creating own servers support.
- Logger.
- Plugin workspaces.

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
		return INIT_SUCCESS;
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

### Creating own self-contained application.
Same API can be used both to create a standalone application and as a plugin.
To start application you can put this code into main method:

```java
public class Example {
    public static void main(String[] args) {
        ServerdApplication.run(ExamplePluginClass.class);
    }
}
```

# Documentation
To generate Javadoc run command: `./mwnw javadoc:javadoc`

Javadoc can be found in `./target/apidocs/index.html` or `./target/ServerD-<version>-javadoc.jar` as jar file.

# Building
### Requirements
- JDK 11+
- Maven or Ant

### Building
To build run the command: `mvn package`

If you don't have Maven you can use Maven wrapper: `./mvnw package`

Jar file can be found in `./target`

You can also build using Ant by command: `ant`

# Docker

ServerD can be run using Docker. 

To build Docker image run command:
`docker build -t rafi612/serverd .`

Then run using command:
`docker run -p 9999:9999 -p 9998:9998/udp -v ./data:/app/data rafi612/serverd`

Or create **docker-compose.yml** file and paste following:

```yaml
version: '3'
services:
  serverd:
    image: rafi612/serverd
    container_name: serverd
    environment:
      - TIMEOUT=90000
      - ENABLE_TCP=true
      - ENABLE_UDP=true
    ports:
      - 9999:9999/tcp
      - 9998:9998/udp
    volumes:
      - ./data:/app/data
```

And run using command: `docker compose up`


