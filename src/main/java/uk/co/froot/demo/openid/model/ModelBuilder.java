package uk.co.froot.demo.openid.model;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.froot.demo.openid.OpenIDDemoConfiguration;
import uk.co.froot.demo.openid.core.InMemoryUserCache;
import uk.co.froot.demo.openid.model.security.User;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import java.util.UUID;

/**
 * <p>Builder to provide the following to resources:</p>
 * <ul>
 * <li>Utility methods to build backing models for Views</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class ModelBuilder {

  private static final Logger log = LoggerFactory.getLogger(ModelBuilder.class);

  public Optional<UUID> extractSessionToken(HttpHeaders httpHeaders) {

    // Want to fail fast to absent() since this will get called a lot
    Optional<UUID> sessionToken = Optional.absent();

    if (httpHeaders == null) {
      // Might be an internal call such as an error condition
      return sessionToken;
    }
    if (httpHeaders.getCookies() == null) {
      // This is a cold user
      return sessionToken;
    }

    Cookie cookie = httpHeaders.getCookies().get(OpenIDDemoConfiguration.SESSION_TOKEN_NAME);
    if (cookie == null) {
      // This is a cold user
      return sessionToken;
    }
    String value = cookie.getValue();
    if (value == null) {
      // This is a broken cookie
      // Rather than throw an error we can force a fresh login to fix it up
      return sessionToken;
    }

    // Must be OK to be here
    return Optional.of(UUID.fromString(value));

  }

  /**
   * @return A new base model with user populated from the session token if present
   */
  public BaseModel newBaseModel(HttpHeaders httpHeaders) {

    BaseModel baseModel = new BaseModel();

    // Locate and populate the user by their session token (if present)
    addUser(httpHeaders, baseModel);

    return baseModel;

  }

  /**
   *
   * @param httpHeaders The HTTP headers containing the session token
   * @param baseModel A base model
   */
  public void addUser(HttpHeaders httpHeaders, BaseModel baseModel) {
    Optional<UUID> sessionToken = extractSessionToken(httpHeaders);
    if (sessionToken.isPresent()) {
      Optional<User> user = InMemoryUserCache.INSTANCE.getBySessionToken(sessionToken.get());
      if (user.isPresent()) {
        log.debug("Found a user to match the session token {}",sessionToken.get());
        baseModel.setUser(user.get());
      } else {
        log.debug("No user matching the session token {}",sessionToken.get());
      }
    }
  }

}
