package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.exceptions.TriggerAuthenticationException;
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
import java.util.Set;


@Service
public class TriggerAuthenticationService {

    private static Gson gson = new Gson();

    public String getPath(HttpServletRequest request, Schema schema, String path, Boolean isRead) {
        String newPath = path;
        if(schema.getTrigger() != null) {
            if(schema.getTrigger().getAction().equals(EnumTriggerAction.ALL) ||
                    (schema.getTrigger().getAction().equals(EnumTriggerAction.UPDATE) && !isRead)) {
                String headerValue = request.getHeader(schema.getTrigger().getHeader());
                if (headerValue == null) {
                    throw new TriggerAuthenticationException("Header " + schema.getTrigger().getHeader() + " was not found in the request");
                }
                HashMap<String, String> parameters = getParameters(schema.getTrigger(), headerValue);
                newPath = replacePathParameters(path, parameters);
            }
        }
        return newPath;
    }

    private HashMap<String, String> getParameters(Trigger trigger, String headerValue) {
        HttpClient client = HttpClient.create()
                        .headers(h -> h.set(trigger.getHeader(), headerValue));

        Mono<HashMap> parameters = client.get()
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

    private String replacePathParameters(String path, HashMap<String, String> parameters) {
        if(!parameters.isEmpty()) {
            Set<String> parameterNames = parameters.keySet();
            for(String parameterName : parameterNames) {
                if(path.lastIndexOf("$"+parameterName) == -1) {
                    throw new TriggerAuthenticationException("The parameter $" + parameterName + " was not found in the path");
                }
                if(parameters.get(parameterName).isEmpty()) {
                    throw new TriggerAuthenticationException("The trigger returned an empty value to parameter $" + parameterName);
                }
                path = path.replace("$"+parameterName, parameters.get(parameterName));
            }
        }
        return path;
    }

}
