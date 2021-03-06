package org.mockito.internal.verification;

import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

public class MockAwareVerificationMode implements VerificationMode {

    private final Object mock;
    private final VerificationMode mode;

    public MockAwareVerificationMode(Object mock, VerificationMode mode) {
        this.mock = mock;
        this.mode = mode;
    }

    @Override
    public void verify(VerificationData data) {
        mode.verify(data);
    }

    public Object getMock() {
        return mock;
    }
}