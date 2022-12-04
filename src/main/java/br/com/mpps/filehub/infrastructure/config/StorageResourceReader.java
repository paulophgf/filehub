package br.com.mpps.filehub.infrastructure.config;

import br.com.mpps.filehub.domain.exceptions.NotFoundException;
import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import br.com.mpps.filehub.domain.interfaces.FileConfigReader;
import br.com.mpps.filehub.domain.model.EnumConfigReaderType;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Storage;
import br.com.mpps.filehub.domain.model.config.StorageResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StorageResourceReader {

    @Value("${filehub.config.type}") private String configType;

    public PropertiesReaderFactory propertiesReaderFactory;
    private static StorageResource storageResource;


    @Autowired
    public StorageResourceReader(PropertiesReaderFactory propertiesReaderFactory) {
        this.propertiesReaderFactory = propertiesReaderFactory;
    }

    public void loadProperties() {
        try {
            EnumConfigReaderType configReaderType = EnumConfigReaderType.valueOf(configType);
            FileConfigReader fileConfigReader = propertiesReaderFactory.findStrategy(configReaderType);
            storageResource = fileConfigReader.readSchemasFromConfigurationFile();
        } catch (IllegalArgumentException e) {
            throw new PropertiesReaderException("Invalid type of configuration reader: " + configType);
        }
    }

    public static Storage getStorage(String storageName) throws NotFoundException {
        Storage storage = storageResource.getStorages().get(storageName);
        if(storage == null) {
            throw new NotFoundException("Storage not found");
        }
        return storage;
    }

    public static Schema getSchema(String schema) throws NotFoundException {
        Schema storageList = storageResource.getSchemas().get(schema);
        if(storageList == null) {
            throw new NotFoundException("Schema not found");
        }
        return storageList;
    }

    public static StorageResource getStorageResource() throws NotFoundException {
        return storageResource;
    }

}
