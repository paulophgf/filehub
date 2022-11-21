package br.com.mpps.filehub.domain.model.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageResource {

    private Map<String, Storage> storages;
    private Map<String, Trigger> triggers;
    private Map<String, Schema> schemas;

}
