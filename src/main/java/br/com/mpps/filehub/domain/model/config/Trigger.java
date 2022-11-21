package br.com.mpps.filehub.domain.model.config;

import br.com.mpps.filehub.domain.model.storage.EnumHttpMethod;
import br.com.mpps.filehub.domain.model.storage.EnumTriggerAction;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Trigger {

    private String url;
    private String header;
    private EnumHttpMethod httpMethod;
    private EnumTriggerAction action;
    private boolean allowDirOperations;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trigger trigger = (Trigger) o;
        return url.equals(trigger.url) && header.equals(trigger.header) && httpMethod == trigger.httpMethod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, header, httpMethod);
    }

}
