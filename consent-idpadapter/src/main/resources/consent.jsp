<%--
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014 ForgeRock AS.
--%><%@ page pageEncoding="UTF-8" import="java.security.AccessController,
             java.util.List,
             java.util.Map,
             java.util.HashMap,
             java.util.Set,
             com.iplanet.sso.SSOToken,
             com.iplanet.sso.SSOTokenManager,
             com.sun.identity.idm.AMIdentity,
             com.sun.identity.idm.IdUtils,
             com.sun.identity.saml2.assertion.Attribute,
             com.sun.identity.saml2.profile.ConsentHelper,
             com.iplanet.am.util.SystemProperties,
             com.sun.identity.shared.encode.Base64,
             com.sun.identity.shared.Constants,
             com.iplanet.sso.SSOToken,
             org.forgerock.openam.utils.CollectionUtils"
%><%
    String contextPath = SystemProperties.get(Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR);

    String answer = request.getParameter("submit");
    if (answer != null) {
        if (answer.equals("Yes")) {
            try {
                SSOTokenManager tokenManager = SSOTokenManager.getInstance();
                SSOToken token = tokenManager.createSSOToken(request);
                AMIdentity identity = IdUtils.getIdentity(token);
                Set<String> consent = identity.getAttribute("givenName");
                consent.add((String) request.getParameter("spEntityID"));
                Map<String, Set<String>> consents = new HashMap<String, Set<String>>();
                consents.put("givenName", consent);
                identity.setAttributes(consents);
                identity.store();
                response.sendRedirect(new String(Base64.decode(request.getParameter("goto"))));
            } catch (Exception ex) {
                out.println("An error occured");
                ex.printStackTrace();
            }
            return;
        } else {
            response.sendRedirect(contextPath + "/consentDeny.jsp");
            return;
        }
    }
%><!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>User Consent</title>
        <link rel="stylesheet/less" type="text/css" href="<%= contextPath %>/XUI/css/styles.less" />
        <script language="javascript" type="text/javascript" src="<%= contextPath %>/XUI/libs/less-1.5.1-min.js"></script>
        <script language="javascript" type="text/javascript">
            less.modifyVars({
                "@background-image": "url('../images/box-bg.png')",
                "@background-position": "950px -100px",
                "@footer-background-color": "rgba(238, 238, 238, 0.7)",
                "@content-background": "#f9f9f9"
            });
        </script>
    </head>
    <body>
        <div id="wrapper">
            <div id="login-base" class="base-wrapper">
                <div id="header">
                    <div id="logo" class="float-left">
                        <a href="" title="ForgeRock"><img src="<%= contextPath %>/XUI/images/logo.png" alt="ForgeRock" style="height: 80px" /></a>
                    </div>
                </div>
                <div id="content" class="content">
                    <div class="container-shadow" id="login-container">
                        <form action="<%= contextPath %>/consent.jsp" method="POST" class="form small">
                        <%
                        List<Attribute> attrs = ConsentHelper.getAttributes(request);
                        if (attrs == null || attrs.isEmpty()) {
                        %>
                            <h5>Do you want to share your account information with <%= request.getAttribute("spEntityID") %>?</h5>
                        <%
                        } else {
                        %>
                            <h5>Are you sure you want to share the following informations with <%= request.getAttribute("spEntityID") %>?</h5>
                            <ul>
                            <%
                            for (Attribute attr : attrs) {
                            %>
                                <li><b><%= attr.getName() %></b>: <%= CollectionUtils.getFirstItem(attr.getAttributeValueString(), "") %></li>
                                <%
                            }
                        }
                            %>
                            </ul>
                            <fieldset>
                                <div class="group-field-block float-right">
                                    <input type="hidden" name="goto" value="${gotoURL}" />
                                    <input type="hidden" name="spEntityID" value="${spEntityID}" />
                                    <input name="submit" type="submit" class="button" index="0" value="Yes" />
                                    <input name="submit" type="submit" class="button" index="0" value="No" />
                                </div>
                            </fieldset>
                        </form>
                    </div>
                </div>
            </div>
        </div>
        <div id="footer">
            <div class="container center">
                <p class="center">
                    <a href="mailto: info@forgerock.com">info@forgerock.com</a>
                    <br>
                    Copyright © 2010-14 ForgeRock AS, all rights reserved.
                </p>
            </div>
        </div>
    </body>
</html>
