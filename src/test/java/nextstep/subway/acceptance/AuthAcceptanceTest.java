package nextstep.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.authentication.FormAuthConfig;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static nextstep.subway.acceptance.MemberSteps.*;


class AuthAcceptanceTest extends AcceptanceTest {

    @DisplayName("Basic Auth")
    @Test
    void myInfoWithBasicAuth() {
        // when
        var response = 베이직_인증으로_내_회원_정보_조회_요청(EMAIL, PASSWORD);

        // then
        회원_정보_조회됨(response, EMAIL, AGE);
    }

    @DisplayName("Session 로그인 후 내 정보 조회")
    @Test
    void myInfoWithSession() {
        // when
        var response = 폼_로그인_후_내_회원_정보_조회_요청(EMAIL, PASSWORD);

        // then
        회원_정보_조회됨(response, EMAIL, AGE);
    }

    @DisplayName("Bearer Auth")
    @Test
    void myInfoWithBearerAuth() {
        // given
        String accessToken = 로그인_되어_있음(EMAIL, PASSWORD);

        // when
        var response = 베어러_인증으로_내_회원_정보_조회_요청(accessToken);

        // then
        회원_정보_조회됨(response, EMAIL, AGE);
    }

    private ExtractableResponse<Response> 폼_로그인_후_내_회원_정보_조회_요청(String email, String password) {
        return RestAssured
                .given().log().all()
                    .auth().form(email, password, new FormAuthConfig("/login/form", "email", "password"))
                .when()
                    .get("/members/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    private ExtractableResponse<Response> 베어러_인증으로_내_회원_정보_조회_요청(String accessToken) {
        return RestAssured
                .given().log().all()
                    .auth().oauth2(accessToken)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .get("/members/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }
}
