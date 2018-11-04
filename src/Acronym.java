import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Acronym implements Module {

    public static String acronym(List<Object> params) {
        char[] letters = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        String acronymStr = "";
        int acronymLength = 5;
        if(params.size() != 0) {
            acronymLength = Integer.parseInt((String) params.get(0));
        }
        Random random = new Random();
        for(int i = 0; i < acronymLength; i++) {
            acronymStr += letters[random.nextInt(letters.length)];
        }
        return acronymStr;
    }

    @Override
    public List<Method> getAllMethods() {
        Class acronymClass = Acronym.class;
        List<Method> allMethods = Arrays.asList(acronymClass.getDeclaredMethods());
        return allMethods;
    }

    @Override
    public String getName() {
        return "acronym";
    }

    @Override
    public String noPrompt(String[] body) {
        return "";
    }
}
