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

import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.test.integration.security.credential.store.SimpleServlet.CREDSTORE_ALIAS;
import static org.jboss.as.test.integration.security.credential.store.SimpleServlet.CREDSTORE_NAME;
import static org.jboss.as.test.integration.security.credential.store.SimpleServlet.KEYSTORE_ACCESS_ALLOWED;
import static org.jboss.as.test.integration.security.credential.store.SimpleServlet.KEYSTORE_ALIAS;
import static org.jboss.as.test.integration.security.credential.store.SimpleServlet.KEYSTORE_NAME;
import static org.jboss.as.test.integration.security.credential.store.SimpleServlet.NUMBER_OF_RECORDS_CS;
import static org.jboss.as.test.integration.security.credential.store.SimpleServlet.NUMBER_OF_RECORDS_CS_NAME;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import javax.resource.spi.IllegalStateException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ContainerResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.test.integration.management.util.CLIWrapper;
import org.jboss.as.test.integration.management.util.MgmtOperationException;
import org.jboss.as.test.integration.security.credential.store.CredentialStoreParams.CredentialStoreParamBuilder;
import org.jboss.as.test.shared.ServerReload;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * The base TestCase class which contains helper methods and other common stuff.
 *
 * @author Hynek Švábek <hsvabek@redhat.com>
 *
 */
public abstract class AbstractCredentialStoreTestCase {

    protected static final String JBOSS_HOME = System.getProperty("jboss.home", null);
    protected static final String JBOSS_DATA_DIR = JBOSS_HOME + "/standalone/data";
    protected static final String CREDENTIAL_STORE_KS = "credentialstore.jceks";
    protected static final String CREDENTIAL_STORE_KS_PASS = "pass123";
    protected static final String FIREFLY_KS = "firefly.keystore";
    protected static final String SPECIAL_CHARS = "@!#?$%^&*()%+-{}";
    protected static final String LIMITED_SPECIAL_CHARS = "@!$%^&*()%+-{}";
    protected static final String CHINESE_CHARS = "用戶名";
    protected static final String ARABIC_CHARS = "اسمالمستخدم";
    protected static CLIWrapper cli;

    @ArquillianResource
    private URL servletUrl;

    @ContainerResource
    private static ManagementClient managementClient;

