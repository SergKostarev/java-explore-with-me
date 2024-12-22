package dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ViewStats {
    private final String app = "ewm-main-service";
    private final String uri;
    private final long hits;
}