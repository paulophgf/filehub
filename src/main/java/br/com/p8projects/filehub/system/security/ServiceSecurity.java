//package br.com.mpps.filehub.system.security;
//
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//
//import java.util.Collections;
//
//public class ServiceSecurity extends User {
//
//    public ServiceSecurity(String serviceType) {
//        super(serviceType, "BasicPasswordService", true, true, true, true, Collections.singleton(new SimpleGrantedAuthority(serviceType.toUpperCase())));
//    }
//
//}
