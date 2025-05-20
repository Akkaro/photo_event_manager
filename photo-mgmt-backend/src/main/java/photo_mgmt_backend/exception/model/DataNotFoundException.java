package photo_mgmt_backend.exception.model;

import lombok.Getter;

@Getter
public class DataNotFoundException extends RuntimeException {

    private final String code;

    public DataNotFoundException(ExceptionCode exceptionCode, Object... args) {
        super(String.format(exceptionCode.getMessage(), args));
        this.code = exceptionCode.getCode();
    }
}