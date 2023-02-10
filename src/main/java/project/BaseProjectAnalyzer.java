package project;

import exceptions.ProjectAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.entry.Config;
import project.entry.Jar;
import project.entry.Project;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import entry.Settings;
import utils.Command;

import java.io.File;
import java.util.*;

public class BaseProjectAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProjectAnalyzer.class);

    private final List<String> CONFIG_SUFFIXES = Arrays.asList("xml", "yaml");

    private final String JRE_DIR = System.getProperty("java.home")+ File.separator+
            "lib" + File.separator + "rt.jar";

    static LinkedList<String> excludeList;
    private List<String> libs = new ArrayList<>();
    private List<String> classFilePaths = new ArrayList<>();
    private List<String> jarFilePaths = new ArrayList<>();


    private Project project;

    private Settings settings;
    private Command command;
    private int tempClassPathLength;

    public void analysis() throws ProjectAnalyzerException {
        analysisConfig();
        analysisJar();
        analysisClasses();
        analysisService();
    }

    public void initialize(Command command, Settings settings){
        this.command = command;
        this.settings = settings;
        this.project = new Project();
    }

    private void scanConfig(File file){
        if (file.isFile()) {
            String fileName = file.getName();
            if(fileName.lastIndexOf(".") == -1)return;
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            if(CONFIG_SUFFIXES.contains(suffix)) {
                String filePath = file.getAbsolutePath();
                Config config = new Config(fileName, filePath, suffix);
                project.addConfig(config);
            }
        }
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                scanConfig(f);
        }
    }

    private void scanClass(File file){
        if (file.isFile()) {
            String fileName = file.getName();
            if(fileName.endsWith(".class")) {
                String filePath = file.getAbsolutePath();
                String path = filePath.substring(this.tempClassPathLength);
                filePath = path.substring(1, path.lastIndexOf("."));
                classFilePaths.add(filePath.replace("\\", "."));
            }
        }
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                scanClass(f);
        }
    }

    private void scanJars(File file){
        if (file.isFile()) {
            String fileName = file.getName();
            if(fileName.endsWith(".jar")) {
                String filePath = file.getAbsolutePath();
                Jar jar = new Jar(fileName, filePath);
                project.addJar(jar);
                jarFilePaths.add(filePath);
            }
        }
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                scanJars(f);
        }
    }

    private void buildSootClass(){
        Set<SootClass> sootClassSet = new HashSet<>();
        for (String classFilePath:
             classFilePaths) {
            SootClass sootClass = Scene.v().loadClassAndSupport(classFilePath);
            if(!sootClass.isJavaLibraryClass()){
                sootClassSet.add(sootClass);
            }
        }
        project.setClasses(sootClassSet);
    }

    private void analysisConfig(){
        String projectPath = command.getProjectPath();
        scanConfig(new File(projectPath));
    }

    private void analysisJar(){
        String libPath = command.getLibPath();
        scanJars(new File(libPath));
    }

    private void analysisService(){
        project.setService("landray");
    }
    private void analysisClasses(){
        String classPath = command.getClassPath();
        this.tempClassPathLength = classPath.length();
        List<String> classPaths = new ArrayList<>();
        classPaths.add(classPath);
        libs.addAll(jarFilePaths);
        libs.add(JRE_DIR);
        excludeJDKLibrary();
        String sootClassPath = String.join(File.pathSeparator, classPaths) + File.pathSeparator +
                String.join(File.pathSeparator, libs);
        Scene.v().setSootClassPath(sootClassPath);
        //whole program analysis
        Options.v().set_process_dir(classPaths);
        Options.v().set_whole_program(true);
        Options.v().set_app(true);
        scanClass(new File(classPath));
        buildSootClass();
    }

    private static LinkedList<String> excludeList()
    {
        if(excludeList==null)
        {
            excludeList = new LinkedList<String> ();

            excludeList.add("java.*");
            excludeList.add("javax.*");
            excludeList.add("sun.*");
            excludeList.add("sunw.*");
            excludeList.add("com.sun.*");
            excludeList.add("com.ibm.*");
            excludeList.add("com.apple.*");
            excludeList.add("apple.awt.*");
            excludeList.add("jdk.internal.*");
        }
        return excludeList;
    }

    private static void excludeJDKLibrary()
    {
        //exclude jdk classes
        Options.v().set_exclude(excludeList());
        //this option must be disabled for a sound call graph
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
    }

    public Project getProject() {
        return project;
    }

}
