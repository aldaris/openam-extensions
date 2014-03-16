# OpenAM Consent extension - Example IDPAdapter implementation

This is a very simple plugin, which basically will ask for the user's consent before sending the SAML assertion to the service provider.

## Features

* check if the user has already accepted the consent (in LDAP using givenName attribute...)
* if not, it will present a very basic page describing what attributes are you going to submit to which remote app. The remote app name will be the spEntityID.
* If you accept the consent it lets you through, and saves the fact in LDAP, so this page won't be shown to the user for the same app.
* it kicks in for both fresh login, and for already logged in situations

## Missing features

* localization - any kind
* configuration - everything is hardcoded
* design - you would customize it anyway
* there is no way to select which attributes to send and which don't - this is not possible at the moment without adding a new extension point/configuration option to core.

## License

Everything in this repo is licensed under the ForgeRock CDDL license: http://forgerock.org/license/CDDLv1.0.html
