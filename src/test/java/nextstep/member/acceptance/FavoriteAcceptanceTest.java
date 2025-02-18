package nextstep.member.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.acceptance.LineSteps;
import nextstep.subway.acceptance.StationSteps;
import nextstep.subway.applicaion.dto.SectionRequest;
import nextstep.utils.AcceptanceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static nextstep.member.acceptance.FavoriteSteps.*;
import static org.assertj.core.api.Assertions.assertThat;

public class FavoriteAcceptanceTest extends AcceptanceTest {

    private static final String 마들역명 = "마들역";
    private static final String 중계역명 = "중계역";

    private Long 마들역_id;
    private Long 노원역_id;
    private Long 중계역_id;
    private Long 칠호선_id;

    private Long 해운대역_id;

    private static final String EMAIL = "email@email.com";
    private static final String PASSWORD = "password";
    private static final int AGE = 20;
    private String accessToken;

    @BeforeEach
    void setUpFixture() {
        마들역_id = StationSteps.지하철역_생성_요청(마들역명);
        노원역_id = StationSteps.지하철역_생성_요청("노원역");
        중계역_id = StationSteps.지하철역_생성_요청(중계역명);
        칠호선_id = LineSteps.지하철_노선_생성_요청("7호선", 중계역_id, 마들역_id, 10);
        LineSteps.지하철_노선_구간_등록_요청(칠호선_id, new SectionRequest(중계역_id, 노원역_id, 3));

        해운대역_id = StationSteps.지하철역_생성_요청("해운대역");

        MemberSteps.회원_생성_요청(EMAIL, PASSWORD, AGE);
        accessToken = TokenSteps.로그인(EMAIL, PASSWORD).jsonPath().getString("accessToken");
    }

    /**
     * Given: 역과 구간을 등록한다.
     * When: 로그인 한다.
     * When: 즐겨찾기를 등록한다.
     * Then: 즐겨찾기를 조회 하면 즐겨찾기로 등록한 역이 조회된다.
     */
    @Test
    void createFavorites() {
        //when
        즐겨찾기_생성(중계역_id, 마들역_id, accessToken);

        //then
        var 즐겨찾기_조회 = 즐겨찾기_조회(accessToken);
        assertThat(즐겨찾기_조회.jsonPath().getString("[0].source.name")).isEqualTo(중계역명);
        assertThat(즐겨찾기_조회.jsonPath().getString("[0].target.name")).isEqualTo(마들역명);
    }

    /**
     * Given: 역과 구간을 등록한다.
     * When: 로그인 한다.
     * When: 이어져 있지 않은 역을 즐겨찾기로 등록한다.
     * Then: 예외가 발생한다.
     */
    @Test
    void createFavoritesWithUnconnectedStation() {
        //when
        var response = 즐겨찾기_생성(중계역_id, 해운대역_id, accessToken);

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given: 역과 구간을 등록한다.
     * When: 로그인 하지 않고 즐겨찾기를 등록한다.
     * Then: 예외가 발생한다.
     */
    @Test
    void createFavoritesWithoutLogin() {
        //when
        var response = 즐겨찾기_생성(중계역_id, 마들역_id);

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * Given: 역과 구간을 등록한다.
     * Given: 로그인 한다.
     * Given: 즐겨찾기를 등록한다.
     * When: 즐겨찾기를 삭제한다.
     * Then: 즐겨찾기를 조회 하면 삭제한 즐겨찾기는 존재하지 않는다.
     */
    @Test
    void deleteFavorite() {
        //given
        var response = 즐겨찾기_생성(중계역_id, 마들역_id, accessToken);

        //when
        즐겨찾기_삭제(response.header("Location"), accessToken);

        //then
        var 즐겨찾기_조회 = 즐겨찾기_조회(accessToken);
        assertThat(즐겨찾기_조회.jsonPath().getList("").size()).isEqualTo(0);
    }

    /**
     * Given: 역과 구간을 등록한다.
     * Given: 로그인 한다.
     * Given: 즐겨찾기를 등록한다.
     * When: 토큰 없이 즐겨찾기를 삭제한다.
     * Then: 예외가 발생한다.
     */
    @Test
    void deleteFavoriteWithoutLogin() {
        //given
        var response = 즐겨찾기_생성(중계역_id, 마들역_id, accessToken);

        //when
        var 즐겨찾기_삭제 = 즐겨찾기_삭제(response.header("Location"));

        //then
        assertThat(즐겨찾기_삭제.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * Given: 역과 구간을 등록한다.
     * Given: 로그인 한다.
     * Given: 즐겨찾기를 등록한다.
     * When: 다른 계정으로 로그인 한다.
     * Then: 등록한 즐겨찾기를 삭제하면 예외가 발생한다.
     */
    @Test
    void deleteOtherMemberFavorite() {
        //given
        var response = 즐겨찾기_생성(중계역_id, 마들역_id, accessToken);

        //when
        String otherMemberEmail = "otherMember@gmail.com";
        String otherMemberPassword = "password";
        MemberSteps.회원_생성_요청(otherMemberEmail, otherMemberPassword, 80);
        String otherMemberAccessToken = TokenSteps.로그인(otherMemberEmail, otherMemberPassword)
                .jsonPath().getString("accessToken");

        //then
        ExtractableResponse<Response> 즐겨찾기_삭제 = 즐겨찾기_삭제(response.header("Location"), otherMemberAccessToken);
        assertThat(즐겨찾기_삭제.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }
}
