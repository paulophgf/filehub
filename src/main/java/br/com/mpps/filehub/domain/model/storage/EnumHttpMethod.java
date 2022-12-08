package br.com.mpps.filehub.domain.model.storage;

import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;

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

}
