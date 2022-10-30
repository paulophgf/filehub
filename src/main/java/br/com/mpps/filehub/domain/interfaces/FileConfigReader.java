package br.com.mpps.filehub.domain.interfaces;

import br.com.mpps.filehub.domain.model.EnumConfigReaderType;
import br.com.mpps.filehub.domain.model.config.Schema;

import java.util.Map;

public interface FileConfigReader {

    EnumConfigReaderType getConfigReaderName();

    Map<String, Schema> readSchemasFromConfigurationFile();

}
