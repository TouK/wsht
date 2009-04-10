package pl.touk.humantask.ws;

/**
 * Temporary fix to remove touk-security dependency which includes dependency to Spring.
 * @author Witek Wołejszo
 */
public interface UserDetails {
    
    String getUsername();
    
}
