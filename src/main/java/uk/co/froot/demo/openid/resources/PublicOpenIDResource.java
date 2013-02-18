package uk.co.froot.demo.openid.resources;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.yammer.dropwizard.views.View;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.froot.demo.openid.auth.InMemoryUserCache;
import uk.co.froot.demo.openid.model.security.Authority;
import uk.co.froot.demo.openid.model.BaseModel;
import uk.co.froot.demo.openid.model.security.User;
import uk.co.froot.demo.openid.views.PublicFreemarkerView;

import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * <p>Resource to provide the following to application:</p>
 * <ul>
 * <li>Provision of configuration for public home page</li>
 * </ul>
 *
 * @since 0.0.1
 */
@Path("/openid")
@Produces(MediaType.TEXT_HTML)
public class PublicOpenIDResource extends BaseResource {

  private static final Logger log = LoggerFactory.getLogger(PublicOpenIDResource.class);

  private static final String OPENID_DISCOVERY_KEY = "openid-discovery-key";
  private final static String YAHOO_ENDPOINT = "https://me.yahoo.com";
  private final static String GOOGLE_ENDPOINT = "https://www.google.com/accounts/o8/id";

  public final ConsumerManager manager;


  public PublicOpenIDResource() {

    // Proxy configuration must come before ConsumerManager construction
//    ProxyProperties proxyProps = new ProxyProperties();
//    proxyProps.setProxyHostName("some-proxy");
//    proxyProps.setProxyPort(8080);
//    HttpClientFactory.setProxyProperties(proxyProps);

    this.manager = new ConsumerManager();

  }

  /**
   * @return A login view
   */
  @GET
  public View login() {

    BaseModel model = new BaseModel();
    return new PublicFreemarkerView<BaseModel>("openid/login.ftl", model);
  }

  /**
   * Handles the authentication request from the user after they select their OpenId server
   *
   * @param identifier The identifier for the OpenId server
   * @return A redirection or a form view containing user-specific permissions
   */
  @POST
  public Response authenticationRequest(
    @FormParam("identifier")
    String identifier
  ) {

    Preconditions.checkNotNull(identifier, "No OpenID identifier submitted");

    try {

      // The OpenId server will use this endpoint to provide authentication
      // Parts of this may be shown to the user
      final String returnToUrl;
      if (request.getServerPort() == 80) {
        returnToUrl = String.format(
          "http://%s/openid/verify",
          request.getServerName());
      } else {
        returnToUrl = String.format(
          "http://%s:%d/openid/verify",
          request.getServerName(),
          request.getServerPort());
      }

      // Perform discovery on the user-supplied identifier
      List discoveries = manager.discover(identifier);

      // Attempt to associate with the OpenID provider
      // and retrieve one service endpoint for authentication
      DiscoveryInformation discovered = manager.associate(discoveries);

      // Store the discovery information in the user's session (creating a new one if required)
      HttpSession session = request.getSession();
      session.setAttribute(OPENID_DISCOVERY_KEY, discovered);

      // Build the AuthRequest message to be sent to the OpenID provider
      AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

      // Build the FetchRequest containing the information to be copied
      // from the OpenID provider
      FetchRequest fetch = FetchRequest.createFetchRequest();
      if (identifier.startsWith(GOOGLE_ENDPOINT)) {
        fetch.addAttribute("email",
          "http://axschema.org/contact/email", true);
        fetch.addAttribute("firstName",
          "http://axschema.org/namePerson/first", true);
        fetch.addAttribute("lastName",
          "http://axschema.org/namePerson/last", true);
      } else if (identifier.startsWith(YAHOO_ENDPOINT)) {
        fetch.addAttribute("email",
          "http://axschema.org/contact/email", true);
        fetch.addAttribute("fullname",
          "http://axschema.org/namePerson", true);
      } else { // works for myOpenID
        fetch.addAttribute("fullname",
          "http://schema.openid.net/namePerson", true);
        fetch.addAttribute("email",
          "http://schema.openid.net/contact/email", true);
      }

      // Attach the extension to the authentication request
      authReq.addExtension(fetch);

      // Redirect the user to their OpenId server authentication process
      return Response.seeOther(URI.create(authReq.getDestinationUrl(true))).build();

    } catch (MessageException e1) {
      e1.printStackTrace();
    } catch (DiscoveryException e1) {
      e1.printStackTrace();
    } catch (ConsumerException e1) {
      e1.printStackTrace();
    }
    return Response.ok().build();
  }

