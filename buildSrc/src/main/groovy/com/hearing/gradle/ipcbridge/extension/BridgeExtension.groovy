package com.hearing.gradle.ipcbridge.extension

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

class BridgeExtension {
    NamedDomainObjectContainer<ProviderConfig> providerConfigs

    BridgeExtension(Project project) {
        providerConfigs = project.container(ProviderConfig)
    }

    void providerConfigs(Action<NamedDomainObjectContainer<ProviderConfig>> action) {
        action.execute(providerConfigs)
    }
}
