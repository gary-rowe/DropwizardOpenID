<#-- @ftlvariable name="" type="uk.co.froot.demo.openid.views.PublicFreemarkerView" -->
<!DOCTYPE html>
<html lang="en">
<head>
<#include "../includes/common/head.ftl">
</head>

<body>
<div>
<#include "../includes/common/header.ftl">

  <h1>Public home page</h1>

  <p>Anyone can access this page</p>

  <p><a href="/private/home">Access protected info</a>. This is available to anyone after authentication</p>

  <p><a href="/private/admin">Access private info</a>. This is only available to people who authenticate with the
    specific email address set in <code>PublicOpenIDResource</code>.</p>

  <hr/>

  <p><a href="/markdown">Show Markdown demo text</a>. This is available to anyone.</p>

  <#include "../includes/common/footer.ftl">

</div>

<#include "../includes/common/cdn-scripts.ftl">

</body>
</html>