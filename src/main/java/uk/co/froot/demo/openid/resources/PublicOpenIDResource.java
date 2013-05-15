package uk.co.froot.demo.openid.resources;

import com.google.common.base.Optional;
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
import uk.co.froot.demo.openid.OpenIDDemoConfiguration;
import uk.co.froot.demo.openid.core.InMemoryOpenIDCache;
import uk.co.froot.demo.openid.core.InMemoryUserCache;
import uk.co.froot.demo.openid.model.BaseModel;
import uk.co.froot.demo.openid.model.ModelBuilder;
import uk.co.froot.demo.openid.model.openid.DiscoveryInformationMemento;
import uk.co.froot.demo.openid.model.security.Authority;
import uk.co.froot.demo.openid.model.security.User;
import uk.co.froot.demo.openid.views.PublicFreemarkerView;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

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

  private final static String YAHOO_ENDPOINT = "https://me.yahoo.com";
  private final static String GOOGLE_ENDPOINT = "https://www.google.com/accounts/o8/id";

  private final ModelBuilder modelBuilder = new ModelBuilder();

  /**
   * Default constructor
   */
  public PublicOpenIDResource() {

    // TODO Externalise this through the configuration
    // Proxy configuration must come before ConsumerManager construction
//    ProxyProperties proxyProps = new ProxyProperties();
//    proxyProps.setProxyHostName("some-proxy");
//    proxyProps.setProxyPort(8080);
//    HttpClientFactory.setProxyProperties(proxyProps);



  }

  /**
   * @return A login view with a session token
   */
  @GET
  @Path("/login")
  public View login() {

    return new PublicFreemarkerView<BaseModel>("openid/login.ftl", modelBuilder.newBaseModel(httpHeaders));
  }

  /**
   * @return A login view with a session token
   */
  @GET
  @Path("/logout")
  public Response logout() {

    BaseModel model = modelBuilder.newBaseModel(httpHeaders);
    User user = model.getUser();
    if (user != null) {
      // Invalidate the session token
      user.setSessionToken(null);
      // (We'll delete the user but really this would just be an update)
      InMemoryUserCache.INSTANCE.hardDelete(user);
      model.setUser(null);
    }

    View view = new PublicFreemarkerView<BaseModel>("openid/logout.ftl", model);

    // Remove the session token which will have the effect of logout
    return Response
      .ok()
      .cookie(replaceSessionTokenCookie(Optional.<User>absent()))
      .entity(view)
      .build();

  }

  /**
   * Handles the authentication request from the user after they select their OpenId server
   *
   * @param identifier The identifier for the OpenId server
   *
   * @return A redirection or a form view containing user-specific permissions
   */
  @POST
  @SuppressWarnings("unchecked")
  public Response authenticationRequest(
    @Context
    HttpServletRequest request,
    @FormParam("identifier")
    String identifier
  ) {

    UUID sessionToken = UUID.randomUUID();

    try {

      // The OpenId server will use this endpoint to provide authentication
      // Parts of this may be shown to the user
      final String returnToUrl;
      if (request.getServerPort() == 80) {
        returnToUrl = String.format(
          "http://%s/openid/verify?token=%s",
          request.getServerName(),
          sessionToken);
      } else {
        returnToUrl = String.format(
          "http://%s:%d/openid/verify?token=%s",
          request.getServerName(),
          request.getServerPort(),
          sessionToken);
      }

      log.debug("Return to URL '{}'", returnToUrl);

      // Create a consumer manager for this specific request and cache it
      // (this is to preserve session state such as nonce values etc)
      ConsumerManager consumerManager = new ConsumerManager();
      InMemoryOpenIDCache.INSTANCE.putConsumerManager(sessionToken, consumerManager);

      // Perform discovery on the user-supplied identifier
      List discoveries = consumerManager.discover(identifier);

      // Attempt to associate with the OpenID provider
      // and retrieve one service endpoint for authentication
      DiscoveryInformation discovered = consumerManager.associate(discoveries);

      // Create a memento to rebuild the discovered information in a subsequent request
      DiscoveryInformationMemento memento = new DiscoveryInformationMemento();
      if (discovered.getClaimedIdentifier() != null) {
        memento.setClaimedIdentifier(discovered.getClaimedIdentifier().getIdentifier());
      }
      memento.setDelegate(discovered.getDelegateIdentifier());
      if (discovered.getOPEndpoint() != null) {
        memento.setOpEndpoint(discovered.getOPEndpoint().toString());
      }

      memento.setTypes(discovered.getTypes());
      memento.setVersion(discovered.getVersion());

      // Create a temporary User to preserve state between requests without
      // using a session (we could be in a cluster)
      User tempUser = new User(null, sessionToken);
      tempUser.setOpenIDDiscoveryInformationMemento(memento);
      tempUser.setSessionToken(sessionToken);

      // Persist the User
      InMemoryUserCache.INSTANCE.put(sessionToken, tempUser);

      // Build the AuthRequest message to be sent to the OpenID provider
      AuthRequest authReq = consumerManager.authenticate(discovered, returnToUrl);

      // Build the FetchRequest containing the information to be copied
      // from the OpenID provider
      FetchRequest fetch = FetchRequest.createFetchRequest();
      // Attempt to decode each entry
      if (identifier.startsWith(GOOGLE_ENDPOINT)) {
        fetch.addAttribute("email", "http://axschema.org/contact/email", true);
        fetch.addAttribute("firstName", "http://axschema.org/namePerson/first", true);
        fetch.addAttribute("lastName", "http://axschema.org/namePerson/last", true);
      } else if (identifier.startsWith(YAHOO_ENDPOINT)) {
        fetch.addAttribute("email", "http://axschema.org/contact/email", true);
        fetch.addAttribute("fullname", "http://axschema.org/namePerson", true);
      } else { // works for myOpenID
        fetch.addAttribute("fullname", "http://schema.openid.net/namePerson", true);
        fetch.addAttribute("email", "http://schema.openid.net/contact/email", true);
      }

      // Attach the extension to the authentication request
      authReq.addExtension(fetch);

      // Redirect the user to their OpenId server authentication process
      return Response
        .seeOther(URI.create(authReq.getDestinationUrl(true)))
        .build();

    } catch (MessageException e1) {
      log.error("MessageException:", e1);
    } catch (DiscoveryException e1) {
      log.error("DiscoveryException:", e1);
    } catch (ConsumerException e1) {
      log.error("ConsumerException:", e1);
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
  public Response verifyOpenIdServerResponse(
    @Context HttpServletRequest request,
    @QueryParam("token") String rawToken) {

    // Retrieve the previously stored discovery information from the temporary User
    if (rawToken == null) {
      log.debug("Authentication failed due to no session token");
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    // Build a session token from the request
    UUID sessionToken = UUID.fromString(rawToken);

    // Attempt to locate the consumer manager by the session token
    Optional<ConsumerManager> consumerManagerOptional = InMemoryOpenIDCache.INSTANCE.getConsumerManager(sessionToken);
    if (!consumerManagerOptional.isPresent()) {
      log.debug("Authentication failed due to no consumer manager matching session token {}", rawToken);
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    ConsumerManager consumerManager = consumerManagerOptional.get();

    // Attempt to locate the user by the session token
    Optional<User> tempUserOptional = InMemoryUserCache.INSTANCE.getBySessionToken(sessionToken);
    if (!tempUserOptional.isPresent()) {
      log.debug("Authentication failed due to no temp User matching session token {}", rawToken);
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    User tempUser = tempUserOptional.get();

    // Retrieve the discovery information
    final DiscoveryInformationMemento memento = tempUser.getOpenIDDiscoveryInformationMemento();
    Identifier identifier = new Identifier() {
      @Override
      public String getIdentifier() {
        return memento.getClaimedIdentifier();
      }
    };

    DiscoveryInformation discovered;
    try {
      discovered = new DiscoveryInformation(
        URI.create(memento.getOpEndpoint()).toURL(),
        identifier,
        memento.getDelegate(),
        memento.getVersion(),
        memento.getTypes()
      );
    } catch (DiscoveryException e) {
      throw new WebApplicationException(e, Response.Status.UNAUTHORIZED);
    } catch (MalformedURLException e) {
      throw new WebApplicationException(e, Response.Status.UNAUTHORIZED);
    }

    // Extract the receiving URL from the HTTP request
    StringBuffer receivingURL = request.getRequestURL();
    String queryString = request.getQueryString();
    if (queryString != null && queryString.length() > 0) {
      receivingURL.append("?").append(request.getQueryString());
    }
    log.debug("Receiving URL = '{}", receivingURL.toString());

    // Extract the parameters from the authentication response
    // (which comes in as a HTTP request from the OpenID provider)
    ParameterList parameterList = new ParameterList(request.getParameterMap());

    try {

      // Verify the response
      // ConsumerManager needs to be the same (static) instance used
      // to place the authentication request
      // This could be tricky if this service is load-balanced
      VerificationResult verification = consumerManager.verify(
        receivingURL.toString(),
        parameterList,
        discovered);

      // Examine the verification result and extract the verified identifier
      Optional<Identifier> verified = Optional.fromNullable(verification.getVerifiedId());
      if (verified.isPresent()) {
        // Verified
        AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

        // We have successfully authenticated so remove the temp user
        // and replace it with a potentially new one
        InMemoryUserCache.INSTANCE.hardDelete(tempUser);

        tempUser = new User(null, UUID.randomUUID());
        tempUser.setOpenIDIdentifier(verified.get().getIdentifier());

        // Provide a basic authority in light of successful authentication
        tempUser.getAuthorities().add(Authority.ROLE_PUBLIC);

        // Extract additional information
        if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
          tempUser.setEmailAddress(extractEmailAddress(authSuccess));
          tempUser.setFirstName(extractFirstName(authSuccess));
          tempUser.setLastName(extractLastName(authSuccess));
        }
        log.info("Extracted a temporary {}", tempUser);

        // Search for a pre-existing User matching the temp User
        Optional<User> userOptional = InMemoryUserCache.INSTANCE.getByOpenIDIdentifier(tempUser.getOpenIDIdentifier());
        User user;
        if (!userOptional.isPresent()) {
          // This is either a new registration or the OpenID identifier has changed
          if (tempUser.getEmailAddress() != null) {
            userOptional = InMemoryUserCache.INSTANCE.getByEmailAddress(tempUser.getEmailAddress());
            if (!userOptional.isPresent()) {
              // This is a new User
              log.debug("Registering new {}", tempUser);
              user = tempUser;
            } else {
              // The OpenID identifier has changed so update it
              log.debug("Updating OpenID identifier for {}", tempUser);
              user = userOptional.get();
              user.setOpenIDIdentifier(tempUser.getOpenIDIdentifier());
            }

          } else {
            // No email address to use as backup
            log.warn("Rejecting valid authentication. No email address for {}");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
          }
        } else {
          // The User has been located by their OpenID identifier
          log.debug("Found an existing User using OpenID identifier {}", tempUser);
          user = userOptional.get();

        }

        // Persist the user with the current session token
        user.setSessionToken(sessionToken);
        InMemoryUserCache.INSTANCE.put(sessionToken, user);

        // Create a suitable view for the response
        // The session token has changed so we create the base model directly
        BaseModel model = new BaseModel();
        model.setUser(user);

        // Authenticated
        View view = new PublicFreemarkerView<BaseModel>("private/home.ftl", model);

        // Refresh the session token cookie
        return Response
          .ok()
          .cookie(replaceSessionTokenCookie(Optional.of(user)))
          .entity(view)
          .build();

      } else {
        log.debug("Failed verification");
      }
    } catch (OpenIDException e) {
      // present error to the user
      log.error("OpenIDException", e);
    }

    // Must have failed to be here
    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
  }

  /**
   * @param user A user with a session token. If absent then the cookie will be removed.
   *
   * @return A cookie with a long term expiry date suitable for use as a session token for OpenID
   */
  private NewCookie replaceSessionTokenCookie(Optional<User> user) {

    if (user.isPresent()) {

      String value = user.get().getSessionToken().toString();

      log.debug("Replacing session token with {}", value);

      return new NewCookie(
        OpenIDDemoConfiguration.SESSION_TOKEN_NAME,
        value,   // Value
        "/",     // Path
        null,    // Domain
        null,    // Comment
        86400 * 30, // 30 days
        false);
    } else {
      // Remove the session token cookie
      log.debug("Removing session token");

      return new NewCookie(
        OpenIDDemoConfiguration.SESSION_TOKEN_NAME,
        null,   // Value
        null,    // Path
        null,   // Domain
        null,   // Comment
        0,      // Expire immediately
        false);
    }
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

  @SuppressWarnings({"unchecked", "unused"})
  private <T> T getAttributeValue(FetchResponse fetchResponse, String attribute, T defaultValue, Class<T> clazz) {
    List list = fetchResponse.getAttributeValues(attribute);
    if (list != null && !list.isEmpty()) {
      return (T) list.get(0);
    }

    return defaultValue;

  }

}
