package br.com.p8projects.filehub.infrastructure.config;

import br.com.p8projects.filehub.domain.interfaces.FileConfigReader;
import br.com.p8projects.filehub.domain.model.EnumConfigReaderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class PropertiesReaderFactory {

    private Map<EnumConfigReaderType, FileConfigReader> strategies;

    @Autowired
    public PropertiesReaderFactory(Set<FileConfigReader> strategySet) {
        createStrategy(strategySet);
    }

    public FileConfigReader findStrategy(EnumConfigReaderType strategyName) {
        return strategies.get(strategyName);
    }

    private void createStrategy(Set<FileConfigReader> strategySet) {
        strategies = new HashMap<>();
        strategySet.forEach(strategy -> strategies.put(strategy.getConfigReaderName(), strategy));
    }

}
