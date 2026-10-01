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
    PHASE_ALREADY_DONE(34, "Phase đã hoàn thành và không thể thay đổi", HttpStatus.BAD_REQUEST),
    PHASE_IN_USE(35, "Không thể xóa hoặc chuyển project của phase đang được sử dụng", HttpStatus.BAD_REQUEST),
    PHASE_PROJECT_MISMATCH(36, "Project của task phải trùng với project của phase", HttpStatus.BAD_REQUEST),
    TASK_OUT_OF_PHASE_RANGE(37, "Thời gian task phải nằm trong thời gian của phase", HttpStatus.BAD_REQUEST),
    TASK_PHASE_DONE(38, "Không thể thay đổi task thuộc phase đã hoàn thành", HttpStatus.BAD_REQUEST),
    PHASE_EVM_BAC_NOT_SET(39, "Project budget chưa được thiết lập để tính EVM", HttpStatus.BAD_REQUEST),
    PHASE_EVM_SNAPSHOT_NOT_FOUND(40, "Không tìm thấy snapshot EVM của phase đã hoàn tất", HttpStatus.NOT_FOUND),
    PHASE_EVM_SNAPSHOT_ALREADY_EXISTS(41, "Snapshot EVM của phase đã tồn tại", HttpStatus.CONFLICT),
    PROJECT_MEMBER_NOT_FOUND(11, "Không tìm thấy thành viên dự án", HttpStatus.NOT_FOUND),
    PROJECT_MEMBER_ALREADY_EXISTS(12, "Thành viên đã tồn tại trong dự án", HttpStatus.CONFLICT),
    PHASE_NOT_SAME_MONTH(13, "Phase start and end must be in the same month", HttpStatus.NOT_FOUND),

    ALLOCATION_ALREADY_EXISTS(14, "Allocation already exists for this member in the phase", HttpStatus.CONFLICT),

    MEMBER_NOT_FOUND(15, "nhân viên không có trong dự án", HttpStatus.NOT_FOUND),
    ALLOCATION_NOT_FOUND(16, "không tìm thấy bản phân bổ", HttpStatus.NOT_FOUND),
    TOTAL_ALLOCATION_EXCEEDS_100(17, "phân bổ nha viên không vượt quá 100", HttpStatus.BAD_REQUEST),
    TASK_NOT_FOUND(18, "Không tìm thấy task", HttpStatus.NOT_FOUND),
    TASK_ASSIGNMENT_ALREADY_EXISTS(19, "Nhân viên đã được phân công vào task", HttpStatus.CONFLICT),
    TASK_IN_PROGRESS_CANNOT_DELETE(20, "Task đang trong trạng thái tiến hành không thể xóa", HttpStatus.BAD_REQUEST),
    TASK_ASSIGNMENT_NOT_FOUND(21, "Không tìm thấy phân công task", HttpStatus.NOT_FOUND),
    TASK_NOTE_NOT_FOUND(22, "Không tìm thấy task note", HttpStatus.NOT_FOUND),
    TASK_NOTE_NOT_PENDING(23, "Chỉ có thể sửa hoặc xóa task note đang chờ xác nhận", HttpStatus.BAD_REQUEST),
    TASK_NOTE_WORKING_DATE_OUT_OF_RANGE(24, "Ngày làm việc phải nằm trong thời gian của task", HttpStatus.BAD_REQUEST),
    TASK_ALREADY_DONE(25, "Task đã hoàn thành và không thể chỉnh sửa", HttpStatus.BAD_REQUEST),
    TASK_CANNOT_START_AS_DONE(26, "Task không thể được tạo trực tiếp với trạng thái DONE", HttpStatus.BAD_REQUEST),
    ALLOCATION_NOT_UNIQUE_FOR_WORK_DATE(27, "Không xác định được allocation duy nhất cho ngày làm việc", HttpStatus.CONFLICT),
    ALLOCATION_RATE_NOT_FOUND(28, "Không tìm thấy resource rate snapshot của allocation", HttpStatus.BAD_REQUEST),
    TASK_HAS_NO_ASSIGNMENTS(29, "Task phải có nhân viên được phân công trước khi hoàn thành", HttpStatus.BAD_REQUEST),
    EARNING_TRANSACTION_NOT_FOUND(30, "Không tìm thấy giao dịch earn gốc", HttpStatus.NOT_FOUND),
    INVALID_EARNING_TRANSFER(31, "Thông tin chuyển earn không hợp lệ", HttpStatus.BAD_REQUEST),
    EARNING_BALANCE_NOT_ENOUGH(32, "Số earn còn lại không đủ để chuyển", HttpStatus.BAD_REQUEST),
    EARNING_CANNOT_REVERSE_AFTER_TRANSFER(33, "Không thể reverse earn đã phát sinh giao dịch transfer", HttpStatus.BAD_REQUEST),
    ONLY_EMPLOYEE(34, "chỉ có thể chọn employee", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

}
