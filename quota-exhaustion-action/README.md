# Quota Exhaustion Action example project

This example implementation shows how to simply implement a quota exhaustion action

## How to install this module

* Put the resulting JAR on the OpenAM classpath (WEB-INF/lib)
* Execute the following ssoadm command:
<pre>
$ openam/bin/ssoadm set-attr-choicevals -s iPlanetAMSessionService -t Global -a iplanet-am-session-constraint-handler -u amadmin -f .pass -p -k myKey=org.forgerock.openam.extensions.quotaexhaustionaction.SampleQuotaExhaustionAction
</pre>
* Edit the WEB-INF/classes/amSession.properties file and add the following content:
<pre>
myKey=Randomly Destroy Session
</pre>
* Restart OpenAM

## License

Everything in this repository is licensed under the ForgeRock CDDL license: http://forgerock.org/license/CDDLv1.0.html

## More info

For more information on this example quota exhaustion action check out this blog entry:
http://blogs.forgerock.org/petermajor/2013/01/session-quota-basics/
