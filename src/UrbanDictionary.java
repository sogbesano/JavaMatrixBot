import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class UrbanDictionary implements Module {


    public static String ud(List<Object> params) throws IOException {
        String urbanDictionaryWord = "";
        for(Object param : params) {
            urbanDictionaryWord += (String) param + " ";
        }
        if(params.size() >= 1) {
            urbanDictionaryWord = urbanDictionaryWord.substring(0, urbanDictionaryWord.length() - 1);//remove end space
            String urbanDictionaryUrlStr = String.format("http://api.urbandictionary.com/v0/define?term={%s}", urbanDictionaryWord);
            URL url = new URL(urbanDictionaryUrlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder urbanDictionaryAPIResponseJsonStr = new StringBuilder();
            while((line = br.readLine()) != null) {
                urbanDictionaryAPIResponseJsonStr.append(line);
            }
            br.close();
            Gson gson = new Gson();
            JsonObject urbanDictionaryAPIResponseJson =  gson.fromJson(urbanDictionaryAPIResponseJsonStr.toString(), JsonObject.class);
            String urbanDictionaryDefinition = gson.fromJson(urbanDictionaryAPIResponseJson.get("list").getAsJsonArray().get(0).toString(), JsonObject.class).get("definition").getAsString();
            return urbanDictionaryDefinition;
        }
        return urbanDictionaryWord;
    }

    @Override
    public List<Method> getAllMethods() {
        Class urbanDictionaryClass = UrbanDictionary.class;
        List<Method> allMethods = Arrays.asList(urbanDictionaryClass.getDeclaredMethods());
        return allMethods;
    }

    @Override
    public String getName() {
        return "urban dictionary";
    }
}
