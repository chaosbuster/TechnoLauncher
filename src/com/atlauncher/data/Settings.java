/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.atlauncher.App;
import com.atlauncher.Update;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.BottomBar;
import com.atlauncher.gui.InstancesPanel;
import com.atlauncher.gui.LauncherConsole;
import com.atlauncher.gui.NewsPanel;
import com.atlauncher.gui.PacksPanel;
import com.atlauncher.gui.ProgressDialog;
import com.atlauncher.utils.Utils;

/**
 * Settings class for storing all data for the Launcher and the settings of the user
 *
 * @author Ryan
 */
public class Settings {

    // Users Settings
    private Language language; // Language for the Launcher
    private Server server; // Server to use for the Launcher
    private String forgeLoggingLevel; // Logging level to use when running Minecraft with Forge
    private int ram; // RAM to use when launching Minecraft
    private int permGen; // PermGenSize to use when launching Minecraft in MB
    private int windowWidth; // Width of the Minecraft window
    private int windowHeight; // Height of the Minecraft window
    private boolean maximiseMinecraft; // If Minecraft should start maximised
    private boolean usingCustomJavaPath; // If the user is using a custom java path
    private String javaPath; // Users path to Java
    private String javaParamaters; // Extra Java paramaters when launching Minecraft
    private boolean sortPacksAlphabetically; // If to sort packs default alphabetically
    private boolean enableConsole; // If to show the console by default
    private boolean enableDebugConsole; // If to enable debugging console
    private boolean enableLeaderboards; // If to enable the leaderboards
    private boolean enableLogs; // If to enable logs
    private Account account; // Account using the Launcher
    private String addedPacks; // The Semi Public packs the user has added to the Launcher

    // Packs, Addons, Instances and Accounts
    private ArrayList<Pack> packs = new ArrayList<Pack>(); // Packs in the Launcher
    private ArrayList<Instance> instances = new ArrayList<Instance>(); // Users Installed Instances
    private ArrayList<MinecraftVersion> minecraftVersions = new ArrayList<MinecraftVersion>(); // Minecraft
    private ArrayList<Addon> addons = new ArrayList<Addon>(); // Addons in the Launcher
    private ArrayList<Account> accounts = new ArrayList<Account>(); // Accounts in the Launcher

    // Directories and Files for the Launcher
    private File baseDir, backupsDir, configsDir, imagesDir, skinsDir, jarsDir, commonConfigsDir,
            resourcesDir, librariesDir, languagesDir, downloadsDir, usersDownloadsFolder,
            instancesDir, serversDir, tempDir, instancesDataFile, userDataFile, propertiesFile;

    // Launcher Settings
    private JFrame parent; // Parent JFrame of the actual Launcher
    private Properties properties = new Properties(); // Properties to store everything in
    private LauncherConsole console = new LauncherConsole(); // Load the Launcher's Console
    private ArrayList<Language> languages = new ArrayList<Language>(); // Languages for the Launcher
    private ArrayList<Server> servers = new ArrayList<Server>(); // Servers for the Launcher
    private ArrayList<Server> triedServers = new ArrayList<Server>(); // Servers tried to connect to
    private InstancesPanel instancesPanel; // The instances panel
    private NewsPanel newsPanel; // The news panel
    private PacksPanel packsPanel; // The packs panel
    private BottomBar bottomBar; // The bottom bar
    private boolean firstTimeRun = false; // If this is the first time the Launcher has been run
    private boolean offlineMode = false; // If offline mode is enabled
    private boolean usingMacApp = false; // If the user is using the Mac App
    private Process minecraftProcess = null; // The process minecraft is running on
    private Server originalServer = null; // Original Server user has saved
    private boolean minecraftLaunched = false; // If Minecraft has been Launched
    private String fileHashes = null; // Hashes for the files to get from the server
    private String version = "%VERSION%"; // Version of the Launcher
    private String userAgent = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36";
    @SuppressWarnings("unused")
    private boolean minecraftLoginServerUp = false; // If the Minecraft Login server is up
    @SuppressWarnings("unused")
    private boolean minecraftSessionServerUp = false; // If the Minecraft Session server is up

    public Settings() {
        setupFiles(); // Setup all the file and directory variables
        checkFolders(); // Checks the setup of the folders and makes sure they're there
        clearTempDir(); // Cleans all files in the Temp Dir
        rotateLogFiles(); // Rotates the log files
        loadStartingProperties(); // Get users Console preference and Java Path
    }

    public void setupFiles() {
        if (Utils.isLinux()) {
            try {
                baseDir = new File(getClass().getClassLoader().getResource("").toURI());
            } catch (URISyntaxException e) {
                baseDir = new File(System.getProperty("user.dir"), "TechnoLauncher");
            }
        } else {
            baseDir = new File(System.getProperty("user.dir"));
        }
        if (Utils.isMac() && new File(baseDir.getParentFile().getParentFile(), "MacOS").exists()) {
            usingMacApp = true;
        }
        usersDownloadsFolder = new File(System.getProperty("user.home"), "Downloads");
        backupsDir = new File(baseDir, "Backups");
        configsDir = new File(baseDir, "Configs");
        imagesDir = new File(configsDir, "Images");
        skinsDir = new File(imagesDir, "Skins");
        jarsDir = new File(configsDir, "Jars");
        commonConfigsDir = new File(configsDir, "Common");
        resourcesDir = new File(configsDir, "Resources");
        librariesDir = new File(configsDir, "Libraries");
        languagesDir = new File(configsDir, "Languages");
        downloadsDir = new File(baseDir, "Downloads");
        instancesDir = new File(baseDir, "Instances");
        serversDir = new File(baseDir, "Servers");
        tempDir = new File(baseDir, "Temp");
        instancesDataFile = new File(configsDir, "instancesdata");
        userDataFile = new File(configsDir, "userdata");
        propertiesFile = new File(configsDir, "TechnoLauncher.conf");
    }

    public void loadEverything() {
        setupServers(); // Setup the servers available to use in the Launcher
        loadServerProperty(); // Get users Server preference
        if (hasUpdatedFiles()) {
            downloadUpdatedFiles(); // Downloads updated files on the server
        }
        loadLanguages(); // Load the Languages available in the Launcher
        loadPacks(0); // Load the Packs available in the Launcher
        loadUsers(); // Load the Testers and Allowed Players for the packs
        loadMinecraftVersions(); // Load info about the different Minecraft versions
        loadAddons(); // Load the Addons available in the Launcher
        loadInstances(); // Load the users installed Instances
        loadAccounts(); // Load the saved Accounts
        loadProperties(); // Load the users Properties
        console.setupLanguage(); // Setup language on the console
        checkResources(); // Check for new format of resources
    }

    public void checkResources() {
        File indexesDir = new File(this.resourcesDir, "indexes");
        if (!indexesDir.exists() || !indexesDir.isDirectory()) {
            final ProgressDialog dialog = new ProgressDialog(
                    getLocalizedString("settings.rearrangingresources"), 0,
                    getLocalizedString("settings.rearrangingresources"), null);
            Thread thread = new Thread() {
                public void run() {
                    File indexesDir = new File(getResourcesDir(), "indexes");
                    File objectsDir = new File(getResourcesDir(), "objects");
                    File virtualDir = new File(getResourcesDir(), "virtual");
                    File legacyDir = new File(virtualDir, "legacy");
                    File tempDir = new File(getTempDir(), "assets");
                    tempDir.mkdir();
                    Utils.moveDirectory(getResourcesDir(), tempDir);
                    indexesDir.mkdirs();
                    objectsDir.mkdirs();
                    virtualDir.mkdirs();
                    legacyDir.mkdirs();
                    Utils.moveDirectory(tempDir, legacyDir);
                    Utils.delete(tempDir);
                    Utils.spreadOutResourceFiles(legacyDir);
                    dialog.close();
                }
            };
            dialog.addThread(thread);
            dialog.start();

        }
    }

