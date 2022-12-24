package br.com.p8projects.filehub.system;

import br.com.p8projects.filehub.infrastructure.config.StorageResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Configuration
public class SystemBoot {

    private Logger logger = LoggerFactory.getLogger(SystemBoot.class);

    private SystemProperties properties;
    private StorageResourceReader storageReader;

    @Autowired
    public SystemBoot(SystemProperties properties, StorageResourceReader storageReader) {
        this.properties = properties;
        this.storageReader = storageReader;
    }


    @PostConstruct
    public void initialization() {
        Locale.setDefault(Locale.of("pt", "BR"));
        showSystemInfo();
        storageReader.loadProperties(properties);
        printStorages();
    }

    private void showSystemInfo() {
        String logo = "\n" +
                "    _____ __     __          __        \n" +
                "   / __(_) /__  / /_  __  __/ /_       \n" +
                "  / /_/ / / _ \\/ __ \\/ / / / __ \\   \n" +
                " / __/ / /  __/ / / / /_/ / /_/ /      \n" +
                "/_/ /_/_/\\___/_/ /_/\\__,_/_.___/     \n" +
                "                                       \n" +
                "#--------------- SYSTEM STARTED ---------------#" +
                "\nCurrent Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) +
                "\nRunning on port: " + properties.getAppPort() +
                "\nVersion: " + properties.getSystemVersion() +
                "\nStorage Reader Type: " + properties.getConfigType();
        logger.info(logo);
    }

    private void printStorages() {
        StringBuilder storages = new StringBuilder();
        storages.append("\n------------------------------------------------");
        storages.append("\nCONFIGURED STORAGES:\n");
        StorageResourceReader.getStorageResource().getStorages().forEach(
            (k, v) -> storages.append("+ ").append(k).append(": ").append(v.getType().name()).append("\n")
        );
        logger.info(storages.toString());
    }

}
