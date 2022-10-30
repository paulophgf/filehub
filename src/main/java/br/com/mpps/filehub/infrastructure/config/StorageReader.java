package br.com.mpps.filehub.infrastructure.config;

import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import br.com.mpps.filehub.domain.exceptions.NotFoundException;
import br.com.mpps.filehub.domain.interfaces.FileConfigReader;
import br.com.mpps.filehub.domain.model.EnumConfigReaderType;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class StorageReader {

    @Value("${filehub.config.type}") private String configType;

    public PropertiesReaderFactory propertiesReaderFactory;
    private static Map<String, Schema> schemas;


    @Autowired
    public StorageReader(PropertiesReaderFactory propertiesReaderFactory) {
        this.propertiesReaderFactory = propertiesReaderFactory;
    }

    public void loadProperties() {
        try {
            EnumConfigReaderType configReaderType = EnumConfigReaderType.valueOf(configType);
            FileConfigReader fileConfigReader = propertiesReaderFactory.findStrategy(configReaderType);
            schemas = fileConfigReader.readSchemasFromConfigurationFile();
        } catch (IllegalArgumentException e) {
            throw new PropertiesReaderException("Invalid type of configuration reader: " + configType);
        }
    }

    public static Storage getStorage(String storage) throws NotFoundException {
        Schema schema = schemas.get(storage);
        if(schema == null || !schema.isStorage()) {
            throw new NotFoundException("Storage not found");
        }
        Optional<Storage> storageOption = schema.getStorages().stream().findFirst();
        if(!storageOption.isPresent()) {
            throw new NotFoundException("Storage not found");
        }
        return storageOption.get();
    }

    public static Schema getStoragesBySchema(String schema) throws NotFoundException {
        Schema storageList = schemas.get(schema);
        if(storageList == null) {
            throw new NotFoundException("Schema not found");
        }
        return storageList;
    }

    public static Map<String, Schema> getSchemas() throws NotFoundException {
        return schemas;
    }

}
