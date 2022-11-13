package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.exceptions.TriggerAuthenticationException;
import br.com.mpps.filehub.domain.model.FileLocation;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Trigger;
import br.com.mpps.filehub.domain.model.storage.EnumTriggerAction;
import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;


@Service
public class TriggerAuthenticationService {

    private static Gson gson = new Gson();

    public FileLocation getFileLocation(HttpServletRequest request, Schema schema, String path, Boolean isRead) {
        FileLocation fileLocation = new FileLocation(path);
        if(schema.getTrigger() != null) {
            String headerValue = request.getHeader(schema.getTrigger().getHeader());
            if(schema.getTrigger().getAction().equals(EnumTriggerAction.ALL) || headerValue != null ||
                    (schema.getTrigger().getAction().equals(EnumTriggerAction.UPDATE) && !isRead)) {
                if (headerValue == null) {
                    throw new TriggerAuthenticationException("Header " + schema.getTrigger().getHeader() + " was not found in the request");
                }
                HashMap<String, String> parameters = getParameters(schema.getTrigger(), headerValue);
                fileLocation.updateAttributesByTriggerParameters(parameters);
            }
        }
        return fileLocation;
    }

    private HashMap<String, String> getParameters(Trigger trigger, String headerValue) {
        HttpClient client = HttpClient.create()
                        .headers(h -> h.set(trigger.getHeader(), headerValue));
        HttpClient.ResponseReceiver<?> responseReceiver = trigger.getHttpMethod().getResponseReceiver(client);

        Mono<HashMap> parameters = responseReceiver
                .uri(trigger.getUrl())
                .responseSingle(
                        (response, bytes) -> {
                            if(!HttpResponseStatus.OK.equals(response.status())) {
                                throw new TriggerAuthenticationException("Trigger request with status code " + response.status().code() + " was returned");
                            }
                            return bytes.asString().map(it -> {
                                HashMap<String, String> params = new HashMap<>();
                                if(it != null && !it.isEmpty()) {
                                    params = gson.fromJson(it, HashMap.class);
                                }
                                return params;
                            });
                        }
                );
        return parameters.block();
    }

}