    @Deployment
    public static WebArchive deployment() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
        war.addAsResource(new StringAsset("Dependencies: org.jboss.as.server,org.jboss.as.controller\n"),
            "META-INF/MANIFEST.MF").addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        war.addClasses(AbstractCredentialStoreTestCase.class, SimpleServlet.class);
        war.addClass(StringUtils.class);
        return war;
    }

    @BeforeClass
    public static void setupCredentialStores() throws Exception {
        initCLI(true);

        if (JBOSS_HOME == null) {
            throw new IllegalStateException("-Djboss.home must be set.");
        }

        Path csPath = Paths.get(JBOSS_DATA_DIR);
        if (!Files.exists(csPath)) {
            Files.createDirectories(csPath);
        }

        URL CredStoreKS = CredentialStoreMiscTestCase.class.getResource(CREDENTIAL_STORE_KS);
        URL fireflyKS = CredentialStoreMiscTestCase.class.getResource(FIREFLY_KS);

        Path credStoreKSPath = Paths.get(csPath.toString(), CREDENTIAL_STORE_KS);
        FileUtils.copyURLToFile(CredStoreKS, credStoreKSPath.toFile());

        Path fireflyKSPath = Paths.get(csPath.toString(), FIREFLY_KS);
        FileUtils.copyURLToFile(fireflyKS, fireflyKSPath.toFile());
    }

    protected static void initCLI(boolean connect) throws Exception {
        if (cli == null) {
            cli = new CLIWrapper(connect);
        }
    }

    protected String getStringWithRandomPrefix(String str) {
        return getRandomString() + "_" + str;
    }

    protected String getStringWithRandomSuffix(String str) {
        return str + "_" + getRandomString();
    }

    private String getRandomString() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    protected void createCS(String csName, String jceksFilename) throws Exception {
        CredentialStoreParamBuilder builder = new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(false);
        createCS(csName, jceksFilename, builder.build());
    }

    protected void createCS(String csName, String jceksFilename, CredentialStoreParams params)
        throws Exception {
        ModelNode addressCS = createCredentialStoreAddress(csName);
        if (!jceksFilename.endsWith(".jceks")) {
            jceksFilename = jceksFilename + ".jceks";
        }
        ModelNode op = createAddCredentialStoreOperation(addressCS, jceksFilename, params);
        execute(op);
    }

    protected void createCSFromScratchAndWriteAlias(String csName, String jceksFilename, String csAlias, String csAliasValue)
        throws Exception {

        CredentialStoreParamBuilder builder = new CredentialStoreParamBuilder()
            .credentialReferenceClearText("pass123")
            .createStorage(true);

        createCSAndWriteAlias(csName, jceksFilename, csAlias, csAliasValue, builder.build());
    }

    protected void createCSAndWriteAlias(String csName, String jceksFilename, String csAlias, String csAliasValue,
        CredentialStoreParams params) throws Exception {
        createCS(csName, jceksFilename, params);
        writeAlias(csName, csAlias, csAliasValue);
    }

    protected void writeAlias(String csName, String csAlias, String csAliasValue) throws Exception {
        ModelNode addressAlias = createCSAliasAddress(csName, csAlias);
        ModelNode op = createAddCSAliasOperation(addressAlias, csAliasValue);
        execute(op);
    }

    protected void removeAlias(String csName, String csAlias) throws Exception {
        ModelNode addressAlias = createCSAliasAddress(csName, csAlias);
        ModelNode op = Operations.createRemoveOperation(addressAlias);
        execute(op);
    }

    protected void createCSFromScratchAndValidate(String csName, String jceksFilename, String csAlias, String csAliasValue)
        throws Exception {
        createCSFromScratchAndWriteAlias(csName, jceksFilename, csAlias, csAliasValue);
        assertCSOverServlet(csName, csAlias, csAliasValue);
    }

    protected void assertCSOverServlet(String credentialStoreName, String csAlias, String csAliasValue) throws Exception {
        List<Pair<String, String>> params = new ArrayList<>();
        params.add(Pair.of(CREDSTORE_NAME, credentialStoreName));
        params.add(Pair.of(CREDSTORE_ALIAS, csAlias));

        HttpResponse response = performCall("simple", params);
        if (HttpStatus.SC_OK != getStatusCode(response)) {
            Assert.fail(getContent(response));
        }
        String responseContent = getContent(response);
        Properties props = processServletResult(responseContent);

        assertPropertyValue(responseContent, props, csAlias, csAliasValue);
    }

    protected void assertNumberOfRecords(String credentialStoreName, int numberOfRecords) throws Exception {
        List<Pair<String, String>> params = new ArrayList<>();
        params.add(Pair.of(NUMBER_OF_RECORDS_CS_NAME, credentialStoreName));
        params.add(Pair.of(NUMBER_OF_RECORDS_CS, ""));

        HttpResponse response = performCall("simple", params);
        if (HttpStatus.SC_OK != getStatusCode(response)) {
            Assert.fail(getContent(response));
        }
        Properties props = processServletResult(getContent(response));
        assertPropertyValue(props, NUMBER_OF_RECORDS_CS, numberOfRecords);
    }

    protected void assertKeyStoreValue(String keystoreName, String alias) throws Exception {
        List<Pair<String, String>> params = new ArrayList<>();
        params.add(Pair.of(KEYSTORE_NAME, keystoreName));
        params.add(Pair.of(KEYSTORE_ALIAS, alias));

        HttpResponse response = performCall("simple", params);
        if (HttpStatus.SC_OK != getStatusCode(response)) {
            Assert.fail(getContent(response));
        }
        String content = getContent(response);
        Properties props = processServletResult(content);
        String property = props.getProperty(KEYSTORE_ACCESS_ALLOWED);
        Assert.assertEquals("Whole response: [" + content + "]\n", Boolean.TRUE.toString(), property);
    }

    private void assertPropertyValue(String responseContent, Properties props, String propertyKey, String expectedValue) {
        String property = props.getProperty(propertyKey);
        Assert.assertEquals("Property must have same value as is expected. Response was: " + responseContent, expectedValue,
            property);
    }

    private void assertPropertyValue(Properties props, String propertyKey, int expectedValue) {
        String propertyVal = props.getProperty(propertyKey);
        Assert.assertNotNull(propertyVal);
        Assert.assertEquals(expectedValue, Integer.parseInt(propertyVal));
    }

    protected static ModelNode execute(final ModelNode op) throws IOException {
        final ModelNode result = managementClient.getControllerClient().execute(op);
        if (!Operations.isSuccessfulOutcome(result)) {
            Assert.fail(Operations.getFailureDescription(result).toString());
        }
        return result;
    }

    private Properties processServletResult(String result) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(result));
        return properties;
    }

    private HttpResponse performCall(String urlPattern, List<Pair<String, String>> params) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(servletUrl.toURI());
        builder.setPath(servletUrl.getPath() + urlPattern);
        for (Pair<String, String> param : params) {
            builder.setParameter(param.getKey(), param.getValue());
        }
        URI uri = builder.build();
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(uri);
            return client.execute(request);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();

    }

    private String getContent(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
            result.append("\n");
        }
        return result.toString();

    }

    protected ModelNode createCredentialStoreAddress(String credentialStoreName) {
        if (StringUtils.isEmpty(credentialStoreName)) {
            throw new IllegalArgumentException(credentialStoreName + " is required");
        }
        return Operations.createAddress(SUBSYSTEM, "elytron", "credential-store", credentialStoreName);
    }

    private ModelNode createAddCredentialStoreOperation(ModelNode addressCS, String csFileName, CredentialStoreParams params) {
        if (StringUtils.isEmpty(csFileName)) {
            throw new IllegalArgumentException(csFileName + " is required.");
        }
        if (params.getCredentialReference() == null) {
            throw new IllegalArgumentException("CredentialReference is required.");
        }

        Map<String, String> uriParams = params.getUriParams();
        uriParams.put("location", "/tmp/test.jceks");
        StringBuilder uriParam = new StringBuilder("cr-store://test/");
        uriParam.append(csFileName);
        if (!uriParams.isEmpty()) {
            uriParam.append("?");
        }
        for (Entry<String, String> param : uriParams.entrySet()) {
            if (param.getValue() != null) {
                if (uriParam.charAt(uriParam.length() - 1) != '?') {
                    uriParam.append(";");
                }
                uriParam.append(param.getKey()).append("=").append(param.getValue());
            }
        }

        ModelNode op = Operations.createAddOperation(addressCS);
        op.get("uri").set(uriParam.toString());
        if (params.getProvider() != null) {
            op.get("provider").set(params.getProvider());
        }
        if (params.getProviderLoader() != null) {
            op.get("provider-loader").set(params.getProviderLoader());
        }
        if (params.getType() != null) {
            op.get("type").set(params.getType());
        }
        if (params.getRelativeTo() != null) {
            op.get("relative-to").set(params.getRelativeTo());
        } else {
            op.get("relative-to").set("jboss.server.data.dir");
        }
        if (params.getCredentialReference() != null) {
            ModelNode credentialRefParams = new ModelNode();
            if (StringUtils.isNotBlank(params.getCredentialReference().getClearText())) {
                credentialRefParams.get("clear-text").set(params.getCredentialReference().getClearText());
            }
            if (StringUtils.isNotBlank(params.getCredentialReference().getType())) {
                credentialRefParams.get("type").set(params.getCredentialReference().getType());
            }
            if (StringUtils.isNotBlank(params.getCredentialReference().getAlias())) {
                credentialRefParams.get("alias").set(params.getCredentialReference().getAlias());
            }
            if (StringUtils.isNotBlank(params.getCredentialReference().getStore())) {
                credentialRefParams.get("store").set(params.getCredentialReference().getStore());
            }
            op.get("credential-reference").set(credentialRefParams);
        }

        return op;
    }

    protected ModelNode createKeystoreAddress(String keystoreName) {
        return Operations.createAddress(SUBSYSTEM, "elytron", "key-store", keystoreName);
    }

    protected ModelNode createAddKeystoreOperation(ModelNode address, ModelNode credentialRefParams) {
        ModelNode op = Operations.createAddOperation(address);
        op.get("path").set(FIREFLY_KS);
        op.get("relative-to").set("jboss.server.data.dir");
        op.get("type").set("JKS");
        op.get("credential-reference").set(credentialRefParams);
        return op;
    }

    protected void createKeyStore(String name, String clearPassword) throws IOException {
        ModelNode addressKS = createKeystoreAddress(name);
        ModelNode params = new ModelNode();
        params.get("clear-text").set(clearPassword);
        ModelNode op = createAddKeystoreOperation(addressKS, params);
        execute(op);
    }

    protected ModelNode createUpdateKeystoreCredRefOperation(ModelNode address, ModelNode credentialRefParams) {
        ModelNode op = Operations.createWriteAttributeOperation(address, "credential-reference", credentialRefParams);
        // https://issues.jboss.org/browse/WFCORE-1953
        // https://issues.jboss.org/browse/WFLY-7512
        ModelNode allowrestartservice = new ModelNode();
        allowrestartservice.get("allow-resource-service-restart").set(true);
        op.get("operation-headers").set(allowrestartservice);
        return op;
    }

    protected ModelNode createCredRefParams(String store, String alias) {
        ModelNode params = new ModelNode();
        params.get("store").set(store);
        params.get("alias").set(alias);
        return params;
    }

    protected ModelNode createCSAliasAddress(String credentialStoreName, String csAlias) {
        return Operations.createAddress(SUBSYSTEM, "elytron", "credential-store", credentialStoreName, "alias", csAlias);
    }

    protected ModelNode createAddCSAliasOperation(ModelNode addressAlias, String secretValue) {
        ModelNode op = Operations.createAddOperation(addressAlias);
        op.get("secret-value").set(secretValue);
        return op;
    }

    protected ModelNode createAddCSAliasOperation(ModelNode addressAlias, String secretValue, String entryType) {
        ModelNode op = Operations.createAddOperation(addressAlias);
        op.get("secret-value").set(secretValue);
        op.get("entry-type").set(entryType);
        return op;
    }

    protected ModelNode createReadChildNamesOperation(String childType) {
        ModelNode addr = Operations.createAddress(SUBSYSTEM, "elytron");
        ModelNode op = new ModelNode();
        op.get(OP).set(ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION);
        op.get(OP_ADDR).set(addr);
        op.get(ModelDescriptionConstants.CHILD_TYPE).set(childType);
        return op;
    }

    protected static boolean isExist(ModelNode address) throws IOException, MgmtOperationException {
        final ModelNode operation = new ModelNode();
        operation.get(ModelDescriptionConstants.OP).set(ModelDescriptionConstants.READ_RESOURCE_OPERATION);
        operation.get(ModelDescriptionConstants.OP_ADDR).set(address);

        ModelNode opResult = managementClient.getControllerClient().execute(operation);
        return ModelDescriptionConstants.SUCCESS.equals(opResult.get(ModelDescriptionConstants.OUTCOME).asString());
    }

    protected void multi(int CS_NUM, int THREAD_NUM, int ALIAS_NUM, String baseCSName, String csFilename) throws Exception {
        if (CS_NUM > 1) {
            for (int i = 0; i < CS_NUM; i++) {
                String generatedCSName = baseCSName + i;
                createCS(generatedCSName, csFilename);
            }
        }

        Callable<Object> task = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                String suffix = "";
                if (CS_NUM > 1) {
                    suffix = "" + ThreadLocalRandom.current().nextInt(0, CS_NUM);
                }
                writeAlias(baseCSName + suffix, getStringWithRandomSuffix("alias").toLowerCase(),
                    getStringWithRandomSuffix("secret"));
                return null;
            }
        };

        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUM);
        try {
            final List<Future<Object>> tasks = new ArrayList<Future<Object>>();
            for (int i = 0; i < ALIAS_NUM; i++) {
                tasks.add(executor.submit(task));
            }

            // wait for completion...
            for (Future<Object> result : tasks) {
                result.get();
            }
        } finally {
            executor.shutdown();
        }
    }

    protected void reload() throws Exception {
        ServerReload.executeReloadAndWaitForCompletion(managementClient.getControllerClient(), 50000);
    }

    protected static String buildExternalCommand(final String extOption, final String delimiter, final String argument) {
        Path passSH = Paths.get(JBOSS_DATA_DIR, "pass.sh");
        try {
            PrintWriter writer = new PrintWriter(passSH.toString(), "UTF-8");
            writer.println("#!/bin/sh");
            writer.println("echo $1");
            writer.close();
        } catch (IOException e) {
            // do something
        }
        passSH.toFile().setExecutable(true);
        return extOption + passSH.toString() + (argument != null ? delimiter + argument : "");
    }

    protected void externalCredentialTestSequence(final String csFilename, final String masterCredentialCommand,
        boolean createStorage, final Map<String, String> additionalAttributes)
        throws Exception {

        CredentialStoreParams params = new CredentialStoreParamBuilder()
            .credentialReferenceClearText(masterCredentialCommand)
            .credentialReferenceType("COMMAND")
            .createStorage(createStorage)
            .additionalParams(additionalAttributes)
            .build();

        String csName = getStringWithRandomSuffix("externalCredentialTestSequence");
        createCSAndWriteAlias(csName, csFilename, "csalias", "Elytron", params);
        assertCSOverServlet(csName, "csalias", "Elytron");
    }
}