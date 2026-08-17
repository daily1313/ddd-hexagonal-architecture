package tobyspring.splearn.application.provided;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import tobyspring.splearn.SplearnTestConfiguration;
import tobyspring.splearn.domain.*;

import java.lang.constant.Constable;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@Import(SplearnTestConfiguration.class)
@SpringBootTest
// @TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public record MemberRegisterTest(MemberRegister memberRegister) {

    @Test
    void register() {
        Member member = memberRegister.register(MemberFixture.createMemberRequest());

        assertThat(member.getId()).isNotNull();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
    }

    @Test
    void duplicateEmailFail() {
        Member member = memberRegister.register(MemberFixture.createMemberRequest());

        assertThatThrownBy(() ->
            memberRegister.register(MemberFixture.createMemberRequest())
        ).isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void memberRegisterRequestFail() {
        extracted(new MemberRegisterRequest("toby@splrean.app", "Toby", "longsecret"));
        extracted(new MemberRegisterRequest("toby@splrean.app", "Charlie1234123412341234", "secret"));
        extracted(new MemberRegisterRequest("toby", "Charlie", "secret"));
    }

    private void extracted(MemberRegisterRequest invalid) {
        assertThatThrownBy(() -> memberRegister.register(invalid))
            .isInstanceOf(ConstraintViolationException.class);
    }
}
