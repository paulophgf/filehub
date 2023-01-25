package br.com.p8projects.filehub.domain.usecase;

import br.com.p8projects.filehub.domain.model.FileItem;
import br.com.p8projects.filehub.domain.model.StorageSynchronize;
import br.com.p8projects.filehub.domain.model.TransferFileObject;
import br.com.p8projects.filehub.domain.model.config.FhStorage;
import br.com.p8projects.filehub.domain.model.config.Schema;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SynchronizationManager {

    @Async
    public void synchronize(StorageSynchronize storageSynchronize) {
        if(storageSynchronize.isFullSynch()) {
            synchronizeStorage(storageSynchronize.getSource(), storageSynchronize.getDestination(), "/");
            synchronizeStorage(storageSynchronize.getDestination(), storageSynchronize.getSource(), "/");
        } else {
            synchronizeStorage(storageSynchronize.getSource(), storageSynchronize.getDestination(), "/");
        }
    }

    @Async
    public void synchronize(Schema schema) {
        for(FhStorage source : schema.getStorages()) {
            for(FhStorage destination : schema.getStorages()) {
                synchronizeStorage(source, destination, "/");
            }
        }
    }


    private void synchronizeStorage(FhStorage source, FhStorage destination, String path) {
        if(!source.equals(destination)) {
            List<FileItem> items = source.listFiles(path);
            for (FileItem item : items) {
                String itemPath = item.getPath() + item.getName();
                if (item.getIsDirectory()) {
                    synchronizeStorage(source, destination, itemPath);
                } else if (!destination.existsFile(itemPath)) {
                    TransferFileObject transferFileObject = source.getTransferFileObject(item.getPath(), item.getName());
                    destination.writeFileInputStream(transferFileObject, true);
                }
            }
        }
    }

}
