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

package org.jboss.as.test.integration.domain.mixed.eap630;

import org.jboss.as.test.integration.domain.mixed.MixedDomainTestSuite;
import org.jboss.as.test.integration.domain.mixed.Version;
import org.jboss.as.test.integration.domain.mixed.Version.AsVersion;
import org.jboss.as.test.integration.domain.mixed.eap630.cli.DataSourceCli630TestCase;
import org.jboss.as.test.integration.domain.mixed.eap630.cli.DeployAllServerGroups630TestCase;
import org.jboss.as.test.integration.domain.mixed.eap630.cli.DeploySingleServerGroup630TestCase;
import org.jboss.as.test.integration.domain.mixed.eap630.cli.DomainDeployWithRuntimeName630TestCase;
import org.jboss.as.test.integration.domain.mixed.eap630.cli.DomainDeploymentOverlay630TestCase;
import org.jboss.as.test.integration.domain.mixed.eap630.cli.RolloutPlan630TestCase;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
@RunWith(Suite.class)
@SuiteClasses(value= {
        SimpleMixedDomain630TestCase.class,
        MixedDomainDeployment630TestCase.class,
        DataSourceCli630TestCase.class,
        DeployAllServerGroups630TestCase.class,
        DeploySingleServerGroup630TestCase.class,
        DomainDeploymentOverlay630TestCase.class,
        DomainDeployWithRuntimeName630TestCase.class,
        RolloutPlan630TestCase.class,
        Datasource630TestCase.class,
        DefaultConfigSmoke630TestCase.class,
        DeploymentOverlay630TestCase.class,
        ExpressionSupportSmoke630TestCase.class,
        OrderedChildResources630TestCase.class,
        ReadEnvironmentVariables630TestCase.class,
        SlaveHostControllerAuthentication630TestCase.class
})
@Version(AsVersion.EAP_6_3_0)
public class MixedDomain630TestSuite extends MixedDomainTestSuite {

    public static void initializeDomain() {
        MixedDomainTestSuite.getSupport(MixedDomain630TestSuite.class);
    }
}
