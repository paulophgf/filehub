package br.com.mpps.filehub.domain.model.storage;

import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;
import reactor.netty.http.client.HttpClient;

public enum EnumHttpMethod {

    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS;

    public static EnumHttpMethod get(String httpMethodAttribute) {
        EnumHttpMethod httpMethod;
        try {
            httpMethod = EnumHttpMethod.valueOf(httpMethodAttribute);
        } catch (IllegalArgumentException e) {
            throw new PropertiesReaderException("Trigger HTTP method " + httpMethodAttribute + " not exists");
        }
        return httpMethod;
    }

    public HttpClient.ResponseReceiver<?> getResponseReceiver(HttpClient client) {
        HttpClient.ResponseReceiver<?> responseReceiver;
        switch (this) {
            case GET: responseReceiver = client.get(); break;
            case HEAD: responseReceiver = client.head(); break;
            case POST: responseReceiver = client.post(); break;
            case PUT: responseReceiver = client.put(); break;
            case PATCH: responseReceiver = client.patch(); break;
            case DELETE: responseReceiver = client.delete(); break;
            case OPTIONS: responseReceiver = client.options(); break;
            default: throw new RuntimeException("Error to find the response receiver");
        }
        return responseReceiver;
    }

}
