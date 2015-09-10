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

package org.jboss.as.test.integration.domain.mixed.eap7x;

import org.jboss.as.test.integration.domain.mixed.MixedDomainTestSuite;
import org.jboss.as.test.integration.domain.mixed.Version;
import org.jboss.as.test.integration.domain.mixed.Version.AsVersion;
import org.jboss.as.test.integration.domain.mixed.eap7x.Datasource7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.DefaultConfigSmoke7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.DeploymentOverlay7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.ExpressionSupportSmoke7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.MixedDomainDeployment7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.OrderedChildResources7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.ReadEnvironmentVariables7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.SimpleMixedDomain7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.SlaveHostControllerAuthentication7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.cli.DataSourceCli7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.cli.DeployAllServerGroups7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.cli.DeploySingleServerGroup7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.cli.DomainDeployWithRuntimeName7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.cli.DomainDeploymentOverlay7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.cli.Jms7xTestCase;
import org.jboss.as.test.integration.domain.mixed.eap7x.cli.RolloutPlan7xTestCase;
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
        SimpleMixedDomain7xTestCase.class,
        MixedDomainDeployment7xTestCase.class,
        DataSourceCli7xTestCase.class,
        DeployAllServerGroups7xTestCase.class,
        DeploySingleServerGroup7xTestCase.class,
        DomainDeploymentOverlay7xTestCase.class,
        DomainDeployWithRuntimeName7xTestCase.class,
        Jms7xTestCase.class,
        RolloutPlan7xTestCase.class,
        Datasource7xTestCase.class,
        DefaultConfigSmoke7xTestCase.class,
        DeploymentOverlay7xTestCase.class,
        ExpressionSupportSmoke7xTestCase.class,
        OrderedChildResources7xTestCase.class,
        ReadEnvironmentVariables7xTestCase.class,
        SlaveHostControllerAuthentication7xTestCase.class
})
@Version(AsVersion.EAP_7_0_0)
public class MixedDomain7xTestSuite extends MixedDomainTestSuite {

    @BeforeClass
    public static void initializeDomain() {
        MixedDomainTestSuite.getSupport(MixedDomain7xTestSuite.class);
    }
}
