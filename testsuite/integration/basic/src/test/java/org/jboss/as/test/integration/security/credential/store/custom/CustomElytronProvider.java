/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.test.integration.security.credential.store.custom;

import java.security.Provider;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.wildfly.security.credential.store.CredentialStore;

public class CustomElytronProvider extends Provider {


    /**
     *
     */
    private static final long serialVersionUID = 1267016094996625988L;

    /**
     * Default constructor for this security provider.
     */
    public CustomElytronProvider() {
        super("CustomWildFlyElytron", 1.0D, "Custom WildFly Elytron Provider");
        putCustomCredentialStoreProviderImplementations();
    }

    private void putCustomCredentialStoreProviderImplementations() {
        final List<String> emptyList = Collections.emptyList();
        final Map<String, String> emptyMap = Collections.emptyMap();
        putService(new Service(this, CredentialStore.CREDENTIAL_STORE_TYPE, CustomCredentialStore.CUSTOM_KEY_STORE_PASSWORD_STORE, CustomCredentialStore.class.getName(), emptyList, emptyMap));
    }
}
