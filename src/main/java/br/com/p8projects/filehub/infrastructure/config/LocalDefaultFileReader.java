package br.com.p8projects.filehub.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.SystemUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class LocalDefaultFileReader {

    public String createLocalDefaultConfigFile() {
        File homeDir = getHomeDir();
        File configFile = new File(homeDir.getPath() + File.separator + "fh-config.xml");
        if(!configFile.exists()) {
            log.info("Creating default configuration file (FileHub XML)");
            String defaultBaseDirPath = homeDir.getPath() + File.separator + "basedir";
            File defaultBaseDir = new File(defaultBaseDirPath);
            if (!defaultBaseDir.exists()) {
                defaultBaseDir.mkdir();
            }
            String defaultContent = readFileDefaultConfig().replace("{BASE_DIR}", defaultBaseDirPath);
            writeFileContent(configFile.getPath(), defaultContent);
        }
        return configFile.getPath();
    }

    private String readFileDefaultConfig() {
        StringBuilder content = new StringBuilder();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream("config/config-default.xml");
        try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            log.error("Error to read the default configurations from project resources");
        }
        return content.toString();
    }

    private void writeFileContent(String filePath, String content) {
        try {
            FileWriter myWriter = new FileWriter(filePath);
            myWriter.write(content);
            myWriter.close();
        } catch (IOException e) {
            log.error("Error to write the default configurations on default local file");
        }
    }

    private File getHomeDir() {
        File homeDir;
        if(SystemUtils.IS_OS_WINDOWS) {
            String userHome = System.getProperty("user.home");
            homeDir = new File(userHome + File.separator + "filehub");
        } else {
            homeDir = new File(File.separator + "filehub");
        }
        if(!homeDir.exists()) {
            homeDir.mkdir();
        }
        return homeDir;
    }

}
