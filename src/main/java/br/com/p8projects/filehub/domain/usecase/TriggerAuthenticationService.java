package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.exceptions.TriggerAuthenticationException;
import br.com.p8projects.filehub.domain.model.EnumFileHubOperation;
import br.com.p8projects.filehub.domain.model.FileLocation;
import br.com.p8projects.filehub.domain.model.TriggerRequestBody;
import br.com.p8projects.filehub.domain.model.config.Trigger;
import br.com.p8projects.filehub.domain.model.storage.EnumTriggerAction;
import br.com.p8projects.filehub.domain.model.upload.UploadObject;
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


    public void checkUploadOperation(HttpServletRequest request, UploadObject uploadObject, EnumFileHubOperation fileHubOperation) {
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
                TriggerRequestBody triggerRequestBody = new TriggerRequestBody(uploadObject, fileHubOperation);
                HashMap<String, String> parameters = triggerCalling.sendRequest(trigger, headerValue, triggerRequestBody);
                if(!parameters.isEmpty()) {
                    String newPath = getPathWithReplacedParameters(uploadObject.getPath(), parameters);
                    uploadObject.setPath(newPath);
                    String filename = parameters.get("filename");
                    uploadObject.setFilename(filename);
                }
            }
        }
    }

    public void checkFileLocation(HttpServletRequest request, FileLocation fileLocation, EnumFileHubOperation fileHubOperation) {
        Trigger trigger = fileLocation.getSchema().getTrigger();
        if(trigger != null) {
            String headerValue = request.getHeader(trigger.getHeader());
            if(trigger.getAction().equals(EnumTriggerAction.ALL) || headerValue != null ||
                    (trigger.getAction().equals(EnumTriggerAction.UPDATE) && !fileHubOperation.isReadOperation())) {
                if (headerValue == null) {
                    throw new TriggerAuthenticationException("Header " + trigger.getHeader() + " was not found in the request");
                }
                TriggerRequestBody triggerRequestBody = new TriggerRequestBody(fileLocation, fileHubOperation);
                HashMap<String, String> parameters = triggerCalling.sendRequest(trigger, headerValue, triggerRequestBody);
                if(!parameters.isEmpty()) {
                    String newPath = getPathWithReplacedParameters(fileLocation.getPath(), parameters);
                    fileLocation.setPath(newPath);
                    String filename = parameters.get("filename");
                    fileLocation.setFilename(filename);
                }
            }
        }
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
