package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.exceptions.TriggerAuthenticationException;
import br.com.mpps.filehub.domain.model.FileLocation;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.StorageResource;
import br.com.mpps.filehub.reader.XLMPropertiesReaderData;
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
        MockitoAnnotations.initMocks(this);
        data = new XLMPropertiesReaderData();
    }


    @Test
    void getFileLocation() {
        StorageResource resource = data.createSchemasModelWithTrigger(false);
        Schema schema = resource.getSchemas().get("S3-And-FileSystem");
        FileLocation model = new FileLocation("/account/user");
        MockHttpServletRequest mockedRequest = new MockHttpServletRequest();
        mockedRequest.addHeader("Authorization", "token-example");
        when(triggerCalling.sendRequest(schema.getTrigger(), "token-example")).thenReturn(new HashMap<>());
        FileLocation result = triggerAuthenticationService.getFileLocation(mockedRequest, schema, "/account/user", false);
        assertEquals(model, result);
    }

    @Test
    void getFileLocationIsRead() {
        StorageResource resource = data.createSchemasModelWithTrigger(false);
        Schema schema = resource.getSchemas().get("S3-And-FileSystem");
        FileLocation model = new FileLocation("/account/user");
        MockHttpServletRequest mockedRequest = new MockHttpServletRequest();
        FileLocation result = triggerAuthenticationService.getFileLocation(mockedRequest, schema, "/account/user", true);
        assertEquals(model, result);
    }

    @Test
    void getFileLocationWithParameters() {
        StorageResource resource = data.createSchemasModelWithTrigger(false);
        Schema schema = resource.getSchemas().get("S3-And-FileSystem");
        FileLocation model = new FileLocation("/account/5/user/123");
        MockHttpServletRequest mockedRequest = new MockHttpServletRequest();
        mockedRequest.addHeader("Authorization", "token-example");
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("account", "5");
        parameters.put("user", "123");
        when(triggerCalling.sendRequest(schema.getTrigger(), "token-example")).thenReturn(parameters);
        FileLocation result = triggerAuthenticationService.getFileLocation(mockedRequest, schema, "/account/$account/user/$user", false);
        assertEquals(model, result);
    }

    @Test
    void getFileLocationHeaderNotFound() {
        StorageResource resource = data.createSchemasModelWithTrigger(false);
        Schema schema = resource.getSchemas().get("S3-And-FileSystem");
        MockHttpServletRequest mockedRequest = new MockHttpServletRequest();
        Throwable exception = assertThrows(TriggerAuthenticationException.class,
                () -> triggerAuthenticationService.getFileLocation(mockedRequest, schema, "/account/user", false)
        );
        String expectedMessage = "Header Authorization was not found in the request";
        assertEquals(expectedMessage, exception.getMessage());
    }

}