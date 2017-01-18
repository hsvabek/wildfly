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

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.test.integration.management.ManagementOperations;
import org.jboss.as.test.integration.management.util.MgmtOperationException;
import org.jboss.as.test.shared.ServerReload;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * NOTE: This setting is needed until elytron will not be default security subsystem. There are some conditions which disable
 * this settings.
 *
 * @author Hynek Švábek <hsvabek@redhat.com>
 *
 */
public class CredentialStoreServerSetupTask implements ServerSetupTask {

    private static Logger LOGGER = Logger.getLogger(CredentialStoreServerSetupTask.class);

    private boolean removeElytronExtension = false;
    private boolean removeElytronSubsystem = false;
    final ModelNode extAddress;
    final ModelNode subAddress;

    public CredentialStoreServerSetupTask() {
        extAddress = new ModelNode();
        extAddress.add("extension", "org.wildfly.extension.elytron");
        extAddress.protect();

        subAddress = new ModelNode();
        subAddress.add("subsystem", "elytron");
        subAddress.protect();
    }

    @Override
    public final void setup(final ManagementClient managementClient, String containerId) throws Exception {
        // Elytron extension
        if (addResourceIfDoesntExist(managementClient, extAddress)) {
            removeElytronExtension = true;
        }

        // Elytron subsystem
        if (addResourceIfDoesntExist(managementClient, subAddress)) {
            removeElytronSubsystem = true;
            // reload the server
            LOGGER.debug("Reloading the server");
            reload(managementClient);
        }
    }

    @Override
    public void tearDown(final ManagementClient managementClient, String containerId) throws Exception {
        if (removeElytronSubsystem) {
            // removeResource(managementClient, subAddress);
        }

        if (removeElytronExtension) {
            // removeResource(managementClient, extAddress);
        }

        if (removeElytronSubsystem || removeElytronExtension) {
            reload(managementClient);
        }
    }

    /**
     * Provide reload operation on the server
     *
     * @throws Exception
     */
    private static void reload(final ManagementClient managementClient) throws Exception {
        ServerReload.executeReloadAndWaitForCompletion(managementClient.getControllerClient());
    }

    /**
     * @param managementClient
     * @param address
     * @return boolean value if resource was created
     * @throws IOException
     * @throws MgmtOperationException
     */
    private boolean addResourceIfDoesntExist(final ManagementClient managementClient, ModelNode address)
        throws IOException, MgmtOperationException {
        if (!isExist(managementClient, address)) {
            executeOperation(managementClient, address, ModelDescriptionConstants.ADD);
            return true;
        }
        return false;
    }

    private boolean removeResource(final ManagementClient managementClient, ModelNode address)
        throws IOException, MgmtOperationException {
        if (isExist(managementClient, address)) {
            executeOperation(managementClient, address, ModelDescriptionConstants.REMOVE);
            return true;
        }
        return false;
    }

    private void executeOperation(final ManagementClient managementClient, ModelNode address, String op)
        throws IOException, MgmtOperationException {
        final ModelNode addExtensionOp = new ModelNode();
        addExtensionOp.get(ModelDescriptionConstants.OP).set(op);
        addExtensionOp.get(ModelDescriptionConstants.OP_ADDR).set(address);

        ManagementOperations.executeOperation(managementClient.getControllerClient(), addExtensionOp);
    }

    private boolean isExist(final ManagementClient managementClient, ModelNode address)
        throws IOException, MgmtOperationException {
        final ModelNode operation = new ModelNode();
        operation.get(ModelDescriptionConstants.OP).set(ModelDescriptionConstants.READ_RESOURCE_OPERATION);
        operation.get(ModelDescriptionConstants.OP_ADDR).set(address);

        ModelNode opResult = managementClient.getControllerClient().execute(operation);
        return ModelDescriptionConstants.SUCCESS.equals(opResult.get(ModelDescriptionConstants.OUTCOME).asString());
    }
}
