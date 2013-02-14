<#-- @ftlvariable name="" type="uk.co.froot.demo.openid.views.PublicFreemarkerView" -->
<!DOCTYPE html>
<html lang="en">
<head>
<#include "../includes/common/head.ftl">

    <!-- Simple OpenID Selector -->
    <link type="text/css" rel="stylesheet" href="/jquery/plugins/openid-selector/css/openid.css"/>

</head>

<body>
<div>
<#include "../includes/common/header.ftl">

    <h2>JQuery Simple OpenID Selector Demo</h2>

    <p>This is a simple example to show how you can include the Javascript into your page.</p>

    <p>This example is based on the <a href="http://code.google.com/p/openid-selector/">openid-selector project</a> that
        also supports Prototype and Mootools.</p>
    <br/>
    <!-- Simple OpenID Selector -->
    <form action="/openid" method="post" id="openid_form">
        <fieldset>
            <legend>Sign-in or Create New Account</legend>
            <div id="openid_choice">
                <p>Please click your account provider:</p>

                <div id="openid_btns"></div>
            </div>
            <div id="openid_input_area">
                <input id="openid_identifier" name="openid_identifier" type="text" value="http://"/>
                <input id="openid_submit" type="submit" value="Sign-In"/>
            </div>
            <noscript>
                <p>OpenID is service that allows you to log-on to many different websites using a single indentity.
                    Find out <a href="http://openid.net/what/">more about OpenID</a> and <a
                            href="http://openid.net/get/">how to get an OpenID enabled account</a>.</p>
            </noscript>
        </fieldset>
    </form>
    <!-- /Simple OpenID Selector -->
</div>

<#include "../includes/common/footer.ftl">

</div>

<#include "../includes/common/cdn-scripts.ftl">

<!-- Page specific scripts -->
<script type="text/javascript" src="/jquery/plugins/openid-selector/js/openid-jquery.js"></script>
<script type="text/javascript" src="/jquery/plugins/openid-selector/js/openid-en.js"></script>
<script type="text/javascript">
  $(document).ready(function () {
      openid.init('identifier');
  });
</script>

</body>
</html>