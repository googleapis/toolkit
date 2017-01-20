/* Copyright 2016 Google Inc
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
package com.google.api.codegen.util;

import com.google.common.base.Strings;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class used to get and store unique symbols.
 *
 * <p>If a symbol is already used, the table will try to append an index number onto the end of it.
 * The index will keep increasing until an unused symbol is found.
 */
public class SymbolTable {

  private final Set<String> symbolTable = new HashSet<>();

  public enum DisambiguationStrategy {
    UNDERSCORE,
    NUMERIC
  }

  private final DisambiguationStrategy disambiguationStrategy;

  public SymbolTable() {
    this.disambiguationStrategy = DisambiguationStrategy.NUMERIC;
  }

  public SymbolTable(DisambiguationStrategy disambiguationStrategy) {
    this.disambiguationStrategy = disambiguationStrategy;
  }

  /**
   * Returns the same SymbolTable seeded with all the words in seed.
   *
   * <p>For example, if seed is {"int"}, a subsequent call to {@link #getNewSymbol(String)} for
   * "int" will return "int2".
   *
   * <p>The behavior of the returned SymbolTable is guaranteed if used with {@link
   * #getNewSymbol(String)}, but not with {@link #getNewSymbol(Name)}.
   */
  public SymbolTable seed(Set<String> seed) {
    SymbolTable symbolTable = new SymbolTable(disambiguationStrategy);
    for (String s : seed) {
      symbolTable.getNewSymbol(s);
    }
    return symbolTable;
  }

  /**
   * Returns a unique name, with a suffix in case of conflicts.
   *
   * <p>Not guaranteed to work as expected if used in combination with {@link
   * #getNewSymbol(String)}.
   */
  public Name getNewSymbol(Name desiredName) {
    String lower = desiredName.toLowerUnderscore();
    String suffix = getAndSaveSuffix(lower);
    if (Strings.isNullOrEmpty(suffix)) {
      return desiredName;
    }
    return desiredName.join(suffix);
  }

  /**
   * Returns a unique name, with a suffix in case of conflicts.
   *
   * <p>Not guaranteed to work as expected if used in combination with {@link #getNewSymbol(Name)}.
   */
  public String getNewSymbol(String desiredName) {
    String suffix = getAndSaveSuffix(desiredName);
    return desiredName + suffix;
  }

  private String getAndSaveSuffix(String desiredName) {
    if (!symbolTable.contains(desiredName)) {
      symbolTable.add(desiredName);
      return "";
    }
    if (disambiguationStrategy == DisambiguationStrategy.NUMERIC) {
      return getAndSaveNumericSuffix(desiredName);
    }
    return getAndSaveUnderscoreSuffix(desiredName);
  }

  /**
   * Returns the next numeric suffix that makes desiredName unique.
   *
   * <p>Stores the joined desiredName/suffix in an internal map. Assumes that desiredName is already
   * in the symbolTable.
   *
   * <p>For example, if "foo" is passed, "2" is returned. If "foo" is passed again, "3" is returned,
   * and then "4" and so on.
   */
  private String getAndSaveNumericSuffix(String desiredName) {
    // Resolve collisions with a numeric suffix, starting with 2.
    int i = 2;
    while (symbolTable.contains(desiredName + Integer.toString(i))) {
      i++;
    }
    symbolTable.add(desiredName + Integer.toString(i));
    return Integer.toString(i);
  }

  /**
   * Returns the next underscore suffix that makes desiredName unique.
   *
   * <p>Stores the joined desiredName/suffix in an internal map. Assumes that desiredName is already
   * in the symbolTable.
   *
   * <p>For example, if "foo" is passed, "_" is returned. If "foo" is passed again, "__" is
   * returned, and then "___" and so on.
   */
  private String getAndSaveUnderscoreSuffix(String desiredName) {
    int i = 0;
    String name;
    do {
      i += 1;
      name = desiredName + Strings.repeat("_", i);
    } while (symbolTable.contains(name));
    symbolTable.add(name);
    return Strings.repeat("_", i);
  }
}
