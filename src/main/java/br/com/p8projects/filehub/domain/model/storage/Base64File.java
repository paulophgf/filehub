package br.com.p8projects.filehub.domain.model.storage;

import br.com.p8projects.filehub.domain.exceptions.StorageException;
import lombok.Data;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

@Data
public class Base64File {

    private String filename;
    private String file;
    private String mimeType;
    private static final String ERROR_MESSAGE = "Invalid Base64 Format. The correct format is: data:{TYPE};base64,{BASE_64_CONTENT} (Example: data:image/jpeg;base64,/9j/4AAQSkZ... )";
    private static final MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();


    private Base64File(String filename, String file, String mimeType) {
        this.file = file;
        this.mimeType = mimeType;
        if(filename.contains(".")) {
            this.filename = filename;
        } else {
            this.filename = filename + getExtension();
        }
    }


    public static Base64File getInstance(String filename, String base64) {
        String[] mainPart = base64.split(",");
        if(mainPart.length != 2) {
            throw new StorageException(ERROR_MESSAGE);
        }
        String file = mainPart[1];
        String[] headerPart = mainPart[0].split(";");
        if(mainPart.length != 2) {
            throw new StorageException(ERROR_MESSAGE);
        }
        String[] typePart = headerPart[0].split(":");
        if(mainPart.length != 2) {
            throw new StorageException(ERROR_MESSAGE);
        }
        String type = typePart[1];
        return new Base64File(filename, file, type);
    }

    public String getExtension() {
        String extension;
        try {
            MimeType mime = allTypes.forName(mimeType);
            extension = mime.getExtension();
        } catch (MimeTypeException e) {
            extension = "";
        }
        return extension;
    }

}
