package br.com.p8projects.filehub.infrastructure.config;

import br.com.p8projects.filehub.domain.exceptions.NotFoundException;
import br.com.p8projects.filehub.domain.exceptions.PropertiesReaderException;
import br.com.p8projects.filehub.domain.interfaces.FileConfigReader;
import br.com.p8projects.filehub.domain.model.EnumConfigReaderType;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.config.StorageResource;
import br.com.p8projects.filehub.system.SystemProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageResourceReader {

    public PropertiesReaderFactory propertiesReaderFactory;
    private static StorageResource storageResource;


    @Autowired
    public StorageResourceReader(PropertiesReaderFactory propertiesReaderFactory) {
        this.propertiesReaderFactory = propertiesReaderFactory;
    }

    public void loadProperties(SystemProperties properties) {
        try {
            EnumConfigReaderType configReaderType = EnumConfigReaderType.valueOf(properties.getConfigType());
            FileConfigReader fileConfigReader = propertiesReaderFactory.findStrategy(configReaderType);
            storageResource = fileConfigReader.readSchemasFromConfigurationFile();
            storageResource.createBaseDirIfNotExist();
        } catch (IllegalArgumentException e) {
            throw new PropertiesReaderException("Invalid type of configuration reader: " + properties.getConfigType());
        }
    }

    public static FhStorage getStorage(String storageName) throws NotFoundException {
        FhStorage storage = storageResource.getStorages().get(storageName);
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
