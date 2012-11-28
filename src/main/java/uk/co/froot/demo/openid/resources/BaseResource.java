package uk.co.froot.demo.openid.resources;

import org.openid4java.consumer.ConsumerManager;
import org.openid4java.util.HttpClientFactory;
import org.openid4java.util.ProxyProperties;
import uk.co.froot.demo.openid.model.BaseModel;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.util.Locale;

/**
 * <p>Abstract base class to provide the following to subclasses:</p>
 * <ul>
 * <li>Provision of common methods</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public abstract class BaseResource {

  protected static final String OPENID_IDENTIFIER_KEY = "openid-identifier-key";

  /**
   * Jersey creates a fresh resource every request so this is safe
   */
  @Context
  protected UriInfo uriInfo;

  /**
   * Jersey creates a fresh resource every request so this is safe
   */
  @Context
  protected HttpHeaders httpHeaders;

  /**
   * Jersey creates a fresh resource every request so this is safe
   */
  @Context
  protected HttpServletRequest request;

  public BaseResource() {

  }

  /**
   * @return The most appropriate locale for the upstream request (never null)
   */
  public Locale getLocale() {
    // TODO This should be a configuration setting
    Locale defaultLocale = Locale.UK;

    Locale locale;
    if (httpHeaders == null) {
      locale = defaultLocale;
    } else {
      locale = httpHeaders.getLanguage();
      if (locale == null) {
        locale = defaultLocale;
      }
    }
    return locale;
  }

  /**
   * Utility method to create a base model present on all non-authenticated resources
   *
   * @return A base model
   */
  protected BaseModel newBaseModel() {

    // Populate the model

    return new BaseModel();
  }

}
