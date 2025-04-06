package photo_mgmt_backend.exception.model;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.Map;

@Builder
public record ExceptionBody(
        ZonedDateTime timestamp,
        String code,
        String message,
        Map<String, String> details
) { }