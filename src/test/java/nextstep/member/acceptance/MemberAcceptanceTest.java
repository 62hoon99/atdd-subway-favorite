package nextstep.member.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.utils.AcceptanceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static nextstep.member.acceptance.MemberSteps.*;
import static nextstep.member.acceptance.TokenSteps.로그인;
import static org.assertj.core.api.Assertions.assertThat;

class MemberAcceptanceTest extends AcceptanceTest {
    public static final String EMAIL = "email@email.com";
    public static final String PASSWORD = "password";
    public static final int AGE = 20;

    private ExtractableResponse<Response> 회원_생성_응답;

    @BeforeEach
    void setUpFixture() {
        회원_생성_응답 = 회원_생성_요청(EMAIL, PASSWORD, AGE);
    }

    @DisplayName("회원가입을 한다.")
    @Test
    void createMember() {
        // then
        assertThat(회원_생성_응답.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    @DisplayName("회원 정보를 조회한다.")
    @Test
    void getMember() {
        // when
        var response = 회원_정보_조회_요청(회원_생성_응답);

        // then
        회원_정보_조회됨(response, EMAIL, AGE);

    }

    @DisplayName("회원 정보를 수정한다.")
    @Test
    void updateMember() {
        // when
        var response = 회원_정보_수정_요청(회원_생성_응답, "new" + EMAIL, "new" + PASSWORD, AGE);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("회원 정보를 삭제한다.")
    @Test
    void deleteMember() {
        // when
        var response = 회원_삭제_요청(회원_생성_응답);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    /**
     * Given 회원 가입을 생성하고
     * And 로그인을 하고
     * When 토큰을 통해 내 정보를 조회하면
     * Then 내 정보를 조회할 수 있다
     */
    @DisplayName("내 정보를 조회한다.")
    @Test
    void getMyInfo() {
        // given
        var loginResponse = 로그인(EMAIL, PASSWORD);

        // when
        var myInfoResponse = 내_정보_조회_요청(loginResponse);

        // then
        assertThat(myInfoResponse.jsonPath().getLong("id")).isEqualTo(1L);
        assertThat(myInfoResponse.jsonPath().getString("email")).isEqualTo(EMAIL);
        assertThat(myInfoResponse.jsonPath().getInt("age")).isEqualTo(AGE);
    }

    /**
     * When 조작된 토큰으로 내 정보를 조회하면
     * Then 예외를 발생한다.
     */
    @DisplayName("조작된 토큰으로 내 정보를 조회한다.")
    @Test
    void getMyInfoByManipulatedToken() {
        // when
        var myInfoResponse = 내_정보_조회_요청("manipulatedToken");

        // then
        assertThat(myInfoResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}