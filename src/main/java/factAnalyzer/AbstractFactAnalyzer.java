package factAnalyzer;

import entry.Settings;
import project.entry.Project;


public abstract class AbstractFactAnalyzer implements FactAnalyzer {
    private Settings settings;
    private Project project;
    private Object object;



    @Override
    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    private boolean enable = true;

    public Settings getSettings() {
        return settings;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public void initialize(Project project, Settings settings) {
        this.project = project;
        this.settings = settings;
    }
}
