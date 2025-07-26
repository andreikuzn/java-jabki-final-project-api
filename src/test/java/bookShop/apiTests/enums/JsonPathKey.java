package bookShop.apiTests.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JsonPathKey {
    DATA_ID("data.id");

    private final String path;
}

