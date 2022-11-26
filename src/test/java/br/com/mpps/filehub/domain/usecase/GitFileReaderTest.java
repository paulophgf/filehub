package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.model.config.StorageResource;
import br.com.mpps.filehub.infrastructure.config.reader.GitFileReader;
import br.com.mpps.filehub.reader.XLMPropertiesReaderData;
import br.com.mpps.filehub.test.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitFileReaderTest {

    @Spy
    @Resource
    @InjectMocks
    private GitFileReader gitFileReader;

    private XLMPropertiesReaderData data;
    private TestProperties testProperties;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        data = new XLMPropertiesReaderData();
        testProperties = new TestProperties();
    }

    @Disabled // Config testProperties to do this test
    @Test
    void readSchemasFromConfigurationFile() {
        ReflectionTestUtils.setField(gitFileReader, "fileURL", testProperties.getGitRepositoryUrl());
        ReflectionTestUtils.setField(gitFileReader, "accessToken", testProperties.getGitRepositoryToken());
        StorageResource model = data.createSchemasModel();
        StorageResource storageResource = gitFileReader.readSchemasFromConfigurationFile();
        assertEquals(model, storageResource);
    }

}