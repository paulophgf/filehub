package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.exceptions.PropertiesReaderException;
import br.com.p8projects.filehub.domain.model.EnumConfigReaderType;
import br.com.p8projects.filehub.domain.model.config.StorageResource;
import br.com.p8projects.filehub.infrastructure.config.PropertiesReaderFactory;
import br.com.p8projects.filehub.infrastructure.config.StorageResourceReader;
import br.com.p8projects.filehub.infrastructure.config.reader.GitFileReader;
import br.com.p8projects.filehub.infrastructure.config.reader.LocalFileReader;
import br.com.p8projects.filehub.reader.XLMPropertiesReaderData;
import br.com.p8projects.filehub.system.SystemProperties;
import br.com.p8projects.filehub.test.TestProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class StorageReaderTest {

    @Mock
    private PropertiesReaderFactory propertiesReaderFactory;

    @Mock
    private SystemProperties systemProperties;

    @Spy
    @Resource
    @InjectMocks
    private StorageResourceReader storageReader;

    private XLMPropertiesReaderData data;
    private TestProperties testProperties;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        data = new XLMPropertiesReaderData();
        testProperties = new TestProperties();
        systemProperties = new SystemProperties();
    }


    @Disabled // Config testProperties to do this test
    @Test
    void loadPropertiesFromLocalFile() {
        StorageResource model = data.createSchemasModel();
        LocalFileReader localFileReader = new LocalFileReader();
        ReflectionTestUtils.setField(systemProperties, "configType", "LOCAL_FILE");
        ReflectionTestUtils.setField(localFileReader, "localFilePath", "src/test/resources/config/success/config.xml");
        when(propertiesReaderFactory.findStrategy(EnumConfigReaderType.LOCAL_FILE)).thenReturn(localFileReader);
        storageReader.loadProperties(systemProperties);
        Assertions.assertEquals(StorageResourceReader.getStorageResource(), model);
    }

    @Disabled // Config testProperties to do this test
    @Test
    void loadPropertiesFromGitRepository() {
        StorageResource model = data.createSchemasModel();
        GitFileReader gitFileReader = new GitFileReader();
        ReflectionTestUtils.setField(systemProperties, "configType", "GIT_FILE");
        ReflectionTestUtils.setField(gitFileReader, "fileURL", testProperties.getGitRepositoryUrl());
        ReflectionTestUtils.setField(gitFileReader, "accessToken", testProperties.getGitRepositoryToken());
        when(propertiesReaderFactory.findStrategy(EnumConfigReaderType.GIT_FILE)).thenReturn(gitFileReader);
        storageReader.loadProperties(systemProperties);
        Assertions.assertEquals(StorageResourceReader.getStorageResource(), model);
    }

    @Test
    void loadPropertiesInvalidType() {
        ReflectionTestUtils.setField(systemProperties, "configType", "XPTO");
        Throwable exception = assertThrows(PropertiesReaderException.class,
                () -> storageReader.loadProperties(systemProperties)
        );
        String expectedMessage = "Invalid type of configuration reader: XPTO";
        assertEquals(expectedMessage, exception.getMessage());
    }

}