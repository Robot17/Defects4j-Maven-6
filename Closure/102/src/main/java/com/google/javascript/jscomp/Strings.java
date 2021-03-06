/*
 * Copyright 2010 Google Inc.
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

package com.google.javascript.jscomp;

import javax.annotation.Nullable;

/**
 * Guava code that will eventually be open-sourced properly. Package-private
 * until they're able to do that. A lot of these methods are discouraged
 * anyways.
 *
*
 */
class Strings {
  private Strings() {} // All static

  /**
   * Returns {@code true} if the given string is either null or the empty string
   * ("").
   *
   * @param string the string reference to check
   * @return {@code true} if {@code string} is either null or the empty string
   */
  static boolean isEmpty(@Nullable String string) {
    return string == null || string.isEmpty();
  }

  /**
   * Returns the given string if it is non-null; the empty string otherwise.
   *
   * @param string the string to test and possibly return
   * @return {@code string} itself if it is non-null; {@code ""} if it is null
   */
  static String makeSafe(@Nullable String string) {
    return (string == null) ? "" : string;
  }

  /**
   * Returns a string consisting of the input string concatenated a given number
   * of times. For example, {@code repeat("hey", 3)} returns the string {@code
   * "heyheyhey"}.
   *
   * @param string any non-null string
   * @param count the number of times to repeat it; a nonnegative integer
   * @return a string containing {@code string} repeated {@code count} times
   *     (the empty string if {@code count} is zero)
   * @throws IllegalArgumentException if {@code count} is negative
   */
  static String repeat(String string, int count) {
    // If this multiplication overflows, a NegativeArraySizeException or
    // OutOfMemoryError is not far behind
    StringBuilder builder = new StringBuilder(string.length() * count);
    for (int i = 0; i < count; i++) {
      builder.append(string);
    }
    return builder.toString();
  }
  /**
   * If this given string is of length {@code maxLength} or less, it will
   * be returned as-is.
   * Otherwise it will be trucated to {@code maxLength}, regardless of whether
   * there are any space characters in the String. If an ellipsis is requested
   * to be appended to the truncated String, the String will be truncated so
   * that the ellipsis will also fit within maxLength.
   * If no truncation was necessary, no ellipsis will be added.
   * @param source the String to truncate if necessary
   * @param maxLength the maximum number of characters to keep
   * @param addEllipsis if true, and if the String had to be truncated,
   *     add "..." to the end of the String before returning. Additionally,
   *     the ellipsis will only be added if maxLength is greater than 3.
   * @return the original string if it's length is less than or equal to
   *     maxLength, otherwise a truncated string as mentioned above
   */
  static String truncateAtMaxLength(String source, int maxLength,
      boolean addEllipsis) {

    if (source.length() <= maxLength) {
      return source;
    }
    if (addEllipsis && maxLength > 3) {
      return unicodePreservingSubstring(source, 0, maxLength - 3) + "...";
    }
    return unicodePreservingSubstring(source, 0, maxLength);
  }

  /**
   * Normalizes {@code index} such that it respects Unicode character
   * boundaries in {@code str}.
   *
   * <p>If {@code index} is the low surrogate of a unicode character,
   * the method returns {@code index - 1}. Otherwise, {@code index} is
   * returned.
   *
   * <p>In the case in which {@code index} falls in an invalid surrogate pair
   * (e.g. consecutive low surrogates, consecutive high surrogates), or if
   * if it is not a valid index into {@code str}, the original value of
   * {@code index} is returned.
   *
   * @param str the String
   * @param index the index to be normalized
   * @return a normalized index that does not split a Unicode character
   */
  private static int unicodePreservingIndex(String str, int index) {
    if (index > 0 && index < str.length()) {
      if (Character.isHighSurrogate(str.charAt(index - 1)) &&
          Character.isLowSurrogate(str.charAt(index))) {
        return index - 1;
      }
    }
    return index;
  }

  /**
   * Returns a substring of {@code str} that respects Unicode character
   * boundaries.
   *
   * <p>The string will never be split between a [high, low] surrogate pair,
   * as defined by {@link Character#isHighSurrogate} and
   * {@link Character#isLowSurrogate}.
   *
   * <p>If {@code begin} or {@code end} are the low surrogate of a unicode
   * character, it will be offset by -1.
   *
   * <p>This behavior guarantees that
   * {@code str.equals(StringUtil.unicodePreservingSubstring(str, 0, n) +
   *     StringUtil.unicodePreservingSubstring(str, n, str.length())) } is
   * true for all {@code n}.
   * </pre>
   *
   * <p>This means that unlike {@link String#substring(int, int)}, the length of
   * the returned substring may not necessarily be equivalent to
   * {@code end - begin}.
   *
   * @param str the original String
   * @param begin the beginning index, inclusive
   * @param end the ending index, exclusive
   * @return the specified substring, possibly adjusted in order to not
   *   split unicode surrogate pairs
   * @throws IndexOutOfBoundsException if the {@code begin} is negative,
   *   or {@code end} is larger than the length of {@code str}, or
   *   {@code begin} is larger than {@code end}
   */
  private static String unicodePreservingSubstring(
      String str, int begin, int end) {
    return str.substring(unicodePreservingIndex(str, begin),
        unicodePreservingIndex(str, end));
  }
}