    public void checkMojangStatus() {
        JSONParser parser = new JSONParser();
        try {
            Downloadable download = new Downloadable("http://status.mojang.com/check", false);
            Object obj = parser.parse(download.getContents());
            JSONArray jsonObject = (JSONArray) obj;
            Iterator<JSONObject> iterator = jsonObject.iterator();
            while (iterator.hasNext()) {
                JSONObject object = iterator.next();
                if (object.containsKey("login.minecraft.net")) {
                    if (((String) object.get("login.minecraft.net")).equalsIgnoreCase("green")) {
                        minecraftLoginServerUp = true;
                    }
                } else if (object.containsKey("session.minecraft.net")) {
                    if (((String) object.get("session.minecraft.net")).equalsIgnoreCase("green")) {
                        minecraftSessionServerUp = true;
                    }
                }
            }
        } catch (ParseException e) {
            this.logStackTrace(e);
        }
    }

    public void downloadUpdate() {
        try {
            File thisFile = new File(Update.class.getProtectionDomain().getCodeSource()
                    .getLocation().getPath());
            String path = thisFile.getCanonicalPath();
            path = URLDecoder.decode(path, "UTF-8");
            String toget;
            String saveAs = thisFile.getName();
            if (path.contains(".exe")) {
                toget = "exe";
            } else {
                toget = "jar";
            }
            File newFile = new File(getTempDir(), saveAs);
            log("Downloading Launcher Update");
            Downloadable update = new Downloadable("TechnoLauncher." + toget, newFile, null, null, true);
            update.download(false);
            runUpdate(path, newFile.getAbsolutePath());
        } catch (IOException e) {
            this.logStackTrace(e);
        }
    }

    public void runUpdate(String currentPath, String temporaryUpdatePath) {
        List<String> arguments = new ArrayList<String>();

        String path = System.getProperty("java.home") + File.separator + "bin" + File.separator
                + "java";
        if (Utils.isWindows()) {
            path += "w";
        }
        arguments.add(path);
        arguments.add("-cp");
        arguments.add(temporaryUpdatePath);
        arguments.add("com.atlauncher.Update");
        arguments.add(currentPath);
        arguments.add(temporaryUpdatePath);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(arguments);

        log("Running launcher update with command " + arguments);

        try {
            processBuilder.start();
        } catch (IOException e) {
            this.logStackTrace(e);
        }

        System.exit(0);
    }

    private String getFileHashes() {
        return this.getFileHashes(false);
    }

    private String getFileHashes(boolean force) {
        if (force) {
            this.fileHashes = null;
        }
        if (this.fileHashes == null) {
            Downloadable download = new Downloadable("launcher/hashes.xml", true);
            String hashes = download.getContents();
            if (hashes == null) {
                return null;
            } else {
                this.fileHashes = hashes;
            }
        }
        return this.fileHashes;
    }

