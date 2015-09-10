/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.test.integration.domain.management.util.DomainTestSupport.validateResponse;

import java.io.IOException;

import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.dmr.ModelNode;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * Basic tests of datasource support in a managed domain.
 *
 * @author Brian Stansberry (c) 2012 Red Hat Inc.
 */
public abstract class DatasourceTestCase {


    private static final ModelNode MAIN_RUNNING_SERVER_DS_ADDRESS = new ModelNode().add(HOST, "slave")
            .add(SERVER, "server-one").add(SUBSYSTEM, "datasources").add("data-source", "ExampleDS");

    static {
        MAIN_RUNNING_SERVER_DS_ADDRESS.protect();
    }
    
    protected static MixedDomainTestSupport support;

    public static void before(Class<?> testClass) throws Exception {
        support = MixedDomainTestSuite.getSupport(testClass);
    }

    @AfterClass
    public synchronized static void afterClass() {
        MixedDomainTestSuite.afterClass();
    }
    
    private DomainClient masterClient;

    @Test
    public void testDatasourceConnection() throws IOException {
        masterClient = support.getDomainMasterLifecycleUtil().createDomainClient();
        
        // AS7-6062 -- validate that  ExampleDS works on a domain server
        ModelNode response = masterClient.execute(getEmptyOperation("test-connection-in-pool", MAIN_RUNNING_SERVER_DS_ADDRESS));
        validateResponse(response);
    }

    private static ModelNode getEmptyOperation(String operationName, ModelNode address) {
        ModelNode op = new ModelNode();
        op.get(OP).set(operationName);
        op.get("statistics-enabled").set(true);
        if (address != null) {
            op.get(OP_ADDR).set(address);
        }
        else {
            // Just establish the standard structure; caller can fill in address later
            op.get(OP_ADDR).setEmptyList();
        }
        return op;
    }
}
