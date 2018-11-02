import java.util.List;

public class ModulesAndModuleCommandResponse {

    private List<Module> modules;
    private String moduleCommandResponse;

    public ModulesAndModuleCommandResponse(List<Module> modules, String moduleCommandResponse) {
        this.modules = modules;
        this.moduleCommandResponse = moduleCommandResponse;
    }

    public List<Module> getModules() {
        return this.modules;
    }

    public String getModuleCommandResponse() {
        return this.moduleCommandResponse;
    }
}
