package org.mockitousage.constructor;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.mock.SerializableMode;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

import static org.mockito.Mockito.*;

public class CreatingMocksWithConstructorTest extends TestBase {

    static abstract class AbstractMessage {
        private final String message;
        AbstractMessage() {
            this.message = "hey!";
        }
        String getMessage() {
            return message;
        }
    }

    static class Message extends AbstractMessage {}
    class InnerClass extends AbstractMessage {}

    @Test
    public void can_create_mock_with_constructor() {
        Message mock = mock(Message.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
        //the message is a part of state of the mocked type that gets initialized in constructor
        assertEquals("hey!", mock.getMessage());
    }

    @Test
    public void can_mock_abstract_classes() {
        AbstractMessage mock = mock(AbstractMessage.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("hey!", mock.getMessage());
    }

    @Test
    public void can_spy_abstract_classes() {
        AbstractMessage mock = spy(AbstractMessage.class);
        assertEquals("hey!", mock.getMessage());
    }

    @Test
    public void can_mock_inner_classes() {
        InnerClass mock = mock(InnerClass.class, withSettings().useConstructor().outerInstance(this).defaultAnswer(CALLS_REAL_METHODS));
        assertEquals("hey!", mock.getMessage());
    }

    static class HasConstructor {
        HasConstructor(String x) {}
    }

    @Test
    public void exception_message_when_constructor_not_found() {
        try {
            //when
            spy(HasConstructor.class);
            //then
            fail();
        } catch (MockitoException e) {
            assertEquals("Unable to create mock instance of type 'HasConstructor'", e.getMessage());
            assertContains("Please ensure it has parameter-less constructor", e.getCause().getMessage());
        }
    }

    @Test
    public void mocking_inner_classes_with_wrong_outer_instance() {
        try {
            //when
            mock(InnerClass.class, withSettings().useConstructor().outerInstance("foo").defaultAnswer(CALLS_REAL_METHODS));
            //then
            fail();
        } catch (MockitoException e) {
            assertEquals("Unable to create mock instance of type 'InnerClass'", e.getMessage());
            assertContains("Please ensure that the outer instance has correct type and that the target class has parameter-less constructor", e.getCause().getMessage());
        }
    }

    @Test
    public void mocking_interfaces_with_constructor() {
        //at the moment this is allowed however we can be more strict if needed
        //there is not much sense in creating a spy of an interface
        mock(IMethods.class, withSettings().useConstructor());
        spy(IMethods.class);
    }

    @Test
    public void prevents_across_jvm_serialization_with_constructor() {
        try {
            //when
            mock(AbstractMessage.class, withSettings().useConstructor().serializable(SerializableMode.ACROSS_CLASSLOADERS));
            //then
            fail();
        } catch (MockitoException e) {
            assertEquals("Mocks instantiated with constructor cannot be combined with " + SerializableMode.ACROSS_CLASSLOADERS + " serialization mode.", e.getMessage());
        }
    }
}
