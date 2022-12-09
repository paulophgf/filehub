package br.com.p8projects.filehub.domain.interfaces;

import br.com.p8projects.filehub.domain.model.EnumConfigReaderType;
import br.com.p8projects.filehub.domain.model.config.StorageResource;

public interface FileConfigReader {

    EnumConfigReaderType getConfigReaderName();

    StorageResource readSchemasFromConfigurationFile();

}
