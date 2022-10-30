package br.com.mpps.filehub.system.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Wifeed - Paulo Henrique Guimarães Fidencio
 * Filtro responsável por verificar se o token existente na requisição é válido,
 * além de adicionar o usuário responsável pelo token no contexto de autenticação do Spring
 */
public class SecurityFilter extends OncePerRequestFilter {

    private Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    public static final String HEADER_SERVICE_TOKEN = "Service-Token";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            String serviceToken = request.getHeader(HEADER_SERVICE_TOKEN);
            if(serviceToken != null) {
                UserDetails userDetails = new ServiceSecurity(serviceToken);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.warn("Unauthorized request");
            throw new RuntimeException("Unauthorized");
        }
        chain.doFilter(request, response);
    }

}
