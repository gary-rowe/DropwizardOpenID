package uk.co.froot.demo.openid.auth.openid;


import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.logging.Log;
import uk.co.froot.demo.openid.auth.annotation.RestrictedTo;

/**
 * <p>Authentication provider to provide the following to Jersey:</p>
 * <ul>
 * <li>Bridge between Dropwizard and Jersey for HMAC authentication</li>
 * <li>Provides additional {@link uk.co.froot.demo.openid.model.Authority} information</li>
 * </ul>
 *
 * @param <T>    the principal type.
 * @since 0.0.1
 */
public class OpenIDRestrictedToProvider<T> implements InjectableProvider<RestrictedTo, Parameter> {
  static final Log LOG = Log.forClass(OpenIDRestrictedToProvider.class);

  private final Authenticator<OpenIDCredentials, T> authenticator;
  private final String realm;

  /**
   * Creates a new {@link OpenIDRestrictedToProvider} with the given {@link com.yammer.dropwizard.auth.Authenticator} and realm.
   *
   * @param authenticator the authenticator which will take the {@link OpenIDCredentials} and
   *                      convert them into instances of {@code T}
   * @param realm         the name of the authentication realm
   */
  public OpenIDRestrictedToProvider(Authenticator<OpenIDCredentials, T> authenticator, String realm) {
    this.authenticator = authenticator;
    this.realm = realm;
  }

  @Override
  public ComponentScope getScope() {
    return ComponentScope.PerRequest;
  }

  @Override
  public Injectable<?> getInjectable(ComponentContext ic,
                                     RestrictedTo a,
                                     Parameter c) {
    return new OpenIDRestrictedToInjectable<T>(authenticator, realm, a.value());
  }
}

