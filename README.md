# OpenId Demo

This project demonstrates the following

* Dropwizard - Serves HTML
* OpenId - Provides integration with OpenId providers (Google, Facebook etc)
* Authorization - Security annotation supporting different levels of access
* Session cookies - Not very stateless, but this is only a demo - you'd use a database in real life
 
## Notation

<project root> - The root directory of the project as checked out through git

All commands will work on *nix without modification, use \ instead of / for Windows.

## Getting started

From the console you can do the following

    cd <project root>
    mvn clean install
    java -jar target/openid-demo-develop-SNAPSHOT.jar server openId-demo.yml

## Proxy settings

If you are behind a firewall you will need to set the proxy. This is configured in `PublicOpenIDResource.

## Authorization

Here is an example of the authorization annotation as used in `PrivateInfoResource. 

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

## Where does the ASCII art come from?

The ASCII art for the startup banner was created using the online tool available at
[Webestools](http://www.webestools.com/ascii-text-generator-ascii-art-code-online-txt2ascii-text2ascii-maker-free-text-to-ascii-converter.html)
with a font of Tiza