import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException {
        String settingsJsonStr = Utils.readFile(new File("settings.json").getAbsolutePath());
        System.out.println(settingsJsonStr);
        Gson gson = new Gson();
        SettingsJson settingsJson = gson.fromJson(settingsJsonStr, SettingsJson.class);
        System.out.println(settingsJson);
        String protocol = settingsJson.getProtocol();
        String homeServer = settingsJson.getHomeServer();
        String username = settingsJson.getUsername();
        String password = settingsJson.getPassword();
        String loginFlowsJson = Matrix.getLoginFlows(protocol, homeServer, "r0");
        System.out.println(loginFlowsJson);
        LoginJsonResponse loginJsonResponse = Matrix.login(protocol, homeServer, "api/v1", username, password);
        System.out.println(loginJsonResponse);
        String accessToken = loginJsonResponse.getAccessToken();
        String userID = loginJsonResponse.getUserID();
        String deviceID = loginJsonResponse.getDeviceID();
        String publicRoomsJsonStr = Matrix.publicRooms(protocol, homeServer, "unstable");
        System.out.println(publicRoomsJsonStr);
        ArrayList publicRoomsJsonList = Utils.getPublicRoomsJsonList(publicRoomsJsonStr);
        List<Map<String, String>> canonicalAliasesAndRoomIDs = Matrix.getCanonicalAliasesAndRoomsIDs(publicRoomsJsonList);
        System.out.println(canonicalAliasesAndRoomIDs);
        String roomID = Matrix.getRoomID(settingsJson.getRoomName(), canonicalAliasesAndRoomIDs);
        System.out.println(roomID);
        Matrix.joinByID(protocol, homeServer, "r0", roomID, accessToken);
        //Matrix.sendMessage(protocol, homeServer, "r0", roomID, accessToken, "Hello, from Java.");
        String syncStr = Matrix.sync(protocol, homeServer, "r0", accessToken);
        List<String> moduleNames = settingsJson.getModules();
        List<Module> modules = Modules.loadModules(moduleNames);
        while (true) {
            MessageJson lastMessageJson = Matrix.extractLastMessage(protocol, homeServer, accessToken, roomID);
            Matrix.markRead(protocol, homeServer, "unstable", roomID, lastMessageJson.getEventID(), accessToken);
            System.out.println(lastMessageJson);
            System.out.println(modules);
            if (!modules.isEmpty()) {
                if (lastMessageJson.getBody().startsWith(settingsJson.getModulePrompt())) {
                    for (Module module : modules) {
                        String moduleCommandName = Modules.getModuleCommandName(lastMessageJson, settingsJson);
                        if (moduleCommandName.length() > 1) {
                            Method moduleCommandMethod = Modules.getModuleCommandMethod(moduleCommandName, module.getAllMethods());
                            //System.out.println(moduleCommandMethod.getName());
                            if (moduleCommandMethod != null) {
                                if (moduleCommandName.equals(lastMessageJson.getBody().contains(" ") ? lastMessageJson.getBody().substring(1, lastMessageJson.getBody().indexOf(" ")) : lastMessageJson.getBody().substring(1, lastMessageJson.getBody().length()))) {
                                    int moduleCommandMethodParameterCount = moduleCommandMethod.getParameterCount();
                                    if (moduleCommandMethodParameterCount == 0) {
                                        String moduleCommandResponse = (String) moduleCommandMethod.invoke(module);
                                        if (!moduleCommandResponse.isEmpty()) {
                                            Matrix.sendMessage(protocol, homeServer, "r0", roomID, accessToken, moduleCommandResponse);
                                        }
                                    } else if (moduleCommandMethodParameterCount == 1) {
                                        List<Object> moduleCommandParameters = Modules.getModuleCommandParameters(lastMessageJson);
                                        System.out.println(moduleCommandParameters);
                                        Class<?>[] moduleCommandParameterTypes = moduleCommandMethod.getParameterTypes();
                                        if (moduleCommandParameterTypes[0].equals(Object[].class)) {
                                            Object[] modulesArr = modules.toArray();
                                            String moduleCommandResponse = (String) moduleCommandMethod.invoke(module, new Object[]{modulesArr});
                                            if (!moduleCommandResponse.isEmpty()) {
                                                Matrix.sendMessage(protocol, homeServer, "r0", roomID, accessToken, moduleCommandResponse);
                                            }
                                        } else {
                                            String moduleCommandResponse = (String) moduleCommandMethod.invoke(module, moduleCommandParameters);
                                            if (!moduleCommandResponse.isEmpty()) {
                                                Matrix.sendMessage(protocol, homeServer, "r0", roomID, accessToken, moduleCommandResponse);
                                            }
                                        }
                                    } else if (moduleCommandMethodParameterCount == 2) {
                                        List<Object> moduleCommandParameters = Modules.getModuleCommandParameters(lastMessageJson);
                                        Class<?>[] moduleCommandParameterTypes = moduleCommandMethod.getParameterTypes();
                                        if (moduleCommandParameterTypes[0].equals(Object[].class) && moduleCommandParameterTypes[1].equals(String.class)) {
                                            if (moduleCommandParameters.size() >= 1) {
                                                String moduleCommandParametersStr = "";
                                                for (Object moduleCommandParameter : moduleCommandParameters) {
                                                    moduleCommandParametersStr += moduleCommandParameter + " ";
                                                }
                                                moduleCommandParametersStr = moduleCommandParametersStr.substring(0, moduleCommandParametersStr.length() - 1);//remove end space
                                                Object[] modulesArr = modules.toArray();
                                                ModulesAndModuleCommandResponse modulesAndModuleCommandResponse = (ModulesAndModuleCommandResponse) moduleCommandMethod.invoke(module, modulesArr, moduleCommandParametersStr);
                                                modules = modulesAndModuleCommandResponse.getModules();
                                                String moduleCommandResponse = modulesAndModuleCommandResponse.getModuleCommandResponse();

                                                if (!moduleCommandResponse.isEmpty()) {
                                                    Matrix.sendMessage(protocol, homeServer, "r0", roomID, accessToken, moduleCommandResponse);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (lastMessageJson.getBody().equals("hello bot")) {
                Matrix.sendMessage(protocol, homeServer, "r0", roomID, accessToken, "yo what's up");
            }
        }
    }
}



