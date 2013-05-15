package uk.co.froot.demo.openid.auth.openid;

import com.google.common.base.Objects;
import uk.co.froot.demo.openid.model.security.Authority;

import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Value object to provide the following to {@link OpenIDAuthenticator}:</p>
 * <ul>
 * <li>Storage of the necessary credentials for OpenID authentication</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class OpenIDCredentials {

  private final UUID sessionToken;
  private final Set<Authority> requiredAuthorities;

  /**
   * @param sessionToken        The session token acting as a surrogate for the OpenID token
   * @param requiredAuthorities The authorities required to authenticate (provided by the {@link uk.co.froot.demo.openid.auth.annotation.RestrictedTo} annotation)
   */
  public OpenIDCredentials(
    UUID sessionToken,
    Set<Authority> requiredAuthorities) {
    this.sessionToken = checkNotNull(sessionToken);
    this.requiredAuthorities = checkNotNull(requiredAuthorities);
  }

  /**
   * @return The OpenID token
   */
  public UUID getSessionToken() {
    return sessionToken;
  }

  /**
   * @return The authorities required to successfully authenticate
   */
  public Set<Authority> getRequiredAuthorities() {
    return requiredAuthorities;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    final OpenIDCredentials that = (OpenIDCredentials) obj;

    return sessionToken.equals(that.sessionToken);
  }

  @Override
  public int hashCode() {
    return (31 * sessionToken.hashCode());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("sessionId", sessionToken)
      .add("authorities", requiredAuthorities)
      .toString();
  }

}
