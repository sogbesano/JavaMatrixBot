import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Core implements Module {

    public static String caw() {
        Random random = new Random();
        int max = 200;
        int min = 1;
        int cawLength = random.nextInt(max) + min;
        String caw = "C";
        char upperCaseA = 'A';
        for(int i = 0; i < cawLength; i++) {
            caw += upperCaseA;
        }
        caw += "W";
        return caw;
    }

    public static String listmodules(Object[] modules) {
        String listModulesStr = "[";
        for(Object module : modules) {
            Module mod = (Module) module;
            listModulesStr += mod.getName() + ",";
        }
        listModulesStr = listModulesStr.substring(0, listModulesStr.length() - 1);//remove end ,
        listModulesStr += "]";
        return listModulesStr;
    }

    public static String listcommands(Object[] modules) {
        List<Method> allModuleCommandMethods = null;
        String listCommandsStr = "[";
        for(Object module : modules) {
            Module mod = (Module) module;
            allModuleCommandMethods = Utils.getStaticMethods(mod.getClass());
            for(Method moduleCommandMethod : allModuleCommandMethods) {
                listCommandsStr += moduleCommandMethod.getName() + ",";
            }
        }
        listCommandsStr = listCommandsStr.substring(0, listCommandsStr.length() - 1);//remove end ,
        listCommandsStr += "]";
        return listCommandsStr;
    }

    public static ModulesAndModuleCommandResponse loadmodule(Object[] modules, String moduleName) {
        List<Module> loadedModules = new ArrayList<>();
        ModulesAndModuleCommandResponse modulesAndModuleCommandResponse = null;
        for(Object module : modules) {
            loadedModules.add((Module) module);
        }
        String moduleCommandResponse = "";
        for(Object module : modules) {//module already loaded
            Module mod = (Module) module;
            if(moduleName.equals(mod.getName())) {
               moduleCommandResponse += mod.getName() + " already loaded";
               modulesAndModuleCommandResponse = new ModulesAndModuleCommandResponse(loadedModules, moduleCommandResponse);
               return modulesAndModuleCommandResponse;
            }
        }
        for(Object module : modules) {//module not loaded
                if(moduleName.equals("core")) {
                    loadedModules.add(new Core());
                    moduleCommandResponse += "core loaded";
                } else if(moduleName.equals("urban dictionary")) {
                    loadedModules.add(new UrbanDictionary());
                    moduleCommandResponse +=  "urban dictionary loaded";
                } else if(moduleName.equals("acronym")) {
                    loadedModules.add(new Acronym());
                    moduleCommandResponse +=  "acronym loaded";
                }
        }
        modulesAndModuleCommandResponse = new ModulesAndModuleCommandResponse(loadedModules, moduleCommandResponse);
        return modulesAndModuleCommandResponse;
    }

    public static ModulesAndModuleCommandResponse unloadmodule(Object[] modules, String moduleName) {
        List<Module> loadedModules = new ArrayList<>();
        String moduleCommandResponse = "";
        ModulesAndModuleCommandResponse modulesAndModuleCommandResponse = null;
        for(Object module : modules) {
            loadedModules.add((Module) module);
        }
        List<Module> removedModule = new ArrayList<>();
        for(Object module : modules) {//module loaded
            Module mod = (Module) module;
            if(mod.getName().equals(moduleName) && !mod.getName().equals("core")) {
                moduleCommandResponse += mod.getName() + " unloaded";
                modulesAndModuleCommandResponse = new ModulesAndModuleCommandResponse(removedModule, moduleCommandResponse);
                return modulesAndModuleCommandResponse;
            }
            removedModule.add(mod);
        }
        if(!moduleName.equals("core")) {
            moduleCommandResponse = moduleName + " not loaded";//module not loaded
        } else {
            moduleCommandResponse = moduleName + " cannot be unloaded";
        }
        modulesAndModuleCommandResponse = new ModulesAndModuleCommandResponse(loadedModules, moduleCommandResponse);
        return modulesAndModuleCommandResponse;
    }

    @Override
    public List<Method> getAllMethods() {
        Class coreClass = Core.class;
        List<Method> allMethods = Arrays.asList(coreClass.getDeclaredMethods());
        return allMethods;
    }

    @Override
    public String getName() {
        return "core";
    }
}
