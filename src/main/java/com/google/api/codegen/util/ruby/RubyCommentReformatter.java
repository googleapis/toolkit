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
package com.google.api.codegen.util.ruby;

import com.google.api.codegen.CommentPatterns;
import com.google.api.codegen.util.CommentReformatter;
import com.google.api.codegen.util.CommentReformatting;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import java.util.regex.Matcher;
import javax.annotation.Nullable;

public class RubyCommentReformatter implements CommentReformatter {

  private static Function<Matcher, String> PROTO_TO_RUBY_DOC =
      new Function<Matcher, String>() {
        @Override
        public String apply(Matcher matcher) {
          return Matcher.quoteReplacement(protoToRubyDoc(matcher.group(1)));
        }
      };
  private static Function<Matcher, String> HEADLINE_REPLACE =
      new Function<Matcher, String>() {
        @Nullable
        @Override
        public String apply(@Nullable Matcher matcher) {
          return matcher.group().replace("#", "=");
        }
      };

  @Override
  public String reformat(String comment) {
    comment = CommentPatterns.BACK_QUOTE_PATTERN.matcher(comment).replaceAll("+");
    return CommentReformatting.of(comment)
        .reformat(CommentPatterns.PROTO_LINK_PATTERN, PROTO_TO_RUBY_DOC)
        .reformatCloudMarkdownLinks("{%s}[%s]")
        .reformatAbsoluteMarkdownLinks("{%s}[%s]")
        .reformat(CommentPatterns.HEADLINE_PATTERN, HEADLINE_REPLACE)
        .toString()
        .trim();
  }

  private static String protoToRubyDoc(String comment) {
    boolean messageFound = false;
    boolean isFirstSegment = true;
    String result = "";
    for (String name : Splitter.on(".").splitToList(comment)) {
      char firstChar = name.charAt(0);
      if (Character.isUpperCase(firstChar)) {
        messageFound = true;
        result += (isFirstSegment ? "" : "::") + name;
      } else if (messageFound) {
        // Lowercase segment after message is found is field.
        // In Ruby, it is referred as "Message#field" format.
        result += "#" + name;
      } else {
        result +=
            (isFirstSegment ? "" : "::") + Character.toUpperCase(firstChar) + name.substring(1);
      }
      isFirstSegment = false;
    }
    return result;
  }
}
