package utils;


import org.apache.commons.cli.*;


public class Command {
    private String classPath;
    private String libPath;
    private String settingPath;

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    private String projectPath;

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getLibPath() {
        return libPath;
    }

    public void setLibPath(String libPath) {
        this.libPath = libPath;
    }

    public String getSettingPath() {
        return settingPath;
    }

    public void setSettingPath(String settingPath) {
        this.settingPath = settingPath;
    }

    public String getOutPut() {
        return outPut;
    }

    public void setOutPut(String outPut) {
        this.outPut = outPut;
    }

    private String outPut;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    private String projectName;

    public Command() {
    }

    public void parse(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information");
        options.addOption("pn", "project-name", true, "Project name");
        options.addOption("pp", "project-path", true, "Project path");
        options.addOption("cp", "class-path", true, "File path of class");
        options.addOption("lp", "lib-path", true, "File path of library");
        options.addOption("sp", "setting-path", true, "File path of basic setting");
        options.addOption("o", "outPut", true, "Output directory");

        CommandLine commandLine = parser.parse(options, args);

        if (commandLine.hasOption("h")) {
            System.out.println("Help Message");
            System.exit(0);
        }

        if (commandLine.hasOption("cp")) {
            this.setClassPath(commandLine.getOptionValue("cp"));
        }

        if (commandLine.hasOption("lp")) {
            this.setLibPath(commandLine.getOptionValue("lp"));
        }

        if (commandLine.hasOption("sp")) {
            this.setSettingPath(commandLine.getOptionValue("sp"));
        }

        if (commandLine.hasOption("pn")) {
            this.setProjectName(commandLine.getOptionValue("pn"));
        }

        if (commandLine.hasOption("pp")) {
            this.setProjectPath(commandLine.getOptionValue("pp"));
        }

        if (commandLine.hasOption("o")) {
            this.setOutPut(commandLine.getOptionValue("o"));
        }

    }
}
