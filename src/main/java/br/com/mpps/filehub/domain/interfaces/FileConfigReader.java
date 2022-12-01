package br.com.mpps.filehub.domain.interfaces;

import br.com.mpps.filehub.domain.model.EnumConfigReaderType;
import br.com.mpps.filehub.domain.model.config.StorageResource;

public interface FileConfigReader {

    EnumConfigReaderType getConfigReaderName();

    StorageResource readSchemasFromConfigurationFile();

}
