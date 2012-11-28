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

  <p><a href="/private/home">Access private info (protected with OpenId) available to public</a>
  <p><a href="/private/admin">Access private info (protected with OpenId) available to Administrators</a>

  <#include "../includes/common/footer.ftl">

</div>

<#include "../includes/common/cdn-scripts.ftl">

</body>
</html>