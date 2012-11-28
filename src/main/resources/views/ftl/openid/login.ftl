<#-- @ftlvariable name="" type="uk.co.froot.demo.openid.views.PublicFreemarkerView" -->
<!DOCTYPE html>
<html lang="en">
<head>
<#include "../includes/common/head.ftl">
</head>

<body>
<div>
<#include "../includes/common/header.ftl">

  <div>
    <form action="/openid?identifier=https://www.google.com/accounts/o8/id" method="post">
      <input type="submit" value="Google" />
    </form>

    <form action="/openid?identifier=https://me.yahoo.com" method="post">
      <input type="submit" value="Yahoo"/>
    </form>
  </div>

<#include "../includes/common/footer.ftl">

</div>

<#include "../includes/common/cdn-scripts.ftl">

</body>
</html>