package br.com.p8projects.filehub.infrastructure.config.reader;

import br.com.p8projects.filehub.domain.exceptions.PropertiesReaderException;
import br.com.p8projects.filehub.domain.interfaces.FileConfigReader;
import br.com.p8projects.filehub.domain.model.EnumConfigReaderType;
import br.com.p8projects.filehub.domain.model.config.StorageResource;
import br.com.p8projects.filehub.infrastructure.config.XMLStorageReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class LocalFileReader implements FileConfigReader {

    @Value("${filehub.config.local-file.path}") private String localFilePath;


    @Override
    public EnumConfigReaderType getConfigReaderName() {
        return EnumConfigReaderType.LOCAL_FILE;
    }

    @Override
    public StorageResource readSchemasFromConfigurationFile() {
        XMLStorageReader XMLStorageReader = new XMLStorageReader();
        String content = getConfigurationFileContent();
        return XMLStorageReader.read(content);
    }

    private String getConfigurationFileContent() {
        if(localFilePath == null || localFilePath.trim().isEmpty()) {
            throw new PropertiesReaderException("Path not informed to local file reader");
        }
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(localFilePath)));
        } catch (IOException e) {
            throw new PropertiesReaderException("File not found in the follow path: " + localFilePath);
        }
        return content;
    }

}
