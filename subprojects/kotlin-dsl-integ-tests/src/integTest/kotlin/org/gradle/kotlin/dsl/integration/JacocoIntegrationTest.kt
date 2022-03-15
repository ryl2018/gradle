package org.gradle.kotlin.dsl.integration

import org.gradle.api.JavaVersion
import org.gradle.integtests.fixtures.RepoScriptBlockUtil
import org.gradle.integtests.fixtures.ToBeFixedForConfigurationCache
import org.gradle.test.fixtures.dsl.GradleDsl
import org.gradle.test.fixtures.file.LeaksFileHandles
import org.junit.Assume
import org.junit.Test


@LeaksFileHandles("Kotlin Compiler Daemon working directory")
class JacocoIntegrationTest : AbstractPluginIntegrationTest() {

    @Test
    @ToBeFixedForConfigurationCache
    fun `jacoco ignore codegen`() {
        Assume.assumeFalse(
            "JaCoCo must be updated to 0.8.8 when it releases for JDK 18",
            JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_18)
        )
        withBuildScript(
            """
            plugins {
                `kotlin-dsl`
                jacoco
            }
            ${RepoScriptBlockUtil.mavenCentralRepository(GradleDsl.KOTLIN)}
            dependencies {
                testImplementation("junit:junit:4.12")
            }
            tasks {
                test {
                    useJUnit()
                }
                jacocoTestCoverageVerification {
                    violationRules {
                        rule {
                            element = "CLASS"
                            includes = listOf("org.gradle.*", "gradle.*")
                            limit {
                                minimum = 1.toBigDecimal()
                            }
                        }
                        rule {
                            element = "METHOD"
                            includes = listOf("org.gradle.*", "gradle.*")
                            limit {
                                minimum = 1.toBigDecimal()
                            }
                        }
                    }
                }
            }
            """
        )

        withFile("src/main/kotlin/foo.gradle.kts", "plugins { base }")
        withFile(
            "src/test/kotlin/fooTest.kt",
            """
            import org.junit.Test
            class FooTest {
                @Test fun testFoo() { }
            }
            """.trimIndent()
        )

        build("test", "jacocoTestCoverageVerification")
    }
}
