/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.as.test.integration.domain.mixed.eap640;

import org.jboss.as.test.integration.domain.mixed.MixedDomainTestSuite;
import org.jboss.as.test.integration.domain.mixed.Version;
import org.jboss.as.test.integration.domain.mixed.Version.AsVersion;
import org.jboss.as.test.integration.domain.mixed.eap640.Datasource640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.DefaultConfigSmoke640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.DeploymentOverlay640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.ExpressionSupportSmoke640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.MixedDomainDeployment640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.OrderedChildResources640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.ReadEnvironmentVariables640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.SimpleMixedDomain640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.SlaveHostControllerAuthentication640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.cli.DataSourceCli640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.cli.DeployAllServerGroups640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.cli.DeploySingleServerGroup640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.cli.DomainDeployWithRuntimeName640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.cli.DomainDeploymentOverlay640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.cli.Jms640TestCase;
import org.jboss.as.test.integration.domain.mixed.eap640.cli.RolloutPlan640TestCase;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
@RunWith(Suite.class)
@SuiteClasses(value= {
        SimpleMixedDomain640TestCase.class,
        MixedDomainDeployment640TestCase.class,
        DataSourceCli640TestCase.class,
        DeployAllServerGroups640TestCase.class,
        DeploySingleServerGroup640TestCase.class,
        DomainDeploymentOverlay640TestCase.class,
        DomainDeployWithRuntimeName640TestCase.class,
        Jms640TestCase.class,
        RolloutPlan640TestCase.class,
        Datasource640TestCase.class,
        DefaultConfigSmoke640TestCase.class,
        DeploymentOverlay640TestCase.class,
        ExpressionSupportSmoke640TestCase.class,
        OrderedChildResources640TestCase.class,
        ReadEnvironmentVariables640TestCase.class,
        SlaveHostControllerAuthentication640TestCase.class
})
@Version(AsVersion.EAP_6_4_0)
public class MixedDomain640TestSuite extends MixedDomainTestSuite {

    @BeforeClass
    public static void initializeDomain() {
        MixedDomainTestSuite.getSupport(MixedDomain640TestSuite.class);
    }
}
