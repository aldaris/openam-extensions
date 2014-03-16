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
--%>

<%@ page pageEncoding="UTF-8" %>
<%@ page import="java.security.AccessController,
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
         com.iplanet.sso.SSOToken"
         %>
<%
    String serviceURI = SystemProperties.get(Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR);

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
            response.sendRedirect(serviceURI + "/consentDeny.jsp");
            return;
        }
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>OpenAM Consent</title>
        <link rel="stylesheet" type="text/css" href="<%= serviceURI%>/com_sun_web_ui/css/css_ns6up.css" />
        <link rel="shortcut icon" href="<%= serviceURI%>/com_sun_web_ui/images/favicon/favicon.ico" type="image/x-icon" />
    </head>
    <body class="DefBdy">
        <div class="SkpMedGry1"><a href="#SkipAnchor3860"><img src="<%= serviceURI%>/com_sun_web_ui/images/other/dot.gif" alt="Jump to End of Masthead" border="0" height="1" width="1" /></a></div><div class="MstDiv">
            <table class="MstTblBot" title="" border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                    <td class="MstTdTtl" width="99%">
                        <div class="MstDivTtl"><img name="AMConfig.configurator.ProdName" src="<%= serviceURI%>/console/images/PrimaryProductName.png" alt="OpenAM" border="0" /></div>
                    </td>
                    <td class="MstTdLogo" width="1%"><img name="AMConfig.configurator.BrandLogo" src="<%= serviceURI%>/com_sun_web_ui/images/other/javalogo.gif" alt="Java(TM) Logo" border="0" height="55" width="31" /></td>
                </tr>
            </table>
            <table class="MstTblEnd" border="0" cellpadding="0" cellspacing="0" width="100%"><tr><td><img name="RMRealm.mhCommon.EndorserLogo" src="<%= serviceURI%>/com_sun_web_ui/images/masthead/masthead-sunname.gif" alt="ForgeRock AS" align="right" border="0" height="10" width="108" /></td></tr></table>
        </div>
        <table class="SkpMedGry1" border="0" cellpadding="5" cellspacing="0" width="100%"><tr><td><img src="<%= serviceURI%>/com_sun_web_ui/images/other/dot.gif" alt="Jump to End of Masthead" border="0" height="1" width="1" /></td></tr></table>
        <table border="0" cellpadding="10" cellspacing="0" width="100%"><tr><td></td></tr></table>

        <form name="form" action="<%= serviceURI%>/consent.jsp" method="POST">

            <%
                List<Attribute> attrs = ConsentHelper.getAttributes(request);
                if (attrs == null || attrs.isEmpty()) {
                    out.println("Do you want to share your account information with " + request.getAttribute("spEntityID"));
                } else {
                    out.println("Are you sure you want to share the following informations with " + request.getAttribute("spEntityID") + "?");
                    out.println("<ul>");
                    for (Attribute attr : attrs) {
                        out.println("<li>" + attr.getName() + ":<ul>");
                        for (Object val : attr.getAttributeValueString()) {
                            out.println("<li>" + val + "</li>");
                        }
                        out.println("</ul></li>");
                    }
                    out.println("</ul>");
                }
            %>
            <input type="hidden" name="goto" value="${gotoURL}" />
            <input type="hidden" name="spEntityID" value="${spEntityID}" />
            <input type="submit" name="submit" value="Yes" />
            <input type="submit" name="submit" value="No" />
        </form>
    </body>
</html>
