/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

package org.wildfly.clustering.server.group;

import java.util.Collection;
import java.util.Collections;

import org.jboss.as.controller.capability.CapabilityServiceSupport;
import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.service.AliasServiceBuilder;
import org.wildfly.clustering.service.Builder;
import org.wildfly.clustering.spi.CacheGroupServiceName;

/**
 * @author Paul Ferraro
 */
public class CacheGroupAliasBuilderProvider implements org.wildfly.clustering.spi.CacheGroupAliasBuilderProvider {

    @Override
    public Collection<Builder<?>> getBuilders(CapabilityServiceSupport support, String containerName, String aliasCacheName, String targetCacheName) {
        return Collections.<Builder<?>>singleton(new AliasServiceBuilder<>(CacheGroupServiceName.GROUP.getServiceName(containerName, aliasCacheName), CacheGroupServiceName.GROUP.getServiceName(containerName, targetCacheName), Group.class));
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
