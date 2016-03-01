/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.audit.event;


import javax.annotation.concurrent.Immutable;

/**
 * Start operation request was accepted.
 */
@Immutable
public class StartOperationAuditEvent extends AbstractUserAuditEvent {

  public static class StartOperationAuditEventBuilder
    extends AbstractUserAuditEventBuilder<StartOperationAuditEvent, StartOperationAuditEventBuilder> {

    private String requestId;

    private String reasonOfFailure;

    private String operation;

    private StartOperationAuditEventBuilder() {
    }

    /**
     * Appends to the audit event the identifier of the
     * operation through whcih the operation progress can be tracked.
     *
     * @param builder builder for the audit event details.
     */
    @Override
    protected void buildAuditMessage(StringBuilder builder) {
      super.buildAuditMessage(builder);

      builder
        .append(", Operation(")
        .append(operation)
        .append("), RequestId(")
        .append(requestId)
        .append("), Status(")
        .append(reasonOfFailure == null ? "Successfully queued" : "Failed to queue");

      if (reasonOfFailure != null) {
        builder.append("), Reason(")
          .append(reasonOfFailure);
      }
      builder.append(")");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected StartOperationAuditEvent newAuditEvent() {
      return new StartOperationAuditEvent(this);
    }

    /**
     * Sets the identifier of the operation through which the operation progress can be tracked.
     *
     * @param requestId the identifier of the operation through which the operation progress can be tracked.
     * @return this builder
     */
    public StartOperationAuditEventBuilder withRequestId(String requestId) {
      this.requestId = requestId;
      return this;
    }

    public StartOperationAuditEventBuilder withReasonOfFailure(String reasonOfFailure) {
      this.reasonOfFailure = reasonOfFailure;
      return this;
    }

    public StartOperationAuditEventBuilder withOperation(String operation) {
      this.operation = operation;
      return this;
    }
  }

  private StartOperationAuditEvent() {
  }

  /**
   * {@inheritDoc}
   */
  private StartOperationAuditEvent(StartOperationAuditEventBuilder builder) {
    super(builder);
  }

  /**
   * Returns an builder for {@link StartOperationAuditEvent}
   *
   * @return a builder instance
   */
  public static StartOperationAuditEventBuilder builder() {
    return new StartOperationAuditEventBuilder();
  }
}
