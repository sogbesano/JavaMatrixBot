import java.lang.reflect.Method;
import java.util.List;

public interface Module {

    List<Method> getAllMethods();

    String getName();

    String noPrompt(String[] body);

}
