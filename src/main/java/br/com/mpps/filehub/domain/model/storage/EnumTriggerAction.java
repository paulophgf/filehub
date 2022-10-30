package br.com.mpps.filehub.domain.model.storage;

import br.com.mpps.filehub.domain.exceptions.PropertiesReaderException;

public enum EnumTriggerAction {

    ALL,
    UPDATE;

    public static EnumTriggerAction get(String triggerAction) {
        EnumTriggerAction type;
        try {
            type = EnumTriggerAction.valueOf(triggerAction);
        } catch (IllegalArgumentException e) {
            throw new PropertiesReaderException("Trigger action " + triggerAction + " not exists");
        }
        return type;
    }

}
