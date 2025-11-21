package timeWizard.bilnut.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import timeWizard.bilnut.dto.ApiResponse;
import timeWizard.bilnut.dto.PasswordChangeRequest;
import timeWizard.bilnut.dto.PreferencesRequest;
import timeWizard.bilnut.dto.PreferencesResponse;
import timeWizard.bilnut.dto.UserInfoResponse;
import timeWizard.bilnut.dto.UserUpdateRequest;
import timeWizard.bilnut.dto.UserUpdateResponse;
import timeWizard.bilnut.security.CustomUserDetails;
import timeWizard.bilnut.service.UserService;

@Tag(name = "회원", description = "회원 정보 조회, 수정, 비밀번호 변경, 선호도 조회/저장 API")
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원 정보 조회", description = "현재 로그인한 사용자의 회원 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 정보 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponse data = userService.getUserInfo(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(data, "회원 정보 조회 성공"));
    }

    @Operation(summary = "회원 정보 수정", description = "현재 로그인한 사용자의 회원 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 정보 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @SecurityRequirement(name = "JWT")
    @PutMapping
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateUserInfo(
            @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserUpdateResponse data = userService.updateUserInfo(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(data, "회원 정보가 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 사용자의 비밀번호를 변경합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (현재 비밀번호 불일치)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @SecurityRequirement(name = "JWT")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.changePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 성공적으로 변경되었습니다."));
    }

    @Operation(summary = "선호도 조회", description = "현재 로그인한 사용자의 선호도를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "선호도 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<PreferencesResponse>> getPreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PreferencesResponse data = userService.getPreferences(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(data, "선호도 조회 성공"));
    }

    @Operation(summary = "선호도 저장/수정", description = "현재 로그인한 사용자의 선호도를 저장하거나 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "선호도 저장/수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @SecurityRequirement(name = "JWT")
    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<PreferencesResponse>> updatePreferences(
            @RequestBody PreferencesRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PreferencesResponse data = userService.updatePreferences(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(data, "선호도가 성공적으로 저장되었습니다."));
    }
}

