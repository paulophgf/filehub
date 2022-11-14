package br.com.mpps.filehub.infrastructure.config.reader;

import br.com.mpps.filehub.domain.model.EnumConfigReaderType;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.infrastructure.config.XMLStorageReader;
import br.com.mpps.filehub.domain.interfaces.FileConfigReader;
import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Component
public class GitFileReader implements FileConfigReader {

    @Value("${filehub.config.git.file-url:NONE}") private String fileURL;
    @Value("${filehub.config.git.access-token:NONE}") private String accessToken;

    int CONNECTION_TIMEOUT = 5000;
    int REQUEST_TIMEOUT = 5000;
    int READ_TIMEOUT = 5000;


    @Override
    public EnumConfigReaderType getConfigReaderName() {
        return EnumConfigReaderType.GIT_FILE;
    }

    @Override
    public Map<String, Schema> readSchemasFromConfigurationFile() {
        checkGitReaderParameters();
        XMLStorageReader XMLStorageReader = new XMLStorageReader();
        String content = getConfigurationFileContent();
        return XMLStorageReader.read(content);
    }

    private void checkGitReaderParameters() {
        if(fileURL.equals("NONE") || accessToken.equals("NONE")) {
            throw new PropertiesReaderException("Parameters not informed to git file reader");
        }
    }

    private String getConfigurationFileContent() {
        String response;
        String url = fileURL;
        try {
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add("Authorization", "token " + accessToken);
            HttpEntity<String> request = new HttpEntity<>(null, headers);
            ResponseEntity<String> restResponse = createRestTemplate().exchange(url, HttpMethod.GET, request, String.class);
            response = restResponse.getBody();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            throw new PropertiesReaderException("Error to get file from github");
        }
        return response;
    }

    private RestTemplate createRestTemplate() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(null, null, null);

        CloseableHttpClient httpClient = HttpClientBuilder.create().setSSLContext(context).build();
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        clientHttpRequestFactory.setConnectTimeout(CONNECTION_TIMEOUT);
        clientHttpRequestFactory.setConnectionRequestTimeout(REQUEST_TIMEOUT);
        clientHttpRequestFactory.setReadTimeout(READ_TIMEOUT);
        return new RestTemplate(clientHttpRequestFactory);
    }

}
