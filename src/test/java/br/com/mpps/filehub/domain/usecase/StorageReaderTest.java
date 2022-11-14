package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.model.EnumConfigReaderType;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import br.com.mpps.filehub.infrastructure.config.reader.GitFileReader;
import br.com.mpps.filehub.infrastructure.config.reader.LocalFileReader;
import br.com.mpps.filehub.infrastructure.config.StorageReader;
import br.com.mpps.filehub.infrastructure.config.PropertiesReaderFactory;
import br.com.mpps.filehub.reader.XLMPropertiesReaderData;
import br.com.mpps.filehub.test.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class StorageReaderTest {

    @Mock
    private PropertiesReaderFactory propertiesReaderFactory;

    @Spy
    @Resource
    @InjectMocks
    private StorageReader storageReader;

    private XLMPropertiesReaderData data;
    private TestProperties testProperties;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        data = new XLMPropertiesReaderData();
        testProperties = new TestProperties();
    }


    @Disabled //TODO Check what the problem
    @Test
    void loadPropertiesFromLocalFile() {
        Map<String, Schema> model = data.createSchemasModel();
        LocalFileReader localFileReader = new LocalFileReader();
        ReflectionTestUtils.setField(storageReader, "configType", "LOCAL_FILE");
        ReflectionTestUtils.setField(localFileReader, "localFilePath", "src/test/resources/config/success/config.xml");
        when(propertiesReaderFactory.findStrategy(EnumConfigReaderType.LOCAL_FILE)).thenReturn(localFileReader);
        storageReader.loadProperties();
        assertEquals(StorageReader.getSchemas(), model);
    }

    @Disabled //TODO Check what the problem
    @Test
    void loadPropertiesFromGitRepository() {
        Map<String, Schema> model = data.createSchemasModel();
        GitFileReader gitFileReader = new GitFileReader();
        ReflectionTestUtils.setField(storageReader, "configType", "GIT_FILE");
        ReflectionTestUtils.setField(gitFileReader, "fileURL", testProperties.getGitRepositoryUrl());
        ReflectionTestUtils.setField(gitFileReader, "accessToken", testProperties.getGitRepositoryToken());
        when(propertiesReaderFactory.findStrategy(EnumConfigReaderType.GIT_FILE)).thenReturn(gitFileReader);
        storageReader.loadProperties();
        assertEquals(StorageReader.getSchemas(), model);
    }

    @Test
    void loadPropertiesInvalidType() {
        ReflectionTestUtils.setField(storageReader, "configType", "XPTO");
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> storageReader.loadProperties()
        );
        String expectedMessage = "Invalid type of configuration reader: XPTO";
        assertEquals(expectedMessage, exception.getMessage());
    }

}