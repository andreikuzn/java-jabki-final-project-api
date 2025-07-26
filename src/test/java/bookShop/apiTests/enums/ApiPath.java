package bookShop.apiTests.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiPath {
    AUTH_REGISTER("/auth/register"),
    AUTH_LOGIN("/auth/login"),
    AUTH_REGISTER_NOT_EXIST("/auth/register-not-exist"),
    AUTH_NOT_EXIST("/auth/not-exist");

    private final String path;
}