  /**
   * Handles the OpenId server response to the earlier AuthRequest
   *
   * @return The OpenId identifier for this user if verification was successful
   */
  @GET
  @Path("/verify")
  public View verifyOpenIdServerResponse() {

    BaseModel model = new BaseModel();

    try {
      // Retrieve the previously stored discovery information
      DiscoveryInformation discovered = (DiscoveryInformation) request
        .getSession(false)
        .getAttribute(OPENID_DISCOVERY_KEY);

      // Fail fast
      if (discovered == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      // Extract the receiving URL from the HTTP request
      StringBuffer receivingURL = request.getRequestURL();
      String queryString = request.getQueryString();
      if (queryString != null && queryString.length() > 0) {
        receivingURL.append("?").append(request.getQueryString());
      }

      // Extract the parameters from the authentication response
      // (which comes in as a HTTP request from the OpenID provider)
      ParameterList parameterList = new ParameterList(request.getParameterMap());

      // Verify the response
      // ConsumerManager needs to be the same (static) instance used
      // to place the authentication request
      // This could be tricky if this service is load-balanced
      VerificationResult verification = manager.verify(
        receivingURL.toString(), parameterList, discovered);

      // Examine the verification result and extract the verified identifier
      Optional<Identifier> verified = Optional.fromNullable(verification.getVerifiedId());
      if (verified.isPresent()) {
        // Verified
        AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

        // Put the result into the user cache
        User user = new User();
        user.setOpenIDIdentifier(verified.get().getIdentifier());

        // Extract additional information
        if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
          user.setEmailAddress(extractEmailAddress(authSuccess));
          user.setFirstName(extractFirstName(authSuccess));
          user.setLastName(extractLastName(authSuccess));
        }
        log.info("Extracted a {}",user);

        // Bind the authorities to the user
        Set<Authority> authorities = Sets.newHashSet();

        // Promote to admin if they have a specific email address
        // (not a good way, but this is only a demo)
        if ("nobody@example.org".equals(user.getEmailAddress())) {
          authorities.add(Authority.ROLE_ADMIN);
          log.info("Granted admin rights");
        }

        authorities.add(Authority.ROLE_PUBLIC);
        user.setAuthorities(authorities);

        // This user may be returning through a verified cookie on a new session
        HttpSession session = request.getSession();

        // Use a central store for Users (keeps the session light)
        InMemoryUserCache.INSTANCE.put(session.getId(), user);

        return new PublicFreemarkerView<BaseModel>("common/home.ftl", model);
      }
    } catch (OpenIDException e) {
      // present error to the user
      e.printStackTrace();
    }

    // Must have failed to be here
    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
  }

  private String extractEmailAddress(AuthSuccess authSuccess) throws MessageException {
    FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
    return getAttributeValue(
      fetchResp,
      "email",
      "",
      String.class);
  }

  private String extractFirstName(AuthSuccess authSuccess) throws MessageException {
    FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
    return getAttributeValue(
      fetchResp,
      "firstname",
      "",
      String.class);
  }

  private String extractLastName(AuthSuccess authSuccess) throws MessageException {
    FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
    return getAttributeValue(
      fetchResp,
      "lastname",
      "",
      String.class);
  }

  @SuppressWarnings("unchecked")
  private <T> T getAttributeValue(FetchResponse fetchResponse, String attribute, T defaultValue, Class<T> clazz) {
    List list = fetchResponse.getAttributeValues(attribute);
    if (list != null && !list.isEmpty()) {
      return (T) list.get(0);
    }

    return defaultValue;

  }

}
