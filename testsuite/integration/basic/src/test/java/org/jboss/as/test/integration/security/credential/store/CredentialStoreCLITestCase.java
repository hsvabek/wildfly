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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.test.integration.security.credential.store.CredentialStoreParams.CredentialStoreParamBuilder;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TestCase contains tests for CredentialStore manipulation over CLI.
 *
 * @author Hynek Švábek <hsvabek@redhat.com>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
@ServerSetup(CredentialStoreServerSetupTask.class)
public class CredentialStoreCLITestCase extends AbstractCredentialStoreTestCase {

    // https://issues.jboss.org/browse/ELY-676

    /**
     * In test are created two CredentialStore(s) over CLI.
     *
     * @throws Exception
     */
    @Test
    public void testManipulateWithMultipleCS() throws Exception {
        String csName1 = getStringWithRandomSuffix("testManipulateWithMultipleCS1");
        String csName2 = getStringWithRandomSuffix("testManipulateWithMultipleCS1");
        String csFile1 = getStringWithRandomSuffix("testManipulateWithMultipleCS2");
        String csFile2 = getStringWithRandomSuffix("testManipulateWithMultipleCS2");

        ModelNode addressCS = createCredentialStoreAddress(csName1);
        createCS(csName1, csFile1,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass987")
            .keyPassword("pass654")
            .createStorage(true)
            .build());

        assertTrue(isExist(addressCS));

        ModelNode addressCS2 = createCredentialStoreAddress(csName2);
        createCS(csName2, csFile2,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass987")
            .createStorage(true)
            .build());

        assertTrue(isExist(addressCS2));

        ModelNode op = createReadChildNamesOperation("credential-store");
        ModelNode result = execute(op);

        List<ModelNode> resultList = result.get(ModelDescriptionConstants.RESULT).asList();
        String credStores = StringUtils.join(resultList, ";");
        assertTrue(credStores.contains(csName1) && credStores.contains(csName2));
    }

    /**
     * In test is created CredentialStore from scratch over CLI.
     *
     * @throws Exception
     */
    @Test
    public void testCreateCSFromScratch() throws Exception {
        String csName = getStringWithRandomSuffix("testCreateCSFromScratch");
        String csFile = getStringWithRandomSuffix("testCreateCSFromScratch");

        ModelNode addressCS = createCredentialStoreAddress(csName);
        createCS(csName, csFile,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass987")
            .createStorage(true)
            .build());

        assertTrue(isExist(addressCS));
    }

    /**
     * In test is created CredentialStore with existing CS file over CLI.
     *
     * @throws Exception
     */
    @Test
    public void testAddCredentialStore() throws Exception {
        String csName = getStringWithRandomSuffix("testAddCredentialStoreCLI");
        createCS(csName, CREDENTIAL_STORE_KS,
            new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .build());
    }

    /**
     * In test is created CredentialStore and there is set non-existing CS file over CLI.
     *
     * @throws Throwable
     */
    @Test(expected = AssertionError.class)
    public void testAddCredentialStoreCSFileDoesntExist() throws Throwable {
        String csName = getStringWithRandomSuffix("testAddCredentialStoreCLIFail");
        String csFile = getStringWithRandomSuffix("testAddCredentialStoreCLIFail");
        try {
            createCS(csName, csFile, new CredentialStoreParamBuilder()
                .credentialReferenceClearText("pass123")
                .build());
        } catch (AssertionError e) {
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Caused by: java.io.FileNotFoundException")) {
                    throw e;
                }
            }
        }
        Assert.fail("It should fail due to non-existing jceks file.");
    }

    /**
     * Test tries remove entry from CredentialStore and then adds new entry under same alias as removed one.
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/ELY-689")
    public void testRemoveAddCSAliasValue() throws Exception {
        String credentialStoreName = getStringWithRandomSuffix("testRemoveAddCSAliasValue");
        String csFile = getStringWithRandomSuffix("testRemoveAddCSAliasValue");
        String csAlias = "ffremoveadd";

        CredentialStoreParams params = new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(true)
            .build();

        createCS(credentialStoreName, csFile, params);

        ModelNode addressAlias = createCSAliasAddress(credentialStoreName, csAlias);
        ModelNode op = createAddCSAliasOperation(addressAlias, "ElytronWrongPass");
        execute(op);

        op = Operations.createRemoveOperation(addressAlias);
        execute(op);

        op = createAddCSAliasOperation(addressAlias, "Elytron");
        execute(op);
    }
}
