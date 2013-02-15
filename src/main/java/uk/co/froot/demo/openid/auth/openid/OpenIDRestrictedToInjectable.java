package uk.co.froot.demo.openid.auth.openid;


import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.froot.demo.openid.model.Authority;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * <p>Injectable to provide the following to {@link OpenIDRestrictedToProvider}:</p>
 * <ul>
 * <li>Performs decode from HTTP request</li>
 * <li>Carries OpenID authentication data</li>
 * </ul>
 *
 * @since 0.0.1
 */
class OpenIDRestrictedToInjectable<T> extends AbstractHttpContextInjectable<T> {

  private static final Logger log = LoggerFactory.getLogger(OpenIDRestrictedToInjectable.class);

  private final Authenticator<OpenIDCredentials, T> authenticator;
  private final String realm;
  private final Set<Authority> requiredAuthorities;

  /**
   * @param authenticator The Authenticator that will compare credentials
   * @param realm The authentication realm
   * @param requiredAuthorities The required authorities as provided by the RestrictedTo annotation
   */
  OpenIDRestrictedToInjectable(
    Authenticator<OpenIDCredentials, T> authenticator,
    String realm,
    Authority[] requiredAuthorities) {
    this.authenticator = authenticator;
    this.realm = realm;
    this.requiredAuthorities = Sets.newHashSet(Arrays.asList(requiredAuthorities));
  }

  public Authenticator<OpenIDCredentials, T> getAuthenticator() {
    return authenticator;
  }

  public String getRealm() {
    return realm;
  }

  public Set<Authority> getRequiredAuthorities() {
    return requiredAuthorities;
  }

  @Override
  public T getValue(HttpContext httpContext) {

    try {

      // Get the Authorization header
      final Map<String,Cookie> cookieMap = httpContext.getRequest().getCookies();
      if (!cookieMap.containsKey("JSESSIONID")) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      String sessionId = cookieMap.get("JSESSIONID").getValue();

      if (sessionId != null) {

        final OpenIDCredentials credentials = new OpenIDCredentials(sessionId, requiredAuthorities);

        final Optional<T> result = authenticator.authenticate(credentials);
        if (result.isPresent()) {
          return result.get();
        }
      }
    } catch (IllegalArgumentException e) {
      log.debug("Error decoding credentials",e);
    } catch (AuthenticationException e) {
      log.warn("Error authenticating credentials",e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }

    // Must have failed to be here
    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
  }

}

