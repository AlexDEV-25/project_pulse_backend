package com.example.project_pulse_backend.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum AppError {

    MISSING_TOKEN(1001, "Thiếu token xác thực", HttpStatus.UNAUTHORIZED),

    UPDATE_PROFILE_FAILED(1005, "Cập nhật thông tin cá nhân thất bại", HttpStatus.BAD_REQUEST),

    INVALID_IMAGE_FORMAT(1006, "Ảnh không đúng định dạng", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    TOKEN_EXPIRED(1010, "Token đã hết hạn", HttpStatus.UNAUTHORIZED),

    INVALID_TOKEN(1011, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),

    INVALID_SECRET_KEY_LENGTH(1012, "Độ dài secret key không hợp lệ", HttpStatus.INTERNAL_SERVER_ERROR),

    JOSE_PROCESSING_ERROR(1013, "Lỗi xử lý JWT/JOSE", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_NOT_FOUND(1015, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),

    EMAIL_ALREADY_EXISTS(1017, "Email đã tồn tại", HttpStatus.CONFLICT),

    USERNAME_ALREADY_EXISTS(1018, "Username đã tồn tại", HttpStatus.CONFLICT),

    INVALID_VERIFICATION_CODE(1020, "Mã xác thực không đúng", HttpStatus.BAD_REQUEST),

    EMAIL_NOT_FOUND(1021, "Email không tồn tại", HttpStatus.NOT_FOUND),

    GOOGLE_LOGIN_FAILED(1022, "Đăng nhập bằng Google thất bại", HttpStatus.UNAUTHORIZED),

    ACCESS_DENIED(1036, "Bạn không có quyền thực hiện chức năng này", HttpStatus.FORBIDDEN),

    LOGIN_FAILED(1044, "Đăng nhập thất bại", HttpStatus.UNAUTHORIZED),

    FAILED_TO_PARSE_DATA(1045, "Không thể phân tích dữ liệu", HttpStatus.BAD_REQUEST),

    INVALID_JSON_FORMAT(1046, "Dữ liệu JSON không hợp lệ", HttpStatus.BAD_REQUEST),

    CANNOT_SEND_EMAIL(1049, "Gửi email thất bại", HttpStatus.BAD_REQUEST),

    DEPARTMENT_NOT_FOUND(1, "Không tìm thấy phòng ban", HttpStatus.NOT_FOUND),

    ACCOUNT_LOCKED(2, "Tài khoản đang bị khóa", HttpStatus.BAD_REQUEST),

    ACCOUNT_NOT_FOUND(3, "Không tìm thấy tài khoản", HttpStatus.NOT_FOUND),

    INCORRECT_PASSWORD(1047, "Mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    ROLE_NOT_FOUND(5, "vai trò không tồn tại", HttpStatus.NOT_FOUND),

    PROJECT_NOT_FOUND(6, "Không tìm thấy project", HttpStatus.NOT_FOUND),
    USER_NOT_PM(7, "user không phải pm", HttpStatus.BAD_REQUEST),
    PHASE_NOT_FOUND(8, "Không tìm thấy phase", HttpStatus.NOT_FOUND),
    INVALID_DATE_RANGE(9, "Ngày bắt đầu phải trước ngày kết thúc", HttpStatus.BAD_REQUEST),
    PHASE_OUT_OF_PROJECT_RANGE(10, "phase phải nằm trong gia đoạn của project", HttpStatus.BAD_REQUEST),
    PROJECT_MEMBER_NOT_FOUND(11, "Không tìm thấy thành viên dự án", HttpStatus.NOT_FOUND),
    PROJECT_MEMBER_ALREADY_EXISTS(12, "Thành viên đã tồn tại trong dự án", HttpStatus.CONFLICT),
    PHASE_NOT_SAME_MONTH(13, "ngày bắt đầu và kết thúc 1 phase phải cùng tháng", HttpStatus.BAD_REQUEST);


    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

}
