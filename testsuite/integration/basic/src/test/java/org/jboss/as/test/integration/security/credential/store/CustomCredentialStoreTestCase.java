/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.test.integration.security.credential.store;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.test.integration.security.credential.store.CredentialStoreParams.CredentialStoreParamBuilder;
import org.jboss.as.test.integration.security.credential.store.custom.CustomCredentialStore;
import org.jboss.as.test.integration.security.credential.store.custom.CustomElytronProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TestCase contains creating Custom Credential Store.
 *
 * @author Hynek Švábek <hsvabek@redhat.com>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
@ServerSetup(CredentialStoreServerSetupTask.class)
public class CustomCredentialStoreTestCase extends AbstractCredentialStoreTestCase {

    /**
     * Creation and work with Custom Credential Store
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/WFLY-7881")
    public void testCustomCredentialStoreProvider() throws Exception {
        final String moduleName = "org.jboss.customcredstore";
        final String jarName = getStringWithRandomPrefix("customcredstoreprovider.jar");
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, jarName);
        jar.addClasses(CustomElytronProvider.class, CustomCredentialStore.class);
        Path jarFile = Paths.get(jarName);

        jar.as(ZipExporter.class).exportTo(jarFile.toFile(), true);

        // try to module remove
        try {
            cli.sendLine("module remove --name=" + moduleName);
        } catch (AssertionError e) {
            // we ignore this error
        }

        StringBuilder cmd = new StringBuilder("module add ");
        cmd.append(" --name=").append(moduleName);
        cmd.append(" --resources=").append(jarFile.toAbsolutePath().toString());
        cmd.append(" --dependencies=");
        cmd.append("org.wildfly.security.elytron,");
        cmd.append("org.wildfly.extension.elytron");
        cmd.append(" --slot=main");

        cli.sendLine(cmd.toString());

        String providerLoader = getStringWithRandomSuffix("customProviderLoader");
        StringBuilder sb = new StringBuilder();
        sb.append("/subsystem=elytron/provider-loader=").append(providerLoader);
        sb.append(":add(providers=[{");
        sb.append("class-names=[").append(CustomElytronProvider.class.getName()).append("]");
        sb.append(",module=").append(moduleName);
        sb.append(",load-services=true");
        sb.append("}],register=true)");
        cli.sendLine(sb.toString());

        String csName = getStringWithRandomSuffix("testCustomCredentialStoreProvider");
        String csFilename = getStringWithRandomSuffix("testCustomCredentialStoreProvider");
        String csAlias = "csaliasabc";
        String csAliasValue = "aliasSecretValue";

        // Add Custom Credential Store
        CredentialStoreParamBuilder builder = new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(true)
            .provider(CustomElytronProvider.class.getName())
            .providerLoader(providerLoader);
        createCS(csName, csFilename, builder.build());
        writeAlias(csName, csAlias, csAliasValue);
        assertCSOverServlet(csName, csAlias, csAliasValue);
    }
}
