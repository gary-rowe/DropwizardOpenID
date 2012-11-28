package uk.co.froot.demo.openid.resources;

import com.yammer.dropwizard.jersey.caching.CacheControl;
import com.yammer.metrics.annotation.Timed;
import uk.co.froot.demo.openid.auth.annotation.RestrictedTo;
import uk.co.froot.demo.openid.model.Authority;
import uk.co.froot.demo.openid.model.BaseModel;
import uk.co.froot.demo.openid.model.User;
import uk.co.froot.demo.openid.views.PublicFreemarkerView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * <p>Resource to provide the following to application:</p>
 * <ul>
 * <li>Provision of configuration for public home page</li>
 * </ul>
 *
 * @since 0.0.1
 */
@Path("/private")
@Produces(MediaType.TEXT_HTML)
public class PrivateInfoResource extends BaseResource {

  /**
   * @return The private home view if authenticated
   */
  @GET
  @Path("/home")
  @Timed
  @CacheControl(noCache = true)
  public PublicFreemarkerView viewHome(
    @RestrictedTo(Authority.ROLE_PUBLIC)
    User publicUser
  ) {

    BaseModel model = newBaseModel();
    return new PublicFreemarkerView<BaseModel>("private/home.ftl", model);

  }

  /**
   * @return The private admin view if authenticated
   */
  @GET
  @Path("/admin")
  @Timed
  @CacheControl(noCache = true)
  public PublicFreemarkerView viewAdmin(
    @RestrictedTo(Authority.ROLE_ADMIN)
    User adminUser
  ) {

    BaseModel model = newBaseModel();
    return new PublicFreemarkerView<BaseModel>("private/admin.ftl", model);

  }

}
