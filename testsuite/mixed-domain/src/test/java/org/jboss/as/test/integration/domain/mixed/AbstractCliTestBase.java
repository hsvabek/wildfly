package org.jboss.as.test.integration.domain.mixed;

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.as.test.integration.common.HttpRequest;
import org.jboss.as.test.integration.domain.management.util.DomainTestSupport;
import org.jboss.as.test.integration.domain.mixed.util.CLIWrapper;


/**
 *
 * @author Dominik Pospisil <dpospisi@redhat.com>
 */
public class AbstractCliTestBase {
    public static final Map<String, String[]> hostServers = new HashMap<String, String[]>();
    public static final Map<String, String> hostAddresses = new HashMap<String, String>();
    public static final Map<String, String[]> serverGroups = new HashMap<String, String[]>();
    public static final Map<String, Integer> portOffsets = new HashMap<String, Integer>();
    public static final Map<String, String[]> serverProfiles = new HashMap<String, String[]>();
    public static final Map<String, Boolean> serverStatus = new HashMap<String, Boolean>();
    
    
    protected static volatile MixedDomainTestSupport support;
    protected static Version.AsVersion version;
    
    
    public static final long WAIT_TIMEOUT = 30000;
    public static final long WAIT_LINETIMEOUT = 1500;
    protected static CLIWrapper cli;

    public static void initCLI() throws Exception {
        initCLI(true);
    }

    public static void initCLI(boolean connect) throws Exception {
        if (cli == null) {
            cli = new CLIWrapper(connect);
        }
    }

    public static void initCLI(String cliAddress) throws Exception {
        if (cli == null) {
            cli = new CLIWrapper(true, cliAddress);
        }
    }

    public static void closeCLI() throws Exception {
        try {
            if (cli != null) cli.quit();
        } finally {
            cli = null;
        }
    }

    protected final String getBaseURL(URL url) throws MalformedURLException {
        return new URL(url.getProtocol(), url.getHost(), url.getPort(), "/").toString();
    }

    protected boolean checkUndeployed(String spec) {
        try {
            final long firstTry = System.currentTimeMillis();
            HttpRequest.get(spec, 10, TimeUnit.SECONDS);
            while (System.currentTimeMillis() - firstTry <= 1000) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                } finally {
                    HttpRequest.get(spec, 10, TimeUnit.SECONDS);
                }
            }
            return false;
        } catch (Exception e) {
        }
        return true;
    }
    
    public static void initSuite() throws Exception {
//        start(CLITestSuite.class);

//        hostServers.put("master", new String[]{"main-one", "main-two", "other-one"});
//        hostServers.put("slave", new String[]{"main-three", "main-four", "other-two"});
        hostServers.put("slave", new String[]{"server-one"});

//        hostAddresses.put("master", DomainTestSupport.masterAddress);
        hostAddresses.put("slave", DomainTestSupport.slaveAddress);

        serverGroups.put("other-server-group", new String[]{"server-one"});

//        serverProfiles.put("default", new String[]{"main-server-group"});
        serverProfiles.put("full-ha", new String[]{"other-server-group"});

        portOffsets.put("server-one", 0);
//        portOffsets.put("main-two", 150);
//        portOffsets.put("other-one", 250);
//        portOffsets.put("main-three", 350);
//        portOffsets.put("main-four", 450);
//        portOffsets.put("other-two", 550);

        serverStatus.put("server-one", true);
//        serverStatus.put("main-two", false);
//        serverStatus.put("main-three", true);
//        serverStatus.put("main-four", false);
//        serverStatus.put("other-one", false);
//        serverStatus.put("other-two", true);

    }

    public static void addServer(String serverName, String hostName, String groupName, String profileName, int portOffset, boolean status) {
        LinkedList<String> hservers;
        if(hostServers.get(hostName) != null){
            hservers = new LinkedList<String>(Arrays.asList(hostServers.get(hostName)));
        }else{
            hservers = new LinkedList<String>();
        }
        hservers.add(serverName);
        hostServers.put(hostName, hservers.toArray(new String[hservers.size()]));

        LinkedList<String> gservers = new LinkedList<String>();
        if (serverGroups.containsKey(groupName)) {
            gservers.addAll(Arrays.asList(serverGroups.get(groupName)));
        }
        gservers.add(serverName);
        serverGroups.put(groupName, gservers.toArray(new String[gservers.size()]));

        LinkedList<String> pgroups = new LinkedList<String>();
        if (serverProfiles.containsKey(profileName)) {
            pgroups.addAll(Arrays.asList(serverProfiles.get(profileName)));
        }
        pgroups.add(groupName);
        serverProfiles.put(profileName, pgroups.toArray(new String[pgroups.size()]));

        portOffsets.put(serverName, portOffset);
        serverStatus.put(serverName, status);
    }

    public static String getServerHost(String serverName) {
        for(Map.Entry<String,String[]> entry : hostServers.entrySet()) {
            if (Arrays.asList(entry.getValue()).contains(serverName)) { return entry.getKey(); }
        }
        return null;
    }
}
