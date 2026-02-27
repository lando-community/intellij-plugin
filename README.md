# Lando Plugin for JetBrains IDEs

![Build](https://github.com/lando-community/intellij-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

<!-- Plugin description -->
This is a plugin for JetBrains IDEs that provides integration with Lando. It allows you to manage your Lando projects directly from the IDE.

- The plugin is currently in development and is not yet available in the JetBrains Plugin Repository.
- The plugin is not yet functional.
- The plugin only works with projects that have a `.lando.yml` file in the project root.
- The plugin only works with a single Lando instance per project.

## Features

An initial release will be published when the following features are implemented:
- [x] Automatic detection of Lando apps in the current project
- [x] Embedded terminal integration for Lando commands
- [ ] Execute arbitrary Lando commands from the user interface
- [ ] Status monitoring for the current Lando app
- [ ] Quick access to start, stop, and restart the current Lando app
- [ ] Quick access to the current Lando app's URL (copy or open in browser)
- [ ] Quick access to Lando logs
- [ ] Syntax validation and hints for `.lando.yml` files

Planned features for subsequent releases:
- [ ] Automatic detection of available Lando commands and tooling
- [ ] Automatic Xdebug configuration for Lando apps
- [ ] Remote php interpreter (VSCode uses the PHP version from the Lando app)
- [ ] Automaticly apply settings to VSCode and its extensions based on the Lando app details
- [ ] Lando app creation wizard (GUI for lando init)
- [ ] Lando app management from the command palette
- [ ] Easy access to Lando documentation
- [ ] Execute relevant Lando commands from the context menu
- [ ] Easily add, remove, and configure services
- [ ] Parity with all Lando CLI features
- [ ] Support for editing Lando's global configuration
- [ ] Built-in plugin manager for Lando
- [ ] Full GUI integration of all Lando features
- [ ] Integration with other Lando tools and plugins (e.g. Pantheon, Acquia, Drupal, etc.)
- [ ] Detection of common Lando issues and suggestions for resolution
- [ ] Lando app templates and presets
- [ ] Advanced filtering and searching of Lando logs


<!-- Plugin description end -->

## Limitations