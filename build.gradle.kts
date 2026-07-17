// Top-level build file where you can add configuration options common to all sub-projects/modules.
import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.kutil.string.WhitespaceUtil.removeMultipleWhitespaces
import de.bixilon.kutil.string.WhitespaceUtil.trimWhitespaces


plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false

    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

buildscript {
    dependencies {
        classpath(libs.kutil)
    }
}

fun getEnv(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

data class GitStatus(
    val commit: String,
    val branch: String,
    val clean: Boolean,
    val tag: String?,
)

fun loadGitFromEnv(): GitStatus? {
    val commit = getEnv("CI_COMMIT_SHA") ?: return null
    val branch = getEnv("CI_COMMIT_BRANCH") ?: return null
    val tag = getEnv("CI_COMMIT_TAG")
    return GitStatus(commit, branch, true, tag)
}

val FDROID = project.properties["fdroid"]?.toBoolean() ?: false

val hasGit by lazy { project.rootDir.resolve(".git").exists() }

fun executeGit(vararg args: String): String? {
    if (!hasGit) return null

    val process = ProcessBuilder()
        .command(*(arrayOf("git") + args))
        .directory(project.rootDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
    if (process.waitFor() != 0) return null

    return process.inputStream.readAsString()
        .trimWhitespaces()
        .trim { it == '\n' || it == '\r' }
        .removeMultipleWhitespaces()
        .takeIf { it.isNotBlank() }
}

fun loadGitFromGit(): GitStatus? {
    val commit = executeGit("rev-parse", "HEAD") ?: return null
    val branch = executeGit("branch", "--show-current") ?: "master"
    val clean = if (FDROID) true else executeGit("status", "--porcelain") == null
    val tag = executeGit("describe", "--exact-match", "--tags")

    return GitStatus(commit, branch, clean, tag)
}

val git by lazy { ignoreAll { loadGitFromGit() } ?: loadGitFromEnv() }

allprojects {
    var version = (git?.tag?.removePrefix("v") ?: git?.commit?.substring(0, 10) ?: "unknown")
    git?.takeIf { !it.clean }?.let { version += "-dirty" }

    project.extra.set("version", version)
    project.extra.set("versionCode", 6)
    project.extra.set("commit", git?.commit ?: "unknown")
}
