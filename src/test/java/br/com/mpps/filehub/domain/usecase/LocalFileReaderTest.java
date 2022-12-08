package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import br.com.mpps.filehub.domain.model.config.StorageResource;
import br.com.mpps.filehub.infrastructure.config.reader.LocalFileReader;
import br.com.mpps.filehub.reader.XLMPropertiesReaderData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalFileReaderTest {

    @Spy
    @Resource
    @InjectMocks
    private LocalFileReader localFileReader;

    private XLMPropertiesReaderData data;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        data = new XLMPropertiesReaderData();
    }

    @Test
    void readSchemasFromConfigurationFile() {
        String xmlPath = "src/test/resources/config/success/config.xml";
        ReflectionTestUtils.setField(localFileReader, "localFilePath", xmlPath);
        StorageResource model = data.createSchemasModel();
        StorageResource schemas = localFileReader.readSchemasFromConfigurationFile();
        assertEquals(schemas, model);
    }

    @Test
    void readSchemasFromConfigurationFilePathNotInformed() {
        ReflectionTestUtils.setField(localFileReader, "localFilePath", null);
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> localFileReader.readSchemasFromConfigurationFile()
        );
        String expectedMessage = "Path not informed to local file reader";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void readSchemasFromConfigurationFileNotFound() {
        ReflectionTestUtils.setField(localFileReader, "localFilePath", "src/test/resources/config/not-found.xml");
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> localFileReader.readSchemasFromConfigurationFile()
        );
        String expectedMessage = "File not found in the follow path: src/test/resources/config/not-found.xml";
        assertEquals(expectedMessage, exception.getMessage());
    }

}