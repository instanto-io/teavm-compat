package com.google.gwt.user.client.rpc;

/** Callback contract for an asynchronous result; does not implement GWT RPC transport. */
public interface AsyncCallback<T> {
  void onFailure(Throwable caught);

  void onSuccess(T result);
}
