[![Build Status](https://travis-ci.com/mtumilowicz/java9-classloaders.svg?branch=master)](https://travis-ci.com/mtumilowicz/java9-classloaders)

# java9-classloaders

_Reference_: https://www.amazon.com/Java-Language-Features-Modules-Expressions/dp/1484233476  
_Reference_: https://stackoverflow.com/questions/46494112/classloaders-hierarchy-in-java-9  
_Reference_: [2019 - Krzysztof Chruściel - Kilka wskazówek jak projektować użyteczne interfejsy](https://www.youtube.com/watch?v=-_dhEkdlsew)

# prior JDK 9
## definition
The Java ClassLoader is a part of the JRE that dynamically loads Java classes into the Java Virtual Machine. 
Usually classes are only loaded on demand. Class Loader is a component with the Java Execution Engine which 
loads the binary data from the `.class` files available in the classpath into the Method Area. Loading of a 
class into the method area occurs only the first time when the class is referenced with in the running Java 
application. For all other references the data is reused from the method area, unless the class has been UNLOADED.  

All JVM (Java virtual machines) include one class loader that is embedded in the virtual machine. This 
embedded loader is called the **primordial** or **bootstrap** class loader. It is somewhat special 
because the VM (virtual machine) assumes that it has access to a repository of trusted classes which can be 
run by the virtual machine without verification.

## types
When the JVM is started, three class loaders are used:
* **Bootstrap class loader** - loads the core Java libraries located in the `<JAVA_HOME>/jre/lib` directory. 
    This class loader, which is part of the core JVM, is written in native code (mostly in `C`). Bootstrap 
    class loader don't have any parents.
    * loads for example `Object` class
    * represented by `null` in code (`Object.class.getClassLoader() == null`)
* **Extensions class loader** - loads the code in the extensions directories (`<JAVA_HOME>/jre/lib/ext`, or 
    any other directory specified by the `java.ext.dirs` system property). 
    It is implemented by the `sun.misc.Launcher$ExtClassLoader` class.
* **System class loader** - loads code found on `java.class.path`, which maps to the `CLASSPATH` environment 
    variable. It is implemented by the `sun.misc.Launcher$AppClassLoader` class.

## properies
`ClassLoader` in Java works on three principle: 
* **Delegation** - when loading a class, a class loader first "delegates" the search for the class to its 
    parent class loader before attempting to find the class itself.
* **Visibility** - allows child class loader to see all the classes loaded by parent class loader, but parent 
    class loader cannot see classes loaded by child.
* **Uniqueness** - allows to load a class exactly once, which is basically achieved by delegation 
    (it ensures that child class loader doesn't reload the class already loaded by parent).

_Remark_: It is completely possible to write class loader which violates Delegation and Uniqueness principles 
    and loads class by itself, its not something which is beneficial. You should follow all class loader 
    principle while writing your own ClassLoader.

_Remark_: Each class loader has its namespace that stores the loaded classes. When a class loader 
loads a class, it searches the class based on FQCN (Fully Qualified Class Name) stored in the namespace to 
check whether or not the class has been already loaded. Even if the class has an identical FQCN but a 
different namespace, it is regarded as a different class. A different namespace means that the class has 
been loaded by another class loader.

_Remark_: The initial class is loaded with the help of public static main() method declared in your class.

## phases
1. loading
    * check if classes (`.class`) match JVM specification, have well-defined structure
        * example - `CAFEBABE` prefix
            * windows differs files by extensions (`.exe`), JVM by prefix
    * check java version
        * example - `invokedynamic` cannot be used with java 6, `UnsupportedVersionException` 
1. linking
    * verifying - check if classes are correct, otherwise `VerifyError`
        * example - accessibility
        * expensive - spring boot loads thousands of classes - every class have to be verified
        * `-Xverify:none`, `-noverify`
    * preparing - default for static fields
        * example - int -> 0
        * basic structures for JVM
    * resolving - constant pool has to become runtime constant pool
        * optional - we dont need to load everything at once
1. initializing
    * static blocks (thread safe)

# JDK 9
* classes are loaded into off-heap regions
    * Runtime Constant Pool
    * ByteCode
    * Field/Method Data
* bootstrap class loader is implemented in the library code and in the virtual machine 
(still represented by `null` in a program - backward compatibility)
* no longer support for the extension mechanism
* extension class loader -> platform class loader
* platform class loader purpose: classes loaded by the bootstrap class loader have all permissions 
by default - several classes did not need all permissions - they are de-privileged in JDK9 loaded by the
 platform class loader
    * loads for example: `java.sql`
* application class loader loads the application modules found on the module path and a few JDK
  modules (for example `jdk.compiler`, `jdk.javadoc`, `jdk.jshell`)
* **Before JDK9, the extension and the application class loader were an instance of the
  `java.net.URLClassLoader` class. In JDK9, they are an instance of an internal JDK class**.
* apart from standard delegation: application -> platform -> bootstrap we have two more:
    * application -> bootstrap
    * platform -> application
* class loading mechanism:
    1. application class loader needs to load a class
    1. it searches modules defined to bootstrap and platform (can delegate directly)
    1. if the class is found in the module defined to bootstrap or platform it is loaded
    1. if a class is not found in a named module defined to bootstrap or platform,
        application delegates loading to platform
    1. if a class is not loaded, application scans the classpath
    1. if found - load as a inhabitant of unnamed module
    1. if not - `ClassNotFoundException`

# project description
1. java 9
    ```
    sourceCompatibility = 9
    ```
1. print all modules with classloaders
    ```
    ModuleLayer layer = ModuleLayer.boot();
    layer.modules().forEach(module -> {
        ClassLoader classLoader = module.getClassLoader();
        String classLoaderName = isNull(classLoader) ? "bootstrap" : classLoader.getName();
        System.out.println(classLoaderName + ": " + module.getName());
    });
    ```
1. output (may differ on different OS)
    ```
    platform: jdk.localedata
    bootstrap: java.base
    bootstrap: java.security.sasl
    platform: jdk.zipfs
    app: jdk.jlink
    bootstrap: java.xml
    platform: jdk.crypto.ec
    platform: jdk.accessibility
    bootstrap: jdk.management.jfr
    app: jdk.compiler
    platform: jdk.naming.dns
    bootstrap: jdk.management
    bootstrap: java.naming
    bootstrap: jdk.naming.rmi
    platform: java.compiler
    bootstrap: java.instrument
    bootstrap: java.rmi
    app: jdk.internal.opt
    bootstrap: java.prefs
    app: jdk.jdeps
    bootstrap: java.management.rmi
    platform: jdk.crypto.mscapi
    app: jdk.jartool
    platform: java.security.jgss
    bootstrap: java.management
    platform: jdk.crypto.cryptoki
    platform: java.smartcardio
    platform: jdk.security.jgss
    bootstrap: java.desktop
    app: jdk.javadoc
    platform: jdk.charsets
    app: jdk.unsupported.desktop
    app: project // out project
    platform: jdk.security.auth
    platform: java.xml.crypto
    bootstrap: java.logging
    bootstrap: jdk.jfr
    bootstrap: java.datatransfer
    ```
