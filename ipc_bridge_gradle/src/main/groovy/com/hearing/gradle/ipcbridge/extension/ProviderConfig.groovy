package com.hearing.gradle.ipcbridge.extension

class ProviderConfig {
    String name
    String authorities
    String process
    boolean exported = false

    ProviderConfig(String name) {
        this.name = name
    }

    void authorities(String authorities) {
        this.authorities = authorities
    }

    void process(String process) {
        this.process = process
    }

    void exported(boolean exported) {
        this.exported = exported
    }

    @Override
    String toString() {
        return "name: $name, authorities: $authorities, process: $process, exported: $exported"
    }
}
