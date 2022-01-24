/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests.tooling.r75

import org.gradle.integtests.tooling.TestLauncherSpec
import org.gradle.integtests.tooling.fixture.TargetGradleVersion
import org.gradle.integtests.tooling.fixture.ToolingApiVersion
import org.gradle.tooling.TestExecutionException
import org.gradle.tooling.TestLauncher
import org.gradle.tooling.TestSpec

@ToolingApiVersion('>=7.5')
@TargetGradleVersion(">=7.5")
class TestLauncherTestSpecCrossVersionSpec extends TestLauncherSpec {

    def setup() {
        withFailingTest() // ensures that withTestsFor statements are not ignored
    }

    @TargetGradleVersion('<7.5')
    def "older Gradle versions ignore witTestsFor calls"() {
        when:
        launchTests { TestLauncher launcher ->
            launcher.withTestsFor { TestSpec spec ->
                spec.forTaskPath(':secondTest').includeClass('example.MyTest')
            }
        }

        then:
        Throwable exception = thrown(TestExecutionException)
        exception.cause.message.startsWith 'No matching tests found in any candidate test task'
    }

    def "can select test classes"() {
        when:
        launchTests { TestLauncher launcher ->
            launcher.withTestsFor { TestSpec spec ->
                spec.forTaskPath(':secondTest')
                    .includeClass('example.MyTest')
                    .includeClass('example2.MyOtherTest2')
            }
        }

        then:
        events.testClassesAndMethods.size() == 5
        assertTestExecuted(className: 'example.MyTest', methodName: 'foo', task: ':secondTest')
        assertTestExecuted(className: 'example.MyTest', methodName: 'foo2', task: ':secondTest')
        assertTestExecuted(className: 'example2.MyOtherTest2', methodName: 'baz', task: ':secondTest')
    }

    def "can select test methods"() {
        when:
        launchTests { TestLauncher launcher ->
            launcher.withTestsFor { TestSpec spec ->
                spec.forTaskPath(':secondTest').includeMethod('example.MyTest', 'foo')
            }
        }

        then:
        events.testClassesAndMethods.size() == 2
        assertTestExecuted(className: 'example.MyTest', methodName: 'foo', task: ':secondTest')
    }

    def "can select package"() {
        setup:
        when:
        launchTests { TestLauncher launcher ->
            launcher.withTestsFor { TestSpec spec ->
                spec.forTaskPath(':secondTest').includePackage('example2')
            }
        }

        then:
        events.testClassesAndMethods.size() == 4
        assertTestExecuted(className: 'example2.MyOtherTest', methodName: 'bar', task: ':secondTest')
        assertTestExecuted(className: 'example2.MyOtherTest2', methodName: 'baz', task: ':secondTest')
    }

    def "can select tests with pattern"() {
        when:
        launchTests { TestLauncher launcher ->
            launcher.withTestsFor { TestSpec spec ->
                spec.forTaskPath(':secondTest').includePattern('example2.MyOtherTest*.ba*')
            }
        }

        then:
        events.testClassesAndMethods.size() == 4
        assertTestExecuted(className: 'example2.MyOtherTest', methodName: 'bar', task: ':secondTest')
        assertTestExecuted(className: 'example2.MyOtherTest2', methodName: 'baz', task: ':secondTest')
    }

    def "can combine different test selection"() {
        setup:
        file('src/test/java/org/AnotherTest.java').text = '''
            package org;
            public class AnotherTest {
                @org.junit.Test public void testThis() throws Exception {
                     org.junit.Assert.assertEquals(1, 1);
                }
            }
        '''

        when:
        launchTests { TestLauncher launcher ->
            launcher.withTestsFor { TestSpec spec ->
                spec.forTaskPath(':secondTest')
                    .includePackage('org')
                    .includeClass('example2.MyOtherTest')
                    .includeMethod('example.MyTest', 'foo')
                    .includePattern('example2.MyOther*2.baz')
            }
        }

        then:
        events.testClassesAndMethods.size() == 8
        assertTestExecuted(className: 'org.AnotherTest', methodName: 'testThis', task: ':secondTest') // selected by includePackage
        assertTestExecuted(className: 'example2.MyOtherTest', methodName: 'bar', task: ':secondTest') // selected by includeClass
        assertTestExecuted(className: 'example.MyTest', methodName: 'foo', task: ':secondTest') // selected by includeMethod
        assertTestExecuted(className: 'example2.MyOtherTest2', methodName: 'baz', task: ':secondTest') // selected by include
    }

    def "can target same test tasks with multiple test specs"() {
        when:
        launchTests { TestLauncher launcher ->
            launcher.withTestsFor { TestSpec spec ->
                spec.forTaskPath(':secondTest').includeClass('example.MyTest')
                spec.forTaskPath(':secondTest').includePackage('example2')
            }
        }

        then:
        events.testClassesAndMethods.size() == 7
        assertTestExecuted(className: 'example.MyTest', methodName: 'foo', task: ':secondTest')
        assertTestExecuted(className: 'example.MyTest', methodName: 'foo2', task: ':secondTest')
        assertTestExecuted(className: 'example2.MyOtherTest', methodName: 'bar', task: ':secondTest')
        assertTestExecuted(className: 'example2.MyOtherTest2', methodName: 'baz', task: ':secondTest')
    }


    def "can target different test tasks with one test spec"() {
        when:
        launchTests { TestLauncher launcher ->
            launcher.withTestsFor { TestSpec spec ->
                spec.forTaskPath(':test').includeClass('example.MyTest')
                spec.forTaskPath(':secondTest').includePackage('example2')
            }
        }

        then:
        events.testClassesAndMethods.size() == 7
        assertTestExecuted(className: 'example.MyTest', methodName: 'foo', task: ':test')
        assertTestExecuted(className: 'example.MyTest', methodName: 'foo2', task: ':test')
        assertTestExecuted(className: 'example2.MyOtherTest', methodName: 'bar', task: ':secondTest')
        assertTestExecuted(className: 'example2.MyOtherTest2', methodName: 'baz', task: ':secondTest')
    }
}

