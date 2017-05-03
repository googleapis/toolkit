/* Copyright 2017 Google Inc
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
package com.google.api.codegen.util.php;

import com.google.api.codegen.util.CommentReformatter;
import com.google.api.codegen.util.CommentReformatting;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

public class PhpCommentReformatter implements CommentReformatter {
  /** Escaper for formatting PHP doc strings. */
  private static final Escaper PHP_ESCAPER =
      Escapers.builder().addEscape('*', "&#42;").addEscape('@', "&#64;").build();

  @Override
  public String reformat(String comment) {
    comment = PHP_ESCAPER.escape(comment);
    comment = CommentReformatting.reformatCloudMarkdownLinks(comment, "[%s](%s)");
    return comment.trim();
  }
}
