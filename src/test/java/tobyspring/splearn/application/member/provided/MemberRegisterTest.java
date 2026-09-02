package tobyspring.splearn.application.member.provided;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tobyspring.splearn.SplearnTestConfiguration;
import tobyspring.splearn.domain.member.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@Import(SplearnTestConfiguration.class)
@SpringBootTest
// @TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
record MemberRegisterTest(MemberRegister memberRegister, EntityManager entityManager) {

    @Test
    void register() {
        Member member = memberRegister.register(MemberFixture.createMemberRequest());

        assertThat(member.getId()).isNotNull();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
    }

    @Test
    void updateInfo() {
        Member member = registerMember();

        memberRegister.activate(member.getId());
        entityManager.flush();
        entityManager.clear();

        member = memberRegister.updateInfo(member.getId(), new MemberInfoUpdateRequest("Peter", "toby100", "자기소개"));

        assertThat(member.getDetail().getProfile().address()).isEqualTo("toby100");
    }

    @Test
    void updateInfoFail() {
        Member member = registerMember();
        memberRegister.activate(member.getId());
        memberRegister.updateInfo(member.getId(), new MemberInfoUpdateRequest("Peter", "toby100", "자기소개"));

        Member member2 = registerMember("toby2@splearn.app");
        memberRegister.activate(member2.getId());
        entityManager.flush();
        entityManager.clear();

        // member2는 기존의 member와 같은 프로필 주소를 사용할 수 없다
        assertThatThrownBy(() -> {
            memberRegister.updateInfo(member2.getId(), new MemberInfoUpdateRequest("Peter", "toby100", "자기소개"));
        }).isInstanceOf(DuplicateProfileException.class);

        // 다른 프로필 주소로는 변경 가능
        memberRegister.updateInfo(member2.getId(), new MemberInfoUpdateRequest("Peter", "toby101", "자기소개"));

        // 기존 프로필 주소를 바꾸는 것도 가능
        memberRegister.updateInfo(member.getId(), new MemberInfoUpdateRequest("Peter", "toby100", "자기소개"));

        // 프로필 주소를 제거하는 것도 가능
        memberRegister.updateInfo(member.getId(), new MemberInfoUpdateRequest("Peter", "", "자기소개"));

        // 프로필 주소 중복은 허용하지 않음
        assertThatThrownBy(() -> {
            memberRegister.updateInfo(member.getId(), new MemberInfoUpdateRequest("Peter", "toby101", "자기소개"));
        }).isInstanceOf(DuplicateProfileException.class);
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
        checkValidation(new MemberRegisterRequest("toby@splrean.app", "Toby", "longsecret"));
        checkValidation(new MemberRegisterRequest("toby@splrean.app", "Charlie1234123412341234", "secret"));
        checkValidation(new MemberRegisterRequest("toby", "Charlie", "secret"));
    }

    @Test
    void activate() {
        Member member = registerMember();

        member = memberRegister.activate(member.getId());

        entityManager.flush();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getDetail().getActivatedAt()).isNotNull();
    }

    @Test
    void deactivate() {
        Member member = registerMember();

        memberRegister.activate(member.getId());
        entityManager.flush();
        entityManager.clear();

        member = memberRegister.deactivate(member.getId());
        assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        assertThat(member.getDetail().getDeactivatedAt()).isNotNull();
    }

    private Member registerMember() {
        Member member = memberRegister.register(MemberFixture.createMemberRequest());
        entityManager.flush();
        entityManager.clear();
        return member;
    }

    private Member registerMember(String email) {
        Member member = memberRegister.register(MemberFixture.createMemberRequest(email));
        entityManager.flush();
        entityManager.clear();
        return member;
    }

    private void checkValidation(MemberRegisterRequest invalid) {
        assertThatThrownBy(() -> memberRegister.register(invalid))
            .isInstanceOf(ConstraintViolationException.class);
    }
}
