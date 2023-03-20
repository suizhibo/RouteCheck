package xxxx.routercheck;

import xxxx.routercheck.annotations.FactAnalyzerAnnotations;
import xxxx.routercheck.exceptions.*;
import xxxx.routercheck.factAnalyzer.FactAnalyzer;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxxx.routercheck.project.BaseProjectAnalyzer;
import xxxx.routercheck.project.entry.Config;
import xxxx.routercheck.project.entry.Project;
import xxxx.routercheck.reporting.ReportGenerator;
import xxxx.routercheck.reporting.ReporterFactory;
import soot.SootClass;
import xxxx.routercheck.utils.Command;
import xxxx.routercheck.utils.CoreClassLoader;
import xxxx.routercheck.entry.Settings;
import xxxx.routercheck.utils.Utils;
import xxxx.routercheck.utils.YamlUtil;
import xxxx.routercheck.entry.Fact;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Engine {
    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    public final ClassLoader FACTANALYZER_CLASSLOADER = new CoreClassLoader(Engine.class.getClassLoader());
    private Command command = new Command();

    private Collection<Fact> factChain = new ArrayList<>();
    private BaseProjectAnalyzer baseProjectAnalyzer = new BaseProjectAnalyzer();

    private Project project;
    private Settings settings;
    private Collection<FactAnalyzer> factAnalyzers;


    protected void parseCommand(String[] args) throws ParseException {
        command.parse(args);
        Utils.command = command;
    }

    protected Settings loadSettings() throws LoadSettingsException {
        try {
            LOGGER.info("Load Settings");
            String settingPath = command.getSettingPath();
            settings = (Settings) YamlUtil.readYaml(settingPath, Settings.class);
            if (command.getOutPut() != null && !command.getOutPut().equals("")) {
                settings.setOutPutDirectory(command.getOutPut());
            }
            return settings;
        } catch (Exception e) {
            throw new LoadSettingsException(e.getMessage());
        }
    }

    protected Collection<FactAnalyzer> loadFactAnalyzer() throws LoadFactAnalyzerException {
        Collection<FactAnalyzer> factAnalyzerCollection = new ArrayList<>();
        try {
            List<String> analyzers = settings.getFactAnalyzers().get(project.getService());
            Map<String, Class> factAnalyzerNameToClass = scanFactAnalyzer();
            for (String analyzer :
                    analyzers) {
                Class clazz = factAnalyzerNameToClass.get(analyzer);
                if (clazz != null) {
                    try {
                        FactAnalyzer factAnalyzer = (FactAnalyzer) clazz.newInstance();
                        factAnalyzer.initialize(project, settings);
                        factAnalyzerCollection.add(factAnalyzer);
                    } catch (Exception e) {
                        LOGGER.debug(e.getMessage());
                    }
                }
            }
            LOGGER.info(String.format("Load FactAnalyzers(%d)", analyzers.size()));
        } catch (Exception e) {
            throw new LoadFactAnalyzerException(e.getMessage());
        }
        return factAnalyzerCollection;
    }

    protected Project analysisProject() throws ProjectAnalyzerException {
        LOGGER.info("Analysis Project");
        baseProjectAnalyzer.initialize(command, settings);
        baseProjectAnalyzer.analysis();
        Project myProject = baseProjectAnalyzer.getProject();
        myProject.setName(command.getProjectName());
        return myProject;
    }

    protected void evaluateFact() throws FactAnalyzerException {
        LOGGER.info("Evaluate Fact");
        Set<SootClass> sootClassSet = project.getClasses();
        Collection<Config> configs = project.getConfigs();
        Collection<FactAnalyzer> classFactAnalyzer = new ArrayList<>();
        Collection<FactAnalyzer> configFactAnalyzer = new ArrayList<>();
        Collection<FactAnalyzer> unionFactAnalyzer = new ArrayList<>();
        try {
            for (FactAnalyzer factAnalyzer :
                    factAnalyzers) {
                if (factAnalyzer.getType().toLowerCase(Locale.ROOT).equals("class")) {
                    classFactAnalyzer.add(factAnalyzer);
                } else if (factAnalyzer.getType().toLowerCase(Locale.ROOT).equals("config")) {
                    configFactAnalyzer.add(factAnalyzer);
                } else if (factAnalyzer.getType().toLowerCase(Locale.ROOT).equals("union")) {
                    unionFactAnalyzer.add(factAnalyzer);
                }
            }

            for (Config config :
                    configs) {
                for (FactAnalyzer fa :
                        configFactAnalyzer) {
                    try {
                        fa.prepare(config);
                        if (fa.isEnable()) {
                            fa.analysis(config, factChain);
                            LOGGER.info(config.getFileName() + ": " + fa.getName() + " Done");
                        }
                    } catch (Exception e) {
                        LOGGER.debug(config.getFileName() + ": " +
                                fa.getName() + "occur error: " + e.getMessage());
                    }
                }
            }

            for (SootClass sootClass :
                    sootClassSet) {
                for (FactAnalyzer fa :
                        classFactAnalyzer) {
                    try {
                        fa.prepare(sootClass);
                        if (fa.isEnable()) {
                            fa.analysis(sootClass, factChain);
                            LOGGER.info(sootClass.getName() + ": " + fa.getName() + " Done");
                        }
                    } catch (Exception e) {
                        LOGGER.debug(sootClass.getName() + ": " +
                                fa.getName() + "occur error: " + e.getMessage());
                    }
                }

            }
            for (FactAnalyzer ufa :
                    unionFactAnalyzer) {
                try {
                    ufa.analysis(null, factChain);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LOGGER.error(String.format("When execute %s occur error", ufa.getName()));
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    protected void writeReport() throws ReportingException {
        String type = settings.getReportType();
        ReportGenerator reportGenerator = ReporterFactory.getReportGenerator(type);
        reportGenerator.initialize(project, factChain, settings);
        reportGenerator.write();
        LOGGER.info("Write Report Done");
    }

    private Map<String, Class> scanFactAnalyzer() throws Exception {
        Map<String, Class> factAnalyzerNameToClass = new HashMap<>();
        try {
            URL url = Engine.class.getResource("/xxxx/routercheck/factAnalyzer/");
            ArrayList<Class> destList = new ArrayList<>();
            scanClass(url.toURI(), "xxxx.routercheck.factAnalyzer", FactAnalyzer.class, FactAnalyzerAnnotations.class, destList);
            destList.forEach(clazz -> {
                String clazzName = clazz.getSimpleName();
                factAnalyzerNameToClass.put(clazzName, clazz);
            });
            return factAnalyzerNameToClass;
        } catch (Exception e) {
            throw e;
        }
    }

    private void scanClass(URI uri, String packageName, Class<?> parentClass, Class<?> annotationClass, ArrayList<Class> destList) throws IOException, ClassNotFoundException {
        try {
            String jarFileString;
            if ((jarFileString = Utils.getJarFileByClass(Engine.class)) != null) {
                scanClassByJar(new File(jarFileString), packageName, parentClass, annotationClass, destList);
            } else {
                File file = new File(uri);
                File[] file2 = file.listFiles();
                for (int i = 0; i < file2.length; i++) {
                    File objectClassFile = file2[i];
                    if (objectClassFile.getPath().endsWith(".class"))
                        try {
                            String objectClassName = String.format("%s.%s", new Object[]{packageName, objectClassFile.getName().substring(0, objectClassFile.getName().length() - ".class".length())});
                            Class<?> objectClass = Class.forName(objectClassName, true, FACTANALYZER_CLASSLOADER);
                            if (parentClass.isAssignableFrom(objectClass) && objectClass.isAnnotationPresent((Class) annotationClass)) {
                                destList.add(objectClass);
                            }
                        } catch (Exception e) {
                            LOGGER.debug(String.format("When scan class %s occur error: %", new Object[]{objectClassFile, e.getMessage()}));
                        }
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void scanClassByJar(File srcJarFile, String packageName, Class<?> parentClass, Class<?> annotationClass, ArrayList<Class> destList) throws IOException, ClassNotFoundException {
        try {
            JarFile jarFile = new JarFile(srcJarFile);
            Enumeration<JarEntry> jarFiles = jarFile.entries();
            packageName = packageName.replace(".", "/");
            while (jarFiles.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) jarFiles.nextElement();
                String name = jarEntry.getName();
                if (name.startsWith(packageName) && name.endsWith(".class")) {
                    name = name.replace("/", ".");
                    name = name.substring(0, name.length() - 6);
                    Class objectClass = Class.forName(name, true, FACTANALYZER_CLASSLOADER);
                    try {
                        if (parentClass.isAssignableFrom(objectClass) && objectClass.isAnnotationPresent(annotationClass)) {
                            destList.add(objectClass);
                        }
                    } catch (Exception e) {
                        LOGGER.debug(String.format("When scan class %s occur error: %", new Object[]{objectClass, e.getMessage()}));
                    }
                }
            }
            jarFile.close();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void run(String[] args) {
        final long analysisStart = System.currentTimeMillis();
        try {
            parseCommand(args);
            LOGGER.info("Analysis Started");
            loadSettings();
            project = analysisProject();
            factAnalyzers = loadFactAnalyzer();
            evaluateFact();
            writeReport();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Analysis occur error: " + e.getMessage());
        }
        LOGGER.info("\n----------------------------------------------------\nEND ANALYSIS\n----------------------------------------------------");
        final long analysisDurationSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - analysisStart);
        LOGGER.info("Analysis Complete ({} seconds)", analysisDurationSeconds);
    }

    public static void main(String[] args) {
        System.out.println(
                ".______        ______    __    __  .___________. _______   ______  __    __   _______   ______  __  ___ \n" +
                        "|   _  \\      /  __  \\  |  |  |  | |           ||   ____| /      ||  |  |  | |   ____| /      ||  |/  / \n" +
                        "|  |_)  |    |  |  |  | |  |  |  | `---|  |----`|  |__   |  ,----'|  |__|  | |  |__   |  ,----'|  '  /  \n" +
                        "|      /     |  |  |  | |  |  |  |     |  |     |   __|  |  |     |   __   | |   __|  |  |     |    <   \n" +
                        "|  |\\  \\----.|  `--'  | |  `--'  |     |  |     |  |____ |  `----.|  |  |  | |  |____ |  `----.|  .  \\  \n" +
                        "| _| `._____| \\______/   \\______/      |__|     |_______| \\______||__|  |__| |_______| \\______||__|\\__\\ \n" +
                        "                                                                                                        ");

        Engine engine = new Engine();
        engine.run(args);
    }
}
