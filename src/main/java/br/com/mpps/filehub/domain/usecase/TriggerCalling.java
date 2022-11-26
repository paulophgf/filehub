package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.exceptions.TriggerAuthenticationException;
import br.com.mpps.filehub.domain.model.config.Trigger;
import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.HashMap;

@Service
public class TriggerCalling {

    private static Gson gson = new Gson();

    public HashMap<String, String> sendRequest(Trigger trigger, String headerValue) {
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
