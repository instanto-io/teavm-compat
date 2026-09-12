/*
 * #%L
 * GWT Bootstrap
 * %%
 * Copyright (C) 2026 Carl Stainton
 * %%
 * Reimplements, over TeaVM's JSO libraries, part of the GWT client API. Class,
 * method and package names follow GWT (https://github.com/gwtproject/gwt),
 * Copyright (C) The GWT Project Authors, licensed under the Apache License,
 * Version 2.0. No GWT source is included.
 * %%
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
 * #L%
 */
package com.google.gwt.user.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Oracle that matches a query against any word boundary of its candidates. */
public class MultiWordSuggestOracle extends SuggestOracle {

  /** A plain string suggestion. */
  public static class MultiWordSuggestion implements Suggestion {

    private final String replacement;
    private final String display;

    public MultiWordSuggestion(final String replacement, final String display) {
      this.replacement = replacement;
      this.display = display;
    }

    @Override
    public String getDisplayString() {
      return display;
    }

    @Override
    public String getReplacementString() {
      return replacement;
    }
  }

  private final List<String> candidates = new ArrayList<>();

  public void add(final String suggestion) {
    if (suggestion != null) {
      candidates.add(suggestion);
    }
  }

  public void addAll(final Collection<String> suggestions) {
    if (suggestions != null) {
      for (final String suggestion : suggestions) {
        add(suggestion);
      }
    }
  }

  public void clear() {
    candidates.clear();
  }

  @Override
  public void requestSuggestions(final Request request, final Callback callback) {
    final String query = request.getQuery() == null ? "" : request.getQuery().toLowerCase();
    final List<Suggestion> matches = new ArrayList<>();
    for (final String candidate : candidates) {
      if (matches.size() >= request.getLimit()) {
        break;
      }
      if (matchesAtWordBoundary(candidate.toLowerCase(), query)) {
        matches.add(new MultiWordSuggestion(candidate, candidate));
      }
    }
    callback.onSuggestionsReady(request, new Response(matches));
  }

  /** True when the query starts the candidate or any of its words. */
  private static boolean matchesAtWordBoundary(final String candidate, final String query) {
    if (query.isEmpty()) {
      return true;
    }
    int index = candidate.indexOf(query);
    while (index >= 0) {
      if (index == 0 || candidate.charAt(index - 1) == ' ') {
        return true;
      }
      index = candidate.indexOf(query, index + 1);
    }
    return false;
  }
}
