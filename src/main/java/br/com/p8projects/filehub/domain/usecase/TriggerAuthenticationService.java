package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.exceptions.TriggerAuthenticationException;
import br.com.p8projects.filehub.domain.model.FileLocation;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.storage.EnumTriggerAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;


@Service
public class TriggerAuthenticationService {

    private TriggerCalling triggerCalling;


    @Autowired
    public TriggerAuthenticationService(TriggerCalling triggerCalling) {
        this.triggerCalling = triggerCalling;
    }


    public FileLocation getFileLocation(HttpServletRequest request, Schema schema, String path, Boolean isRead) {
        FileLocation fileLocation = new FileLocation(path);
        if(schema.getTrigger() != null) {
            String headerValue = request.getHeader(schema.getTrigger().getHeader());
            if(schema.getTrigger().getAction().equals(EnumTriggerAction.ALL) || headerValue != null ||
                    (schema.getTrigger().getAction().equals(EnumTriggerAction.UPDATE) && !isRead)) {
                if (headerValue == null) {
                    throw new TriggerAuthenticationException("Header " + schema.getTrigger().getHeader() + " was not found in the request");
                }
                HashMap<String, String> parameters = triggerCalling.sendRequest(schema.getTrigger(), headerValue);
                fileLocation.updateAttributesByTriggerParameters(parameters);
            }
        }
        return fileLocation;
    }

}
