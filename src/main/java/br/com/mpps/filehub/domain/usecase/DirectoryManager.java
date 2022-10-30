package br.com.mpps.filehub.domain.usecase;

import br.com.mpps.filehub.domain.model.FileItem;
import br.com.mpps.filehub.domain.model.config.Schema;
import br.com.mpps.filehub.domain.model.config.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class DirectoryManager {

    public boolean createDirectory(Schema schema, String path) {
        Boolean result = true;
        Collection<Storage> storages = schema.getStorages();
        for(Storage storage : storages) {
            result = result && storage.createDirectory(path);
        }
        return result;
    }

    public boolean deleteDirectory(Schema schema, String path, boolean isRecursive) {
        Boolean result = true;
        Collection<Storage> storages = schema.getStorages();
        for(Storage storage : storages) {
            result = result && storage.deleteDirectory(path, isRecursive);
        }
        return result;
    }

    public List<FileItem> listFiles(Schema schema, String path) {
        Collection<Storage> storages = schema.getStorages();
        Storage firstStorage = storages.stream().findFirst().get();
        return firstStorage.listFiles(path);
    }

}
