package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.exceptions.TriggerAuthenticationException;
import br.com.p8projects.filehub.domain.model.FileLocation;
import br.com.p8projects.filehub.domain.model.upload.UploadObject;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.config.Trigger;
import br.com.p8projects.filehub.domain.model.storage.EnumTriggerAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Set;


@Service
public class TriggerAuthenticationService {

    private TriggerCalling triggerCalling;


    @Autowired
    public TriggerAuthenticationService(TriggerCalling triggerCalling) {
        this.triggerCalling = triggerCalling;
    }


    public void checkUploadOperation(HttpServletRequest request, UploadObject uploadObject) {
        Trigger trigger = uploadObject.getSchema().getTrigger();
        if(trigger != null) {
            String headerName = trigger.getHeader();
            String headerValue = request.getHeader(headerName);
            if(uploadObject.isMkdir()) {
                uploadObject.setMkdir(trigger.isAllowDirOperations());
            }
            boolean isTriggerActionAll = trigger.getAction().equals(EnumTriggerAction.ALL);
            if(isTriggerActionAll || headerName != null) {
                if (headerValue == null) {
                    throw new TriggerAuthenticationException("Header " + headerName + " was not found in the request");
                }
                HashMap<String, String> parameters = triggerCalling.sendRequest(trigger, headerValue);
                if(!parameters.isEmpty()) {
                    String newPath = getPathWithReplacedParameters(uploadObject.getPath(), parameters);
                    uploadObject.setPath(newPath);
                    String filename = parameters.get("filename");
                    uploadObject.setFilename(filename);
                }
            }
        }
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
                if(!parameters.isEmpty()) {
                    String newPath = getPathWithReplacedParameters(path, parameters);
                    fileLocation.setPath(newPath);
                    String filename = parameters.get("filename");
                    fileLocation.setFilename(filename);
                }
            }
        }
        return fileLocation;
    }

    private String getPathWithReplacedParameters(String path, HashMap<String, String> parameters) {
        Set<String> parameterNames = parameters.keySet();
        for(String parameterName : parameterNames) {
            if(!"filename".equals(parameterName)) {
                if (path.lastIndexOf("$" + parameterName) == -1) {
                    throw new TriggerAuthenticationException("The parameter $" + parameterName + " was not found in the path");
                }
                String parameterValue = String.valueOf(parameters.get(parameterName));
                if (parameterValue.isEmpty()) {
                    throw new TriggerAuthenticationException("The trigger returned an empty value to parameter $" + parameterName);
                }
                path = path.replace("$" + parameterName, parameterValue);
            }
        }
        return path;
    }

}
