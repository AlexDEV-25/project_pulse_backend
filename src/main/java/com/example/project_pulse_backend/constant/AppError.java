package com.example.project_pulse_backend.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum AppError {

    MISSING_TOKEN(1001, "Thiếu token xác thực", HttpStatus.UNAUTHORIZED),
    UPDATE_PROFILE_FAILED(1002, "Cập nhật thông tin cá nhân thất bại", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_FORMAT(1003, "Ảnh không đúng định dạng", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    TOKEN_EXPIRED(1004, "Token đã hết hạn", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1005, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    INVALID_SECRET_KEY_LENGTH(1006, "Độ dài secret key không hợp lệ", HttpStatus.INTERNAL_SERVER_ERROR),
    JOSE_PROCESSING_ERROR(1007, "Lỗi xử lý JWT/JOSE", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND(1008, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS(1009, "Email đã tồn tại", HttpStatus.CONFLICT),
    INVALID_VERIFICATION_CODE(1010, "Mã xác thực không đúng", HttpStatus.BAD_REQUEST),
    GOOGLE_LOGIN_FAILED(1011, "Đăng nhập bằng Google thất bại", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1012, "Bạn không có quyền thực hiện chức năng này", HttpStatus.FORBIDDEN),
    LOGIN_FAILED(1013, "Đăng nhập thất bại", HttpStatus.UNAUTHORIZED),
    FAILED_TO_PARSE_DATA(1014, "Không thể phân tích dữ liệu", HttpStatus.BAD_REQUEST),
    CANNOT_SEND_EMAIL(1015, "Gửi email thất bại", HttpStatus.BAD_REQUEST),
    DEPARTMENT_NOT_FOUND(1016, "Không tìm thấy phòng ban", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND(1017, "Không tìm thấy tài khoản", HttpStatus.NOT_FOUND),
    INCORRECT_PASSWORD(1018, "Mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    ROLE_NOT_FOUND(1019, "vai trò không tồn tại", HttpStatus.NOT_FOUND),
    PROJECT_NOT_FOUND(1020, "Không tìm thấy project", HttpStatus.NOT_FOUND),
    USER_NOT_PM(1021, "user không phải pm", HttpStatus.BAD_REQUEST),
    PHASE_NOT_FOUND(1022, "Không tìm thấy phase", HttpStatus.NOT_FOUND),
    INVALID_DATE_RANGE(1023, "Ngày bắt đầu phải trước ngày kết thúc", HttpStatus.BAD_REQUEST),
    PHASE_OUT_OF_PROJECT_RANGE(1024, "phase phải nằm trong gia đoạn của project", HttpStatus.BAD_REQUEST),
    PHASE_ALREADY_DONE(1025, "Phase đã hoàn thành và không thể thay đổi", HttpStatus.BAD_REQUEST),
    PHASE_IN_USE(1026, "Không thể xóa hoặc chuyển project của phase đang được sử dụng", HttpStatus.BAD_REQUEST),
    TASK_OUT_OF_PHASE_RANGE(1027, "Thời gian task phải nằm trong thời gian của phase", HttpStatus.BAD_REQUEST),
    TASK_PHASE_DONE(1028, "Không thể thay đổi task thuộc phase đã hoàn thành", HttpStatus.BAD_REQUEST),
    PHASE_EVM_SNAPSHOT_NOT_FOUND(1029, "Không tìm thấy snapshot EVM của phase đã hoàn tất", HttpStatus.NOT_FOUND),
    PHASE_EVM_SNAPSHOT_ALREADY_EXISTS(1030, "Snapshot EVM của phase đã tồn tại", HttpStatus.CONFLICT),
    PROJECT_MEMBER_NOT_FOUND(1031, "Không tìm thấy thành viên dự án", HttpStatus.NOT_FOUND),
    PROJECT_MEMBER_ALREADY_EXISTS(1032, "Thành viên đã tồn tại trong dự án", HttpStatus.CONFLICT),
    PHASE_INVALID_SPECIAL_DATE(1033, "Ngày nghỉ và ngày làm bù phải duy nhất và nằm trong thời gian phase", HttpStatus.BAD_REQUEST),
    ALLOCATION_ALREADY_EXISTS(1034, "Allocation already exists for this member in the phase", HttpStatus.CONFLICT),
    MEMBER_NOT_FOUND(1035, "nhân viên không có trong dự án", HttpStatus.NOT_FOUND),
    ALLOCATION_NOT_FOUND(1036, "không tìm thấy bản phân bổ", HttpStatus.NOT_FOUND),
    TOTAL_ALLOCATION_EXCEEDS_100(1037, "phân bổ nha viên không vượt quá 100", HttpStatus.BAD_REQUEST),
    TASK_NOT_FOUND(1038, "Không tìm thấy task", HttpStatus.NOT_FOUND),
    TASK_ASSIGNMENT_ALREADY_EXISTS(1039, "Nhân viên đã được phân công vào task", HttpStatus.CONFLICT),
    TASK_IN_PROGRESS_CANNOT_DELETE(1040, "Task đang trong trạng thái tiến hành không thể xóa", HttpStatus.BAD_REQUEST),
    TASK_NOTE_NOT_FOUND(1041, "Không tìm thấy task note", HttpStatus.NOT_FOUND),
    TASK_NOTE_NOT_PENDING(1042, "Chỉ có thể sửa hoặc xóa task note đang chờ xác nhận", HttpStatus.BAD_REQUEST),
    TASK_NOTE_WORKING_DATE_OUT_OF_RANGE(1043, "Ngày làm việc phải nằm trong thời gian của task", HttpStatus.BAD_REQUEST),
    TASK_ALREADY_DONE(1044, "Task đã hoàn thành và không thể chỉnh sửa", HttpStatus.BAD_REQUEST),
    ALLOCATION_RATE_NOT_FOUND(1045, "Không tìm thấy resource rate snapshot của allocation", HttpStatus.BAD_REQUEST),
    TASK_HAS_NO_ASSIGNMENTS(1046, "Task phải có nhân viên được phân công trước khi hoàn thành", HttpStatus.BAD_REQUEST),
    EARNING_TRANSACTION_NOT_FOUND(1047, "Không tìm thấy giao dịch earn gốc", HttpStatus.NOT_FOUND),
    INVALID_EARNING_TRANSFER(1048, "Thông tin chuyển earn không hợp lệ", HttpStatus.BAD_REQUEST),
    EARNING_BALANCE_NOT_ENOUGH(1049, "Số earn còn lại không đủ để chuyển", HttpStatus.BAD_REQUEST),
    ONLY_EMPLOYEE(1050, "chỉ có thể chọn employee", HttpStatus.BAD_REQUEST),
    PROJECT_IN_PROGRESS(1051, "Dự án đang trong trạng thái tiến hành", HttpStatus.BAD_REQUEST),
    PROJECT_ALREADY_DONE(1052, "Dự án đã hoàn thành", HttpStatus.BAD_REQUEST),
    PROJECT_MISMATCH(1053, "Dự án không khớp", HttpStatus.BAD_REQUEST),
    EARNING_NOT_FOUND(1054, "Không tìm thấy bản ghi giao dịch", HttpStatus.BAD_REQUEST),
    TASK_COST_TRANSACTION_NOT_FOUND(1055, "Không thấy bản ghi cost tiêu thụ", HttpStatus.BAD_REQUEST),
    DEPARTMENT_NOT_EMPTY(1056, "phòng ban vẫn còn người", HttpStatus.BAD_REQUEST),
    TASK_NOT_IN_PROGRESS(1057, "task chưa được tiến hành làm", HttpStatus.BAD_REQUEST),
    INVALID_TASK_STATUS_TRANSITION(1058, "Nhân viên chỉ có thể bắt đầu task đang chờ", HttpStatus.BAD_REQUEST),
    USER_NOT_BELONG_TO_TASK(1059, "nhân viên không thuộc về task", HttpStatus.BAD_REQUEST),
    PERMISSIONS_NOT_MATCH(1060, "quyền truyền vào không khớp", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}