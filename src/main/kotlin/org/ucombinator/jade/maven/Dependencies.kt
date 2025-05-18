package org.ucombinator.jade.maven

import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.resolution.ArtifactResolutionException
import org.eclipse.aether.util.artifact.JavaScopes
import org.ucombinator.jade.util.Exceptions
import org.ucombinator.jade.util.IO
import org.ucombinator.jade.util.Json
import org.ucombinator.jade.util.Log
import org.ucombinator.jade.util.Parallel

import java.io.File
import kotlin.time.Duration

/** TODO:doc. */
object Dependencies {
  private val log = Log {}

  /** TODO:doc. */
  fun main( // TODO: rename to "get" or "download" or "run" or something
    // TODO: remoterepos(default=central)
    // TODO: overwrite
    // TODO: mkdir
    // TODO: proxy
    timeout: Duration,
    localRepoDir: File,
    dependenciesDir: File, // TODO: or to stdout
    artifacts: List<Artifact>,
  ) {
    val session = Maven.session(LocalRepository(localRepoDir))
    // TODO: support multiple remotes
    val compressorStreamFactory = CompressorStreamFactory()
    // org.eclipse.aether.collection.DependencyCollectionException: Failed to collect dependencies
    // Caused by: org.eclipse.aether.transfer.ArtifactNotFoundException: Could not find artifact

    // TODO: INFO  .org.eclipse.aether.internal.impl.DefaultArtifactResolver: Artifact net.shibboleth.utilities:java-support:pom:8.4.0 is present in the local repository, but cached from a remote repository ID that is unavailable in current build context, verifying that is downloadable from [google-maven-central-ap (https://maven-central-asia.storage-download.googleapis.com/maven2, default, releases+snapshots), central (https://repo.maven.apache.org/maven2, default, releases), Shibbolet (https://build.shibboleth.net/nexus/content/repositories/releases/, default, releases+snapshots)]
    // TODO: INFO  .org.apache.http.impl.execchain.RetryExec: I/O exception (java.net.SocketException) caught when processing request to {}->http://repo.typesafe.com:80: Network is unreachable
    // TODO: INFO  .org.apache.http.impl.execchain.RetryExec: Retrying request to {}->http://repo.typesafe.com:80
    // TODO: WARN  .org.apache.http.client.protocol.ResponseProcessCookies: Invalid cookie header: "set-cookie: atlCohort={"bucketAll":{"bucketedAtUTC":"2024-07-04T02:35:55.466Z","version":"2","index":95,"bucketId":0}}; Max-Age=315360000; Domain=bitbucket.org; Path=/; Expires=Sun, 02 Jul 2034 02:35:55 GMT". Invalid 'expires' attribute: Sun, 02 Jul 2034 02:35:55 GMT
    // TODO: WARN  .org.apache.http.client.protocol.ResponseProcessCookies: Invalid cookie header: "set-cookie: bxp_gateway_anchor=%7B%22anchor%22%3A%22Brand%22%2C%22type%22%3A%22SEO%22%7D; Max-Age=604800; Domain=bitbucket.org; Path=/; Expires=Thu, 11 Jul 2024 02:35:55 GMT". Invalid 'expires' attribute: Thu, 11 Jul 2024 02:35:55 GMT
    // TODO: WARN  .org.apache.http.client.protocol.ResponseProcessCookies: Invalid cookie header: "set-cookie: ajs_anonymous_id=%227af8e221-fde3-2ff2-379e-672e65091286%22; Max-Age=315360000; Domain=bitbucket.org; Path=/; Expires=Sun, 02 Jul 2034 02:35:55 GMT". Invalid 'expires' attribute: Sun, 02 Jul 2034 02:35:55 GMT
    // TODO: WARN  .org.apache.http.client.protocol.ResponseProcessCookies: Invalid cookie header: "set-cookie: bxp_gateway_request_id=ca38a1f2-817d-1efc-e2f6-aa13b5322cb1; Max-Age=300; Domain=bitbucket.org; Path=/; Expires=Thu, 04 Jul 2024 02:40:55 GMT". Invalid 'expires' attribute: Thu, 04 Jul 2024 02:40:55 GMT
    // TODO: WARN  .org.apache.http.client.protocol.ResponseProcessCookies: Invalid cookie header: "set-cookie: atlCohort={"bucketAll":{"bucketedAtUTC":"2024-07-04T02:35:56.203Z","version":"2","index":97,"bucketId":0}}; Max-Age=315360000; Domain=bitbucket.org; Path=/; Expires=Sun, 02 Jul 2034 02:35:56 GMT". Invalid 'expires' attribute: Sun, 02 Jul 2034 02:35:56 GMT
    // TODO: WARN  .org.apache.http.client.protocol.ResponseProcessCookies: Invalid cookie header: "set-cookie: bxp_gateway_anchor=%7B%22anchor%22%3A%22Brand%22%2C%22type%22%3A%22SEO%22%7D; Max-Age=604800; Domain=bitbucket.org; Path=/; Expires=Thu, 11 Jul 2024 02:35:56 GMT". Invalid 'expires' attribute: Thu, 11 Jul 2024 02:35:56 GMT
    // TODO: WARN  .org.apache.http.client.protocol.ResponseProcessCookies: Invalid cookie header: "set-cookie: ajs_anonymous_id=%22e0730275-2bec-bdbd-8d9a-976589361431%22; Max-Age=315360000; Domain=bitbucket.org; Path=/; Expires=Sun, 02 Jul 2034 02:35:56 GMT". Invalid 'expires' attribute: Sun, 02 Jul 2034 02:35:56 GMT
    // TODO: WARN  .org.apache.http.client.protocol.ResponseProcessCookies: Invalid cookie header: "set-cookie: bxp_gateway_request_id=0ee3020b-1b8e-e963-6e00-cf18bdd044c2; Max-Age=300; Domain=bitbucket.org; Path=/; Expires=Thu, 04 Jul 2024 02:40:56 GMT". Invalid 'expires' attribute: Thu, 04 Jul 2024 02:40:56 GMT

    // TODO: pull from mirrors in parallel
    // TODO: collect list of repositories
    Parallel.run(
      log,
      timeout,
      artifacts,
      { Maven.artifactFile(dependenciesDir, it, ".dependencies.json.zst", ".dependencies.err") },
      {
        Exceptions.isClasses(
          it,
          org.eclipse.aether.collection.DependencyCollectionException::class,
          org.eclipse.aether.resolution.ArtifactDescriptorException::class,
          org.eclipse.aether.resolution.ArtifactResolutionException::class,
          org.eclipse.aether.transfer.ArtifactNotFoundException::class,
        ) ||
          Exceptions.isClasses(
            it,
            org.eclipse.aether.collection.DependencyCollectionException::class,
            org.eclipse.aether.resolution.ArtifactDescriptorException::class,
            org.eclipse.aether.resolution.ArtifactResolutionException::class,
            org.eclipse.aether.transfer.ArtifactTransferException::class,
            java.net.UnknownHostException::class,
          ) ||
          Exceptions.isClasses(
            it,
            org.eclipse.aether.collection.DependencyCollectionException::class,
            org.eclipse.aether.resolution.ArtifactDescriptorException::class,
            org.eclipse.aether.resolution.ArtifactResolutionException::class,
            org.eclipse.aether.transfer.ArtifactTransferException::class,
            org.apache.http.client.HttpResponseException::class,
          ) ||
          Exceptions.isClasses(
            it,
            org.eclipse.aether.collection.DependencyCollectionException::class,
            org.eclipse.aether.resolution.ArtifactDescriptorException::class,
            org.apache.maven.model.building.ModelBuildingException::class,
          ) ||
          Exceptions.isClasses(
            it,
            org.eclipse.aether.collection.DependencyCollectionException::class,
            org.eclipse.aether.resolution.ArtifactDescriptorException::class,
            org.apache.maven.model.resolution.UnresolvableModelException::class,
            org.eclipse.aether.resolution.ArtifactResolutionException::class,
            org.eclipse.aether.transfer.ArtifactNotFoundException::class,
          ) ||
          Exceptions.isClasses(
            it,
            org.eclipse.aether.collection.DependencyCollectionException::class,
            org.eclipse.aether.resolution.VersionRangeResolutionException::class,
          ) ||
          Exceptions.isClasses(
            it,
            org.ucombinator.jade.maven.DollarInCoordinateException::class,
          ) ||
          Exceptions.isClasses(
            it,
            org.eclipse.aether.collection.DependencyCollectionException::class,
            org.eclipse.aether.collection.UnsolvableVersionConflictException::class,
          ) ||
          Exceptions.isClasses(
            it,
            org.ucombinator.jade.maven.CaretInVersionException::class,
          ) ||
          Exceptions.isClasses(
            it,
            org.eclipse.aether.collection.DependencyCollectionException::class,
            org.eclipse.aether.resolution.ArtifactDescriptorException::class,
            org.eclipse.aether.resolution.ArtifactResolutionException::class,
            org.eclipse.aether.transfer.ArtifactTransferException::class,
            org.eclipse.aether.transfer.ChecksumFailureException::class,
          )
      },
    ) { artifact ->
      val collectRequest = CollectRequest(Dependency(artifact, JavaScopes.COMPILE), Maven.remotes)
      // TODO: note difference between root artifact and root dependency (see https://maven.apache.org/resolver/maven-resolver-api/apidocs/org/eclipse/aether/collection/CollectRequest.html#setRootArtifact(org.eclipse.aether.artifact.Artifact))
      val collectResult = Maven.system.collectDependencies(session, collectRequest)
      // // collectResult.getRoot().accept(ConsoleDependencyGraphDumper())
      // // TODO: rename artifacts to dependencies or dependencyArtifacts
      // TODO: example of slow: io.netty:netty-common:4.1.111.Final
      // TODO: com.fasterxml.jackson.core:jackson-databind:2.17.1

      // TODO: Add buildByteArray(block: ByteArrayOutputStream.() -> Unit): ByteArray
      // --compress zstd:.json.zst (should use as default)
      // --compress none
      // --no-compress
      // --list-compressors (or just part of help)
      IO.buildByteArray {
        compressorStreamFactory.createCompressorOutputStream("zstd", this).use {
          it.write(Json.of(collectResult.getRoot()).toString().toByteArray())
          it.write("\n".toByteArray())
        }
      }
      // name = format (we should use zstd as default)
      // getOutputStreamCompressorNames
      // findAvailableCompressorOutputStreamProviders()
      // getCompressorOutputStreamProviders (use this one)
      // FileOutputStream(path).use { fileOutputStream ->
      //   compressorStreamFactory.createCompressorOutputStream(format, fileOutputStream).use { compressorOutputStream ->
      //     compressorOutputStream.println(Json.of(root.toString()))
      //   }
      // }
      // (input only) YOU_NEED_BROTLI_DEC = youNeed("Google Brotli Dec", "https://github.com/google/brotli/")
      // (unsure if needed) YOU_NEED_XZ_JAVA = youNeed("XZ for Java", "https://tukaani.org/xz/java.html")
      // (unsure if needed) YOU_NEED_ZSTD_JNI = youNeed("Zstd JNI", "https://github.com/luben/zstd-jni")

      // TODO: content type is the layout
      // In Maven, a repository content type refers to the format or structure of the artifacts (such as JAR files, WAR files, etc.) stored within a Maven repository.
      // The main repository content types in Maven are:
      // 1. **Maven 2/3 Repository (default)**: This is the default and most commonly used repository content type in Maven. It follows the Maven 2/3 repository layout, where artifacts are organized into directories based on their group ID, artifact ID, and version.
      // 2. **Legacy Maven 1 Repository**: This content type is used for repositories that follow the older Maven 1 repository layout, which was used in earlier versions of Maven.
      // 3. **Ivy Repository**: This content type is used for repositories that follow the Ivy repository layout, which is a different format used by the Ivy dependency management tool.
      // 4. **P2 Repository**: This content type is used for repositories that follow the P2 (Eclipse Plug-in) repository layout, which is commonly used for Eclipse plugin dependencies.
      // 5. **Raw Repository**: This content type is used for repositories that do not follow any specific layout and simply store the artifacts as-is, without any directory structure.
      // The repository content type is important because it determines how Maven will interact with the repository and how it will locate and download the required artifacts. When configuring a Maven repository, you need to specify the correct content type to ensure that Maven can properly access and manage the artifacts stored in the repository.
    }
  }

  // TODO: root.accept(ConsoleDependencyGraphDumper())
  // TODO: record optionality (maybe look at ConsoleDependencyGraphDuper for code)
  // TODO: fix cookie error

  // properties: {language=none, constitutesBuildPath=false, type=jar, includesDependencies=false}
  // includesDependencies=false
  // require(node.version === node.artifact.version)
  // data: Map<Object, Object>
  //   ConflictResolver.NODE_DATA_WINNER

  // transitive dependencies
}
