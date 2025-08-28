# node-sandbox

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/node-sandbox-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## NPM Package Setup for Offline Sandboxes

Since the Node.js sandboxes run with no network access, you need to pre-install NPM packages on the host and map them into containers.

### 1. Install NPM packages globally on host

```bash
# Essential packages for Express apps
npm install -g express
npm install -g cors
npm install -g body-parser
npm install -g helmet
npm install -g morgan
npm install -g dotenv
npm install -g nodemon
npm install -g concurrently

# Additional useful packages
npm install -g lodash
npm install -g moment
npm install -g axios
npm install -g uuid
npm install -g bcrypt
npm install -g jsonwebtoken
npm install -g multer
```

### 2. Set environment variable

Your NPM global directory is: `/Users/shelajev/.nvm/versions/node/v24.0.1/lib/node_modules`

Set the environment variable to map this into containers:

```bash
export NPM_CACHE_DIR="/Users/shelajev/.nvm/versions/node/v24.0.1/lib/node_modules"
```

Or add to your shell profile:
```bash
echo 'export NPM_CACHE_DIR="/Users/shelajev/.nvm/versions/node/v24.0.1/lib/node_modules"' >> ~/.zshrc
```

### 3. Running with NPM packages

When you start the application, the global packages will be available in the sandbox at `/usr/local/share/npm-global/lib/node_modules`.

### 4. Using packages in sandbox

In your Node.js projects within the sandbox, you can reference these globally installed packages:

```javascript
// These will work even without internet
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
```

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
