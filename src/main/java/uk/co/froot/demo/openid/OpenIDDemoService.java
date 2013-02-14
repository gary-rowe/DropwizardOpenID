package uk.co.froot.demo.openid;

import com.google.common.cache.CacheBuilderSpec;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.bundles.AssetsBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;
import com.yammer.dropwizard.views.ViewMessageBodyWriter;
import org.eclipse.jetty.server.session.SessionHandler;
import uk.co.froot.demo.openid.auth.openid.OpenIDAuthenticator;
import uk.co.froot.demo.openid.auth.openid.OpenIDRestrictedToProvider;
import uk.co.froot.demo.openid.model.User;
import uk.co.froot.demo.openid.resources.PublicHomeResource;

/**
 * <p>Service to provide the following to application:</p>
 * <ul>
 * <li>Provision of access to resources</li>
 * </ul>
 * <p>Use <code>java -jar mbm-develop-SNAPSHOT.jar server openid-demo.yml</code> to start the demo</p>
 *
 * @since 0.0.1
 *         
 */
public class OpenIDDemoService extends Service<OpenIDDemoConfiguration> {

  /**
   * Main entry point to the application
   *
   * @param args CLI arguments
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    new OpenIDDemoService().run(args);
  }

  private OpenIDDemoService() {
    super("store");
    CacheBuilderSpec cacheBuilderSpec = (System.getenv("FILE_CACHE_ENABLED") == null) ? CacheBuilderSpec.parse("maximumSize=0") : AssetsBundle.DEFAULT_CACHE_SPEC;
    addBundle(new AssetsBundle("/assets/images", cacheBuilderSpec, "/images"));
    addBundle(new AssetsBundle("/assets/jquery", cacheBuilderSpec, "/jquery"));

  }

  @Override
  protected void initialize(OpenIDDemoConfiguration configuration,
                            Environment environment) {

    // Configure authenticator
    OpenIDAuthenticator authenticator = new OpenIDAuthenticator();

    // Configure environment
    environment.scanPackagesForResourcesAndProviders(PublicHomeResource.class);

    // Health checks
    environment.addHealthCheck(new uk.co.froot.demo.openid.health.OpenIdDemoHealthCheck());

    // Providers
    environment.addProvider(new ViewMessageBodyWriter());
    environment.addProvider(new OpenIDRestrictedToProvider<User>(authenticator, "OpenID"));

    // Session handler
    environment.setSessionHandler(new SessionHandler());

    // Bundles
    addBundle(new ViewBundle());

  }

}
