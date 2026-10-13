# Central Publishing Maven Plugin

<!-- Badges Section -->
[![shield_license]][license_file]
![Maven Central Version](https://img.shields.io/maven-central/v/org.sonatype.central/central-publishing-maven-plugin)


---

A Maven plugin for publishing components to [Maven Central](https://central.sonatype.com) through the Central Portal.
It bundles the artifacts of a build, uploads the bundle to the Central Portal and, optionally, waits for it to be
validated or published. For full usage documentation see
[Publish via the Central Portal Maven plugin](https://central.sonatype.org/publish/publish-portal-maven).

- [Usage](#usage)
- [Compatibility](#compatibility)
- [Development](#development)
- [The Fine Print](#the-fine-print)

## Usage

Add the plugin to your project with `<extensions>true</extensions>` and configure the server id that holds your Central
Portal user token in `settings.xml`:

```xml
<plugin>
  <groupId>org.sonatype.central</groupId>
  <artifactId>central-publishing-maven-plugin</artifactId>
  <version>VERSION</version>
  <extensions>true</extensions>
  <configuration>
    <publishingServerId>central</publishingServerId>
  </configuration>
</plugin>
```

Then run `mvn deploy`. See the
[plugin documentation](https://central.sonatype.org/publish/publish-portal-maven) for all configuration options,
including `autoPublish`, `waitUntil`, `checksums`, `excludeArtifacts` and snapshot publishing.

## Compatibility

| Maven | Status |
|-------|--------|
| 3.9.x | Supported |
| 4.x   | **Not yet supported.** See the note below. |

When the plugin is used with `<extensions>true</extensions>`, it replaces the `maven-deploy-plugin` bindings at runtime.
This is known to **not work on Maven 4**: the build can finish successfully without publishing anything. Do not rely on
this plugin with Maven 4 until support is added. Binding the `publish` goal explicitly in your POM, and skipping
`maven-deploy-plugin`, has been seen to work, but is not a supported configuration yet.

## Development

See [CONTRIBUTING.md](./CONTRIBUTING.md) for details.

## The Fine Print

Remember:

This project is part of the [Sonatype Nexus Community](https://github.com/sonatype-nexus-community) organization, which is not officially supported by Sonatype. Please review the latest pull requests, issues, and commits to understand this project's readiness for contribution and use.

* File suggestions and requests on this repo through GitHub Issues, so that the community can pitch in
* Use or contribute to this project according to your organization's policies and your own risk tolerance
* Don't file Sonatype support tickets related to this project— it won't reach the right people that way

Last but not least of all - have fun!

<!-- Links Section -->
[shield_license]: https://img.shields.io/github/license/sonatype-nexus-community/central-publishing-maven-plugin?logo=open%20source%20initiative&logoColor=white "license"

[license_file]: https://github.com/sonatype-nexus-community/central-publishing-maven-plugin/blob/main/LICENSE
