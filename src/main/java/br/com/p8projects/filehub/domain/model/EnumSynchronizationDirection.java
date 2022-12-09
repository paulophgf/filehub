package br.com.p8projects.filehub.domain.model;

import br.com.p8projects.filehub.domain.exceptions.StorageException;

public enum EnumSynchronizationDirection {

    BOTH,
    TO_RIGHT,
    TO_LEFT;

    public static EnumSynchronizationDirection getFromString(String direction) {
        EnumSynchronizationDirection value;
        try {
            value = EnumSynchronizationDirection.valueOf(direction);
        } catch (IllegalArgumentException e) {
            throw new StorageException("Invalid value to direction parameter");
        }
        return value;
    }

}
