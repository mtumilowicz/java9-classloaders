package project;

import static java.util.Objects.isNull;

/**
 * Created by mtumilowicz on 2019-02-19.
 */
class Printer {
    public static void main(String[] args) {
        ModuleLayer layer = ModuleLayer.boot();
        layer.modules().forEach(module -> {
            ClassLoader classLoader = module.getClassLoader();
            String classLoaderName = isNull(classLoader) ? "bootstrap" : classLoader.getName();
            System.out.println(classLoaderName + ": " + module.getName());
        });
    }
}
