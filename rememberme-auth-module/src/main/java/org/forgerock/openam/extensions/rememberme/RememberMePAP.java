/*
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
 */
package org.forgerock.openam.extensions.rememberme;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.authentication.service.AuthUtils;
import com.sun.identity.authentication.spi.AMPostAuthProcessInterface;
import com.sun.identity.authentication.spi.AuthenticationException;
import com.sun.identity.security.EncodeAction;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.encode.CookieUtils;
import java.security.AccessController;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A Post Authentication Processing plugin for the Remember Me module. Upon successful authentication this PAP will add
 * the persistent cookie to the response if a persistent cookie wasn't already present. The created cookie will follow
 * the global cookie related settings (httpOnly/Secure), and by default will have 30 days of lifetime. The time of
 * authentication is also saved in the cookie value in order to make sure that the validity period is not extended. This
 * also means, that at least every 30 day the user must provide his credentials to get a new persistent cookie. When the
 * user logs out, this PAP will also try to clear out the cookie in order to prevent auto re-login.
 */
public class RememberMePAP implements AMPostAuthProcessInterface {

    private static final Debug DEBUG = Debug.getInstance("RememberMe");

    @Override
    public void onLoginSuccess(Map requestMap, HttpServletRequest request, HttpServletResponse response,
            SSOToken ssoToken) throws AuthenticationException {
        if (request != null) {
            String pCookie = CookieUtils.getCookieValueFromReq(request, RememberMeUtils.PERSISTENT_COOKIE_NAME);
            if (pCookie == null) { //We don't have a persistent cookie yet, so let's create one
                try {
                    StringBuilder sb = new StringBuilder(80);
                    sb.append(ssoToken.getProperty("UserId"));
                    sb.append('%');
                    sb.append(ssoToken.getProperty("Organization"));
                    sb.append('%');
                    sb.append(System.currentTimeMillis());
                    String content = AccessController.doPrivileged(new EncodeAction(sb.toString()));
                    DEBUG.message("Creating cookie with: " + content);
                    for (String domain : (Set<String>) AuthUtils.getCookieDomains()) {
                        Cookie cookie = new Cookie(RememberMeUtils.PERSISTENT_COOKIE_NAME, content);
                        cookie.setMaxAge(RememberMeUtils.VALIDITY_IN_DAYS * 24 * 60 * 60);
                        cookie.setDomain(domain);
                        cookie.setPath("/");
                        CookieUtils.addCookieToResponse(response, cookie);
                    }
                } catch (SSOException ssoe) {
                    DEBUG.warning("Unable to create persistent cookie", ssoe);
                }
            }
        }
    }

    @Override
    public void onLoginFailure(Map requestMap, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
    }

    @Override
    public void onLogout(HttpServletRequest request, HttpServletResponse response, SSOToken ssoToken)
            throws AuthenticationException {
        if (response != null) {
            //we can only delete the cookie if the response is present, this can cause troubles when using
            //REST/ClientSDK for logout - user might get reauthenticated immediately after pressing Logout...
            RememberMeUtils.clearPersistentCookie(response);
        }
    }
}
