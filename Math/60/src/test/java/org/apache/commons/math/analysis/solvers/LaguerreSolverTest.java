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
package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.MathException;
import org.apache.commons.math.TestUtils;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;

/**
 * Testcase for Laguerre solver.
 * <p>
 * Laguerre's method is very efficient in solving polynomials. Test runs
 * show that for a default absolute accuracy of 1E-6, it generally takes
 * less than 5 iterations to find one root, provided solveAll() is not
 * invoked, and 15 to 20 iterations to find all roots for quintic function.
 *
 * @version $Revision$ $Date$
 */
public final class LaguerreSolverTest {
    /**
     * Test of solver for the linear function.
     */
    @Test
    public void testLinearFunction() {
        double min, max, expected, result, tolerance;

        // p(x) = 4x - 1
        double coefficients[] = { -1.0, 4.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();
        solver.setMaxEvaluations(10);

        min = 0.0; max = 1.0; expected = 0.25;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

    /**
     * Test of solver for the quadratic function.
     */
    @Test
    public void testQuadraticFunction() {
        double min, max, expected, result, tolerance;

        // p(x) = 2x^2 + 5x - 3 = (x+3)(2x-1)
        double coefficients[] = { -3.0, 5.0, 2.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();
        solver.setMaxEvaluations(10);

        min = 0.0; max = 2.0; expected = 0.5;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -4.0; max = -1.0; expected = -3.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

    /**
     * Test of solver for the quintic function.
     */
    @Test
    public void testQuinticFunction() {
        double min, max, expected, result, tolerance;

        // p(x) = x^5 - x^4 - 12x^3 + x^2 - x - 12 = (x+1)(x+3)(x-4)(x^2-x+1)
        double coefficients[] = { -12.0, -1.0, 1.0, -12.0, -1.0, 1.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();
        solver.setMaxEvaluations(10);

        min = -2.0; max = 2.0; expected = -1.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = -5.0; max = -2.5; expected = -3.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        Assert.assertEquals(expected, result, tolerance);

        min = 3.0; max = 6.0; expected = 4.0;
        tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
                    FastMath.abs(expected * solver.getRelativeAccuracy()));
        result = solver.solve(f, min, max);
        Assert.assertEquals(expected, result, tolerance);
    }

    /**
     * Test of solver for the quintic function using solveAll().
     * XXX commented out because "solveAll" is not part of the API.
     */
    // public void testQuinticFunction2() {
    //     double initial = 0.0, tolerance;
    //     Complex expected, result[];

    //     // p(x) = x^5 + 4x^3 + x^2 + 4 = (x+1)(x^2-x+1)(x^2+4)
    //     double coefficients[] = { 4.0, 0.0, 1.0, 4.0, 0.0, 1.0 };
    //     LaguerreSolver solver = new LaguerreSolver();
    //     result = solver.solveAll(coefficients, initial);

    //     expected = new Complex(0.0, -2.0);
    //     tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
    //                 FastMath.abs(expected.abs() * solver.getRelativeAccuracy()));
    //     TestUtils.assertContains(result, expected, tolerance);

    //     expected = new Complex(0.0, 2.0);
    //     tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
    //                 FastMath.abs(expected.abs() * solver.getRelativeAccuracy()));
    //     TestUtils.assertContains(result, expected, tolerance);

    //     expected = new Complex(0.5, 0.5 * FastMath.sqrt(3.0));
    //     tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
    //                 FastMath.abs(expected.abs() * solver.getRelativeAccuracy()));
    //     TestUtils.assertContains(result, expected, tolerance);

    //     expected = new Complex(-1.0, 0.0);
    //     tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
    //                 FastMath.abs(expected.abs() * solver.getRelativeAccuracy()));
    //     TestUtils.assertContains(result, expected, tolerance);

    //     expected = new Complex(0.5, -0.5 * FastMath.sqrt(3.0));
    //     tolerance = FastMath.max(solver.getAbsoluteAccuracy(),
    //                 FastMath.abs(expected.abs() * solver.getRelativeAccuracy()));
    //     TestUtils.assertContains(result, expected, tolerance);
    // }

    /**
     * Test of parameters for the solver.
     */
    @Test
    public void testParameters() {
        double coefficients[] = { -3.0, 5.0, 2.0 };
        PolynomialFunction f = new PolynomialFunction(coefficients);
        LaguerreSolver solver = new LaguerreSolver();
        solver.setMaxEvaluations(10);

        try {
            // bad interval
            solver.solve(f, 1, -1);
            Assert.fail("Expecting IllegalArgumentException - bad interval");
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            // no bracketing
            solver.solve(f, 2, 3);
            Assert.fail("Expecting IllegalArgumentException - no bracketing");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }
}
