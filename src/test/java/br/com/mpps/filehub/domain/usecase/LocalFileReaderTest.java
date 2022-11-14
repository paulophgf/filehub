package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import br.com.mpps.filehub.infrastructure.config.reader.LocalFileReader;
import br.com.mpps.filehub.reader.XLMPropertiesReaderData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileReaderTest {

    @Spy
    @Resource
    @InjectMocks
    private LocalFileReader localFileReader;

    private XLMPropertiesReaderData data;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        data = new XLMPropertiesReaderData();
    }

    @Disabled //TODO Check what the problem
    @Test
    void readSchemasFromConfigurationFile() {
        String xmlPath = "src/test/resources/config/success/config.xml";
        ReflectionTestUtils.setField(localFileReader, "localFilePath", xmlPath);
        Map<String, Schema> model = data.createSchemasModel();
        Map<String, Schema> schemas = localFileReader.readSchemasFromConfigurationFile();
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