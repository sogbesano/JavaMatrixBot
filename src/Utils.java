import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {

    public static String readFile(String filepath) throws IOException {
        File file = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String result = "";
        String line = "";
        while((line = br.readLine()) != null) {
            result += line;
        }
        return result;
    }

    public static ArrayList getPublicRoomsJsonList(String publicRoomsJsonStr) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(publicRoomsJsonStr);
        JsonArray jsonArr = jo.getAsJsonArray("chunk");
        Gson gson = new Gson();
        ArrayList jsonObjList = gson.fromJson(jsonArr, ArrayList.class);
        return jsonObjList;
    }

    public static List<Method> getStaticMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<Method>();
        for (Method method : clazz.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                methods.add(method);
            }
        }
        return Collections.unmodifiableList(methods);
    }

}
