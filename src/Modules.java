import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Modules {

    public static List<Module> loadModules(List<String> moduleNames) {
        List<Module> modules = new ArrayList<>();
        for(String moduleName : moduleNames) {
            if(moduleName.equals("core")) {
                modules.add(new Core());
            } else if(moduleName.equals("urban dictionary")) {
                modules.add(new UrbanDictionary());
            } else if(moduleName.equals("acronym")) {
                modules.add(new Acronym());
            } else if(moduleName.equals("yesno")) {
                modules.add(new YesNo());
            } else if(moduleName.equals("cioran")) {
                modules.add(new Cioran());
            } else if(moduleName.equals("log")) {
                modules.add(new Log());
            }
        }
        return modules;
    }

    public static Method getModuleCommandMethod(String moduleCommandName, List<Method> moduleMethods) {
        Method moduleCommandMethod = null;
        for(Method method : moduleMethods) {
            if(moduleCommandName.equals(method.getName())) {
                moduleCommandMethod = method;
            }
        }
        return moduleCommandMethod;
    }

    public static String getModuleCommandName(MessageJson lastMessageJson, SettingsJson settingsJson) {
        if(lastMessageJson.getBody().length() > 1) {
            String moduleCommandName = Arrays.asList(lastMessageJson.getBody().substring(1, lastMessageJson.getBody().length()).split(" ")).get(0);
            return moduleCommandName;
        }
        return "";
    }

    public static List<Object> getModuleCommandParameters(MessageJson lastMessageJson) {
        List<Object> parametersAndMethodName = Arrays.asList(lastMessageJson.getBody().split(" "));
        List<Object> parameters = new ArrayList<>();
        for(int i = 0; i < parametersAndMethodName.size(); i++) {
            if(i != 0) {//ignore first element in paramtersAndMethodName
                parameters.add(parametersAndMethodName.get(i));
            }
        }
        return parameters;
    }

}
