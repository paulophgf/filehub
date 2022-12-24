package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.exceptions.PropertiesReaderException;
import br.com.p8projects.filehub.domain.model.config.StorageResource;
import br.com.p8projects.filehub.infrastructure.config.reader.LocalFileReader;
import br.com.p8projects.filehub.reader.XLMPropertiesReaderData;
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
    void readSchemasFromConfigurationFileNotFound() {
        ReflectionTestUtils.setField(localFileReader, "localFilePath", "src/test/resources/config/not-found.xml");
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> localFileReader.readSchemasFromConfigurationFile()
        );
        String expectedMessage = "File not found in the follow path: src/test/resources/config/not-found.xml";
        assertEquals(expectedMessage, exception.getMessage());
    }

}