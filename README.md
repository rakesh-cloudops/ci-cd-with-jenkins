# CI/CD with Jenkins

Public sample from the [portfolio index](https://github.com/davraops/devsecops-portfolio). A Java 21 service, a Jenkins pipeline, and the image it ships.

`/health` returns `ok` on port 8080. The unit test calls that endpoint. The pipeline does not deploy unless you ask it to.

## Run the tests

JDK 21 and Maven 3.9.

```bash
mvn -B verify
```

Local image:

```bash
docker compose up --build
curl http://127.0.0.1:8080/health
```

## Jenkins

Create a Pipeline job from SCM pointed at this repo. The agent needs JDK 21, Maven 3.9, and a Linux shell. Docker and kubectl are only required when you enable publish.

Plugins: Pipeline, Git, Docker Pipeline, SonarQube Scanner.

Default build runs checkout and `mvn verify`. Two parameters turn on the rest:

| Parameter | What it does |
| --- | --- |
| `ANALYZE` | SonarQube, then the quality gate. The server configured in Jenkins must be named `SonarQube`. SonarQube needs a webhook to `https://<jenkins>/sonarqube-webhook/`. The token stays in the Jenkins credential. The pipeline does not pass `sonar.login`. |
| `PUBLISH` | `docker build`, push with the credential id `registry-credentials`, then `kubectl apply`. The image tag is `BUILD_NUMBER`. `__IMAGE__` in the deployment is replaced with `IMAGE:BUILD_NUMBER`. |

`REGISTRY` defaults to Docker Hub (`https://index.docker.io/v1/`). `IMAGE` defaults to `example.com/my-app`.

## What the pipeline does

1. Checkout from the job SCM.
2. `mvn -B verify`.
3. SonarQube and quality gate, only with `ANALYZE`.
4. Image, push, and deploy, only with `PUBLISH`.

## Layout

- `Jenkinsfile` is the pipeline.
- `Dockerfile` is a two-stage build. The runtime user is uid 10001, the same id the pod uses.
- `k8s/deployment.yaml` sets requests, limits, probes, and a non-root read-only filesystem.
- `k8s/service.yaml` publishes port 80 to container port 8080.

MIT. See [License.md](License.md).
