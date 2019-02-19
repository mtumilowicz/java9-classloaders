package project;

/**
 * Created by mtumilowicz on 2019-02-19.
 */
class Printer {
    public static void main(String[] args) {
        ModuleLayer layer = ModuleLayer.boot();
        layer.modules().forEach(module -> {
            ClassLoader loader = module.getClassLoader();
            String loaderName = loader == null ? "bootstrap" : loader.getName();
            System.out.printf("%s: %s%n", loaderName, module.getName());
        });
    }
}
