package br.com.mpps.filehub.domain.model;

import br.com.mpps.filehub.domain.exceptions.TriggerAuthenticationException;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Set;

@Data
@NoArgsConstructor
public class FileLocation {

    private String path;
    private String filename;


    public FileLocation(String path) {
        this.path = path;
    }


    public FileLocation setFilename(String filename) {
        if(this.filename == null && (filename != null && !filename.isEmpty())) {
            this.filename = filename;
        }
        return this;
    }

    public void updateAttributesByTriggerParameters(HashMap<String, String> parameters) {
        if(!parameters.isEmpty()) {
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
            setFilename(parameters.get("filename"));
        }
    }

}
