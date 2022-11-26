package br.com.mpps.filehub.domain.model.config;

import br.com.mpps.filehub.domain.model.storage.EnumHttpMethod;
import br.com.mpps.filehub.domain.model.storage.EnumTriggerAction;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Trigger {

    private String id;
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
        return allowDirOperations == trigger.allowDirOperations && Objects.equals(id, trigger.id) && Objects.equals(url, trigger.url) && Objects.equals(header, trigger.header) && httpMethod == trigger.httpMethod && action == trigger.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, header, httpMethod, action, allowDirOperations);
    }

}
