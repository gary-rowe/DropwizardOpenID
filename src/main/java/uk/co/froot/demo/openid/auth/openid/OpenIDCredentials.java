package uk.co.froot.demo.openid.auth.openid;

import com.google.common.base.Objects;
import uk.co.froot.demo.openid.model.Authority;

import java.util.Set;

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

  private final String sessionId;
  private final Set<Authority> requiredAuthorities;

  /**
   * @param sessionId           The session ID acting as a surrogate for the OpenID token
   * @param requiredAuthorities The authorities required to authenticate (provided by the {@link uk.co.froot.demo.openid.auth.annotation.RestrictedTo} annotation)
   */
  public OpenIDCredentials(
    String sessionId,
    Set<Authority> requiredAuthorities) {
    this.sessionId = checkNotNull(sessionId);
    this.requiredAuthorities = checkNotNull(requiredAuthorities);
  }

  /**
   * @return The OpenID token
   */
  public String getSessionId() {
    return sessionId;
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

    return sessionId.equals(that.sessionId);
  }

  @Override
  public int hashCode() {
    return (31 * sessionId.hashCode());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("sessionId", sessionId)
      .add("authorities", requiredAuthorities)
      .toString();
  }

}
