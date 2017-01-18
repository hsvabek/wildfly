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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.test.integration.security.credential.store.CredentialStoreParams.CredentialStoreParamBuilder;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TestCase contains various tests for obtaining password through external command or password is masked. There are tests which
 * working with special, chinese and arabic characters.
 *
 * @author Hynek Švábek <hsvabek@redhat.com>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
@ServerSetup(CredentialStoreServerSetupTask.class)
public class CredentialStoreMiscTestCase extends AbstractCredentialStoreTestCase {

    private static Logger LOGGER = Logger.getLogger(CredentialStoreMiscTestCase.class);

    /**
     * Special characters are set to CredentialStore name, CredentialStore filename, entry alias, entry secret value.
     *
     * @throws Exception
     */
    @Test
    public void testCredentialStoreWithSpecialChars() throws Exception {
        createCSFromScratchAndValidate(getStringWithRandomSuffix("csSpecChars"), getStringWithRandomSuffix("abc123"), "csalias",
            SPECIAL_CHARS);
        createCSFromScratchAndValidate(getStringWithRandomSuffix(SPECIAL_CHARS), getStringWithRandomSuffix("abc123"),
            "csalias", "csAliasValue");

        List<String> problemWithCharsInFilename = new ArrayList<>();
        List<String> problemWithCharsInAlias = new ArrayList<>();

        for (char specialChar : LIMITED_SPECIAL_CHARS.toCharArray()) {
            String special = String.valueOf(specialChar);
            try {
                createCSFromScratchAndValidate(getStringWithRandomSuffix("csSpecChars"), getStringWithRandomPrefix(special),
                    "csalias", "csAliasValue");
            } catch (Throwable e) {
                    LOGGER.errorf(e, "There is problem with special char {} in filename", special);
                    problemWithCharsInFilename.add(special);
            }
        }

        for (char specialChar : SPECIAL_CHARS.toCharArray()) {
            String special = String.valueOf(specialChar);
            try {
                createCSFromScratchAndValidate(getStringWithRandomSuffix("csSpecChars"), getStringWithRandomSuffix("abc123"),
                    getStringWithRandomPrefix(special).toLowerCase(), "csAliasValue");
            } catch (Throwable e) {
                LOGGER.errorf(e, "There is problem with special char {} in alias", special);
                problemWithCharsInAlias.add(special);
            }
        }

        StringBuilder msg = new StringBuilder();
        if (!problemWithCharsInFilename.isEmpty()) {
            msg.append("There is problem with these special characters in credentialstore filename: "
                + String.join("", problemWithCharsInFilename));
            msg.append("\n");
        }
        if (!problemWithCharsInAlias.isEmpty()) {
            msg.append("There is problem with these special characters in credential store alias: "
                + String.join("", problemWithCharsInAlias));
        }
        if (msg.length() > 0) {
            Assert.fail(msg.toString());
        }

        createCSFromScratchAndValidate(getStringWithRandomSuffix("csSpecChars"),
            getStringWithRandomSuffix(LIMITED_SPECIAL_CHARS),
            "csalias", "csAliasValue");
        createCSFromScratchAndValidate(getStringWithRandomSuffix("csSpecChars"), getStringWithRandomSuffix("abc123"),
            SPECIAL_CHARS, "csAliasValue");

        createCSFromScratchAndValidate(getStringWithRandomSuffix(SPECIAL_CHARS),
            getStringWithRandomSuffix(LIMITED_SPECIAL_CHARS), SPECIAL_CHARS, SPECIAL_CHARS);
    }

    /**
     * Chinese characters are set to CredentialStore name, CredentialStore filename, entry alias, entry secret value.
     *
     * @throws Exception
     */
    @Test
    public void testCredentialStoreWithChineseChars() throws Exception {
        createCSFromScratchAndValidate(getStringWithRandomSuffix("csChineseChars"), getStringWithRandomSuffix("abc123"),
            "csalias",
            CHINESE_CHARS);
        createCSFromScratchAndValidate(getStringWithRandomSuffix("csChineseChars"), getStringWithRandomSuffix("abc123"),
            CHINESE_CHARS, "csAliasValue");
        createCSFromScratchAndValidate(getStringWithRandomSuffix("csChineseChars"), getStringWithRandomSuffix(CHINESE_CHARS),
            "csalias", "csAliasValue");
        createCSFromScratchAndValidate(getStringWithRandomSuffix(CHINESE_CHARS), getStringWithRandomSuffix("abc123"),
            "csalias", "csAliasValue");
        createCSFromScratchAndValidate(getStringWithRandomSuffix(CHINESE_CHARS), getStringWithRandomSuffix(CHINESE_CHARS),
            CHINESE_CHARS, CHINESE_CHARS);
    }

    /**
     * Arabic characters are set to CredentialStore name, CredentialStore filename, entry alias, entry secret value.
     *
     * @throws Exception
     */
    @Test
    public void testCredentialStoreWithArabicChars() throws Exception {
        createCSFromScratchAndValidate(getStringWithRandomSuffix("csArabicChars"), getStringWithRandomSuffix("abc123"),
            "csalias",
            ARABIC_CHARS);
        createCSFromScratchAndValidate(getStringWithRandomSuffix("csArabicChars"), getStringWithRandomSuffix(ARABIC_CHARS),
            "csalias", "csAliasValue");
        createCSFromScratchAndValidate(getStringWithRandomSuffix(ARABIC_CHARS), getStringWithRandomSuffix("abc123"),
            "csalias", "csAliasValue");
        createCSFromScratchAndValidate(getStringWithRandomSuffix("csArabicChars"), getStringWithRandomSuffix("abc123"),
            ARABIC_CHARS, "csAliasValue");
        createCSFromScratchAndValidate(getStringWithRandomSuffix(ARABIC_CHARS), getStringWithRandomSuffix(ARABIC_CHARS),
            ARABIC_CHARS, ARABIC_CHARS);
    }

