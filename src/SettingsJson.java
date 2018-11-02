import java.util.Arrays;
import java.util.List;

public class SettingsJson {

    private String protocol;
    private String home_server;
    private String username;
    private String password;
    private String room_name;
    private String[] modules;
    private String module_prompt;

    @Override
    public String toString() {
        return "protocol: " + this.protocol + "\n" + "home server: " + this.home_server + "\n" + "username: " + this.username + "\n" + "password: " + this.password + "\n" + "room name: " + this.room_name + "\n" + "modules: " + this.getModules() + "\n" + "module prompt: " + this.module_prompt;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getHomeServer() {
        return this.home_server;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getRoomName() {
        return this.room_name;
    }

    public List<String> getModules() {
        return Arrays.asList(this.modules);
    }

    public String getModulePrompt() {
        return this.module_prompt;
    }
}
