# How to contribute

It's great you're here and reading this guide, because we need volunteers to help keep this project active and alive for the greater benefit of everyone!

- [Engaging with this project](#engaging-with-this-project)
- [Development Guidelines](#development-guidelines)
  - [Requirements](#requirements)
  - [Coding Conventions](#coding-conventions)
- [Testing](#testing)
  - [Unit tests](#unit-tests)
  - [Integration tests](#integration-tests)
  - [Debugging](#debugging)
- [Submitting Contributions](#submitting-contributions)

## Engaging with this project

Here are some important resources:
- [GitHub Issues](https://github.com/sonatype-nexus-community/central-publishing-maven-plugin/issues) - a place for bugs to be raised and feature requests made
- [GitHub Discussions](https://github.com/sonatype-nexus-community/central-publishing-maven-plugin/discussions) - a place to discuss ideas or real-world usage

## Development Guidelines

### Requirements

- **Java 8 source level.** The plugin is compiled with `source`/`target` 8, so do not use newer language features or APIs. Any JDK that can compile at level 8 will work; Eclipse Temurin is a good choice.
- **Maven 3.9.0 or later** (enforced during the build). The Maven wrapper (`./mvnw`, pinned to Maven 3.9.16) is included, so a separate install is not required.
- **Docker**, only if you want to run the integration tests that use a mock Central Portal (see below).

### Coding Conventions

- In order to help verify the authenticity of contributed code, we ask that your [commits be signed](https://docs.github.com/en/authentication/managing-commit-signature-verification/signing-commits). 
  All commits must be signed off to show that you agree to publish your changes under the current terms and licenses of the project.
  
  Here are some notes we found helpful in configuring a local environment to automatically sign git commits:
    - [GPG commit signature verification](https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification#gpg-commit-signature-verification)
    - [Telling Git about your GPG key](https://docs.github.com/en/authentication/managing-commit-signature-verification/telling-git-about-your-signing-key#telling-git-about-your-gpg-key)

- Follow the style of the surrounding code (formatting, naming, comment density).
- Every source file carries the license header found in [`header.txt`](./header.txt).

## Testing

### Unit tests

```
./mvnw verify
```

### Integration tests

Integration tests are written with the [`maven-invoker-plugin`](https://maven.apache.org/plugins/maven-invoker-plugin/)
and live under [`src/it/`](./src/it). Each one uses [BeanShell](https://github.com/beanshell/beanshell) scripts named
`setup.bsh` and `verify.bsh` to assert pre- and post-conditions.

Run them with the `run-its` profile: `./mvnw verify -P run-its`. To run a single test, add `-Dinvoker.test=<test pattern>`.

There are two kinds of integration test, distinguished by the name of their folder:

| Folder prefix | Needs a mock Central Portal (Docker)? | Profile to run only these |
|---------------|---------------------------------------|---------------------------|
| `bundle*`     | No  | `run-bundle-its`  |
| `publish*`    | Yes | `run-publish-its` |

If your test needs a mock Central Portal server, its folder name must start with `publish`; otherwise start it with `bundle`.

### Debugging

Use `mvnDebug` to run Maven in debug mode ([documentation](https://maven.apache.org/surefire/maven-surefire-plugin/examples/debugging.html)).
Maven pauses and listens on port `8000` for a debug connection. The invoker tests start Maven themselves, so you need to
attach a debugger, disconnect it, and then attach again for each test invocation.

## Submitting Contributions

Please send Pull Requests that:
1. Have a singular purpose, and that is backed by one or more GitHub Issues in this project
2. Are clear
3. Have appropriate test coverage for the Pull Requests purpose
4. Meet our Code Style Convention (see [above](#coding-conventions))
5. Sign off your commits to show that you agree to publish your changes under the current terms and licenses of the project, and to indicate agreement with [Developer Certificate of Origin (DCO)](https://developercertificate.org/).