    /**
     * CredentialStore password is obtained through external command using java.lang.Runtime#exec(java.lang.String).
     * CredentialStore is created from scratch.
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/WFLY-7876")
    public void testExternalPasswordTypeEXTCreateStorage() throws Exception {
        String csFilename = getStringWithRandomSuffix("testExternalPasswordTypeEXTCreateStorage");

        String masterCommand = buildExternalCommand("{EXT}", " ", "secret_store_THREE");
        externalCredentialTestSequence(csFilename, masterCommand, true, null);
    }

    /**
     * CredentialStore password is obtained through external command using java.lang.Runtime#exec(java.lang.String).
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/WFLY-7876")
    public void testExternalPasswordTypeEXT() throws Exception {
        String csName = getStringWithRandomSuffix("testExternalPasswordTypeEXT");
        String csFilename = getStringWithRandomSuffix("testExternalPasswordTypeEXT");
        CredentialStoreParams params = new CredentialStoreParamBuilder()
            .credentialReferenceClearText("secret_store_THREE")
            .createStorage(true)
            .build();
        createCSAndWriteAlias(csName, csFilename, "ff", "Elytron", params);

        String masterCommand = buildExternalCommand("{EXT}", " ", "secret_store_THREE");
        externalCredentialTestSequence(csFilename, masterCommand, false, null);
    }

    /**
     * CredentialStore password is obtained through external command using java.lang.ProcessBuilder. CredentialStore is created
     * from scratch.
     *
     * @throws Exception
     */
    @Test
    public void testExternalPasswordTypeCMDCreateStorage() throws Exception {
        String csFilename = getStringWithRandomSuffix("testExternalPasswordTypeCMDCreateStorage");

        String masterCommand = buildExternalCommand("{CMD}", ",", "VerySecretPassword");
        externalCredentialTestSequence(csFilename, masterCommand, true, null);
    }

    /**
     * CredentialStore password is obtained through external command using java.lang.ProcessBuilder.
     *
     * @throws Exception
     */
    @Test
    public void testExternalPasswordTypeCMD() throws Exception {
        String csName = getStringWithRandomSuffix("testExternalPasswordTypeCMD");
        String csFilename = getStringWithRandomSuffix("testExternalPasswordTypeCMD");
        CredentialStoreParams params = new CredentialStoreParamBuilder()
            .credentialReferenceClearText("VerySecretPassword")
            .createStorage(true)
            .build();
        createCSAndWriteAlias(csName, csFilename, "ff", "Elytron", params);

        String masterCommand = buildExternalCommand("{CMD}", ",", "VerySecretPassword");
        externalCredentialTestSequence(csFilename, masterCommand, false, null);
    }

    /**
     * CredentialStore password is masked using PBE (Password Based Encryption). CredentialStore is created from scratch.
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/ELY-868")
    public void testExternalPasswordTypeMaskedCreateStorage() throws Exception {
        String csFilename = getStringWithRandomSuffix("testExternalPasswordTypeMasked");

        Map<String, String> attributes = new HashMap<>();
        attributes.put("store.password" + "." + "iteration", "23");
        attributes.put("store.password" + "." + "salt", "HJU90jqw");
        // attributes.put(KeystorePasswordStore.STORE_PASSWORD + "." + MaskedPasswordStore.ITERATION_COUNT, "23");
        // attributes.put(KeystorePasswordStore.STORE_PASSWORD + "." + MaskedPasswordStore.SALT, "HJU90jqw");
        // secret_store_THREE
        String masterCommand = "MASK-vXSK9HZ0XD8w3VPFfUY5T3xz0/./3r/3";

        externalCredentialTestSequence(csFilename, masterCommand, true, attributes);
    }

    /**
     * CredentialStore password is masked using PBE (Password Based Encryption).
     *
     * @throws Exception
     */
    @Test
    @Ignore("https://issues.jboss.org/browse/ELY-868")
    public void testExternalPasswordTypeMasked() throws Exception {
        String csName = getStringWithRandomSuffix("testExternalPasswordTypeMasked");
        String csFilename = getStringWithRandomSuffix("testExternalPasswordTypeMasked");
        CredentialStoreParams params = new CredentialStoreParamBuilder()
            .credentialReferenceClearText("secret_store_THREE")
            .createStorage(true)
            .build();
        createCSAndWriteAlias(csName, csFilename, "ff", "Elytron", params);

        Map<String, String> attributes = new HashMap<>();
        // attributes.put(KeystorePasswordStore.STORE_PASSWORD + "." + MaskedPasswordStore.ITERATION_COUNT, "23");
        // attributes.put(KeystorePasswordStore.STORE_PASSWORD + "." + MaskedPasswordStore.SALT, "HJU90jqw");
        // secret_store_THREE
        String masterCommand = "MASK-vXSK9HZ0XD8w3VPFfUY5T3xz0/./3r/3";

        externalCredentialTestSequence(csFilename, masterCommand, false, attributes);
    }
}
