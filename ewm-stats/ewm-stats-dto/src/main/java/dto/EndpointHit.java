package dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EndpointHit {//TODO validation
    private final String app = "ewm-main-service";
    private final String uri;
    private final String ip;
    private final String timestamp;
}
