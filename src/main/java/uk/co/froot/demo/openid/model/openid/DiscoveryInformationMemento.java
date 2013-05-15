package uk.co.froot.demo.openid.model.openid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * <p>Memento to provide the following to OpenID authentication web handling:</p>
 * <ul>
 * <li>Persistence store of the discovery information</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public class DiscoveryInformationMemento {

  /**
   * The OP endpoint URL.
   */
  @JsonProperty
  String opEndpoint;

  /**
   * The claimed identifier, i.e. the user's identity key.
   */
  @JsonProperty
  String claimedIdentifier;

  /**
   * The delegate, or OP-Local identifier.
   * The key through which the OP remembers the user's account.
   */
  @JsonProperty
  String delegate;

  /**
   * The OpenID protocol version, or target service type discovered through Yadis.
   */
  @JsonProperty
  String version;

  /**
   * All service types discovered for the endpoint.
   */
  @JsonProperty
  Set<String> types = Sets.newLinkedHashSet();

  public String getOpEndpoint() {
    return opEndpoint;
  }

  public void setOpEndpoint(String opEndpoint) {
    this.opEndpoint = opEndpoint;
  }

  public String getClaimedIdentifier() {
    return claimedIdentifier;
  }

  public void setClaimedIdentifier(String claimedIdentifier) {
    this.claimedIdentifier = claimedIdentifier;
  }

  public String getDelegate() {
    return delegate;
  }

  public void setDelegate(String delegate) {
    this.delegate = delegate;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Set<String> getTypes() {
    return types;
  }

  public void setTypes(Set<String> types) {
    this.types = types;
  }
}
