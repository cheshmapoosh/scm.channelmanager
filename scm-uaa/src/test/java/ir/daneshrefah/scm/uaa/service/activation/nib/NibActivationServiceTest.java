package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.uaa.exception.activation.nib.NibActivationSourceUserNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NibActivationServiceTest {
    private final NibRoleProvisioningService roles = mock(NibRoleProvisioningService.class);
    private final NibChannelAuthenticationDuplicator channelAuthentication =
            mock(NibChannelAuthenticationDuplicator.class);
    private final NibMembershipAccessDuplicator memberships = mock(NibMembershipAccessDuplicator.class);
    private final NibActivationService service = new NibActivationService(roles, channelAuthentication, memberships);

    @Test
    void orchestratesMainDatabaseStepsInOrder() {
        GeneralPerson person = mock(GeneralPerson.class);
        when(person.getId()).thenReturn(42);
        when(person.getPersonType()).thenReturn(PersonType.REAL);

        service.activate(person, TerminalType.IB);

        InOrder order = inOrder(roles, channelAuthentication, memberships);
        order.verify(roles).provision(42, PersonType.REAL);
        order.verify(channelAuthentication).duplicate(42, TerminalType.IB);
        order.verify(memberships).duplicate(42, TerminalType.IB);
    }

    @Test
    void rejectsMissingSourceIdentity() {
        GeneralPerson person = mock(GeneralPerson.class);

        assertThrows(
                NibActivationSourceUserNotFoundException.class,
                () -> service.activate(person, TerminalType.IB)
        );
    }

    @Test
    void usesMainTransactionManager() throws Exception {
        Method activate = NibActivationService.class.getMethod(
                "activate",
                GeneralPerson.class,
                TerminalType.class
        );

        assertEquals(
                "mainTransactionManager",
                activate.getAnnotation(Transactional.class).transactionManager()
        );
    }
}
