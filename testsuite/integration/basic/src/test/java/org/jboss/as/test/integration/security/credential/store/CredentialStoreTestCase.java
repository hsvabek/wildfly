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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.resource.spi.IllegalStateException;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.test.integration.security.credential.store.CredentialStoreParams.CredentialStoreParamBuilder;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.security.credential.PasswordCredential;

/**
 *
 * @author Hynek Švábek <hsvabek@redhat.com>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
@ServerSetup(CredentialStoreServerSetupTask.class)
public class CredentialStoreTestCase extends AbstractCredentialStoreTestCase {

    /**
     * CredetentialStore is read-only.
     *
     * @throws Exception
     */
    @Test
    public void testReadonly() throws Exception {
        String credentialStoreName = getStringWithRandomSuffix("testReadonly");
        String csAlias = "ff";

        createCS(credentialStoreName, CREDENTIAL_STORE_KS,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(true)
            .modifiable(false)
            .build());

        try {
            writeAlias(credentialStoreName, csAlias, "csAliasValue");
        } catch (AssertionError e) {
            if (e.getMessage().contains("Cannot perform operation 'store': Credential store is set non modifiable")) {
                return;
            }
            new AssertionError("Test must fail. There isn't modifiable credential store", e);
        }
    }

    /**
     * CredetentialStore obtains password from another CredentialStore.
     *
     * @throws Exception
     */
    @Test
    public void testPassFromAnotherCS() throws Exception {
        String credentialStoreName = getStringWithRandomSuffix("testPassFromAnotherCS001");
        String csFile = getStringWithRandomSuffix("testPassFromAnotherCS001");
        String csAlias = "alias001";
        String credentialStoreName2 = getStringWithRandomSuffix("testPassFromAnotherCS002");
        String csAlias2 = getStringWithRandomSuffix("alias2").toLowerCase();

        createCS(credentialStoreName, csFile,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("abcdef")
            .createStorage(true)
            .build());

        writeAlias(credentialStoreName, csAlias, CREDENTIAL_STORE_KS_PASS);

        createCS(credentialStoreName2, CREDENTIAL_STORE_KS,
            new CredentialStoreParamBuilder()
            .credentialReferenceStore(credentialStoreName)
            .credentialReferenceAlias(csAlias)
            .createStorage(true)
            .build());

        writeAlias(credentialStoreName2, csAlias2, "csAliasValue");
        assertCSOverServlet(credentialStoreName2, csAlias2, "csAliasValue");
    }

    /**
     * CredetentialStore obtains wrong password from another CredentialStore.
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/ELY-676")
    public void testPassFromAnotherCS2() throws Exception {
        String credentialStoreName = getStringWithRandomSuffix("testPassFromAnotherCS2001");
        String csFile = getStringWithRandomSuffix("testPassFromAnotherCS2001");
        String csAlias = "alias001";
        String credentialStoreName2 = getStringWithRandomSuffix("testPassFromAnotherCS2002");
        String csAlias2 = getStringWithRandomSuffix("alias2").toLowerCase();

        createCS(credentialStoreName, csFile,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("xyz789")
            .createStorage(true)
            .build());

        writeAlias(credentialStoreName, csAlias, CREDENTIAL_STORE_KS_PASS);

        createCS(credentialStoreName2, CREDENTIAL_STORE_KS,
            new CredentialStoreParamBuilder()
            .credentialReferenceStore(credentialStoreName)
            .credentialReferenceAlias(csAlias)
            .createStorage(true)
            .build());

        writeAlias(credentialStoreName2, csAlias2, "csAliasValue");
        assertCSOverServlet(credentialStoreName2, csAlias2, "csAliasValue");

        // update secret value in first CS
        removeAlias(credentialStoreName, csAlias);
        writeAlias(credentialStoreName, csAlias, "csAliasValueWrong");
        assertCSOverServlet(credentialStoreName, csAlias, "csAliasValueWrong");

        try {
            writeAlias(credentialStoreName2, "aliasabcd", "csAliasValueABCD");
        } catch (AssertionError e) {
            if (e.getMessage().contains("Cannot write credential to store")) {
                return;
            }
            new AssertionError("Test must fail. There is obtained wrong password from another credential store", e);
        }
    }

    /**
     * After server startup and creating CredentialStore is removed backend CS file from filesystem. CredentialStore must work
     * fine. When is added new entry to CredentialStore the CS file must be created on filesystem again.
     *
     * @throws Exception
     */
    @Test
    // @Ignore("https://issues.jboss.org/browse/ELY-820")
    public void testRecreateCSAfterDetele() throws Exception {
        String dataDir = System.getProperty("jboss.home", null) + File.separator + "standalone" + File.separator + "data";
        String csName = getStringWithRandomSuffix("testRecreateCSAfterDeteleFromFS");
        String csFilename = getStringWithRandomSuffix("testRecreateCSAfterDeteleFromFS") + ".jceks";

        createCSFromScratchAndWriteAlias(getStringWithRandomSuffix("csTemp"), csFilename, "csalias", "csAliasValue");

        createCS(csName, csFilename, new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(false)
            .relativeTo("jboss.server.data.dir")
            .build());
        writeAlias(csName, "csaliasabc", "aliasSecretValue");

        Path csPath = Paths.get(dataDir, csFilename);
        if (!Files.exists(csPath)) {
            throw new IllegalStateException("CredentialStore file must exist [" + csPath + "].");
        }
        Files.delete(csPath);
        if (Files.exists(csPath)) {
            throw new IllegalStateException("CredentialStore file was not deleted successful [" + csPath + "].");
        }
        writeAlias(csName, "csaliasdef", "aliasSecretValue2");
        if (!Files.exists(csPath)) {
            throw new IllegalStateException("CredentialStore file must be recreated again [" + csPath + "].");
        }
        assertCSOverServlet(csName, "csaliasabc", "aliasSecretValue");
        assertCSOverServlet(csName, "csaliasdef", "aliasSecretValue2");
    }

    /**
     * Entry type is explicitly defined.
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/ELY-652")
    public void testEntryType() throws Exception {
        String csName = getStringWithRandomSuffix("csName");
        String csFilename = getStringWithRandomSuffix("entryType");

        createCS(csName, csFilename, new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(true)
            .build());

        ModelNode addressAlias = createCSAliasAddress(csName, "csalias");
        ModelNode op = createAddCSAliasOperation(addressAlias, "Elytron", PasswordCredential.class.getName());
        execute(op);
    }

    /**
     * Entry type is set to invalid value.
     *
     * @throws Exception
     */
    @Test
    public void testInvalidEntryType() throws Exception {
        String csName = getStringWithRandomSuffix("csName");
        String csFilename = getStringWithRandomSuffix("entryType");

        CredentialStoreParamBuilder builder = new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(true);

        createCS(csName, csFilename, builder.build());

        try {
            ModelNode addressAlias = createCSAliasAddress(csName, "csalias");
            ModelNode op = createAddCSAliasOperation(addressAlias, "Elytron", "wrongEntryType");
            execute(op);
        } catch (AssertionError e) {
            if (e.getMessage().contains("does not support given credential store entry type")) {
                return;
            }
            new AssertionError("Test must fail. There isn't valid credential store entry type", e);
        }
        Assert.fail("Test must fail. There isn't valid credential store entry type");
    }

    /**
     * Test set base directory for location CS files to dir defined by jboss property.
     *
     * @throws Exception
     */
    @Test
    public void testRelativeTo() throws Exception {
        String home = System.getProperty("jboss.home");
        if (Files.notExists(Paths.get(home))) {
            throw new IllegalStateException("Folder ["+home+"] must exist.");
        }

        String csName = getStringWithRandomSuffix("csName");
        String csFilename = getStringWithRandomSuffix("storeBase") + ".jceks";

        CredentialStoreParamBuilder builder = new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(true)
            .relativeTo("jboss.home.dir");

        createCS(csName, csFilename, builder.build());
        writeAlias(csName, "csaliasabc", "aliasSecretValue");

        Path csPath = Paths.get(home, csFilename);
        if (Files.notExists(csPath)) {
            Assert.fail("CS file must be located in [" + csPath.toString() + "].");
        }
    }

    /**
     * Test set base directory for location CS files to dir defined by custom property.
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/JBEAP-8180")
    public void testCustomRelativeTo() throws Exception {
        String home = System.getProperty("java.io.tmpdir");
        if (Files.notExists(Paths.get(home))) {
            throw new IllegalStateException("Folder ["+home+"] must exist.");
        }

        String csName = getStringWithRandomSuffix("csName");
        String csFilename = getStringWithRandomSuffix("storeBase") + ".jceks";

        CredentialStoreParamBuilder builder = new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(true)
            .relativeTo("java.io.tmpdir");

        createCS(csName, csFilename, builder.build());
        writeAlias(csName, "csaliasabc", "aliasSecretValue");

        Path csPath = Paths.get(home, csFilename);
        if (Files.notExists(csPath)) {
            Assert.fail("CS file must be located in [" + csPath.toString() + "].");
        }
    }

    /**
     * Simultaneous access to multiple CredentialStore(s) which have set same backend CS file.
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/WFLY-7623")
    public void testMultipleCredentialStoreWithSameBackendFile() throws Exception {
        int CS_NUM = 3;
        int THREAD_NUM = 10;
        int ALIAS_NUM = 1000;

        String csName = getStringWithRandomSuffix("csName");
        String csFilename = getStringWithRandomSuffix("testMultipleCredentialStoreWithSameBackendFile");
        // create CredentialStore file on filesystem
        createCSFromScratchAndWriteAlias(csName, csFilename, "csalias", "secretValue");

        multi(CS_NUM, THREAD_NUM, ALIAS_NUM, csName, csFilename);

        // We have one backend CS file, but different CS can have different values in memory. We need re-init values from cs
        // file.
        String csForValidation = getStringWithRandomSuffix("csForValidating");
        createCS(csForValidation, csFilename);
        assertNumberOfRecords(csForValidation, ALIAS_NUM);
    }

    /**
     * Parallel access to one CredentialStore.
     *
     * @throws Exception
     */
    @Test
    public void testParallelAccessToCS() throws Exception {
        int CS_NUM = 1;
        int THREAD_NUM = 10;
        int ALIAS_NUM = 1000;

        String csName = getStringWithRandomSuffix("csName");
        String csFilename = getStringWithRandomSuffix("testParallelAccessToCS");
        // create CredentialStore file on filesystem
        createCS(csName, csFilename,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(true)
            .build());

        multi(CS_NUM, THREAD_NUM, ALIAS_NUM, csName, csFilename);

        // We have one backend CS file, but different CS can have different values in memory. We need re-init values from cs
        // file.
        String csForValidation = getStringWithRandomSuffix("csForValidating");
        createCS(csForValidation, csFilename);
        assertNumberOfRecords(csForValidation, ALIAS_NUM);
    }
}
