package br.com.p8projects.filehub.domain.model.upload;

import br.com.p8projects.filehub.domain.model.config.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;

@Data
public class UploadMultipartObject extends UploadObject {
    private List<FileUploadObject> files;

    public UploadMultipartObject(Schema schema, String path, MultipartFile[] files, String[] filenames, boolean mkdir) {
        this.schema = schema;
        this.path = path;
        this.files = new LinkedList<>();
        for(MultipartFile file : files) {
            this.files.add(new FileUploadObject(file.getOriginalFilename(), file));
        }
        if(filenames != null) {
            for(int i=0; i<filenames.length; i++) {
                if(!filenames[i].isEmpty()) {
                    this.files.get(i).setFilename(filenames[i]);
                }
            }
        }
        this.mkdir = mkdir;
    }

    @Override
    public void setFilename(String filename) {
        if(files.size() == 1 && filename != null && !filename.isEmpty()) {
            this.files.get(0).setFilename(filename);
        }
    }

    @Override
    public List<String> listFilenames() {
        return files.stream().map(FileUploadObject::getFilename).toList();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class FileUploadObject {
        private String filename;
        private MultipartFile file;
    }

}
