package nextstep.subway.unit;

import nextstep.subway.applicaion.LineService;
import nextstep.subway.applicaion.StationService;
import nextstep.subway.applicaion.dto.SectionRequest;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.LineRepository;
import nextstep.subway.domain.Station;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LineServiceMockTest {
    @Mock
    private LineRepository lineRepository;
    @Mock
    private StationService stationService;

    @Test
    void addSection() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        Long lineId = 1L;
        Long upStationId = 1L;
        Long downStationId = 2L;
        when(stationService.findById(upStationId)).thenReturn(new Station(upStationId, "상행역"));
        when(stationService.findById(downStationId)).thenReturn(new Station(downStationId, "하행역"));
        when(lineRepository.findById(lineId)).thenReturn(Optional.of(new Line()));
        LineService lineService = new LineService(lineRepository, stationService);

        // when
        // lineService.addSection 호출
        SectionRequest sectionRequest = new SectionRequest(upStationId, downStationId, 10);
        lineService.addSection(lineId, sectionRequest);

        // then
        // lineService.findLineById 메서드를 통해 검증
        Assertions.assertThat(lineService.findById(lineId).getStations().size()).isEqualTo(2);
    }
}
