package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.infrastructure.config.reader.GitFileReader;
import br.com.mpps.filehub.reader.XLMPropertiesReaderData;
import br.com.mpps.filehub.test.TestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.util.Map;

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

    @Disabled //TODO Check what the problem
    @Test
    void readSchemasFromConfigurationFile() {
        ReflectionTestUtils.setField(gitFileReader, "fileURL", testProperties.getGitRepositoryUrl());
        ReflectionTestUtils.setField(gitFileReader, "accessToken", testProperties.getGitRepositoryToken());
        Map<String, Schema> model = data.createSchemasModel();
        Map<String, Schema> schemas = gitFileReader.readSchemasFromConfigurationFile();
        assertEquals(schemas, model);
    }

}