JBoss Enterprise Application Platform (JBoss EAP) 7
===================================================
http://wildfly.org

* Fast Startup
* Small Footprint
* Modular Design
* Unified Configuration and Management

And of course Java EE!

Building
-------------------

Ensure you have JDK 8 (or newer) installed

> java -version

On *nix-like system use the prepared script

> ./build.sh

On Windows use the corresponding batch script

> build.bat

If you already have Maven 3.2.5 (or newer) installed you can use it directly

> mvn install


Starting and Stopping JBoss EAP
--------------------------------
Change to the bin directory after a successful build

> $ cd build/target/jboss-eap/bin

Start the server in domain mode

> $ ./domain.sh

Start the server in standalone mode

> $ ./standalone.sh

To stop the server, press Ctrl + C, or use the admin console

> $ ./jboss-cli.sh --connect command=:shutdown

Contributing
------------------
https://developer.jboss.org/wiki/HackingOnWildFly

Build vs. Dist directories
--------------------------

After running `mvn install`, JBoss EAP 7 will be available in two distinct directories, `build` and `dist`.

* The `build` directory contains a build of JBoss EAP 7 that is based on Maven artifact resolution for module configuration
* The `dist` directory, on the other hand, contains a full distributable build of JBoss EAP 7

Using the `build` directory makes iterating with subsystem or module development easier since there is no need to rebuild the whole of JBoss EAP 7 or copy JAR files around on every change.

The `dist` directory is better suited when a full build of JBoss EAP 7 is needed for development or test purposes.

Build vs. Dist directories
--------------------------

After running `mvn install`, WildFly will be available in two distinct directories, `build` and `dist`.

* The `build` directory contains a build of WildFly that is based on Maven artifact resolution for module configuration
* The `dist` directory, on the other hand, contains a full distributable build of WildFly

Using the `build` directory makes iterating with subsystem or module development easier since there is no need to rebuild the whole of WildFly or copy JAR files around on every change.

The `dist` directory is better suited when a full build of WildFly is needed for development or test purposes.

Running the Testsuite
--------------------
The testsuite module contains several submodules including the following:

* "smoke" -- core tests that should be run as part of every build of the AS. Failures here will fail the build.
* "api" -- tests of features that involve end user use of the public JBoss AS 8 API. Should be run with no failures before any major commits.
* "cluster" -- tests of the WildFly HA clustering features. Should be run with no failures before any major commits.
* "domain" -- tests of the domain management features. Should be run with no failures before any major commits.
* "integration" -- tests of a WildFly standalone server's internals. Should be run with no failures before any major commits.
* "spec" -- tests of features that only involve end user use of the Java EE 7 spec APIs. Should be run with no failures before any major commits.

To run the basic testsuite including smoke tests from the root directory, run the build script "./build.sh" or "build.bat":

For basic smoke tests, simply: "./build.sh test"

To run all the tests

> $ ./build.sh install -DallTests

Using Eclipse
-------------
1. Install the latest version of eclipse
2. Make sure Xmx in eclipse.ini is at least 1280M, and it's using Java 8
3. Launch eclipse and install the m2e plugin, make sure it uses your repo configs
   (get it from: http://www.eclipse.org/m2e/
   or install "Maven Integration for Eclipse" from the Eclipse Marketplace)
4. In eclipse preferences Java->Compiler->Errors/Warnings->Deprecated and restricted
   set forbidden reference to WARNING
5. In eclipse preferences Java->Code Style, import the cleanup, templates, and
   formatter configs in [ide-configs/eclipse](https://github.com/wildfly/wildfly-core/tree/master/ide-configs) in the wildfly-core repository.
6. In eclipse preferences Java->Editor->Save Actions enable "Additional Actions",
   and deselect all actions except for "Remove trailing whitespace"
7. Use import on the root pom, which will pull in all modules
8. Wait (m2e takes a while on initial import)

License
-------
* [GNU Lesser General Public License Version 2.1](http://www.gnu.org/licenses/lgpl-2.1-standalone.html)

