package br.com.mpps.filehub.system;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class SystemProperties {

    @Value("${server.port}") private String appPort;
    @Value("${system.version}") private String systemVersion;

}
