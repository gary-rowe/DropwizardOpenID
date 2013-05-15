package uk.co.froot.demo.openid.model.openid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Memento to provide the following to OAuth authorization:</p>
 * <ul>
 * <li>Storage of token state</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public class TokenMemento {

  private final String token;
  private final String secret;
  private final String rawResponse;

  @JsonCreator
  public TokenMemento(
    @JsonProperty("token") String token,
    @JsonProperty("secret") String secret,
    @JsonProperty("rawResponse") String rawResponse) {
    this.token = token;
    this.secret = secret;
    this.rawResponse = rawResponse;
  }

  public String getToken() {
    return token;
  }

  public String getSecret() {
    return secret;
  }

  public String getRawResponse() {
    return rawResponse;
  }
}
