/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.junit5;

import com.intellij.rt.execution.junit.IdeaTestRunner;
import com.intellij.rt.execution.junit.segments.OutputObjectRegistry;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;
import org.junit.gen5.launcher.main.LauncherFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JUnit5IdeaTestRunner implements IdeaTestRunner {
  private JUnit5TestExecutionListener myListener;
  private TestPlan myTestPlan;

  @Override
  public int startRunnerWithArgs(String[] args, ArrayList listeners, String name, int count, boolean sendTree) {
    Launcher launcher = LauncherFactory.create();
    launcher.registerTestExecutionListeners(myListener);
    final String[] packageNameRef = new String[1];
    final TestDiscoveryRequest discoveryRequest = JUnit5TestRunnerUtil.buildRequest(args, packageNameRef);
    myTestPlan = launcher.discover(discoveryRequest);
    myListener.sendTree(myTestPlan, packageNameRef[0]);
    launcher.execute(discoveryRequest);

    return 0;
  }

  @Override
  public void setStreams(Object segmentedOut, Object segmentedErr, int lastIdx) {
    myListener = new JUnit5TestExecutionListener(System.out);
  }

  @Override
  public OutputObjectRegistry getRegistry() {
    return null;
  }

  @Override
  public Object getTestToStart(String[] args, String name) {
    final TestDiscoveryRequest request = JUnit5TestRunnerUtil.buildRequest(args, new String[1]);
    Launcher launcher = LauncherFactory.create();
    myTestPlan = launcher.discover(request);
    final Set<TestIdentifier> roots = myTestPlan.getRoots();
    
    return roots.isEmpty() ? null : roots.iterator().next();
  }

  @Override
  public List getChildTests(Object description) {
    return new ArrayList<>(myTestPlan.getChildren((TestIdentifier)description));
  }

  @Override
  public String getStartDescription(Object child) {
    final TestIdentifier testIdentifier = (TestIdentifier)child;
    final String className = JUnit5TestExecutionListener.getClassName(testIdentifier);
    final String methodName = JUnit5TestExecutionListener.getMethodName(testIdentifier);
    if (methodName != null) {
      return className + "#" + methodName;
    }
    return className != null ? className : (testIdentifier).getDisplayName();
  }

  @Override
  public String getTestClassName(Object child) {
    return child.toString();
  }
}
