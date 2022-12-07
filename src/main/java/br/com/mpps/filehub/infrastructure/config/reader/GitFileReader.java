package br.com.mpps.filehub.infrastructure.config.reader;

import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import br.com.mpps.filehub.domain.interfaces.FileConfigReader;
import br.com.mpps.filehub.domain.model.EnumConfigReaderType;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.infrastructure.config.XMLStorageReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
public class GitFileReader implements FileConfigReader {

    @Value("${filehub.config.git.file-url:NONE}") private String fileURL;
    @Value("${filehub.config.git.access-token:NONE}") private String accessToken;

    int REQUEST_TIMEOUT_SECONDS = 10;


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
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(fileURL))
                    .timeout(Duration.of(REQUEST_TIMEOUT_SECONDS, ChronoUnit.SECONDS))
                    .header("Authorization", "token " + accessToken)
                    .GET().build();

            HttpResponse<String> responseObject = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            response = responseObject.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            throw new PropertiesReaderException("Error to get file from github");
        }
        return response;
    }

}
