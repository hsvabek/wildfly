/*
* JBoss, Home of Professional Open Source.
* Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.as.test.integration.domain.mixed;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING_GROUP;
import static org.jboss.as.test.integration.domain.management.util.DomainTestSupport.validateResponse;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HTTP;
import org.jboss.as.arquillian.container.NetworkUtils;
import org.jboss.as.controller.client.helpers.domain.DeploymentPlan;
import org.jboss.as.controller.client.helpers.domain.DeploymentPlanResult;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.as.controller.client.helpers.domain.DomainDeploymentManager;
import org.jboss.as.test.integration.domain.management.util.DomainLifecycleUtil;
import org.jboss.as.test.integration.domain.management.util.DomainTestSupport;
import org.jboss.as.test.integration.domain.mixed.servlet.EnvironmentTestServlet;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.xnio.IoUtils;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public abstract class ReadEnvironmentVariablesTestCase {
    private static DomainTestSupport testSupport;
    private static DomainLifecycleUtil domainMasterLifecycleUtil;
    private static DomainLifecycleUtil domainSlaveLifecycleUtil;

    public static void before(Class<?> testClass) throws Exception {
        testSupport = MixedDomainTestSuite.getSupport(testClass); 
        domainMasterLifecycleUtil = testSupport.getDomainMasterLifecycleUtil();
        domainSlaveLifecycleUtil = testSupport.getDomainSlaveLifecycleUtil();
    }

    @AfterClass
    public static void tearDownDomain() throws Exception {
        MixedDomainTestSuite.afterClass();
        testSupport = null;
        domainMasterLifecycleUtil = null;
        domainSlaveLifecycleUtil = null;
    }

    @Test
    public void testReadEnvironmentVariablesForServers() throws Exception {
        DomainClient client = domainMasterLifecycleUtil.createDomainClient();
        DomainDeploymentManager manager = client.getDeploymentManager();

        try {
            //Deploy the archive
            WebArchive archive = ShrinkWrap.create(WebArchive.class, "env-test.war").addClass(EnvironmentTestServlet.class);
            archive.addAsResource(new StringAsset("Manifest-Version: 1.0\nDependencies: org.jboss.dmr \n"),"META-INF/MANIFEST.MF");

            final InputStream contents = archive.as(ZipExporter.class).exportAsInputStream();
            try {
                DeploymentPlan plan = manager.newDeploymentPlan().add("env-test.war", contents).deploy("env-test.war").toServerGroup("main-server-group").toServerGroup("other-server-group").build();
                DeploymentPlanResult result = manager.execute(plan).get();
                Assert.assertTrue(result.isValid());
            } finally {
                IoUtils.safeClose(contents);
            }

            Map<String, String> env = getEnvironmentVariables(client, "slave", "server-one", "full-ha-sockets");          
            checkEnvironmentVariable(env, "DOMAIN_TEST_SERVER", "server");
        } finally {
            DeploymentPlanResult result = manager.execute(manager.newDeploymentPlan().undeploy("env-test.war").build()).get();
            Assert.assertTrue(result.isValid());
            IoUtils.safeClose(client);
        }
    }

    private void checkEnvironmentVariable(Map<String, String> env, String name, String expected) {
        Assert.assertTrue(env.containsKey(name));
        Assert.assertEquals(expected, env.get(name));
    }

    private Map<String, String> getEnvironmentVariables(DomainClient client, String host, String server, String socketBindingGroup) throws Exception {
        ModelNode op = new ModelNode();
        op.get(OP).set(READ_RESOURCE_OPERATION);
        op.get(OP_ADDR).add(HOST, host).add(SERVER, server).add(SOCKET_BINDING_GROUP, socketBindingGroup).add(SOCKET_BINDING, "http");
        op.get(INCLUDE_RUNTIME).set(true);
        ModelNode socketBinding = validateResponse(client.execute(op));

        URL url = new URL("http",
                NetworkUtils.formatPossibleIpv6Address(socketBinding.get("bound-address").asString()),
                socketBinding.get("bound-port").asInt(),
                "/env-test/env");
        HttpGet get = new HttpGet(url.toURI());
        final HttpParams params = new SyncBasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEF_CONTENT_CHARSET.name());
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpClient httpClient = new DefaultHttpClient(params);
        HttpResponse response = httpClient.execute(get);
        ModelNode env = ModelNode.fromJSONStream(response.getEntity().getContent());
        Map<String, String> environment = new HashMap<String, String>();
        for (Property property : env.asPropertyList()) {
            environment.put(property.getName(), property.getValue().asProperty().getName());
        }
        return environment;
    }

}
