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

import java.util.Collection;

/** Supplies completion suggestions for a {@link SuggestBox}. */
public abstract class SuggestOracle {

  /** One completion offered to the user. */
  public interface Suggestion {
    String getDisplayString();

    String getReplacementString();
  }

  /** A request for suggestions matching a query. */
  public static class Request {

    private String query;
    private int limit = 20;

    public Request() {}

    public Request(final String query) {
      this.query = query;
    }

    public Request(final String query, final int limit) {
      this.query = query;
      this.limit = limit;
    }

    public String getQuery() {
      return query;
    }

    public void setQuery(final String query) {
      this.query = query;
    }

    public int getLimit() {
      return limit;
    }

    public void setLimit(final int limit) {
      this.limit = limit;
    }
  }

  /** The suggestions produced for a {@link Request}. */
  public static class Response {

    private Collection<? extends Suggestion> suggestions;

    public Response() {}

    public Response(final Collection<? extends Suggestion> suggestions) {
      this.suggestions = suggestions;
    }

    public Collection<? extends Suggestion> getSuggestions() {
      return suggestions;
    }

    public void setSuggestions(final Collection<? extends Suggestion> suggestions) {
      this.suggestions = suggestions;
    }
  }

  /** Notified when suggestions are ready. */
  public interface Callback {
    void onSuggestionsReady(Request request, Response response);
  }

  public abstract void requestSuggestions(Request request, Callback callback);

  public void requestDefaultSuggestions(final Request request, final Callback callback) {
    requestSuggestions(request, callback);
  }

  public boolean isDisplayStringHTML() {
    return false;
  }
}
