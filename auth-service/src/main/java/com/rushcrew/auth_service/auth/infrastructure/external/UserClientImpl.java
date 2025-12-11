package com.rushcrew.auth_service.auth.infrastructure.external;

import com.rushcrew.auth_service.auth.application.client.UserClient;
import com.rushcrew.auth_service.auth.application.command.LoginCommand;
import com.rushcrew.auth_service.auth.application.command.SignUpCommand;
import com.rushcrew.auth_service.auth.application.result.UserCreateResult;
import com.rushcrew.auth_service.auth.application.result.UserInfoResult;
import com.rushcrew.auth_service.auth.application.result.VerifyPasswordResult;
import com.rushcrew.auth_service.auth.domain.exception.AuthErrorCode;
import com.rushcrew.auth_service.auth.infrastructure.external.dto.UserCreateRequest;
import com.rushcrew.auth_service.auth.infrastructure.external.dto.UserCreateResponse;
import com.rushcrew.auth_service.auth.infrastructure.external.dto.UserInfoResponse;
import com.rushcrew.auth_service.auth.infrastructure.external.dto.VerifyPasswordRequest;
import com.rushcrew.auth_service.auth.infrastructure.external.dto.VerifyPasswordResponse;
import com.rushcrew.common.exception.BusinessException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserClientImpl implements UserClient {

    private final UserFeignClient userFeignClient;

    @Override
    public UserCreateResult createUser(SignUpCommand command) {
        UserCreateRequest request = UserCreateRequest.fromCommand(command);

        try {
            UserCreateResponse response = userFeignClient.createUser(request);
            return response.toResult();
        } catch (FeignException e) {
            if (e.status() == 409) {
                throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);
            }

            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    public VerifyPasswordResult verifyPassword(LoginCommand command) {
        VerifyPasswordRequest request = VerifyPasswordRequest.fromCommand(command);

        try {
            VerifyPasswordResponse response = userFeignClient.verifyPassword(request);
            return response.toResult();
        } catch (FeignException e) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    public UserInfoResult getUserById(Long userId) {
        try {
            UserInfoResponse response = userFeignClient.getUserById(userId);
            return response.toResult();
        } catch (FeignException e) {
            throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);
        }
    }
}
