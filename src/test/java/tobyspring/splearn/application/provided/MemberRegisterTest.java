package tobyspring.splearn.application.provided;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import tobyspring.splearn.SplearnTestConfiguration;
import tobyspring.splearn.domain.DuplicateEmailException;
import tobyspring.splearn.domain.Member;
import tobyspring.splearn.domain.MemberFixture;
import tobyspring.splearn.domain.MemberStatus;

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
}
