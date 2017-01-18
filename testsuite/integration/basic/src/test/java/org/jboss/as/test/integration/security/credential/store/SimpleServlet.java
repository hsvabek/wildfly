/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
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
import java.io.PrintWriter;
import java.security.KeyStore;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.credential.store.CredentialStore;
import org.wildfly.security.credential.store.CredentialStoreException;
import org.wildfly.security.password.interfaces.ClearPassword;

/**
 *
 * @author Hynek Švábek <hsvabek@redhat.com>
 *
 */
@WebServlet(name = "SimpleServlet", urlPatterns = { "/simple" })
public class SimpleServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String KEYSTORE_NAME = "keystoreName";
    public static final String KEYSTORE_ALIAS = "keystoreAlias";
    public static final String CREDSTORE_NAME = "credstoreName";
    public static final String CREDSTORE_ALIAS = "credstoreAlias";
    public static final String KEYSTORE_ACCESS_ALLOWED = "keystoreAccessAllowed";
    public static final String NUMBER_OF_RECORDS_CS = "numberOfRecordsCS";
    public static final String NUMBER_OF_RECORDS_CS_NAME = "numberOfRecordsCSCredstoreName";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        final String keystoreName = req.getParameter(KEYSTORE_NAME);
        final String keystoreAlias = req.getParameter(KEYSTORE_ALIAS);
        final String credstoreName = req.getParameter(CREDSTORE_NAME);
        final String credstoreAlias = req.getParameter(CREDSTORE_ALIAS);
        final String numberOfRecordsCredStoreName = req.getParameter(NUMBER_OF_RECORDS_CS_NAME);

        ServiceRegistry registry = CurrentServiceContainer.getServiceContainer();
        if (req.getParameterMap().containsKey(NUMBER_OF_RECORDS_CS)) {
            if (StringUtils.isBlank(numberOfRecordsCredStoreName)) {
                writer.println("For getting CS number of aliases the numberOfRecordsCSCredstoreName param must be defined.");
                resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            } else {
                CredentialStore credStore = getCredentialStore(registry, numberOfRecordsCredStoreName);
                try {
                    int numberOfRecords = credStore.getAliases().size();
                    writer.println(NUMBER_OF_RECORDS_CS + "=" + numberOfRecords);
                } catch (UnsupportedOperationException | CredentialStoreException e) {
                    writer.println(e.toString());
                }
            }
        } else {
            if (StringUtils.isBlank(credstoreName) && StringUtils.isBlank(keystoreName)) {
                writer.println("Either CredentialStore or Keystore name must be defined.");
                resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            }
            if (StringUtils.isNotBlank(credstoreName) && StringUtils.isBlank(credstoreAlias)) {
                writer.println("Credential store alias must be defined.");
                resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            }

            if (HttpStatus.SC_OK == resp.getStatus()) {
                try {
                    if (StringUtils.isNotBlank(keystoreName)) {
                        writer.println("RESPONSE_KS");
                        KeyStore keystore = getKeyStore(registry, keystoreName);
                        try {
                            keystore.containsAlias(keystoreAlias);
                            writer.println(KEYSTORE_ACCESS_ALLOWED + "=" + true);
                        } catch (Exception e) {
                            writer.println(KEYSTORE_ACCESS_ALLOWED + "=" + false);
                            writer.println(e.toString());
                        }
                    }

                    if (StringUtils.isNotBlank(credstoreName)) {
                        writer.println("RESPONSE_CS");
                        CredentialStore credStore = getCredentialStore(registry, credstoreName);
                        boolean credStoreAliasExists = credStore.exists(credstoreAlias, PasswordCredential.class);
                        writer.println("alias " + credstoreAlias);
                        writer.println("exists " + credStoreAliasExists);
                        // writer.println(CREDSTORE_ALIAS_EXISTS + "=" + credStoreAliasExists);
                        if (credStoreAliasExists) {
                            ClearPassword cc = (ClearPassword) credStore.retrieve(credstoreAlias, PasswordCredential.class)
                                .getPassword();
                            writer.println(credstoreAlias + "=" + new String(cc.getPassword()));
                        }
                    }
                } catch (Exception e) {
                    writer.println(e.toString());
                }
            }
        }
        writer.println("RESPONSE_END");
        writer.close();
    }

    private KeyStore getKeyStore(ServiceRegistry registry, String keystoreName) {
        ServiceController<?> keystoreService = registry
            .getService(ServiceName.of("org", "wildfly", "security", "key-store", keystoreName));
        if (keystoreService != null && keystoreService.getValue() != null && keystoreService.getValue() instanceof KeyStore) {
            return (KeyStore) keystoreService.getValue();
        }
        throw new IllegalStateException("KeyStore [" + keystoreName + "] must exist.");
    }

    private CredentialStore getCredentialStore(ServiceRegistry registry, String credstoreName) {
        ServiceController<?> credStoreService = registry
            .getService(ServiceName.of("org", "wildfly", "security", "credential-store", credstoreName));
        if (credStoreService != null && credStoreService.getValue() instanceof CredentialStore) {
            return (CredentialStore) credStoreService.getValue();
        }
        throw new IllegalStateException(
            "CredentialStore [" + credstoreName + "] must exist.");
    }
}
