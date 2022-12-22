package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.model.FileItem;
import br.com.p8projects.filehub.domain.model.config.Schema;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class DirectoryManager {

    public boolean createDirectory(Schema schema, String path) {
        Boolean result = true;
        Collection<FhStorage> storages = schema.getStorages();
        for(FhStorage storage : storages) {
            result = result && storage.createDirectory(path);
        }
        return result;
    }

    public boolean renameDirectory(Schema schema, String path, String name) {
        Boolean result = true;
        Collection<FhStorage> storages = schema.getStorages();
        for(FhStorage storage : storages) {
            result = result && storage.renameDirectory(path, name);
        }
        return result;
    }

    public boolean deleteDirectory(Schema schema, String path, boolean isRecursive) {
        Boolean result = true;
        Collection<FhStorage> storages = schema.getStorages();
        for(FhStorage storage : storages) {
            result = result && storage.deleteDirectory(path, isRecursive);
        }
        return result;
    }

    public List<FileItem> listFiles(Schema schema, String path) {
        Collection<FhStorage> storages = schema.getStorages();
        FhStorage firstStorage = storages.stream().findFirst().get();
        return firstStorage.listFiles(path);
    }

    public boolean existsDirectory(Schema schema, String path) {
        Collection<FhStorage> storages = schema.getStorages();
        FhStorage firstStorage = storages.stream().findFirst().get();
        return firstStorage.existsDirectory(path);
    }

}
