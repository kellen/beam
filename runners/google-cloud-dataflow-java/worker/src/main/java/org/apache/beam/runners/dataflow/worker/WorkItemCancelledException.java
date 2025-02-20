/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.runners.dataflow.worker;

/** Indicates that the work item was cancelled and should not be retried. */
@SuppressWarnings({
  "nullness" // TODO(https://github.com/apache/beam/issues/20497)
})
public class WorkItemCancelledException extends RuntimeException {
  public WorkItemCancelledException(long sharding_key) {
    super("Work item cancelled for key " + sharding_key);
  }

  public WorkItemCancelledException(String message, Throwable cause) {
    super(message, cause);
  }

  public WorkItemCancelledException(Throwable cause) {
    super(cause);
  }

  /** Returns whether an exception was caused by a {@link WorkItemCancelledException}. */
  public static boolean isWorkItemCancelledException(Throwable t) {
    while (t != null) {
      if (t instanceof WorkItemCancelledException) {
        return true;
      }
      t = t.getCause();
    }
    return false;
  }
}
