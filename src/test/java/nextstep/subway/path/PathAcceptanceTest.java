package nextstep.subway.path;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.auth.dto.TokenResponse;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.dto.StationResponse;
import nextstep.subway.utils.AcceptanceDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {

    private static final String 회원_12_이메일 = "mwkwon12@test.com";
    private static final String 회원_13_이메일 = "mwkwon13@test.com";
    private static final String 회원_19_이메일 = "mwkwon19@test.com";
    private static final String PASSWORD = "password";

    private StationResponse 고속버스터미널역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;
    private StationResponse 양재역;
    private StationResponse 서초역;
    private StationResponse 강남역;
    private StationResponse 역삼역;
    private StationResponse 양재시민의숲역;
    private LineResponse 신분당선;
    private LineResponse 삼호선;
    private LineResponse 이호선;

    private TokenResponse 회원_12세;
    private TokenResponse 회원_13세;
    private TokenResponse 회원_19세;

    /**
     *             3호선
     *         고속버스터미널역
     *             |
     *             3
     *             |
     * 서초역--10--교대역--10--강남역--5--역삼역    2호선
     *             |         |
     *             4         |
     *             |         15
     *          남부터미널역     |
     *             |         |
     *             |--5----양재역
     *                       |
     *                       12
     *                       |
     *                   양재시민의숲역
     *                     신분당선
     */
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        고속버스터미널역 = AcceptanceDataGenerator.지하철역_등록되어_있음("고속버스터미널역");
        교대역 = AcceptanceDataGenerator.지하철역_등록되어_있음("교대역");
        남부터미널역 = AcceptanceDataGenerator.지하철역_등록되어_있음("남부터미널역");
        양재역 = AcceptanceDataGenerator.지하철역_등록되어_있음("양재역");
        서초역 = AcceptanceDataGenerator.지하철역_등록되어_있음("서초역");
        강남역 = AcceptanceDataGenerator.지하철역_등록되어_있음("강남역");
        역삼역 = AcceptanceDataGenerator.지하철역_등록되어_있음("역삼역");
        양재시민의숲역 = AcceptanceDataGenerator.지하철역_등록되어_있음("양재시민의숲역");

        신분당선 = AcceptanceDataGenerator.지하철_노선_등록되어_있음("신분당선", "bg-red-600", 강남역, 양재시민의숲역, 27, 900);
        AcceptanceDataGenerator.지하철_노선에_지하철역_등록_요청(신분당선, 강남역, 양재역, 15);

        삼호선 = AcceptanceDataGenerator.지하철_노선_등록되어_있음("삼호선", "bg-red-600", 고속버스터미널역, 양재역, 12);
        AcceptanceDataGenerator.지하철_노선에_지하철역_등록_요청(삼호선, 고속버스터미널역, 교대역, 3);
        AcceptanceDataGenerator.지하철_노선에_지하철역_등록_요청(삼호선, 교대역, 남부터미널역, 4);

        이호선 = AcceptanceDataGenerator.지하철_노선_등록되어_있음("이호선", "bg-red-600", 서초역, 역삼역, 25);
        AcceptanceDataGenerator.지하철_노선에_지하철역_등록_요청(이호선, 서초역, 교대역, 10);
        AcceptanceDataGenerator.지하철_노선에_지하철역_등록_요청(이호선, 교대역, 강남역, 10);
        AcceptanceDataGenerator.회원_생성을_요청(회원_12_이메일, PASSWORD, 12);
        AcceptanceDataGenerator.회원_생성을_요청(회원_13_이메일, PASSWORD, 13);
        AcceptanceDataGenerator.회원_생성을_요청(회원_19_이메일, PASSWORD, 19);
        회원_12세 = AcceptanceDataGenerator.로그인_요청(회원_12_이메일, PASSWORD);
        회원_13세 = AcceptanceDataGenerator.로그인_요청(회원_13_이메일, PASSWORD);
        회원_19세 = AcceptanceDataGenerator.로그인_요청(회원_19_이메일, PASSWORD);
    }

    @Test
    void 경로_조회() {
        // when: 출발역 강남역 도착역 서초역의 경로를 조회한다(같은 라인 - 2호선 추가 요금 없음)
        ExtractableResponse<Response> 강남역_서초역_경로_조회 = AcceptanceDataGenerator.지하철_경로_조회(강남역, 서초역, 회원_19세);
        // then: 경로가 조회 된다.
        경로가_조회됨(강남역_서초역_경로_조회);
        // then: 경유 지하철역은 강남역, 교대역, 서초역이다.
        경유_지하철역_확인(강남역_서초역_경로_조회, Arrays.asList(강남역, 교대역, 서초역));
        // then: 이동 거리는 20이다.
        이동_거리_확인(강남역_서초역_경로_조회, 20);
        // then: 지하철 이용 요금은 1,450원이다.
        이용_요금_확인(강남역_서초역_경로_조회, 1450);

        // when: 출발역 교대역 도착역 양재시민의숲역의 경로를 조회한다(1회 환승)
        ExtractableResponse<Response> 교대역_양재시민의숲역_경로_조회 = AcceptanceDataGenerator.지하철_경로_조회(교대역, 양재시민의숲역, 회원_19세);
        // then: 경로가 조회 된다.
        경로가_조회됨(교대역_양재시민의숲역_경로_조회);
        // then: 경유 지하철역은 교대역, 남부터미널역, 양재역, 양재시민의숲역이다.
        경유_지하철역_확인(교대역_양재시민의숲역_경로_조회, Arrays.asList(교대역, 남부터미널역, 양재역, 양재시민의숲역));
        // then: 이동 거리는 21이다.
        이동_거리_확인(교대역_양재시민의숲역_경로_조회, 21);
        // then: 지하철 이용 요금은 2,450원이다.
        이용_요금_확인(교대역_양재시민의숲역_경로_조회, 2450);

        // when: 출발역 서초역 도착역 양재시민의숲역의 경로를 조회한다(2회 환승)
        ExtractableResponse<Response> 서초역_양재시민의숲역_경로_조회 = AcceptanceDataGenerator.지하철_경로_조회(서초역, 양재시민의숲역, 회원_19세);
        // then: 경로가 조회 된다.
        경로가_조회됨(서초역_양재시민의숲역_경로_조회);
        // then: 경유 지하철역은 서초역, 교대역, 남부터미널역, 양재역, 양재시민의숲역이다.
        경유_지하철역_확인(서초역_양재시민의숲역_경로_조회, Arrays.asList(서초역, 교대역, 남부터미널역, 양재역, 양재시민의숲역));
        // then: 이동 거리는 31이다.
        이동_거리_확인(서초역_양재시민의숲역_경로_조회, 31);
        // then: 지하철 이용 요금은 2,750원이다.
        이용_요금_확인(서초역_양재시민의숲역_경로_조회, 2650);

        // when: 12세 회원이 출발역 강남역 도착역 서초역의 경로를 조회한다(같은 라인 - 2호선 추가 요금 없음)
        ExtractableResponse<Response> 회원_12세_강남역_서초역_경로_조회 = AcceptanceDataGenerator.지하철_경로_조회(강남역, 서초역, 회원_12세);
        // then: 경로가 조회 된다.
        경로가_조회됨(회원_12세_강남역_서초역_경로_조회);
        // then: 경유 지하철역은 강남역, 교대역, 서초역이다.
        경유_지하철역_확인(회원_12세_강남역_서초역_경로_조회, Arrays.asList(강남역, 교대역, 서초역));
        // then: 이동 거리는 20이다.
        이동_거리_확인(회원_12세_강남역_서초역_경로_조회, 20);
        // then: 지하철 이용 요금은 550원이다.
        이용_요금_확인(회원_12세_강남역_서초역_경로_조회, 725);

        // when: 13세 회원이 출발역 교대역 도착역 양재시민의숲역의 경로를 조회한다(1회 환승)
        ExtractableResponse<Response> 회원_13세_교대역_양재시민의숲역_경로_조회 = AcceptanceDataGenerator.지하철_경로_조회(교대역, 양재시민의숲역, 회원_13세);
        // then: 경로가 조회 된다.
        경로가_조회됨(회원_13세_교대역_양재시민의숲역_경로_조회);
        // then: 경유 지하철역은 교대역, 남부터미널역, 양재역, 양재시민의숲역이다.
        경유_지하철역_확인(회원_13세_교대역_양재시민의숲역_경로_조회, Arrays.asList(교대역, 남부터미널역, 양재역, 양재시민의숲역));
        // then: 이동 거리는 21이다.
        이동_거리_확인(회원_13세_교대역_양재시민의숲역_경로_조회, 21);
        // then: 지하철 이용 요금은 1,680원이다.
        이용_요금_확인(회원_13세_교대역_양재시민의숲역_경로_조회, 1960);
    }

    @Test
    void 경로_조회_실패() {
        // given: 사당역, 과천역이 등록되어 있음.
        StationResponse 사당역 = AcceptanceDataGenerator.지하철역_등록되어_있음("사당역");
        StationResponse 과천역 = AcceptanceDataGenerator.지하철역_등록되어_있음("과천역");
        // and: 4호선이 사당역-10-과천역이 등록되어 있음.
        LineResponse 사호선 = AcceptanceDataGenerator.지하철_노선_등록되어_있음("사호선", "blue", 사당역, 과천역, 10);

        // when: 출발역 교대역, 도착역 압구정역의 경로를 조회한다.
        StationResponse 압구정역 = new StationResponse(99L, "압구정역", LocalDateTime.now(), LocalDateTime.now());
        ExtractableResponse<Response> 도착역_존재하지_않는_경우 = AcceptanceDataGenerator.지하철_경로_조회(교대역, 압구정역, 회원_19세);
        // then: 경로 조회가 실패한다.(존재하지 않은 출발역이나 도착역을 조회할 경우)
        경로_조회가_실패됨(도착역_존재하지_않는_경우);

        // when: 출발역 압구정역, 도착역 교대역의 경로를 조회한다.
        ExtractableResponse<Response> 출발역_존재하지_않는_경우 = AcceptanceDataGenerator.지하철_경로_조회(압구정역, 교대역, 회원_19세);
        // then: 경로 조회가 실패한다.(존재하지 않은 출발역이나 도착역을 조회할 경우)
        경로_조회가_실패됨(출발역_존재하지_않는_경우);

        // when: 출발역 강남역 도착역 강남역의 경로를 조회한다
        ExtractableResponse<Response> 출발역과_도착역이_같은_경우 = AcceptanceDataGenerator.지하철_경로_조회(강남역, 강남역, 회원_19세);
        // then: 경로 조회가 실패한다.(출발역과 도착역이 같은 경우)
        경로_조회가_실패됨(출발역과_도착역이_같은_경우);

        // when: 출발역 강남역, 도착역 과천역의 경로를 조회한다.
        ExtractableResponse<Response> 출발역과_도착역이_연결_되어있지않은_경우 = AcceptanceDataGenerator.지하철_경로_조회(강남역, 과천역, 회원_19세);
        // then: 경로 조회가 실패한다.(출발역과 도착역이 연결이 되어 있지 않은 경우)
        경로_조회가_실패됨(출발역과_도착역이_연결_되어있지않은_경우);

        // when: 비로그인 회원이 출발역 강남역 도착역 서초역의 경로를 조회한다(같은 라인 - 2호선 추가 요금 없음)
        ExtractableResponse<Response> 비회원_강남역_서초역_경로_조회 = AcceptanceDataGenerator.지하철_경로_조회(강남역, 서초역, new TokenResponse());
        비로그인_경로_조회_실패(비회원_강남역_서초역_경로_조회);


    }

    private void 경로가_조회됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 경유_지하철역_확인(ExtractableResponse<Response> response, List<StationResponse> expectedStations) {
        PathResponse pathResponse = response.as(PathResponse.class);
        List<Long> stationsIds = pathResponse.getStations().stream().map(StationResponse::getId).collect(Collectors.toList());
        List<Long> expected = expectedStations.stream().map(StationResponse::getId).collect(Collectors.toList());
        assertThat(stationsIds).containsExactlyElementsOf(expected);

    }

    private void 이동_거리_확인(ExtractableResponse<Response> response, int expected) {
        PathResponse pathResponse = response.as(PathResponse.class);
        assertThat(pathResponse.getDistance()).isEqualTo(expected);
    }

    private void 이용_요금_확인(ExtractableResponse<Response> response, int expected) {
        PathResponse pathResponse = response.as(PathResponse.class);
        assertThat(pathResponse.getSubwayFare()).isEqualTo(expected);
    }

    private void 경로_조회가_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 비로그인_경로_조회_실패(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
