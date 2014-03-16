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

import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.security.DecodeAction;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.encode.CookieUtils;
import java.security.AccessController;
import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

/**
 * This authentication module provides an example of implementing Remember Me functionality. This implementation does
 * not rely on any sort of user interaction, it just checks for the presence of the persistent cookie. When the cookie
 * is present, additional checks are performed in order to verify the user's identity. In order to leverage the remember
 * me feature, you need to modify the organization authentication chain to include the remember me module with
 * SUFFICIENT criteria.
 */
public class RememberMe extends AMLoginModule {

    public static final String MODULE_NAME = "RememberMe";
    public static final String BUNDLE_NAME = "amAuthRememberMe";
    private static final String AUTHLEVEL = "sunAMAuthRememberMeAuthLevel";
    private static final Debug DEBUG = Debug.getInstance(MODULE_NAME);
    private String authenticatedUser = null;

    public RememberMe() {
        DEBUG.message("In RememberMe.RememberMe()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Subject subject, Map sharedState, Map options) {
        DEBUG.message("In RememberMe.init()");

        amCache.getResBundle(BUNDLE_NAME, getLoginLocale());

        String authLevel = CollectionHelper.getMapAttr(options, AUTHLEVEL);
        if (authLevel != null) {
            try {
                setAuthLevel(Integer.parseInt(authLevel));
            } catch (NumberFormatException nfe) {
                DEBUG.error("Unable to set auth level " + authLevel, nfe);
            }
        }
    }

    /**
     * Processes the incoming request and looks for persistent cookies. The cookie value is first decrypted, then
     * expected to have the format of: username%realm%timestamp Once the cookie is validated the module may return
     * LOGIN_IGNORE (advances to next module), or LOGIN_SUCCEED.
     *
     * @param callbacks Submitted callbacks, but in the current case, it is ignored (since there is no user
     * interaction).
     * @param state Current state of the module - always LOGIN_START for this module.
     * @return LOGIN_SUCCEED if the persistent cookie was present and valid, LOGIN_IGNORE otherwise.
     * @throws LoginException should not happen for this module.
     */
    @Override
    public int process(Callback[] callbacks, int state) throws LoginException {
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) { //request may be null for REST and ClientSDK authentication attempts
            String pCookie = CookieUtils.getCookieValueFromReq(request, RememberMeUtils.PERSISTENT_COOKIE_NAME);
            String content = AccessController.doPrivileged(new DecodeAction(pCookie));
            if (content != null && !content.isEmpty()) { //there was a persistent cookie, so let's decrypt it
                String[] components = content.split("%");
                if (components.length != 3) { //something is wrong, let's remove the cookie
                    RememberMeUtils.clearPersistentCookie(getHttpServletResponse());
                    return ISAuthConstants.LOGIN_IGNORE;
                } else {
                    String userName = components[0];
                    String realm = components[1];
                    String timestamp = components[2];
                    if (!realm.equals(getRequestOrg())) {
                        //This cookie was made for a different realm, don't let the user in
                        return ISAuthConstants.LOGIN_IGNORE;
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.valueOf(timestamp));
                    cal.add(Calendar.DATE, RememberMeUtils.VALIDITY_IN_DAYS);
                    if (cal.before(new Date())) { //Cookie refers to too old authentication
                        RememberMeUtils.clearPersistentCookie(getHttpServletResponse());
                        return ISAuthConstants.LOGIN_IGNORE;
                    } else { //everything looks okay, let's authenticate the user
                        authenticatedUser = userName;
                        return ISAuthConstants.LOGIN_SUCCEED;
                    }
                }
            }
        }
        return ISAuthConstants.LOGIN_IGNORE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Principal getPrincipal() {
        if (authenticatedUser != null) {
            return new RememberMePrincipal(authenticatedUser);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroyModuleState() {
        authenticatedUser = null;
    }
}
