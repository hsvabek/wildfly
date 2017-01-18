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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Hynek Švábek <hsvabek@redhat.com>
 *
 */
public class CredentialStoreParams {

    private final Map<String, String> uriParams;
    private final String provider;
    private final String providerLoader;
    private final String type;
    private final CredentialReference credentialReference;
    private final String relativeTo;


    protected CredentialStoreParams(CredentialStoreParamBuilder builder) {
        uriParams = new HashMap<>();

        if (builder.createStorage != null) {
            uriParams.put("create.storage", builder.createStorage.toString());
        }
        if (builder.modifiable != null) {
            uriParams.put("modifiable", builder.modifiable.toString());
        }
        if (builder.additionalParams != null) {
            uriParams.putAll(builder.additionalParams);
        }

        this.provider = builder.provider;
        this.providerLoader = builder.providerLoader;
        this.type = builder.type;
        this.relativeTo = builder.relativeTo;
        //credential reference
        this.credentialReference = new CredentialReference(builder.credentialReferenceClearText,
            builder.credentialReferenceType,
            builder.credentialReferenceAlias, builder.credentialReferenceStore);
    }

    public Map<String, String> getUriParams() {
        return uriParams;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderLoader() {
        return providerLoader;
    }

    public String getType() {
        return type;
    }

    public CredentialReference getCredentialReference() {
        return credentialReference;
    }

    public String getRelativeTo() {
        return relativeTo;
    }

    public final class CredentialReference {

        private final String clearText;
        private final String type;
        private final String alias;
        private final String store;

        public CredentialReference(String clearText, String type, String alias, String store) {
            super();
            this.clearText = clearText;
            this.type = type;
            this.alias = alias;
            this.store = store;
        }

        public String getClearText() {
            return clearText;
        }

        public String getType() {
            return type;
        }

        public String getAlias() {
            return alias;
        }

        public String getStore() {
            return store;
        }
    }

    public static final class CredentialStoreParamBuilder {
        private String storePassword;
        private String keyPassword;;
        private Boolean createStorage;
        private String keyAlias;
        private Boolean modifiable;
        private String storeBase;
        // parameters
        private String provider;
        private String providerLoader;
        private String type;
        // CredentialReference
        private String credentialReferenceClearText;
        private String credentialReferenceType;
        private String credentialReferenceAlias;
        private String credentialReferenceStore;

        private String relativeTo;
        // additional parameters
        private Map<String, String> additionalParams;

        public CredentialStoreParamBuilder storePassword(String storePassword) {
            this.storePassword = storePassword;
            return this;
        }

        public CredentialStoreParamBuilder keyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
            return this;
        }

        public CredentialStoreParamBuilder createStorage(Boolean createStorage) {
            this.createStorage = createStorage;
            return this;
        }

        public CredentialStoreParamBuilder keyAlias(String keyAlias) {
            this.keyAlias = keyAlias;
            return this;
        }

        public CredentialStoreParamBuilder modifiable(Boolean modifiable) {
            this.modifiable = modifiable;
            return this;
        }

        public CredentialStoreParamBuilder storeBase(String storeBase) {
            this.storeBase = storeBase;
            return this;
        }

        public CredentialStoreParamBuilder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public CredentialStoreParamBuilder providerLoader(String providerLoader) {
            this.providerLoader = providerLoader;
            return this;
        }

        public CredentialStoreParamBuilder type(String type) {
            this.type = type;
            return this;
        }

        public CredentialStoreParamBuilder credentialReferenceClearText(String clearText) {
            this.credentialReferenceClearText = clearText;
            return this;
        }

        public CredentialStoreParamBuilder credentialReferenceType(String type) {
            this.credentialReferenceType = type;
            return this;
        }

        public CredentialStoreParamBuilder credentialReferenceAlias(String alias) {
            this.credentialReferenceAlias = alias;
            return this;
        }

        public CredentialStoreParamBuilder credentialReferenceStore(String store) {
            this.credentialReferenceStore = store;
            return this;
        }

        public CredentialStoreParamBuilder relativeTo(String relativeTo) {
            this.relativeTo = relativeTo;
            return this;
        }

        public CredentialStoreParamBuilder additionalParams(Map<String, String> additionalParams) {
            this.additionalParams = additionalParams;
            return this;
        }

        public CredentialStoreParams build() {
            return new CredentialStoreParams(this);
        }
    }

}
