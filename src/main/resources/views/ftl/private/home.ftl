<#-- @ftlvariable name="" type="uk.co.froot.demo.openid.views.PublicFreemarkerView" -->
<!DOCTYPE html>
<html lang="en">
<head>
<#include "../includes/common/head.ftl">
</head>

<body>
<div>
<#include "../includes/common/header.ftl">

  <h1>Private data</h1>
  <p>Congratulations! You authenticated through OpenId</p>
  <p>This can be seen by administrators and authenticated public</p>
  <p>Try to get to the <a href="/private/admin">admin page</a></p>

<#include "../includes/common/footer.ftl">

</div>

<#include "../includes/common/cdn-scripts.ftl">

</body>
</html>