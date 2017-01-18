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

import java.io.IOException;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.test.integration.security.credential.store.CredentialStoreParams.CredentialStoreParamBuilder;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Keystore password is obtained from referenced CredentialStore.
 *
 * @author Hynek Švábek <hsvabek@redhat.com>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
@ServerSetup(CredentialStoreServerSetupTask.class)
public class CredentialStoreKeystoreTestCase extends AbstractCredentialStoreTestCase {

    /**
     * CredetentialStore is created from scratch and keystore obtains password from this CredentailStore.
     *
     * @throws Exception
     */
    @Test
    public void testUsageCSFromScratch() throws Exception {
        String credentialStoreName = getStringWithRandomSuffix("testUsageCSFromScratch");
        String csFile = getStringWithRandomSuffix("testUsageCSFromScratch");
        String csAlias = "ff";

        createCS(credentialStoreName, csFile,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass987")
            .createStorage(true)
            .build());

        ModelNode addressAlias = createCSAliasAddress(credentialStoreName, csAlias);
        ModelNode op = createAddCSAliasOperation(addressAlias, "Elytron");
        execute(op);
        assertCSOverServlet(credentialStoreName, csAlias, "Elytron");

        ModelNode addressKS = createKeystoreAddress("scratchCS");
        op = createAddKeystoreOperation(addressKS, createCredRefParams(credentialStoreName, csAlias));
        execute(op);
    }

    /**
     * CredetentialStore is created and uses existing CS file. Keystore obtains password from this CredentailStore.
     *
     * @throws Exception
     */
    @Test
    public void testKeystorePassFromCS() throws Exception {
        String credentialStoreName = getStringWithRandomSuffix("testKeystorePassFromCS");
        String keystoreName = getStringWithRandomSuffix("firefly");
        String csAlias = "ff";

        createCS(credentialStoreName, CREDENTIAL_STORE_KS,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .build());

        ModelNode addressAlias = createCSAliasAddress(credentialStoreName, csAlias);
        ModelNode op = createAddCSAliasOperation(addressAlias, "Elytron");
        execute(op);
        assertCSOverServlet(credentialStoreName, csAlias, "Elytron");

        ModelNode addressKS = createKeystoreAddress(keystoreName);
        op = createAddKeystoreOperation(addressKS, createCredRefParams(credentialStoreName, csAlias));
        execute(op);
        assertKeyStoreValue(keystoreName, "doesntMatterOnlyForAccessToKS");
    }

    /**
     * CredetentialStore is created and uses existing CS file. Keystore obtains password from this CredentailStore but this
     * password is wrong.
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/ELY-678")
    public void testKeystoreWrongPassFromCS() throws Exception {
        String credentialStoreName = getStringWithRandomSuffix("testKeystoreWrongPassFromCS");
        String keystoreName = getStringWithRandomSuffix("fireflyWrong");
        String csAlias = "ffwrong";

        createCS(credentialStoreName, CREDENTIAL_STORE_KS,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .build());

        ModelNode addressAlias = createCSAliasAddress(credentialStoreName, csAlias);
        ModelNode op = createAddCSAliasOperation(addressAlias, "ElytronWrongPass");
        execute(op);

        ModelNode addressKS = createKeystoreAddress(keystoreName);
        op = createAddKeystoreOperation(addressKS, createCredRefParams(credentialStoreName, csAlias));
        execute(op);
        try{
            // It must fail here
            assertKeyStoreValue(keystoreName, "doesntMatterOnlyForAccessToKS");
        } catch (AssertionError e) {
        }
    }

    /**
     * CredetentialStore is created and uses existing CS file. Keystore obtains password from this CredentailStore but there
     * isn't any password for given CredentialStore alias.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testKeystoreNonExistsPassInCS() throws Exception {
        String credentialStoreName = getStringWithRandomSuffix("testKeystoreNonExistsPassInCS");
        String keystoreName = getStringWithRandomSuffix("fireflyWrong");

        createCS(credentialStoreName, CREDENTIAL_STORE_KS,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .build());

        ModelNode addressKS = createKeystoreAddress(keystoreName);
        ModelNode op = createAddKeystoreOperation(addressKS, createCredRefParams(credentialStoreName, "ffnonexists"));
        execute(op);
    }

    /**
     * Keystore tries to obtain password from non-existing CredentailStore.
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/ELY-677")
    public void testKeystorePassFromNonExistsCS() throws Exception {
        String keystoreName = getStringWithRandomSuffix("fireflyNonExistsCS");
        ModelNode addressKS = createKeystoreAddress(keystoreName);
        ModelNode op = createAddKeystoreOperation(addressKS, createCredRefParams("NonExistsCredStore", "ff"));
        execute(op);
    }

    /**
     * Keystore has defined clear-text password.
     *
     * @throws IOException
     */
    @Test
    public void testCSReferenceClearTextPass() throws IOException {
        String ksName = getStringWithRandomSuffix("fireflyCSRef001");
        createKeyStore(ksName, "Elytron");
    }

    /**
     * Keystore has defined clear-text password. But this password is wrong.
     *
     * @throws IOException
     */
    @Test(expected = AssertionError.class)
    public void testCSReferenceClearTextWrongPass() throws IOException {
        String ksName = getStringWithRandomSuffix("fireflyCSRef001w");
        createKeyStore(ksName, "ElytronWrongPass");
    }

    /**
     * Keystore has defined reference to CredentialStore where is wrong password to access to keystore. We change this reference
     * to another CredentialStore where is right password to access.
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/WFCORE-1953"
        + "https://issues.jboss.org/browse/WFLY-7512")
    public void testCredentialReferenceKeystore() throws Exception {
        String credentialStoreName = getStringWithRandomSuffix("testCredentialReferenceKeystore1");
        String credentialStoreName2 = getStringWithRandomSuffix("testCredentialReferenceKeystore2");
        String csFile1 = getStringWithRandomSuffix("credStore");
        String csFile2 = getStringWithRandomSuffix("credStore");
        String keystoreName = getStringWithRandomSuffix("firefly");
        String csAlias = "ff";

        createCS(credentialStoreName, csFile1,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(true)
            .build());

        ModelNode addressAlias = createCSAliasAddress(credentialStoreName, csAlias);
        ModelNode op = createAddCSAliasOperation(addressAlias, "Elytron");
        execute(op);

        createCS(credentialStoreName2, csFile2,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(true)
            .build());

        addressAlias = createCSAliasAddress(credentialStoreName2, csAlias);
        op = createAddCSAliasOperation(addressAlias, "Elytron2");
        execute(op);

        assertCSOverServlet(credentialStoreName, csAlias, "Elytron");
        assertCSOverServlet(credentialStoreName2, csAlias, "Elytron2");

        ModelNode addressKS = createKeystoreAddress(keystoreName);
        op = createAddKeystoreOperation(addressKS, createCredRefParams(credentialStoreName, csAlias));
        execute(op);
        assertKeyStoreValue(keystoreName, "doesntMatterOnlyForAccessToKS");

        // now we must change credRef to CredStore2
        op = createUpdateKeystoreCredRefOperation(addressKS, createCredRefParams(credentialStoreName2, csAlias));
        execute(op);
        try {
            // It must fail here
            assertKeyStoreValue(keystoreName, "doesntMatterOnlyForAccessToKS");
        } catch (AssertionError e) {
            return;
        }
        Assert.fail("Access to KeyStore must fail.");
    }
}
