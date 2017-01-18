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

package org.jboss.as.test.integration.security.credential.store.custom;

import static org.wildfly.security._private.ElytronMessages.log;

import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.Map;

import org.wildfly.security.credential.Credential;
import org.wildfly.security.credential.store.CredentialStore;
import org.wildfly.security.credential.store.CredentialStore.ProtectionParameter;
import org.wildfly.security.credential.store.CredentialStoreException;
import org.wildfly.security.credential.store.CredentialStoreSpi;
import org.wildfly.security.credential.store.UnsupportedCredentialTypeException;

/**
 *
 * @author Hynek Švábek <hsvabek@redhat.com>
 *
 */
public class CustomCredentialStore extends CredentialStoreSpi {

    /**
     * Type of {@link CredentialStoreSpi} implementation. Will be used as algorithm name when registering service in
     * {@link org.jboss.as.test.integration.security.credential.store.custom.CustomElytronProvider}.
     */
    public static final String CUSTOM_KEY_STORE_PASSWORD_STORE = "CustomKeyStorePasswordStore";

    private Map<String, Credential> storage = new HashMap<>();

    @Override
    public void initialize(Map<String, String> attributes, ProtectionParameter protectionParameter) throws CredentialStoreException {
    }

    @Override
    public boolean isModifiable() {
        return true;
    }

    @Override
    public void store(String credentialAlias, Credential credential, CredentialStore.ProtectionParameter protectionParameter)
        throws CredentialStoreException, UnsupportedCredentialTypeException {
        storage.put(credentialAlias, credential);
    }

    @Override
    public <C extends Credential> C retrieve(String credentialAlias, Class<C> credentialType, String credentialAlgorithm,
        AlgorithmParameterSpec parameterSpec, ProtectionParameter protectionParameter)
        throws CredentialStoreException {
        Credential credential = storage.get(credentialAlias);
        if (credential != null) {
            return credentialType.cast(credential);
        } else {
            throw log.credentialAliasNotFoundNotFound(credentialAlias, CUSTOM_KEY_STORE_PASSWORD_STORE);
        }
    }

    @Override
    public void remove(String credentialAlias, Class<? extends Credential> credentialType, String credentialAlgorithm,
        AlgorithmParameterSpec parameterSpec) throws CredentialStoreException {
        storage.remove(credentialAlias);
    }
}