# OpenId Demo

This project demonstrates the following

* Dropwizard - Serves HTML
* OpenId - Provides integration with OpenId providers (Google, Facebook etc)
* Authorization - Security annotation supporting different levels of access
* Session token cookie - Demonstrates stateless operation
                         (but uses an in-memory cache so not horizontally scalable)

## Getting started

From the console you can do the following
```
cd <project root>
mvn clean install
java -jar target/dropwizard-openid-1.0.0.jar server openid-demo.yml
```

## Proxy settings

If you are behind a firewall you will need to set the proxy. This is configured in ```PublicOpenIDResource```.

## Authorization

Here is an example of the authorization annotation as used in ```PrivateInfoResource```. 

```java
/**
 * @return The private home view if authenticated
 */
@GET
@Path("/home")
@Timed
@CacheControl(noCache = true)
public PublicFreemarkerView viewHome(
@RestrictedTo(Authority.ROLE_PUBLIC)
User publicUser) {

  BaseModel model = newBaseModel();
  return new PublicFreemarkerView<BaseModel>("private/home.ftl", model);

}
```

## Adding yourself as an "admin"

The code supports different levels of authority (PUBLIC and ADMIN). To add yourself as an admin, 
simply edit the following code in `PublicOpenIDResource` to be the email address you use in your 
selected OpenID provider:

    // Promote to admin if they have a specific email address
    // (not a good way, but this is only a demo)
    if ("nobody@example.org".equals(user.getEmailAddress())) {
      authorities.add(Authority.ROLE_ADMIN);
      log.info("Granted admin rights");
    }
        
## Where does the ASCII art come from?

The ASCII art for the startup banner was created using the online tool available at
[Webestools](http://www.webestools.com/ascii-text-generator-ascii-art-code-online-txt2ascii-text2ascii-maker-free-text-to-ascii-converter.html)
with a font of Tiza
