# java9-classloaders

_Reference_: https://www.amazon.com/Java-Language-Features-Modules-Expressions/dp/1484233476  
_Reference_: https://stackoverflow.com/questions/46494112/classloaders-hierarchy-in-java-9

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

## JDK 9
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