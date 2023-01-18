package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.exceptions.TriggerAuthenticationException;
import br.com.p8projects.filehub.domain.model.EnumFileHubOperation;
import br.com.p8projects.filehub.domain.model.FileLocation;
import br.com.p8projects.filehub.domain.model.TriggerRequestBody;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.config.StorageResource;
import br.com.p8projects.filehub.reader.XLMPropertiesReaderData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.annotation.Resource;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class TriggerAuthenticationServiceTest {

    @Spy
    @Resource
    @InjectMocks
    private TriggerAuthenticationService triggerAuthenticationService;

    private XLMPropertiesReaderData data;

    @Mock
    private TriggerCalling triggerCalling;


    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        data = new XLMPropertiesReaderData();
    }


    @Test
    void getFileLocation() {
        StorageResource resource = data.createSchemasModelWithTrigger(true);
        Schema schema = resource.getSchemas().get("S3-And-FileSystem");
        FileLocation model = new FileLocation(schema, "/account/user");
        TriggerRequestBody triggerRequestBody = new TriggerRequestBody(model, EnumFileHubOperation.CREATE_DIRECTORY);
        MockHttpServletRequest mockedRequest = new MockHttpServletRequest();
        mockedRequest.addHeader("Authorization", "token-example");
        when(triggerCalling.sendRequest(schema.getTrigger(), "token-example", triggerRequestBody)).thenReturn(new HashMap<>());
        triggerAuthenticationService.checkFileLocation(mockedRequest, model, EnumFileHubOperation.CREATE_DIRECTORY);
        assertEquals("/account/user", model.getPath());
    }

    @Test
    void getFileLocationIsRead() {
        StorageResource resource = data.createSchemasModelWithTrigger(true);
        Schema schema = resource.getSchemas().get("S3-And-FileSystem");
        FileLocation model = new FileLocation(schema,"/account/user");
        MockHttpServletRequest mockedRequest = new MockHttpServletRequest();
        triggerAuthenticationService.checkFileLocation(mockedRequest, model, EnumFileHubOperation.EXIST_DIRECTORY);
        assertEquals("/account/user", model.getPath());
    }

    @Test
    void getFileLocationWithParameters() {
        StorageResource resource = data.createSchemasModelWithTrigger(true);
        Schema schema = resource.getSchemas().get("S3-And-FileSystem");
        FileLocation model = new FileLocation(schema,"/account/$account/user/$user");
        TriggerRequestBody triggerRequestBody = new TriggerRequestBody(model, EnumFileHubOperation.CREATE_DIRECTORY);
        MockHttpServletRequest mockedRequest = new MockHttpServletRequest();
        mockedRequest.addHeader("Authorization", "token-example");
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("account", "5");
        parameters.put("user", "123");
        when(triggerCalling.sendRequest(schema.getTrigger(), "token-example", triggerRequestBody)).thenReturn(parameters);
        triggerAuthenticationService.checkFileLocation(mockedRequest, model, EnumFileHubOperation.CREATE_DIRECTORY);
        assertEquals("/account/5/user/123", model.getPath());
    }

    @Test
    void getFileLocationHeaderNotFound() {
        StorageResource resource = data.createSchemasModelWithTrigger(true);
        Schema schema = resource.getSchemas().get("S3-And-FileSystem");
        FileLocation model = new FileLocation(schema,"/account/user");
        MockHttpServletRequest mockedRequest = new MockHttpServletRequest();
        Throwable exception = assertThrows(TriggerAuthenticationException.class,
                () -> triggerAuthenticationService.checkFileLocation(mockedRequest, model, EnumFileHubOperation.CREATE_DIRECTORY)
        );
        String expectedMessage = "Header Authorization was not found in the request";
        assertEquals(expectedMessage, exception.getMessage());
    }

}