    /**
     * This checks the servers hashes.xml file and gets the files that the Launcher needs to have
     */
    private ArrayList<Downloadable> getLauncherFiles(boolean force) {
        String hashes = getFileHashes(force);
        if (hashes == null) {
            this.offlineMode = true;
            return null;
        }
        ArrayList<Downloadable> downloads = new ArrayList<Downloadable>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(hashes)));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("hash");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    String type = element.getAttribute("type");
                    String md5 = element.getAttribute("md5");
                    File file = null;
                    if (type.equalsIgnoreCase("Root")) {
                        file = new File(configsDir, name);
                    } else if (type.equalsIgnoreCase("Images")) {
                        file = new File(imagesDir, name);
                        name = "images/" + name;
                    } else if (type.equalsIgnoreCase("Skins")) {
                        file = new File(skinsDir, name);
                        name = "skins/" + name;
                    } else if (type.equalsIgnoreCase("Languages")) {
                        file = new File(languagesDir, name);
                        name = "languages/" + name;
                    } else if (type.equalsIgnoreCase("Libraries")) {
                        file = new File(librariesDir, name);
                        name = "libraries/" + name;
                    } else if (type.equalsIgnoreCase("Launcher")) {
                        String version = element.getAttribute("version");
                        if (!getVersion().equalsIgnoreCase(version)) {
                            if (getVersion().contains("-dev")) {
                                continue; // Don't update dev versions
                            } else if (getVersion().equalsIgnoreCase("%VERSION%")) {
                                continue; // Don't even think about updating my unbuilt copy
                            } else {
                                log("Update to Launcher found. Current version: " + this.version
                                        + ", New version: " + version);
                                downloadUpdate();
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue; // Don't know what to do with this file so ignore it
                    }
                    downloads.add(new Downloadable("launcher/" + name, file, md5, null, true));
                }
            }
        } catch (SAXException e) {
            logStackTrace(e);
        } catch (ParserConfigurationException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
        return downloads;
    }

    public void downloadUpdatedFiles() {
        ArrayList<Downloadable> downloads = getLauncherFiles(false);
        if (downloads != null) {
            ExecutorService executor = Executors.newFixedThreadPool(8);
            for (final Downloadable download : downloads) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (download.needToDownload()) {
                            log("Downloading Launcher File " + download.getFile().getName());
                            download.download(false);
                        }
                    }
                });
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
        }
    }

    /**
     * This checks the servers hashes.xml file and looks for new/updated files that differ from what
     * the user has
     */
    public boolean hasUpdatedFiles() {
        if (isInOfflineMode()) {
            return false;
        }
        log("Checking for updated files!");
        ArrayList<Downloadable> downloads = getLauncherFiles(true);
        if (downloads == null) {
            this.offlineMode = true;
            return false;
        }
        for (Downloadable download : downloads) {
            if (download.needToDownload()) {
                log("Updates found!");
                return true; // 1 file needs to be updated so there is updated files
            }
        }
        log("No updates found!");
        return false; // No updates
    }

    public void reloadLauncherData() {
        final JDialog dialog = new JDialog(this.parent, ModalityType.APPLICATION_MODAL);
        dialog.setSize(300, 100);
        dialog.setTitle("Updating Launcher");
        dialog.setLocationRelativeTo(App.settings.getParent());
        dialog.setLayout(new FlowLayout());
        dialog.setResizable(false);
        dialog.add(new JLabel("Updating Launcher... Please Wait"));
        Thread updateThread = new Thread() {
            public void run() {
                downloadUpdatedFiles(); // Download updated files
                reloadNewsPanel(); // Reload news panel
                loadPacks(0); // Load the Packs available in the Launcher
                loadUsers(); // Load the Testers and Allowed Players for the packs
                reloadPacksPanel(); // Reload packs panel
                loadAddons(); // Load the Addons available in the Launcher
                loadInstances(); // Load the users installed Instances
                reloadInstancesPanel(); // Reload instances panel
                dialog.setVisible(false); // Remove the dialog
                dialog.dispose(); // Dispose the dialog
            };
        };
        updateThread.start();
        dialog.setVisible(true);
    }

    /**
     * Checks the directory to make sure all the necessary folders are there
     */
    private void checkFolders() {
        File[] files = { backupsDir, configsDir, commonConfigsDir, imagesDir, skinsDir, jarsDir,
                resourcesDir, librariesDir, languagesDir, downloadsDir, instancesDir, serversDir,
                tempDir };
        for (File file : files) {
            if (!file.exists()) {
                file.mkdir();
            }
        }
    }

    /**
     * Returns the base directory
     *
     * @return File object for the base directory
     */
    public File getBaseDir() {
        return this.baseDir;
    }

    /**
     * Returns the backups directory
     *
     * @return File object for the backups directory
     */
    public File getBackupsDir() {
        return this.backupsDir;
    }

    /**
     * Returns the configs directory
     *
     * @return File object for the configs directory
     */
    public File getConfigsDir() {
        return this.configsDir;
    }

    /**
     * Returns the common configs directory
     *
     * @return File object for the common configs directory
     */
    public File getCommonConfigsDir() {
        return this.commonConfigsDir;
    }

    /**
     * Returns the images directory
     *
     * @return File object for the images directory
     */
    public File getImagesDir() {
        return this.imagesDir;
    }

    /**
     * Returns the skins directory
     *
     * @return File object for the skins directory
     */
    public File getSkinsDir() {
        return this.skinsDir;
    }

    /**
     * Returns the jars directory
     *
     * @return File object for the jars directory
     */
    public File getJarsDir() {
        return this.jarsDir;
    }

    /**
     * Returns the resources directory
     *
     * @return File object for the resources directory
     */
    public File getResourcesDir() {
        return this.resourcesDir;
    }

    public File getVirtualAssetsDir() {
        return new File(this.resourcesDir, "virtual");
    }

    public File getObjectsAssetsDir() {
        return new File(this.resourcesDir, "objects");
    }

    public File getLegacyVirtualAssetsDir() {
        return new File(getVirtualAssetsDir(), "legacy");
    }

    /**
     * Returns the libraries directory
     *
     * @return File object for the libraries directory
     */
    public File getLibrariesDir() {
        return this.librariesDir;
    }

    /**
     * Returns the languages directory
     *
     * @return File object for the languages directory
     */
    public File getLanguagesDir() {
        return this.languagesDir;
    }

    /**
     * Returns the downloads directory
     *
     * @return File object for the downloads directory
     */
    public File getDownloadsDir() {
        return this.downloadsDir;
    }

    /**
     * Returns the downloads directory for the user
     *
     * @return File object for the downloads directory for the users account
     */
    public File getUsersDownloadsDir() {
        return this.usersDownloadsFolder;
    }

    /**
     * Returns the instances directory
     *
     * @return File object for the instances directory
     */
    public File getInstancesDir() {
        return this.instancesDir;
    }

    /**
     * Returns the servers directory
     *
     * @return File object for the servers directory
     */
    public File getServersDir() {
        return this.serversDir;
    }

    /**
     * Returns the temp directory
     *
     * @return File object for the temp directory
     */
    public File getTempDir() {
        return this.tempDir;
    }

    /**
     * Deletes all files in the Temp directory
     */
    public void clearTempDir() {
        Utils.deleteContents(getTempDir());
    }

    public void rotateLogFiles() {
        File logFile1 = new File(getBaseDir(), "TechnoLauncher-Log-1.txt");
        File logFile2 = new File(getBaseDir(), "TechnoLauncher-Log-2.txt");
        File logFile3 = new File(getBaseDir(), "TechnoLauncher-Log-3.txt");
        if (logFile3.exists()) {
            Utils.delete(logFile3);
        }
        if (logFile2.exists()) {
            logFile2.renameTo(logFile3);
        }
        if (logFile1.exists()) {
            logFile1.renameTo(logFile2);
        }
        try {
            logFile1.createNewFile();
        } catch (IOException e) {
            String[] options = { "OK" };
            JOptionPane.showOptionDialog(null,
                    "<html><center>Cannot create the log file.<br/><br/>Make sure"
                            + " you are running the Launcher from somewhere with<br/>write"
                            + " permissions for your user account such as your Home/Users folder"
                            + " or desktop.</center></html>", "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                    options[0]);
            System.exit(0);
        }
    }

    /**
     * Returns the instancesdata file
     *
     * @return File object for the instancesdata file
     */
    public File getInstancesDataFile() {
        return instancesDataFile;
    }

    /**
     * Sets the main parent JFrame reference for the Launcher
     *
     * @param parent
     *            The Launcher main JFrame
     */
    public void setParentFrame(JFrame parent) {
        this.parent = parent;
    }

    /**
     * Load the users Server preference from file
     */
    public void loadServerProperty() {
        try {
            this.properties.load(new FileInputStream(propertiesFile));
            String serv = properties.getProperty("server", "Auto");
            if (isPackTester()) {
                this.server = getServerByName("Master"); // If tester use Master Server
                if (isServerByName(serv)) {
                    this.originalServer = this.server;
                } else {
                    this.server = getServerByName("Auto"); // Server not found, use default of Auto
                }
            } else if (isServerByName(serv)) {
                this.server = getServerByName(serv);
                this.originalServer = this.server;
            } else {
                log("Server " + serv + " is invalid", LogMessageType.warning, false);
                this.server = getServerByName("Auto"); // Server not found, use default of Auto
                this.originalServer = this.server;
            }
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    /**
     * Load the users Console preference from file
     */
    public void loadStartingProperties() {
        try {
            if (!propertiesFile.exists()) {
                propertiesFile.createNewFile();
            }
        } catch (IOException e) {
            String[] options = { "OK" };
            JOptionPane.showOptionDialog(null,
                    "<html><center>Cannot create the config file.<br/><br/>Make sure"
                            + " you are running the Launcher from somewhere with<br/>write"
                            + " permissions for your user account such as your Home/Users folder"
                            + " or desktop.</center></html>", "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                    options[0]);
            System.exit(0);
        }
        try {
            this.properties.load(new FileInputStream(propertiesFile));
            this.enableConsole = Boolean.parseBoolean(properties.getProperty("enableconsole",
                    "true"));
            this.enableDebugConsole = Boolean.parseBoolean(properties.getProperty(
                    "enabledebugconsole", "false"));
            if (!properties.containsKey("usingcustomjavapath")) {
                this.usingCustomJavaPath = false;
                this.javaPath = Utils.getJavaHome();
            } else {
                this.usingCustomJavaPath = Boolean.parseBoolean(properties.getProperty(
                        "usingcustomjavapath", "false"));
                if (isUsingCustomJavaPath()) {
                    this.javaPath = properties.getProperty("javapath", Utils.getJavaHome());
                } else {
                    this.javaPath = Utils.getJavaHome();
                }
            }
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    /**
     * Load the properties from file
     */
    public void loadProperties() {
        try {
            this.properties.load(new FileInputStream(propertiesFile));
            this.firstTimeRun = Boolean
                    .parseBoolean(properties.getProperty("firsttimerun", "true"));

            String lang = properties.getProperty("language", "English");
            if (isLanguageByName(lang)) {
                this.language = getLanguageByName(lang);
            } else {
                log("Invalid language " + lang + ". Defaulting to English!",
                        LogMessageType.warning, false);
                this.language = getLanguageByName("English"); // Language not found, use default
            }

            this.forgeLoggingLevel = properties.getProperty("forgelogginglevel", "INFO");
            if (!this.forgeLoggingLevel.equalsIgnoreCase("SEVERE")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("WARNING")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("INFO")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("CONFIG")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("FINE")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("FINER")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("FINEST")) {
                log("Invalid Forge Logging level " + this.forgeLoggingLevel
                        + ". Defaulting to INFO!", LogMessageType.warning, false);
                this.forgeLoggingLevel = "INFO";
            }

            if (Utils.is64Bit()) {
                int halfRam = (Utils.getMaximumRam() / 1000) * 512;
                int defaultRam = (halfRam >= 4096 ? 4096 : halfRam); // Default ram
                this.ram = Integer.parseInt(properties.getProperty("ram", defaultRam + ""));
                if (this.ram > Utils.getMaximumRam()) {
                    log("Tried to allocate " + this.ram + "MB of Ram but only "
                            + Utils.getMaximumRam() + "MB is available to use!",
                            LogMessageType.warning, false);
                    this.ram = defaultRam; // User tried to allocate too much ram, set it back to
                                           // half, capped at 4GB
                }
            } else {
                this.ram = Integer.parseInt(properties.getProperty("ram", "1024"));
                if (this.ram > Utils.getMaximumRam()) {
                    log("Tried to allocate " + this.ram + "MB of Ram but only "
                            + Utils.getMaximumRam() + "MB is available to use!",
                            LogMessageType.warning, false);
                    this.ram = 1024; // User tried to allocate too much ram, set it back to 1GB
                }
            }

            this.permGen = Integer.parseInt(properties.getProperty("permGen", "256"));

            this.windowWidth = Integer.parseInt(properties.getProperty("windowwidth", "854"));
            if (this.windowWidth > Utils.getMaximumWindowWidth()) {
                log("Tried to set window width to " + this.windowWidth
                        + " pixels but the maximum is " + Utils.getMaximumWindowWidth()
                        + " pixels!", LogMessageType.warning, false);
                this.windowWidth = Utils.getMaximumWindowWidth(); // User tried to make screen size
                                                                  // wider than they have
            }

            this.windowHeight = Integer.parseInt(properties.getProperty("windowheight", "480"));
            if (this.windowHeight > Utils.getMaximumWindowHeight()) {
                log("Tried to set window height to " + this.windowHeight
                        + " pixels but the maximum is " + Utils.getMaximumWindowHeight()
                        + " pixels!", LogMessageType.warning, false);
                this.windowHeight = Utils.getMaximumWindowHeight(); // User tried to make screen
                                                                    // size wider than they have
            }

            this.usingCustomJavaPath = Boolean.parseBoolean(properties.getProperty(
                    "usingcustomjavapath", "false"));

            if (isUsingCustomJavaPath()) {
                this.javaPath = properties.getProperty("javapath", Utils.getJavaHome());
            } else {
                this.javaPath = Utils.getJavaHome();
            }

            this.javaParamaters = properties.getProperty("javaparameters", "");

            this.maximiseMinecraft = Boolean.parseBoolean(properties.getProperty(
                    "maximiseminecraft", "false"));

            this.sortPacksAlphabetically = Boolean.parseBoolean(properties.getProperty(
                    "sortpacksalphabetically", "false"));

            this.enableConsole = Boolean.parseBoolean(properties.getProperty("enableconsole",
                    "true"));

            this.enableDebugConsole = Boolean.parseBoolean(properties.getProperty(
                    "enabledebugconsole", "false"));

            this.enableLeaderboards = Boolean.parseBoolean(properties.getProperty(
                    "enableleaderboards", "false"));

            this.enableLogs = Boolean.parseBoolean(properties.getProperty("enablelogs", "true"));

            String lastAccountTemp = properties.getProperty("lastaccount", "");
            if (!lastAccountTemp.isEmpty()) {
                if (isAccountByName(lastAccountTemp)) {
                    this.account = getAccountByName(lastAccountTemp);
                } else {
                    log("The Account " + lastAccountTemp
                            + " is no longer available. Logging out of Account!",
                            LogMessageType.warning, false);
                    this.account = null; // Account not found
                }
            }

            this.addedPacks = properties.getProperty("addedpacks", "");
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    /**
     * Save the properties to file
     */
    public void saveProperties() {
        try {
            properties.setProperty("firsttimerun", "false");
            properties.setProperty("language", this.language.getName());
            properties.setProperty("server", this.server.getName());
            properties.setProperty("forgelogginglevel", this.forgeLoggingLevel);
            properties.setProperty("ram", this.ram + "");
            properties.setProperty("permGen", this.permGen + "");
            properties.setProperty("windowwidth", this.windowWidth + "");
            properties.setProperty("windowheight", this.windowHeight + "");
            properties.setProperty("usingcustomjavapath", (this.usingCustomJavaPath) ? "true"
                    : "false");
            properties.setProperty("javapath", this.javaPath);
            properties.setProperty("javaparameters", this.javaParamaters);
            properties
                    .setProperty("maximiseminecraft", (this.maximiseMinecraft) ? "true" : "false");
            properties.setProperty("sortpacksalphabetically",
                    (this.sortPacksAlphabetically) ? "true" : "false");
            properties.setProperty("enableconsole", (this.enableConsole) ? "true" : "false");
            properties.setProperty("enabledebugconsole", (this.enableDebugConsole) ? "true"
                    : "false");
            properties.setProperty("enableleaderboards", (this.enableLeaderboards) ? "true"
                    : "false");
            properties.setProperty("enablelogs", (this.enableLogs) ? "true" : "false");
            if (account != null) {
                properties.setProperty("lastaccount", account.getUsername());
            } else {
                properties.setProperty("lastaccount", "");
            }
            properties.setProperty("addedpacks", this.addedPacks);
            this.properties.store(new FileOutputStream(propertiesFile), "TechnoLauncher Settings");
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    /**
     * Switch account currently used and save it
     *
     * @param account
     *            Account to switch to
     */
    public void switchAccount(Account account) {
        if (account == null) {
            log("Logging out of account");
            this.account = null;
        } else {
            if (account.isReal()) {
                log("Changed account to " + account);
                this.account = account;
            } else {
                log("Logging out of account");
                this.account = null;
            }
        }
        reloadPacksPanel();
        reloadInstancesPanel();
        reloadAccounts();
        try {
            properties.setProperty("firsttimerun", "false");
            properties.setProperty("language", this.language.getName());
            properties.setProperty("server", this.server.getName());
            properties.setProperty("ram", this.ram + "");
            properties.setProperty("windowwidth", this.windowWidth + "");
            properties.setProperty("windowheight", this.windowHeight + "");
            properties.setProperty("usingcustomjavapath", (this.usingCustomJavaPath) ? "true"
                    : "false");
            properties.setProperty("javapath", this.javaPath);
            properties.setProperty("javaparameters", this.javaParamaters);
            properties
                    .setProperty("maximiseminecraft", (this.maximiseMinecraft) ? "true" : "false");
            properties.setProperty("sortpacksalphabetically",
                    (this.sortPacksAlphabetically) ? "true" : "false");
            properties.setProperty("enableconsole", (this.enableConsole) ? "true" : "false");
            properties.setProperty("enabledebugconsole", (this.enableDebugConsole) ? "true"
                    : "false");
            properties.setProperty("enableleaderboards", (this.enableLeaderboards) ? "true"
                    : "false");
            properties.setProperty("enablelogs", (this.enableLogs) ? "true" : "false");
            if (account == null) {
                properties.setProperty("lastaccount", "");
            } else {
                properties.setProperty("lastaccount", account.getUsername());
            }
            properties.setProperty("addedpacks", this.addedPacks);
            this.properties.store(new FileOutputStream(propertiesFile), "TechnoLauncher Settings");
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    /**
     * The servers available to use in the Launcher
     *
     * These MUST be hardcoded in order for the Launcher to make the initial connections to download
     * files
     */
    private void setupServers() {
        // INSERT SERVERS HERE



    }

    public boolean disableServerGetNext() {
        this.server.disableServer(); // Disable the server
        for (Server server : this.servers) {
            if (!server.isDisabled()) {
                log("Server " + this.server.getName() + " Not Available! Switching To "
                        + server.getName(), LogMessageType.warning, true);
                this.server = server; // Setup next available server
                return true;
            }
        }
        return false;
    }

    public void clearTriedServers() {
        this.triedServers = new ArrayList<Server>(); // Clear the list
    }

    public boolean getNextServer() {
        this.triedServers.add(this.server);
        for (Server server : this.servers) {
            if (!this.triedServers.contains(server)) {
                log("Server " + this.server.getName() + " Not Available! Switching To "
                        + server.getName(), LogMessageType.warning, true);
                this.server = server; // Setup next available server
                return true;
            }
        }
        return false;
    }

    /**
     * Loads the languages for use in the Launcher
     */
    private void loadLanguages() {
        Language language;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(configsDir, "languages.xml"));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("language");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    String localizedName = element.getAttribute("localizedname");
                    language = new Language(name, localizedName);
                    languages.add(language);
                }
            }
        } catch (SAXException e) {
            logStackTrace(e);
        } catch (ParserConfigurationException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    /**
     * Loads the Packs for use in the Launcher
     */
    private void loadPacks(int tries) {
        if (this.packs.size() != 0) {
            this.packs = new ArrayList<Pack>();
        }
        boolean errored = false;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(configsDir, "packs.xml"));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("pack");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    int id = Integer.parseInt(element.getAttribute("id"));
                    String name = element.getAttribute("name");
                    boolean createServer = Boolean.parseBoolean(element
                            .getAttribute("createserver"));
                    boolean leaderboards = Boolean.parseBoolean(element
                            .getAttribute("leaderboards"));
                    boolean isMinecraftEdu = Boolean.parseBoolean(element
                            .getAttribute("minecraftedu"));
                    boolean isTeacher = Boolean.parseBoolean(element
                            .getAttribute("teacher"));
                    boolean logging = Boolean.parseBoolean(element.getAttribute("logging"));
                    boolean crashReports = Boolean.parseBoolean(element
                            .getAttribute("crashreports"));
                    String[] versions;
                    if (element.getAttribute("versions").isEmpty()) {
                        versions = new String[0];
                    } else {
                        versions = element.getAttribute("versions").split(",");
                    }
                    String[] noUpdateVersions;
                    if (element.getAttribute("noupdateversions").isEmpty()) {
                        noUpdateVersions = new String[0];
                    } else {
                        noUpdateVersions = element.getAttribute("noupdateversions").split(",");
                    }
                    String[] minecraftVersions;
                    if (element.getAttribute("minecraftversions").isEmpty()) {
                        minecraftVersions = new String[0];
                    } else {
                        minecraftVersions = element.getAttribute("minecraftversions").split(",");
                    }
                    String[] devVersions;
                    if (element.getAttribute("devversions").isEmpty()) {
                        devVersions = new String[0];
                    } else {
                        devVersions = element.getAttribute("devversions").split(",");
                    }
                    String[] devMinecraftVersions;
                    if (element.getAttribute("devminecraftversions").isEmpty()) {
                        devMinecraftVersions = new String[0];
                    } else {
                        devMinecraftVersions = element.getAttribute("devminecraftversions").split(
                                ",");
                    }
                    String description = element.getAttribute("description");
                    String supportURL = element.getAttribute("supporturl");
                    String websiteURL = element.getAttribute("websiteurl");
                    if (element.getAttribute("type").equalsIgnoreCase("private")) {
                        packs.add(new PrivatePack(id, name, createServer, leaderboards, isMinecraftEdu, isTeacher, logging,
                                crashReports, versions, noUpdateVersions, minecraftVersions,
                                devVersions, devMinecraftVersions, description, supportURL,
                                websiteURL));
                    } else if (element.getAttribute("type").equalsIgnoreCase("semipublic")) {
                        if (element.hasAttribute("code")) {
                            packs.add(new SemiPublicPack(id, name, element.getAttribute("code"),
                                    createServer, leaderboards, isMinecraftEdu, isTeacher, logging, crashReports, versions,
                                    noUpdateVersions, minecraftVersions, devVersions,
                                    devMinecraftVersions, description, supportURL, websiteURL));
                        }
                    } else {
                        packs.add(new Pack(id, name, createServer, leaderboards, isMinecraftEdu, isTeacher, logging,
                                crashReports, versions, noUpdateVersions, minecraftVersions,
                                devVersions, devMinecraftVersions, description, supportURL,
                                websiteURL));
                    }
                }
            }
        } catch (SAXException e) {
            errored = true;
            logStackTrace(e);
        } catch (ParserConfigurationException e) {
            errored = true;
            logStackTrace(e);
        } catch (IOException e) {
            errored = true;
            logStackTrace(e);
        }
        if (errored && tries <= 3) {
            forceDownloadPacksXML();
            loadPacks(++tries);
        }
    }

    private void forceDownloadPacksXML() {
        Downloadable download = new Downloadable("launcher/packs.xml", new File(configsDir,
                "packs.xml"), null, null, true);
        download.download(false);
    }

    /**
     * Loads the Testers and Allowed Players for the packs in the Launcher
     */
    private void loadUsers() {
        Downloadable download = new Downloadable("launcher/users.xml", true);
        String users = download.getContents();
        if (users == null) {
            this.offlineMode = true;
            return;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(users));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("pack");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    int id = Integer.parseInt(element.getAttribute("id"));
                    Pack pack;
                    try {
                        pack = getPackByID(id);
                    } catch (InvalidPack e) {
                        log(e.getMessage(), LogMessageType.error, false);
                        continue;
                    }
                    if (!element.getAttribute("testers").isEmpty()) {
                        if (element.getAttribute("testers").contains(",")) {
                            pack.addTesters(element.getAttribute("testers").split(","));
                        } else {
                            pack.addTesters(new String[] { element.getAttribute("testers") });
                        }
                    }
                    if (pack instanceof PrivatePack) {
                        if (!element.getAttribute("allowedplayers").isEmpty()) {
                            if (element.getAttribute("allowedplayers").contains(",")) {
                                ((PrivatePack) pack).addAllowedPlayers(element.getAttribute(
                                        "allowedplayers").split(","));
                            } else {
                                ((PrivatePack) pack).addAllowedPlayers(new String[] { element
                                        .getAttribute("allowedplayers") });
                            }
                        }
                    }
                }
            }
        } catch (SAXException e) {
            logStackTrace(e);
        } catch (ParserConfigurationException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    /**
     * Loads info about the different Minecraft versions
     */
    private void loadMinecraftVersions() {
        if (this.minecraftVersions.size() != 0) {
            this.minecraftVersions = new ArrayList<MinecraftVersion>();
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(configsDir, "minecraftversions.xml"));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("minecraft");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String version = element.getAttribute("version");
                    String type = element.getAttribute("type");
                    boolean server = Boolean.parseBoolean(element.getAttribute("server"));
                    boolean legacy = Boolean.parseBoolean(element.getAttribute("legacy"));
                    boolean coremods = Boolean.parseBoolean(element.getAttribute("coremods"));
                    minecraftVersions.add(new MinecraftVersion(version, type, server, legacy,
                            coremods));
                }
            }
        } catch (SAXException e) {
            logStackTrace(e);
        } catch (ParserConfigurationException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    /**
     * Loads the Addons for use in the Launcher
     */
    private void loadAddons() {
        if (this.addons.size() != 0) {
            this.addons = new ArrayList<Addon>();
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(configsDir, "addons.xml"));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("addon");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    int id = Integer.parseInt(element.getAttribute("id"));
                    String name = element.getAttribute("name");
                    String[] versions;
                    if (element.getAttribute("versions").isEmpty()) {
                        versions = new String[0];
                    } else {
                        versions = element.getAttribute("versions").split(",");
                    }
                    String description = element.getAttribute("description");
                    Pack forPack;
                    Pack pack = getPackByID(id);
                    if (pack != null) {
                        forPack = pack;
                    } else {
                        log("Addon " + name + " is not available for any packs!",
                                LogMessageType.warning, false);
                        continue;
                    }
                    Addon addon = new Addon(id, name, versions, description, forPack);
                    addons.add(addon);
                }
            }
        } catch (SAXException e) {
            logStackTrace(e);
        } catch (ParserConfigurationException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        } catch (InvalidPack e) {
            log(e.getMessage(), LogMessageType.error, false);
        }
    }

    /**
     * Loads the user installed Instances
     */
    private void loadInstances() {
        if (this.instances.size() != 0) {
            this.instances = new ArrayList<Instance>();
        }
        boolean wasConversion = false;
        if (instancesDataFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(instancesDataFile);
                ObjectInputStream objIn = new ObjectInputStream(in);
                try {
                    Object obj;
                    while ((obj = objIn.readObject()) != null) {
                        if (obj instanceof Instance) {
                            File dir = new File(getInstancesDir(), ((Instance) obj).getSafeName());
                            if (dir.exists()) {
                                Instance instance = (Instance) obj;
                                if (!instance.hasBeenConverted()) {
                                    log("Instance "
                                            + instance.getName()
                                            + " is being converted! This is normal and should only appear once!",
                                            LogMessageType.warning, false);
                                    instance.convert();
                                    wasConversion = true;
                                }
                                if (!instance.getDisabledModsDirectory().exists()) {
                                    instance.getDisabledModsDirectory().mkdir();
                                }
                                instances.add(instance);
                                if (isPackByName(instance.getPackName())) {
                                    instance.setRealPack(getPackByName(instance.getPackName()));
                                }
                            }
                        }
                    }
                } catch (EOFException e) {
                    // Don't log this, it always happens when it gets to the end of the file
                } finally {
                    objIn.close();
                    in.close();
                }
            } catch (Exception e) {
                logStackTrace(e);
            }
        }
        if (wasConversion) {
            saveInstances();
        }
    }

    public void saveInstances() {
        FileOutputStream out = null;
        ObjectOutputStream objOut = null;
        try {
            out = new FileOutputStream(instancesDataFile);
            objOut = new ObjectOutputStream(out);
            for (Instance instance : instances) {
                objOut.writeObject(instance);
            }
        } catch (IOException e) {
            logStackTrace(e);
        } finally {
            try {
                objOut.close();
                out.close();
            } catch (IOException e) {
                logStackTrace(e);
            }
        }
    }

    /**
     * Loads the saved Accounts
     */
    private void loadAccounts() {
        if (userDataFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(userDataFile);
                ObjectInputStream objIn = new ObjectInputStream(in);
                try {
                    Object obj;
                    while ((obj = objIn.readObject()) != null) {
                        if (obj instanceof Account) {
                            accounts.add((Account) obj);
                        }
                    }
                } catch (EOFException e) {
                    // Don't log this, it always happens when it gets to the end of the file
                } finally {
                    objIn.close();
                    in.close();
                }
            } catch (Exception e) {
                logStackTrace(e);
            }
        }
    }

    public void saveAccounts() {
        FileOutputStream out = null;
        ObjectOutputStream objOut = null;
        try {
            out = new FileOutputStream(userDataFile);
            objOut = new ObjectOutputStream(out);
            for (Account account : accounts) {
                objOut.writeObject(account);
            }
        } catch (IOException e) {
            logStackTrace(e);
        } finally {
            try {
                objOut.close();
                out.close();
            } catch (IOException e) {
                logStackTrace(e);
            }
        }
    }

    public void removeAccount(Account account) {
        if (this.account == account) {
            switchAccount(null);
        }
        accounts.remove(account);
        saveAccounts();
        reloadAccounts();
    }

    /**
     * Finds out if this is the first time the Launcher has been run
     *
     * @return true if the Launcher hasn't been run and setup yet, false for otherwise
     */
    public boolean isFirstTimeRun() {
        return this.firstTimeRun;
    }

    public boolean isMinecraftLaunched() {
        return this.minecraftLaunched;
    }

    public void setMinecraftLaunched(boolean launched) {
        this.minecraftLaunched = launched;
    }

    /**
     * Get the Packs available in the Launcher
     *
     * @return The Packs available in the Launcher
     */
    public ArrayList<Pack> getPacks() {
        return this.packs;
    }

    /**
     * Get the Packs available in the Launcher sorted alphabetically
     *
     * @return The Packs available in the Launcher sorted alphabetically
     */
    public ArrayList<Pack> getPacksSorted() {
        ArrayList<Pack> packs = new ArrayList<Pack>(this.packs);
        Collections.sort(packs, new Comparator<Pack>() {
            public int compare(Pack result1, Pack result2) {
                return result1.getName().compareTo(result2.getName());
            }
        });
        return packs;
    }

    public void setPackVisbility(Pack pack, boolean collapsed) {
        if (pack != null && account.isReal()) {
            if (collapsed) {
                // Closed It
                if (!account.getCollapsedPacks().contains(pack.getName())) {
                    account.getCollapsedPacks().add(pack.getName());
                }
            } else {
                // Opened It
                if (account.getCollapsedPacks().contains(pack.getName())) {
                    account.getCollapsedPacks().remove(pack.getName());
                }
            }
            saveAccounts();
            reloadPacksPanel();
        }
    }

    public boolean isUsingMacApp() {
        return this.usingMacApp;
    }

    public void setInstanceVisbility(Instance instance, boolean collapsed) {
        if (instance != null && account.isReal()) {
            if (collapsed) {
                // Closed It
                if (!account.getCollapsedInstances().contains(instance.getName())) {
                    account.getCollapsedInstances().add(instance.getName());
                }
            } else {
                // Opened It
                if (account.getCollapsedInstances().contains(instance.getName())) {
                    account.getCollapsedInstances().remove(instance.getName());
                }
            }
            saveAccounts();
            reloadInstancesPanel();
        }
    }

    /**
     * Get the Instances available in the Launcher
     *
     * @return The Instances available in the Launcher
     */
    public ArrayList<Instance> getInstances() {
        return this.instances;
    }

    /**
     * Get the Instances available in the Launcher sorted alphabetically
     *
     * @return The Instances available in the Launcher sorted alphabetically
     */
    public ArrayList<Instance> getInstancesSorted() {
        ArrayList<Instance> instances = new ArrayList<Instance>(this.instances);
        Collections.sort(instances, new Comparator<Instance>() {
            public int compare(Instance result1, Instance result2) {
                return result1.getName().compareTo(result2.getName());
            }
        });
        return instances;
    }

    public void setInstanceUnplayable(Instance instance) {
        instance.setUnplayable(); // Set the instance as unplayable
        saveInstances(); // Save the instancesdata file
        reloadInstancesPanel(); // Reload the instances tab
    }

    /**
     * Removes an instance from the Launcher
     */
    public void removeInstance(Instance instance) {
        if (this.instances.remove(instance)) { // Remove the instance
            Utils.delete(instance.getRootDirectoryToDelete());
            saveInstances(); // Save the instancesdata file
            reloadInstancesPanel(); // Reload the instances panel
        }
    }

    public String apiCall(String username, String action, String extra1, String extra2,
            String extra3, boolean debug) {
        String response = "We hope you find this TechnoLauncher helpful!";
    	/*
        String response = "";
        try {
            String data = URLEncoder.encode("username", "UTF-8") + "="
                    + URLEncoder.encode(username, "UTF-8");
            data += "&" + URLEncoder.encode("action", "UTF-8") + "="
                    + URLEncoder.encode(action, "UTF-8");
            data += "&" + URLEncoder.encode("extra1", "UTF-8") + "="
                    + URLEncoder.encode(extra1, "UTF-8");
            data += "&" + URLEncoder.encode("extra2", "UTF-8") + "="
                    + URLEncoder.encode(extra2, "UTF-8");
            data += "&" + URLEncoder.encode("extra3", "UTF-8") + "="
                    + URLEncoder.encode(extra3, "UTF-8");


            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                response += line;
                if (debug) {
                    log("API Call Response: " + line);
                }
            }
            wr.close();
        } catch (Exception e) {
            logStackTrace(e);
        }
        */

        return response;
    }

    public void apiCall(String username, String action, String extra1, String extra2, String extra3) {
        apiCall(username, action, extra1, extra2, extra3, false);
    }

    public void apiCall(String username, String action, String extra1, String extra2) {
        apiCall(username, action, extra1, extra2, "", false);
    }

    public void apiCall(String username, String action, String extra1) {
        apiCall(username, action, extra1, "", "", false);
    }

    public void apiCall(String username, String action) {
        apiCall(username, action, "", "", "", false);
    }

    public String apiCallReturn(String username, String action, String extra1, String extra2,
            String extra3) {
        return apiCall(username, action, extra1, extra2, extra3, false);
    }

    public String apiCallReturn(String username, String action, String extra1, String extra2) {
        return apiCall(username, action, extra1, extra2, "", false);
    }

    public String apiCallReturn(String username, String action, String extra1) {
        return apiCall(username, action, extra1, "", "", false);
    }

    public String apiCallReturn(String username, String action) {
        return apiCall(username, action, "", "", "", false);
    }

    public boolean canViewSemiPublicPackByCode(String packCode) {
        for (String code : this.addedPacks.split(",")) {
            if (Utils.getMD5(code).equalsIgnoreCase(packCode)) {
                return true;
            }
        }
        return false;
    }

    public MinecraftVersion getMinecraftVersion(String version) throws InvalidMinecraftVersion {
        for (MinecraftVersion minecraftVersion : minecraftVersions) {
            if (minecraftVersion.getVersion().equalsIgnoreCase(version)) {
                return minecraftVersion;
            }
        }
        throw new InvalidMinecraftVersion("No Minecraft version found matching " + version);
    }

    public boolean semiPublicPackExistsFromCode(String packCode) {
        String packCodeMD5 = Utils.getMD5(packCode);
        for (Pack pack : this.packs) {
            if (pack instanceof SemiPublicPack) {
                if (((SemiPublicPack) pack).getCode().equalsIgnoreCase(packCodeMD5)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addPack(String packCode) {
        String packCodeMD5 = Utils.getMD5(packCode);
        for (Pack pack : this.packs) {
            if (pack instanceof SemiPublicPack
                    && !App.settings.canViewSemiPublicPackByCode(packCodeMD5)) {
                if (((SemiPublicPack) pack).getCode().equalsIgnoreCase(packCodeMD5)) {
                    if (pack.isTester()) {
                        return false;
                    }
                    this.addedPacks += packCode + ",";
                    this.saveProperties();
                    this.reloadInstancesPanel();
                    return true;
                }
            }
        }
        return false;
    }

    public void removePack(String packCode) {
        for (String code : this.addedPacks.split(",")) {
            if (Utils.getMD5(code).equalsIgnoreCase(packCode)) {
                this.addedPacks = this.addedPacks.replace(code + ",", ""); // Remove the string
                this.saveProperties();
                this.reloadInstancesPanel();
            }
        }
    }

    /**
     * Get the Addons available in the Launcher
     *
     * @return The Addons available in the Launcher
     */
    public ArrayList<Addon> getAddons() {
        return this.addons;
    }

    /**
     * Get the Accounts added to the Launcher
     *
     * @return The Accounts added to the Launcher
     */
    public ArrayList<Account> getAccounts() {
        return this.accounts;
    }

    /**
     * Get the Languages available in the Launcher
     *
     * @return The Languages available in the Launcher
     */
    public ArrayList<Language> getLanguages() {
        return this.languages;
    }

    /**
     * Get the Servers available in the Launcher
     *
     * @return The Servers available in the Launcher
     */
    public ArrayList<Server> getServers() {
        return this.servers;
    }

    /**
     * Determines if offline mode is enabled or not
     *
     * @return true if offline mode is enabled, false otherwise
     */
    public boolean isInOfflineMode() {
        return this.offlineMode;
    }

    public void checkOnlineStatus() {
        for (Server server : servers) {
            server.enableServer();
        }
        this.offlineMode = false;
        Downloadable download = new Downloadable("launcher/users.xml", true);
        String test = download.getContents();
        if (test != null && test.equalsIgnoreCase("pong")) {
            this.offlineMode = false;
            reloadPacksPanel();
            reloadInstancesPanel();
        } else {
            this.offlineMode = true;
        }
    }

    /**
     * Sets the launcher to offline mode
     */
    public void setOfflineMode() {
        this.offlineMode = true;
    }

    /**
     * Sets the launcher to online mode
     */
    public void setOnlineMode() {
        this.offlineMode = false;
    }

    /**
     * Returns the JFrame reference of the main Launcher
     *
     * @return Main JFrame of the Launcher
     */
    public Window getParent() {
        return this.parent;
    }

    /**
     * Sets the panel used for Instances
     *
     * @param instancesPanel
     *            Instances Panel
     */
    public void setInstancesPanel(InstancesPanel instancesPanel) {
        this.instancesPanel = instancesPanel;
    }

    /**
     * Reloads the panel used for Instances
     */
    public void reloadInstancesPanel() {
        if (instancesPanel != null) {
            this.instancesPanel.reload(); // Reload the instances panel
        }
    }

    /**
     * Sets the panel used for Packs
     *
     * @param packsPanel
     *            Packs Panel
     */
    public void setPacksPanel(PacksPanel packsPanel) {
        this.packsPanel = packsPanel;
    }

    /**
     * Sets the panel used for News
     *
     * @param newsPanel
     *            News Panel
     */
    public void setNewsPanel(NewsPanel newsPanel) {
        this.newsPanel = newsPanel;
    }

    /**
     * Reloads the panel used for News
     */
    public void reloadNewsPanel() {
        this.newsPanel.reload(); // Reload the news panel
    }

    /**
     * Reloads the panel used for Packs
     */
    public void reloadPacksPanel() {
        this.packsPanel.reload(); // Reload the instances panel
    }

    /**
     * Sets the bottom bar
     *
     * @param bottomBar
     *            The Bottom Bar
     */
    public void setBottomBar(BottomBar bottomBar) {
        this.bottomBar = bottomBar;
    }

    /**
     * Reloads the bottom bar accounts combobox
     */
    public void reloadAccounts() {
        this.bottomBar.reloadAccounts(); // Reload the Bottom Bar accounts combobox
    }

    /**
     * Checks to see if there is already an instance with the name provided or not
     *
     * @param name
     *            The name of the instance to check for
     * @return True if there is an instance with the same name already
     */
    public boolean isInstance(String name) {
        for (Instance instance : instances) {
            if (instance.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", ""))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Pack from the given ID number
     *
     * @param id
     *            ID of the Pack to find
     * @return Pack if the pack is found from the ID
     * @throws InvalidPack
     *             If ID is not found
     */
    public Pack getPackByID(int id) throws InvalidPack {
        for (Pack pack : packs) {
            if (pack.getID() == id) {
                return pack;
            }
        }
        throw new InvalidPack("No pack exists with ID " + id);
    }

    /**
     * Checks if there is a pack by the given name
     *
     * @param name
     *            name of the Pack to find
     * @return True if the pack is found from the name
     */
    public boolean isPackByName(String name) {
        for (Pack pack : packs) {
            if (pack.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the user is a tester of any packs
     *
     * @return True if they are, false otherwise
     */
    public boolean isPackTester() {
        for (Pack pack : packs) {
            if (pack.isTester()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Pack from the given name
     *
     * @param name
     *            name of the Pack to find
     * @return Pack if the pack is found from the name
     */
    public Pack getPackByName(String name) {
        for (Pack pack : packs) {
            if (pack.getName().equalsIgnoreCase(name)) {
                return pack;
            }
        }
        return null;
    }

    /**
     * Checks if there is an instance by the given name
     *
     * @param name
     *            name of the Instance to find
     * @return True if the instance is found from the name
     */
    public boolean isInstanceByName(String name) {
        for (Instance instance : instances) {
            if (instance.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is an instance by the given name
     *
     * @param name
     *            name of the Instance to find
     * @return True if the instance is found from the name
     */
    public boolean isInstanceBySafeName(String name) {
        for (Instance instance : instances) {
            if (instance.getSafeName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Instance from the given name
     *
     * @param name
     *            name of the Instance to find
     * @return Instance if the instance is found from the name
     */
    public Instance getInstanceByName(String name) {
        for (Instance instance : instances) {
            if (instance.getName().equalsIgnoreCase(name)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Finds a Instance from the given name
     *
     * @param name
     *            name of the Instance to find
     * @return Instance if the instance is found from the name
     */
    public Instance getInstanceBySafeName(String name) {
        for (Instance instance : instances) {
            if (instance.getSafeName().equalsIgnoreCase(name)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Finds a Language from the given name
     *
     * @param name
     *            Name of the Language to find
     * @return Language if the language is found from the name
     */
    private Language getLanguageByName(String name) {
        for (Language language : languages) {
            if (language.getName().equalsIgnoreCase(name)) {
                return language;
            }
        }
        return null;
    }

    /**
     * Finds a Server from the given name
     *
     * @param name
     *            Name of the Server to find
     * @return Server if the server is found from the name
     */
    private Server getServerByName(String name) {
        for (Server server : servers) {
            if (server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }

    /**
     * Finds an Account from the given username
     *
     * @param username
     *            Username of the Account to find
     * @return Account if the Account is found from the username
     */
    private Account getAccountByName(String username) {
        for (Account account : accounts) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Finds if a language is available
     *
     * @param name
     *            The name of the Language
     * @return true if found, false if not
     */
    public boolean isLanguageByName(String name) {
        for (Language language : languages) {
            if (language.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds if a server is available
     *
     * @param name
     *            The name of the Server
     * @return true if found, false if not
     */
    public boolean isServerByName(String name) {
        for (Server server : servers) {
            if (server.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds if an Account is available
     *
     * @param username
     *            The username of the Account
     * @return true if found, false if not
     */
    public boolean isAccountByName(String username) {
        for (Account account : accounts) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the URL for a file on the user selected server
     *
     * @param filename
     *            Filename including directories on the server
     * @return URL of the file
     */
    public String getFileURL(String filename) {
        return this.server.getFileURL(filename);
    }

    /**
     * Finds out if the Launcher Console is visible or not
     *
     * @return true if the console is visible, false if it's been hidden
     */
    public boolean isConsoleVisible() {
        return this.console.isVisible();
    }

    /**
     * Gets the Launcher's current Console instance
     *
     * @return The Launcher's Console instance
     */
    @Deprecated
    public LauncherConsole getConsole() {
        return this.console;
    }

    public void clearConsole() {
        this.console.clearConsole();
    }

    public void addConsoleListener(ComponentAdapter ca) {
        this.console.addComponentListener(ca);
    }

    public String getLog() {
        return this.console.getLog();
    }

    /**
     * Log a non Minecraft related info message to the console
     */
    public void log(String message) {
        this.console.log(message, LogMessageType.info, false);
    }

    /**
     * Log a Minecraft related message to the console
     */
    public void logMinecraft(String message) {
        this.console.logMinecraft(message);
    }

    /**
     * Logs a stack trace to the console window
     *
     * @param exception
     *            The exception to show in the console
     */
    public void logStackTrace(Exception exception) {
        exception.printStackTrace();
        log(exception.getMessage(), LogMessageType.error, false);
        if (enableDebugConsole()) {
            for (StackTraceElement element : exception.getStackTrace()) {
                if (element.toString() != null) {
                    log(element.toString(), LogMessageType.error, false);
                }
            }
        }
    }

    /**
     * Log something to the console
     */
    public void log(String message, LogMessageType type, boolean isMinecraft) {
        this.console.log(message, type, isMinecraft);
    }

    public void showKillMinecraft(Process minecraft) {
        this.minecraftProcess = minecraft;
        this.console.showKillMinecraft();
    }

    public void hideKillMinecraft() {
        this.console.hideKillMinecraft();
    }

    public void killMinecraft() {
        if (this.minecraftProcess != null) {
            log("Killing Minecraft", LogMessageType.error, false);
            this.minecraftProcess.destroy();
            this.minecraftProcess = null;
        }
    }

    /**
     * Gets the users current active Language
     *
     * @return The users set language
     */
    public Language getLanguage() {
        return this.language;
    }

    /**
     * Sets the users current active Language
     *
     * @param language
     *            The language to set to
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Gets the users current active Server
     *
     * @return The users set server
     */
    public Server getServer() {
        return this.server;
    }

    /**
     * Gets the users saved Server
     *
     * @return The users saved server
     */
    public Server getOriginalServer() {
        return this.originalServer;
    }

    /**
     * Gets the users setting for Forge Logging Level
     *
     * @return The users setting for Forge Logging Level
     */
    public String getForgeLoggingLevel() {
        return this.forgeLoggingLevel;
    }

    /**
     * Sets the users setting for Forge Logging Level
     */
    public void setForgeLoggingLevel(String forgeLoggingLevel) {
        this.forgeLoggingLevel = forgeLoggingLevel;
    }

    /**
     * Sets the users current active Server
     *
     * @param server
     *            The server to set to
     */
    public void setServer(Server server) {
        this.server = server;
        this.originalServer = server;
    }

    public int getMemory() {
        return this.ram;
    }

    public void setMemory(int memory) {
        this.ram = memory;
    }

    public int getPermGen() {
        return this.permGen;
    }

    public void setPermGen(int permGen) {
        this.permGen = permGen;
    }

    public int getWindowWidth() {
        return this.windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return this.windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public boolean isUsingCustomJavaPath() {
        return this.usingCustomJavaPath;
    }

    public String getJavaPath() {
        return this.javaPath;
    }

    public void setJavaPath(String javaPath) {
        if (javaPath.equalsIgnoreCase(Utils.getJavaHome())) {
            this.usingCustomJavaPath = false;
        } else {
            this.usingCustomJavaPath = true;
        }
        this.javaPath = javaPath;
    }

    public String getJavaParameters() {
        return this.javaParamaters;
    }

    public void setJavaParameters(String javaParamaters) {
        this.javaParamaters = javaParamaters;
    }

    public Account getAccount() {
        return this.account;
    }

    /**
     * If the user has selected to start Minecraft maximised
     *
     * @return true if yes, false if not
     */
    public boolean startMinecraftMaximised() {
        return this.maximiseMinecraft;
    }

    public void setStartMinecraftMaximised(boolean maximiseMinecraft) {
        this.maximiseMinecraft = maximiseMinecraft;
    }

    /**
     * If the user has selected to display packs alphabetically or nto
     *
     * @return true if yes, false if not
     */
    public boolean sortPacksAlphabetically() {
        return this.sortPacksAlphabetically;
    }

    public void setSortPacksAlphabetically(boolean sortPacksAlphabetically) {
        this.sortPacksAlphabetically = sortPacksAlphabetically;
    }

    /**
     * If the user has selected to show the console always or not
     *
     * @return true if yes, false if not
     */
    public boolean enableConsole() {
        return this.enableConsole;
    }

    /**
     * If the user has selected to see debug messages in the console
     *
     * @return true if yes, false if not
     */
    public boolean enableDebugConsole() {
        return this.enableDebugConsole;
    }

    /**
     * Set the Launcher console's visibility
     *
     * @param visible
     *            The Launcher console's visibility
     */
    public void setConsoleVisible(boolean visible) {
        this.console.setVisible(visible);
    }

    public void setEnableConsole(boolean enableConsole) {
        this.enableConsole = enableConsole;
    }

    public void setEnableDebugConsole(boolean enableDebugConsole) {
        this.enableDebugConsole = enableDebugConsole;
    }

    public boolean enableLeaderboards() {
        return this.enableLeaderboards;
    }

    public void setEnableLeaderboards(boolean enableLeaderboards) {
        this.enableLeaderboards = enableLeaderboards;
    }

    public boolean enableLogs() {
        return this.enableLogs;
    }

    public void setEnableLogs(boolean enableLogs) {
        this.enableLogs = enableLogs;
    }

    public String getVersion() {
        return this.version;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public String getLocalizedString(String string) {
        return language.getString(string);
    }

    public String getLocalizedString(String string, String replace) {
        return language.getString(string).replace("%s", replace);
    }

    public void restartLauncher() {
        File thisFile = new File(Update.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath());
        String path = null;
        try {
            path = thisFile.getCanonicalPath();
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }

        List<String> arguments = new ArrayList<String>();

        if (usingMacApp) {
            arguments.add("open");
            arguments.add("-n");
            arguments
                    .add(baseDir.getParentFile().getParentFile().getParentFile().getAbsolutePath());

        } else {
            String jpath = System.getProperty("java.home") + File.separator + "bin"
                    + File.separator + "java";
            if (Utils.isWindows()) {
                jpath += "w";
            }
            arguments.add(jpath);
            arguments.add("-jar");
            arguments.add(path);
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(arguments);

        try {
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
