package br.com.mpps.filehub.system;

import br.com.mpps.filehub.infrastructure.config.StorageReader;

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
    private StorageReader storageReader;

    @Autowired
    public SystemBoot(SystemProperties properties, StorageReader storageReader) {
        this.properties = properties;
        this.storageReader = storageReader;
    }


    @PostConstruct
    public void initialization() {
        Locale.setDefault(Locale.of("pt", "BR"));
        showSystemInfo();
        storageReader.loadProperties();
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
                "\nVersion: " + properties.getSystemVersion();
        logger.info(logo);
    }

}
