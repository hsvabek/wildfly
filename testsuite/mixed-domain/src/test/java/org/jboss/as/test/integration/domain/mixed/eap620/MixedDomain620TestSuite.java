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
package org.jboss.as.test.integration.domain.mixed.eap620;

import org.jboss.as.test.integration.domain.mixed.MixedDomainTestSuite;
import org.jboss.as.test.integration.domain.mixed.Version;
import org.jboss.as.test.integration.domain.mixed.Version.AsVersion;
import org.jboss.as.test.integration.domain.mixed.eap620.cli.DataSourceCli620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.cli.DeployAllServerGroups620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.cli.DeploySingleServerGroup620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.cli.DomainDeployWithRuntimeName620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.cli.DomainDeploymentOverlay620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.cli.Jms620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.cli.RolloutPlan620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.Datasource620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.DefaultConfigSmoke620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.DeploymentOverlay620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.ExpressionSupportSmoke620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.OrderedChildResources620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.ReadEnvironmentVariables620TestCase;
import org.jboss.as.test.integration.domain.mixed.eap620.SlaveHostControllerAuthentication620TestCase;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
@RunWith(Suite.class)
@SuiteClasses(value= {SimpleMixedDomain620TestCase.class,
        MixedDomainDeployment620TestCase.class,
        DataSourceCli620TestCase.class,
        DeployAllServerGroups620TestCase.class,
        DeploySingleServerGroup620TestCase.class,
        DomainDeploymentOverlay620TestCase.class,
        DomainDeployWithRuntimeName620TestCase.class,
        Jms620TestCase.class,
        RolloutPlan620TestCase.class,
        Datasource620TestCase.class,
        DefaultConfigSmoke620TestCase.class,
        DeploymentOverlay620TestCase.class,
        ExpressionSupportSmoke620TestCase.class,
        OrderedChildResources620TestCase.class,
        ReadEnvironmentVariables620TestCase.class,
        SlaveHostControllerAuthentication620TestCase.class
        })
@Version(AsVersion.EAP_6_2_0)
public class MixedDomain620TestSuite extends MixedDomainTestSuite {

    @BeforeClass
    public static void initializeDomain() {
        MixedDomainTestSuite.getSupport(MixedDomain620TestSuite.class);
    }
}
