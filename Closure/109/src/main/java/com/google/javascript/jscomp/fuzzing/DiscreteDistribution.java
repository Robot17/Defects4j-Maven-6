/*
 * Copyright 2013 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.javascript.jscomp.fuzzing;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @param <T> The type of items
 * UNDER DEVELOPMENT. DO NOT USE!
 * @author zplin@google.com (Zhongpeng Lin)
 */
public class DiscreteDistribution<T> {
  private Random random;
  private List<T> items;
  private List<Double> weights;

  public DiscreteDistribution(Random random, Map<T, Double> pmf) {
    this.random = random;
    items = new ArrayList<T>(pmf.size());
    weights = new ArrayList<Double>(pmf.size());
    double sum = 0;

    for (T item : pmf.keySet()) {
      double p = pmf.get(item);
      Preconditions.checkArgument(p >= 0);
      Preconditions.checkArgument(!Double.isInfinite(p));
      Preconditions.checkArgument(!Double.isNaN(p));
      weights.add(p);
      sum += p;
      items.add(item);
    }

    for (int i = 0; i < pmf.size(); i++) {
      weights.set(i, weights.get(i) / sum);
    }
  }

  public T nextItem() {
      final double randomValue = random.nextDouble();
      double sum = 0;

      for (int i = 0; i < weights.size(); i++) {
          sum += weights.get(i);
          if (randomValue < sum) {
              return items.get(i);
          }
      }
      return null;
  }
}
