package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import br.com.mpps.filehub.domain.exceptions.TriggerAuthenticationException;
import br.com.mpps.filehub.domain.model.config.Trigger;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

@Service
public class TriggerCalling {

    private static Gson gson = new Gson();
    private static final int REQUEST_TIMEOUT_SECONDS = 5;

    public HashMap<String, String> sendRequest(Trigger trigger, String headerValue) {
        HashMap<String, String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(trigger.getUrl()))
                    .timeout(Duration.of(REQUEST_TIMEOUT_SECONDS, ChronoUnit.SECONDS))
                    .header(trigger.getHeader(), headerValue).method(trigger.getHttpMethod().name(), HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> responseObject = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if(responseObject.statusCode() != 200) {
                throw new TriggerAuthenticationException("Trigger request with status code " + responseObject.statusCode() + " was returned");
            }
            response = new HashMap<>();
            if(responseObject.body() != null) {
                response = gson.fromJson(responseObject.body(), HashMap.class);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            throw new PropertiesReaderException("Error to get file from github");
        }
        return response;
    }

}
