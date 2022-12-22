package br.com.p8projects.filehub.domain.model.storage;

import br.com.p8projects.filehub.domain.exceptions.PropertiesReaderException;
import br.com.p8projects.filehub.domain.model.OptionalProperty;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public interface StorageProperties extends Cloneable {

    EnumStorageType getType();

    void afterReadProperties(String storageName);

    static void checkRequiredProperties(StorageProperties object, String storageName, String group, String... excluded) {
        try {
            Field[] fields = object.getClass().getDeclaredFields();
            List<String> excludedFields = Arrays.asList(excluded);
            for (Field field : fields) {
                if (field.isAnnotationPresent(OptionalProperty.class) && !excludedFields.contains(field.getName())) {
                    OptionalProperty optionalProperty = field.getDeclaredAnnotation(OptionalProperty.class);
                    if (optionalProperty.group().equals(group)) {
                        field.setAccessible(true);
                        Object value = field.get(object);
                        if (value == null || value.toString().isEmpty()) {
                            throw new PropertiesReaderException("The property " + field.getName() + " not found in the storage " + storageName);
                        }
                        field.setAccessible(false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new PropertiesReaderException("Error to check properties from the storage " + storageName);
        }
    }

}
