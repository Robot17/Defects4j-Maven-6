/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math.ode;

/**
 * This class implements a second order Runge-Kutta integrator for
 * Ordinary Differential Equations.
 *
 * <p>This method is an explicit Runge-Kutta method, its Butcher-array
 * is the following one :
 * <pre>
 *    0  |  0    0
 *   1/2 | 1/2   0
 *       |----------
 *       |  0    1
 * </pre>
 * </p>
 *
 * @see EulerIntegrator
 * @see ClassicalRungeKuttaIntegrator
 * @see GillIntegrator
 *
 * @version $Revision$ $Date$
 * @since 1.2
 */

public class MidpointIntegrator
  extends RungeKuttaIntegrator {

  /** Integrator method name. */
  private static final String methodName = "midpoint";

  /** Time steps Butcher array. */
  private static final double[] c = {
    1.0 / 2.0
  };

  /** Internal weights Butcher array. */
  private static final double[][] a = {
    { 1.0 / 2.0 }
  };

  /** Propagation weights Butcher array. */
  private static final double[] b = {
    0.0, 1.0
  };

  /** Simple constructor.
   * Build a midpoint integrator with the given step.
   * @param step integration step
   */
  public MidpointIntegrator(double step) {
    super(c, a, b, new MidpointStepInterpolator(), step);
  }

  /** Get the name of the method.
   * @return name of the method
   */
  public String getName() {
    return methodName;
  }

}
