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

import com.sun.identity.authentication.service.AuthUtils;
import com.sun.identity.shared.encode.CookieUtils;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple helper class for remember me functionality.
 */
public class RememberMeUtils {

    public static final String PERSISTENT_COOKIE_NAME = "pCookie";
    public static final int VALIDITY_IN_DAYS = 30;

    /**
     * Clears the persistent cookie from the response.
     *
     * @param response The HttpServletResponse.
     */
    public static void clearPersistentCookie(HttpServletResponse response) {
        for (String domain : (Set<String>) AuthUtils.getCookieDomains()) {
            Cookie cookie = new Cookie(RememberMeUtils.PERSISTENT_COOKIE_NAME, "");
            cookie.setMaxAge(0);
            cookie.setDomain(domain);
            cookie.setPath("/");
            CookieUtils.addCookieToResponse(response, cookie);
        }
    }
}
