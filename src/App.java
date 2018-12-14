import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {

    public static List<Module> getModules(SettingsJson settings) {
        List<String> moduleNames = settings.getModules();
        List<Module> modules = Modules.loadModules(moduleNames);
        return modules;
    }

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException {
        String settingsJsonStr = Utils.readFile(new File("settings.json").getAbsolutePath());
        SettingsJson settings = new Gson().fromJson(settingsJsonStr, SettingsJson.class);
        Matrix connection = new Matrix(settings);

        connection.login();
        String publicRoomsStr = connection.publicRooms();
        ArrayList publicRoomsList = Utils.getPublicRoomsJsonList(publicRoomsStr);
        List<Map<String, String>> canonicalAliasesAndRoomIDs = connection.getCanonicalAliasesAndRoomsIDs(
                publicRoomsList
        );
        String roomID = connection.getRoomID(canonicalAliasesAndRoomIDs);
        connection.joinByID(roomID);

        // initial sync
        connection.sync();

        List<Module> modules = getModules(settings);

        while (true) {
            MessageJson lastMessageJson = connection.extractLastMessage(roomID);
            if (!modules.isEmpty()) {
                if (lastMessageJson.getBody().startsWith(settings.getModulePrompt())) {
                    for (Module module : modules) {
                        String moduleCommand = Modules.getModuleCommandName(lastMessageJson, settings);
                        if (moduleCommand.length() > 0) {
                            Method method = Modules.getModuleCommandMethod(moduleCommand, module.getAllMethods());
                            if (method != null) {
                                String userCommand = lastMessageJson.getBody().substring(1);
                                if (lastMessageJson.getBody().contains(" ")) {
                                    userCommand = userCommand.substring(0, userCommand.indexOf(" "));
                                }
                                if (moduleCommand.equals(userCommand)) {
                                    int paramCount = method.getParameterCount();
                                    if (paramCount == 0) {
                                        String response = (String) method.invoke(module);
                                        if (!response.isEmpty()) {
                                            connection.sendMessage(roomID, response);
                                        }
                                    } else if (paramCount == 1) {
                                        List<Object> params = Modules.getModuleCommandParameters(lastMessageJson);
                                        Class<?>[] paramTypes = method.getParameterTypes();
                                        if (paramTypes[0].equals(Object[].class)) {
                                            Object[] modulesArr = modules.toArray();
                                            String response = (String) method.invoke(module, new Object[]{modulesArr});
                                            if (!response.isEmpty()) {
                                                connection.sendMessage(roomID, response);
                                            }
                                        } else {
                                            String response = (String) method.invoke(module, params);
                                            if (!response.isEmpty()) {
                                                connection.sendMessage(roomID, response);
                                            }
                                        }
                                    } else if (paramCount == 2) {
                                        List<Object> params = Modules.getModuleCommandParameters(lastMessageJson);
                                        Class<?>[] paramTypes = method.getParameterTypes();
                                        if (paramTypes[0].equals(Object[].class) && paramTypes[1].equals(String.class)) {
                                            if (params.size() >= 1) {
                                                String paramsStr = "";
                                                for (Object param : params) {
                                                    paramsStr += param + " ";
                                                }
                                                // remove end space
                                                paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
                                                Object[] modulesArr = modules.toArray();
                                                ModulesAndModuleCommandResponse modulesResponse =
                                                        (ModulesAndModuleCommandResponse) method.invoke(module, modulesArr, paramsStr);
                                                modules = modulesResponse.getModules();
                                                String response = modulesResponse.getModuleCommandResponse();

                                                if (!response.isEmpty()) {
                                                    connection.sendMessage(roomID, response);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for (Module module : modules) {
                    String response = module.noPrompt(lastMessageJson.getBody().split(" "));
                    if(module.getName().equals("markov talk")) {
                        MarkovTalk markovTalkModule = new MarkovTalk(lastMessageJson, settings);
                        response = markovTalkModule.noPrompt(lastMessageJson.getBody().split(" "));
                    }
                    if(module.getName().equals("log")) {
                        Log logModule = new Log(lastMessageJson, settings);
                        response = logModule.noPrompt(lastMessageJson.getBody().split(" "));
                    }
                    if (response.length() > 0) {
                        connection.sendMessage(roomID, response);
                    }
                }
            }
            connection.markRead(roomID, lastMessageJson.getEventID());
        }
    }
}



