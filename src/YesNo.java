import java.lang.reflect.Method;
import java.util.*;

public class YesNo implements Module {
    static List<String> modalVerbs = Arrays.asList(
        "can", "could", "may", "might", "shall", "should", "will", "would", "must",
        "ought", "are", "am", "is", "does", "did", "didnt", "didn't", "do", "don't",
        "dont", "was"
    );
    static List<String> replies = Arrays.asList(
            "yes", "no"
    );
    static Random rng = new Random();

    @Override
    public List<Method> getAllMethods() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "yesno";
    }

    @Override
    public String noPrompt(String[] body) {
        if (body.length > 0 && modalVerbs.contains(body[0].toLowerString())) {
            return replies.get(rng.nextInt(2));
        }
        return "";
    }
}
