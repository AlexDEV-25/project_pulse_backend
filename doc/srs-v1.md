# SRS v1 - Project Pulse Backend (Đặc tả yêu cầu phần mềm, cập nhật API)

## 1. Mục tiêu hệ thống
**Project Pulse** là nền tảng quản trị dự án thông minh theo mô hình điểm số đóng góp (**Point-based Management**) kết hợp phương pháp quản trị giá trị thu được quốc tế (**Earned Value Management - EVM**). Hệ thống cung cấp giải pháp toàn diện cho doanh nghiệp:
- Quản lý vòng đời dự án và các giai đoạn thực hiện (Phase) chi tiết với lịch làm việc linh hoạt.
- Phân bổ nguồn lực nhân sự (Allocation) theo tỷ lệ phần trăm thời gian, tự động phát hiện và cảnh báo các ngày quá tải nhân lực (Overload Detection).
- Ghi nhận chi tiết nhật ký công việc (Task Note) theo ngày và số giờ làm thực tế, có quy trình phê duyệt của Quản lý dự án (PM).
- Quy đổi và ghi nhận điểm số đóng góp thực nhận của nhân viên (Earning Transaction) gắn liền với kết quả hoàn thành task.
- Tự động hạch toán chi phí điểm nội bộ (Task Cost Transaction) dựa trên thời gian làm việc thực tế và snapshot đơn giá nhân sự.
- Tính toán tự động và trực quan hóa bộ chỉ số đo lường sức khỏe dự án EVM (BAC, PV, EV, AC, CV, SV, CPI, SPI, EAC, ETC, VAC, TCPI).
- Quản lý thông báo người dùng theo thời gian thực (Notification) với deep linking trực tiếp tới trang và component liên quan (Nhiệm vụ, Điểm thưởng, Chuyển nhượng).
- Cung cấp hệ thống Dashboard thống kê phân quyền đa chiều dành cho Admin, Project Manager và Employee.
- Cung cấp lịch phân bổ theo tuần và danh sách task của nhân viên theo ngày, có thể lọc theo phase.

> [!NOTE]
> Hệ thống được thiết kế theo nguyên tắc **bảo mật thông tin tiền lương trực tiếp**. Đơn giá tài nguyên (`resourceRate`), snapshot đơn giá (`resourceRateSnapshot`), ngân sách khách hàng (`clientBudget`), và các giao dịch point (Earning / Cost) là các chỉ số nội bộ phục vụ quy đổi, kiểm soát ngân sách và đánh giá hiệu suất, được phân quyền hiển thị nghiêm ngặt theo vai trò.

---

## 2. Kiến trúc & Công nghệ (Tech Stack)
- **Ngôn ngữ & Nền tảng:** Java 21, Spring Boot 3.3.5.
- **Bảo mật & Xác thực:** Spring Security 6, Stateless JWT (Nimbus JOSE + JWT) mã hóa chữ ký HS512, cơ chế **2-step Role Selection** khi đăng nhập, kiểm soát quyền truy cập mức hàm (`@EnableMethodSecurity`, `@PreAuthorize`).
- **Cơ sở dữ liệu:** MySQL 8, Spring Data JPA / Hibernate (DDL Auto: `update`).
- **Ánh xạ dữ liệu:** MapStruct (`componentModel = "spring"`) tạo mapper dùng chung cho các chuyển đổi Entity–Request/Response tại helper và service. Logic nghiệp vụ, phân quyền dữ liệu và các giá trị tính toán như progress/allocationPoint vẫn được xử lý ở lớp tương ứng.
- **Xử lý đồng thời (Concurrency Control):** Cơ chế khóa bi quan (Pessimistic Locking `PESSIMISTIC_WRITE` / `SELECT ... FOR UPDATE`) áp dụng tại:
  - **Phân bổ nhân sự (`Allocation`):** Khóa toàn bộ các bản ghi `project_member` của nhân viên (`lockAllProjectMembersByUserId`) nhằm ngăn ngừa xung đột dữ liệu (race condition) khi tính toán lịch làm việc và quét các ngày quá tải (`allocation_overloaded_dates`).
  - **Chuyển nhượng điểm thưởng (`transferEarn`):** Khóa bi quan bản ghi giao dịch `EarningTransaction` nguồn (`findByIdForUpdate`) để ngăn ngừa tuyệt đối nguy cơ rút quá số dư (`double-spending`) khi có nhiều yêu cầu chuyển điểm đồng thời.
  - **Nhật ký công việc (`TaskNote`):** Khóa bi quan khi PM phê duyệt (`confirmTaskNotes`) hoặc nhân viên chỉnh sửa/xóa note (`findByIdForUpdate`).
- **An toàn dữ liệu thời gian (Null-safety & Date Range Guarantees):** `TimeHelper` được trang bị các lớp kiểm tra null (`Objects.requireNonNull`) và kiểm định phạm vi ngày tháng (`startAt <= endAt`, ngày task thuộc phase, ngày phase thuộc project), đảm bảo 100% không phát sinh lỗi `NullPointerException` (HTTP 500) khi tham số ngày tháng bị thiếu hoặc không hợp lệ.
- **Tích hợp bên ngoài (Integrations):**
  - **Google OAuth2:** Đăng nhập thông qua Google Identity & Google Userinfo API (Spring Cloud OpenFeign).
  - **Dịch vụ Email (Brevo / Sendinblue):** Gửi email chứa mã xác thực đổi/khôi phục mật khẩu.
  - **Quản lý Media (Cloudinary):** Upload, tối ưu hóa và xóa ảnh đại diện avatar (hỗ trợ JPG, JPEG, PNG, WEBP).
  - **Spring AI:** Tích hợp mô hình Gemini 2.5 Flash thông qua OpenAI standard API interface.

---

## 3. Vai trò người dùng & Ma trận phân quyền (RBAC)

### 3.1 Các vai trò trong hệ thống (Roles)
Hệ thống hỗ trợ 3 vai trò chính. Một người dùng (`User`) có thể sở hữu nhiều vai trò trong cơ sở dữ liệu (`user_role`). Khi đăng nhập, người dùng sẽ chủ động chọn một vai trò kích hoạt (**Active Role**) cho phiên làm việc để tạo JWT mang quyền tương ứng:
1. **ADMIN (Quản trị viên hệ thống):**
   - Quản trị tài khoản, danh sách nhân sự, phân vai trò, gán phòng ban, điều chỉnh đơn giá nhân sự (`resourceRate`).
   - Quản lý danh mục phòng ban (Department).
   - Khởi tạo, cập nhật dự án và phân công PM chịu trách nhiệm.
   - Nắm toàn quyền xem ngân sách khách hàng (`clientBudget`) và chi phí dự án (`projectBudget`).
   - Giám sát toàn bộ dự án, phase, chỉ số EVM và báo cáo thống kê tổng thể toàn hệ thống.
2. **PM (Project Manager - Quản lý dự án):**
   - Trực tiếp quản trị các dự án được phân công làm PM.
   - Thiết lập các giai đoạn (Phase), ngày làm bù (`makeUpDates`), ngày nghỉ (`offDates`).
   - Quản lý danh sách thành viên dự án (`ProjectMember`).
   - Lập kế hoạch phân bổ nhân sự (`Allocation`) cho từng Phase.
   - Tạo, phân công (`TaskAssignment`), theo dõi và cập nhật trạng thái Task.
   - Phê duyệt xác nhận nhật ký công việc (`TaskNote`) của nhân viên.
   - Hoàn thành task (kích hoạt tự động tạo giao dịch Earning & Cost).
   - Thực hiện chuyển nhượng điểm thưởng (`Transfer Earn`) giữa các thành viên trong dự án.
   - Theo dõi biểu đồ sức khỏe EVM và báo cáo tiến độ các dự án do mình quản lý.
3. **EMPLOYEE (Nhân viên / Thành viên dự án):**
   - Xem thông tin cá nhân, cập nhật ảnh đại diện avatar.
   - Xem danh sách các dự án mình được thêm vào làm thành viên (`ProjectMember`).
   - Xem các Phase, danh sách Task được phân công.
   - Nhận việc: Chuyển trạng thái task từ `TODO` sang `IN_PROGRESS`.
   - Tạo, chỉnh sửa, xóa Task Note (khi còn ở trạng thái `PENDING_CONFIRMATION`).
   - Theo dõi điểm tích lũy cá nhân (`EarningTransaction`), chi phí tiêu thụ (`TaskCostTransaction`) và Dashboard hiệu suất cá nhân.

---

### 3.2 Ma trận phân quyền chi tiết (65 Permissions)

| Nhóm phân hệ | Mã Permission | Mô tả chức năng | ADMIN | PM | EMPLOYEE |
| :--- | :--- | :--- | :---: | :---: | :---: |
| **User & Account** | `CREATE_ACCOUNT` | Tạo tài khoản người dùng mới | ✅ | ❌ | ❌ |
| | `CHANGE_PASSWORD` | Đổi mật khẩu cá nhân | ✅ | ✅ | ✅ |
| | `GET_USERS` | Lấy danh sách nhân sự (kèm lọc theo role, department) | ✅ | ✅ | ❌ |
| | `GET_USER_BY_ID` | Xem thông tin chi tiết nhân sự theo ID | ✅ | ✅ | ❌ |
| | `GET_MY_INFO` | Lấy thông tin cá nhân của phiên đăng nhập hiện tại | ✅ | ✅ | ✅ |
| | `UPDATE_AVATAR` | Tải lên và cập nhật ảnh đại diện cá nhân | ✅ | ✅ | ✅ |
| | `UPDATE_USER` | Cập nhật thông tin nhân viên, phòng ban, role, rate | ✅ | ❌ | ❌ |
| | `GET_STATISTICS_USERS` | Xem thống kê số lượng nhân sự theo vai trò | ✅ | ❌ | ❌ |
| **Department** | `CREATE_DEPARTMENT` | Tạo phòng ban mới | ✅ | ❌ | ❌ |
| | `UPDATE_DEPARTMENT` | Cập nhật tên hoặc ẩn/hiện phòng ban | ✅ | ❌ | ❌ |
| | `DELETE_DEPARTMENT` | Xóa phòng ban (chỉ khi không còn nhân sự) | ✅ | ❌ | ❌ |
| | `GET_STATISTICS_DEPARTMENTS`| Lấy số liệu thống kê phòng ban (active/hidden) | ✅ | ❌ | ❌ |
| **Project** | `CREATE_PROJECT` | Khởi tạo dự án mới | ✅ | ❌ | ❌ |
| | `UPDATE_PROJECT` | Cập nhật thông tin dự án, đổi PM, đổi trạng thái | ✅ | ❌ | ❌ |
| | `DELETE_PROJECT` | Xóa dự án (chỉ khi ở trạng thái DRAFT và chưa có member/phase) | ✅ | ❌ | ❌ |
| | `GET_PROJECTS` | Xem danh sách dự án (phân vùng dữ liệu theo role) | ✅ | ✅ | ✅ |
| | `GET_PROJECT_BY_ID` | Xem chi tiết 1 dự án (phân vùng trường dữ liệu theo role) | ✅ | ✅ | ✅ |
| **Phase** | `CREATE_PHASE` | Tạo giai đoạn mới cho dự án | ❌ | ✅ | ❌ |
| | `UPDATE_PHASE` | Cập nhật phase, lịch làm việc bù/nghỉ, chốt hoàn thành | ❌ | ✅ | ❌ |
| | `DELETE_PHASE` | Xóa phase (chỉ khi chưa có allocation và task) | ❌ | ✅ | ❌ |
| | `GET_PHASES_BY_PROJECT` | Xem danh sách các phase của dự án | ✅ | ✅ | ✅ |
| | `GET_PHASE_BY_ID` | Xem chi tiết một phase | ✅ | ✅ | ❌ |
| **Project Member** | `CREATE_PROJECT_MEMBER` | Thêm nhân viên vào dự án (chỉ nhận role EMPLOYEE) | ❌ | ✅ | ❌ |
| | `DELETE_PROJECT_MEMBER` | Xóa thành viên khỏi dự án (chỉ khi chưa có phân bổ và task) | ❌ | ✅ | ❌ |
| | `GET_PROJECT_MEMBERS_BY_PROJECT` | Xem danh sách thành viên dự án | ✅ | ✅ | ❌ |
| **Allocation** | `CREATE_ALLOCATION` | Lập phân bổ nhân sự cho phase (% thời gian) | ❌ | ✅ | ❌ |
| | `UPDATE_ALLOCATION` | Cập nhật tỷ lệ phân bổ nhân sự | ❌ | ✅ | ❌ |
| | `DELETE_ALLOCATION` | Xóa phân bổ nhân sự | ❌ | ✅ | ❌ |
| | `GET_ALLOCATIONS_BY_PROJECT`| Xem danh sách phân bổ trong dự án/phase | ✅ | ✅ | ✅ |
| | `GET_ALLOCATIONS_BY_ID` | Xem chi tiết bản ghi phân bổ | ✅ | ✅ | ✅ |
| | `GET_EMPLOYEE_WORK_SCHEDULE` | Nhân viên xem lịch phân bổ của mình theo tuần | ❌ | ❌ | ✅ |
| | `GET_EMPLOYEE_WORK_SCHEDULE_TASKS` | Nhân viên xem task được giao theo ngày và phase | ❌ | ❌ | ✅ |
| **Task & Assignment**| `CREATE_TASK` | Tạo task và gán danh sách nhân sự đóng góp | ❌ | ✅ | ❌ |
| | `UPDATE_TASK` | Cập nhật task, phân công lại, chốt hoàn thành | ❌ | ✅ | ❌ |
| | `DELETE_TASK` | Xóa task (chỉ khi task ở trạng thái TODO và chưa có task note) | ❌ | ✅ | ❌ |
| | `GET_TASKS_BY_PROJECT` | Xem danh sách task theo dự án/phase/nhân viên | ✅ | ✅ | ✅ |
| | `GET_TASK_BY_ID` | Xem chi tiết một task | ✅ | ✅ | ✅ |
| | `UPDATE_MY_TASK_STATUS_BY_ID` | Nhân viên chuyển trạng thái task từ TODO sang IN_PROGRESS | ❌ | ❌ | ✅ |
| **Task Note** | `CREATE_TASK_NOTE` | Nhân viên tạo ghi chú nhật ký công việc (giờ làm, ngày) | ❌ | ❌ | ✅ |
| | `UPDATE_TASK_NOTE` | Nhân viên chỉnh sửa ghi chú của mình (PENDING_CONFIRMATION) | ❌ | ❌ | ✅ |
| | `DELETE_TASK_NOTE` | Nhân viên xóa ghi chú của mình (PENDING_CONFIRMATION) | ❌ | ❌ | ✅ |
| | `GET_TASK_NOTES_BY_TASK` | Xem danh sách nhật ký công việc của task | ✅ | ✅ | ✅ |
| | `CONFIRM_TASK_NOTES` | PM phê duyệt xác nhận nhật ký công việc | ❌ | ✅ | ❌ |
| **Earning Point** | `GET_EARNINGS_BY_PROJECT`| Xem lịch sử giao dịch earn của dự án | ✅ | ✅ | ✅ |
| | `GET_EARNINGS_BY_ID` | Xem chi tiết một giao dịch earn | ✅ | ✅ | ✅ |
| | `TRANSFER_EARN` | PM chuyển nhượng điểm earn giữa các thành viên | ❌ | ✅ | ❌ |
| **Task Cost** | `GET_TASK_COSTS_BY_PROJECT` | Xem lịch sử chi phí point tiêu thụ của dự án | ✅ | ✅ | ✅ |
| | `GET_TASK_COSTS_BY_ID` | Xem chi tiết bản ghi chi phí task | ✅ | ✅ | ✅ |
| **EVM Health** | `GET_PROJECT_EVM_HEALTH` | Xem sức khỏe EVM tổng thể của dự án | ✅ | ✅ | ❌ |
| | `GET_PROJECT_PHASES_EVM` | Xem các chỉ số EVM theo phase của dự án | ✅ | ✅ | ❌ |
| | `GET_PROJECT_EMPLOYEES_EVM` | Xem hiệu suất EVM theo nhân viên trong dự án | ✅ | ✅ | ❌ |
| | `GET_PROJECT_EVM_FINANCIAL_SUMMARY` | Xem tổng hợp tài chính EVM của dự án | ✅ | ✅ | ❌ |
| | `EXPORT_PROJECT_EVM_CSV` | Xuất báo cáo EVM của dự án dạng CSV | ✅ | ✅ | ❌ |
| **Statistics** | `GET_STATISTICS_SUMMARY` | Thống kê số lượng dự án, quá hạn, cận hạn | ✅ | ✅ | ❌ |
| | `GET_PROJECT_STATUS_STATISTICS` | Thống kê cơ cấu dự án theo trạng thái (%) | ✅ | ✅ | ❌ |
| | `GET_PROJECT_OVERVIEW_STATISTICS` | Thống kê tổng quan số lượng dự án theo trạng thái | ✅ | ✅ | ❌ |
| | `GET_PROJECT_EV_AC_STATISTICS` | Thống kê đối sánh EV và AC theo từng dự án | ✅ | ✅ | ❌ |
| | `GET_IN_PROGRESS_PROJECT_STATISTICS`| Báo cáo tiến độ và chỉ số SPI/CPI các dự án đang chạy | ✅ | ✅ | ❌ |
| | `GET_EMPLOYEE_STATISTICS_SUMMARY` | Báo cáo cá nhân: tổng dự án, tổng point, task đã xong | ❌ | ❌ | ✅ |
| | `GET_EMPLOYEE_PROJECT_POINT_STATISTICS`| Thống kê điểm kiếm được theo từng dự án của cá nhân | ❌ | ❌ | ✅ |
| | `GET_EMPLOYEE_TASK_STATISTICS` | Thống kê task đang làm và task sắp tới hạn của cá nhân | ❌ | ❌ | ✅ |
| **Notification** | `GET_ALL_USER_NOTIFICATION` | Xem toàn bộ danh sách thông báo của người dùng | ✅ | ✅ | ✅ |
| | `GET_UNREAD_USER_NOTIFICATION` | Xem danh sách thông báo chưa đọc của người dùng | ✅ | ✅ | ✅ |
| | `READ_NOTIFICATION` | Đánh dấu một thông báo đã đọc | ✅ | ✅ | ✅ |
| | `READ_ALL_NOTIFICATION` | Đánh dấu tất cả thông báo đã đọc | ✅ | ✅ | ✅ |

---

## 4. Đối tượng nghiệp vụ & Mô hình dữ liệu (Domain Entities)

### 4.1 User & Auth
- **User (`users`):** Lưu trữ thông tin nhân sự.
  - `id` (Long, PK, Auto Increment)
  - `user_name` (String, Not Null)
  - `position` (String, Not Null)
  - `resource_rate` (BigDecimal, tỷ lệ chi phí giờ/ngày cơ bản)
  - `avatar_url` (String, URL ảnh Cloudinary)
  - `department_id` (Long, FK -> `departments.id`, Not Null)
  - Quan hệ nhiều-nhiều với `Role` qua bảng nối `user_role` (`user_id`, `role_name`).
- **Auth (`authentications`):** Lưu trữ thông tin đăng nhập và cơ chế đặt lại mật khẩu.
  - `id` (Long, PK)
  - `email` (String, Unique, Not Null)
  - `password` (String, BCrypt Hash, Not Null)
  - `forgot_password_code` (String, mã UUID kích hoạt đặt lại mật khẩu)
  - `forgot_password_code_expires_at` (LocalDateTime, hết hạn sau 15 phút)
  - `user_id` (Long, FK 1-1 -> `users.id`)

### 4.2 Department (`departments`)
- Đại diện phòng ban trong tổ chức (`id`, `department_name`, `hidden`).
- Khởi tạo mặc định: `ADMIN`, `DEV`, `BA`, `QC`.
- Ràng buộc: Không được xóa phòng ban nếu vẫn còn nhân sự trực thuộc (`DEPARTMENT_NOT_EMPTY` - mã 1056).

### 4.3 Role & Permission
- **Role (`roles`):** Khóa chính là `name` (`ADMIN`, `PM`, `EMPLOYEE`), `description`.
- **Permission (`permissions`):** Khóa chính là `name` (63 quyền), `description`.
- Liên kết nhiều-nhiều qua bảng `role_permission` (`role_name`, `permission_name`).

### 4.4 Project (`projects`)
- Đại diện một dự án phần mềm:
  - `id` (Long, PK)
  - `project_name` (String, Not Null)
  - `pm_id` (Long, FK -> `users.id`, người được chọn phải sở hữu role `PM`)
  - `start_at`, `end_at` (LocalDate, `startAt` phải trước hoặc bằng `endAt`)
  - `client_budget` (BigDecimal, ngân sách khách hàng - **chỉ ADMIN được xem**)
  - `project_budget` (BigDecimal, ngân sách nội bộ dùng làm BAC - **ADMIN và PM được xem**)
  - `description` (String, Lob)
  - `project_status` (Enum `ProjectStatus`, mặc định khởi tạo là `DRAFT`)
- **Quy tắc phân vùng xem dữ liệu Project:**
  - `ADMIN`: Xem tất cả dự án; hiển thị đầy đủ `clientBudget` và `projectBudget`.
  - `PM`: Chỉ xem các dự án mà mình là PM phụ trách (`pm_id == user.id`); `clientBudget` bị ẩn (`null`).
  - `EMPLOYEE`: Chỉ xem các dự án mà mình có mặt trong bảng `project_member`; cả `clientBudget` và `projectBudget` đều bị ẩn (`null`).
- **Quy tắc nghiệp vụ vòng đời Dự án:**
  - **Chuyển sang `DONE` (Nghiệm thu hoàn tất):** Bắt buộc **TẤT CẢ** các Phase thuộc dự án phải ở trạng thái `DONE`. Nếu còn bất kỳ Phase nào chưa hoàn tất (`PLANNED` hoặc `IN_PROGRESS`), hệ thống chặn lại và ném lỗi `HAS_PHASE_NOT_DONE` (mã 1141).
  - Dự án đã ở trạng thái `DONE` thì không cho phép chỉnh sửa thông tin hoặc xóa (`PROJECT_ALREADY_DONE` - mã 1052).
  - **Xóa dự án:** Chỉ áp dụng khi dự án đang ở trạng thái `DRAFT` VÀ chưa có bất kỳ thành viên (`ProjectMember`) hoặc giai đoạn (`Phase`) nào. Nếu đã phát sinh thành viên hoặc giai đoạn, ném lỗi `PROJECT_IN_USE` (mã 1140).

### 4.5 ProjectMember (`project_member`)
- Danh sách thành viên tham gia dự án do PM chỉ định:
  - `id` (Long, PK)
  - `project_id` (Long, FK -> `projects.id`)
  - `user_id` (Long, FK -> `users.id`)
  - Ràng buộc duy nhất `uk_project_user (project_id, user_id)`.
  - Điều kiện thêm: User được thêm vào phải sở hữu vai trò `EMPLOYEE`.
- **Ràng buộc khi xóa thành viên (`deleteProjectMember`):**
  - Trước khi xóa một bản ghi `ProjectMember`, hệ thống bắt buộc kiểm tra xem thành viên này đã được phân bổ (`Allocation`) trong bất kỳ Phase nào hay chưa (`allocationRepo.existsByMember_Id`), hoặc đã được phân công công việc (`TaskAssignment`) hay chưa (`taskAssignmentRepo.existsByEmployee_Id`).
  - Nếu đã phát sinh phân bổ hoặc phân công, hệ thống lập tức chặn lại và ném lỗi `PROJECT_MEMBER_IN_USE` (mã 1135) nhằm bảo đảm an toàn dữ liệu và ngăn chặn hoàn toàn lỗi Database Foreign Key Constraint Violation (HTTP 500).

### 4.6 Phase (`phases`)
- Giai đoạn cụ thể của dự án:
  - `id` (Long, PK)
  - `project_id` (Long, FK -> `projects.id`)
  - `phase_name` (String, Unique, Not Null)
  - `start_at`, `end_at` (LocalDate, bắt buộc nằm trọn vẹn trong khoảng thời gian của Project)
  - `status` (Enum `PhaseStatus`: `PLANNED`, `IN_PROGRESS`, `DONE`)
  - `make_up_date` (Bảng phụ `phase_make_up_date`: các ngày làm việc bù rơi vào cuối tuần Thứ Bảy / Chủ Nhật)
  - `off_date` (Bảng phụ `phase_off_date`: các ngày nghỉ lễ / nghỉ phép rơi vào ngày làm việc thường Thứ Hai - Thứ Sáu)
- **Quy tắc nghiệp vụ vòng đời Giai đoạn:**
  - **Khởi động Phase:** Phase chỉ có thể chuyển sang trạng thái `IN_PROGRESS` khi Project chứa nó đang ở trạng thái `IN_PROGRESS`. Nếu Project đang ở `DRAFT` hoặc `ON_HOLD`, ném lỗi `PROJECT_NOT_IN_PROGRESS`.
  - **Điều kiện chuyển sang `DONE` (Phương án A - Nghiêm ngặt):**
    1. Phase hiện tại phải đang ở trạng thái `IN_PROGRESS`. Nghiêm cấm chuyển trực tiếp từ `PLANNED` sang `DONE` (ném lỗi `PHASE_NOT_IN_PROGRESS` - mã 1139) để không bỏ sót quy trình thực hiện và tính toán EVM.
    2. **TẤT CẢ** các Task thuộc Phase bắt buộc phải ở trạng thái `DONE` hoặc `CANCELED`. Nếu còn bất kỳ Task nào ở trạng thái `TODO`, `IN_PROGRESS` hoặc `BLOCKED`, hệ thống ném lỗi `HAS_TASK_NOT_DONE` (mã 1137) / `PHASE_NOT_IN_PROGRESS`.
    3. Tự động tính toán và lưu bản ghi bất biến `PhaseEvmSnapshot` (BAC, PV, EV, AC).
  - **Tính bất biến:** Khi Phase đã `DONE`, không cho phép thay đổi thông tin Phase (`PHASE_ALREADY_DONE` - mã 1025) và không cho phép thay đổi trạng thái của các Task trực thuộc (`TASK_PHASE_DONE` - mã 1028).
  - Không cho phép xóa Phase nếu đã phát sinh `Allocation` hoặc `Task` (`PHASE_IN_USE` - mã 1026).

### 4.7 Allocation (`allocations`)
- Kế hoạch phân bổ nhân sự vào giai đoạn:
  - `id` (Long, PK)
  - `phase_id` (Long, FK -> `phases.id`)
  - `member_id` (Long, FK -> `project_member.id`)
  - `allocation_percentage` (Integer, tối thiểu 1%)
  - `resource_rate_snapshot` (BigDecimal, snapshot giá trị `User.resourceRate` tại thời điểm tạo phân bổ; cố định bất biến trong suốt Phase)
  - `overloadedDates` (Bảng phụ `allocation_overloaded_dates`: danh sách các ngày và tổng tỷ lệ phân bổ của nhân sự bị vượt quá 100%).
- **Chính sách phân bổ vượt 100% & Quản lý quá tải:**
  - Hệ thống **cho phép** phân bổ vượt quá 100% nhằm tạo sự linh hoạt tối đa cho các tình huống dự án đặc thù (làm thêm giờ - OT, kiêm nhiệm nhiều dự án song song).
  - Mỗi khi tạo/sửa/xóa phân bổ, hệ thống tự động quét lại toàn bộ lịch công tác của nhân sự đó trên tất cả các Phase. Những ngày có tổng tỷ lệ phân bổ vượt quá 100% sẽ được tự động lưu vào `allocation_overloaded_dates` (`allocation_id`, `overloaded_date`, `total_allocation_percentage`) để cảnh báo trực quan cho PM.
- **Cơ chế chống Race Condition:** Áp dụng khóa bi quan (`lockAllProjectMembersByUserId`) trên toàn bộ các bản ghi `project_member` của user đó khi tạo, cập nhật hoặc xóa phân bổ.

### 4.8 Task (`tasks`) & TaskAssignment (`task_assignment`)
- **Task:** Đơn vị công việc thuộc một giai đoạn:
  - `id` (Long, PK)
  - `phase_id` (Long, FK -> `phases.id`)
  - `task_name` (String, Not Null)
  - `start_at`, `end_at` (LocalDate, bắt buộc nằm trong thời gian của Phase)
  - `task_point` (BigDecimal, điểm số giá trị của task, > 0)
  - `task_status` (Enum `TaskStatus`: `TODO`, `IN_PROGRESS`, `DONE`, `BLOCKED`, `CANCELED`)
  - `description` (String, Lob)
- **TaskAssignment:** Phân công đóng góp cho Task:
  - `id` (Long, PK)
  - `task_id` (Long, FK -> `tasks.id`)
  - `employee_id` (Long, FK -> `project_member.id`)
  - `contribution_percentage` (Integer, từ 0 đến 100, đại diện cho % công sức/đóng góp vào kết quả hoàn thành task).
  - Ràng buộc duy nhất `uk_task_employee (task_id, employee_id)`.
  - Được quản lý và gán trực tiếp lồng trong payload tạo/sửa Task (không tách API riêng).
- **Quy tắc nghiệp vụ Task:**
  - Task chỉ có thể chuyển sang `IN_PROGRESS` khi Phase đang ở trạng thái `IN_PROGRESS` (`PHASE_NOT_IN_PROGRESS`).
  - Task thuộc Phase đã `DONE` thì bị khóa hoàn toàn, không thể thay đổi trạng thái (`TASK_PHASE_DONE` - mã 1028).
  - **Quy tắc xóa Task (`deleteTask`):**
    - Task chỉ có thể xóa khi đang ở trạng thái `TODO` (`TASK_IN_PROGRESS_CANNOT_DELETE` - mã 1040).
    - Task **chưa có bất kỳ nhật ký công việc (`TaskNote`) nào** được tạo. Nếu đã phát sinh Task Note, ném lỗi `TASK_IN_USE` (mã 1142).
    - *Lưu ý:* Hệ thống cho phép xóa Task ngay cả khi đã có bản ghi phân công `TaskAssignment` (hệ thống tự động giải phóng các bản ghi phân công này khi xóa Task), giúp PM dễ dàng sửa sai khi lỡ phân công nhầm.

### 4.9 TaskNote (`task_note`)
- Nhật ký thực hiện công việc hàng ngày của nhân viên:
  - `id` (Long, PK)
  - `task_id` (Long, FK -> `tasks.id`)
  - `author_id` (Long, FK -> `project_member.id`)
  - `content` (String, Lob, Not Null)
  - `working_hours` (Integer, số giờ làm việc thực tế trong ngày, từ 0 đến 8 giờ)
  - `working_date` (LocalDate, bắt buộc nằm trong khoảng `startAt` - `endAt` của Task)
  - `created_at` (LocalDate, ngày tạo)
  - `updated_at` (LocalDate, ngày cập nhật)
  - `confirmed_at` (LocalDateTime, thời điểm PM phê duyệt)
  - `task_note_status` (Enum `TaskNoteStatus`: `PENDING_CONFIRMATION`, `CONFIRMED`)
- **Quy tắc:** Chỉ được thêm note khi Task ở trạng thái `IN_PROGRESS`. Nhân viên chỉ được sửa/xóa note của chính mình khi note đang ở trạng thái `PENDING_CONFIRMATION`.

### 4.10 EarningTransaction (`earning_transaction`)
- Bản ghi giao dịch điểm thưởng thực nhận của nhân sự:
  - `id` (Long, PK)
  - `task_id` (Long, FK -> `tasks.id`)
  - `executor_id` (Long, FK -> `project_member.id`)
  - `reference_transaction_id` (Long, FK tự tham chiếu tới `earning_transaction.id`)
  - `points` (BigDecimal, số điểm thưởng)
  - `earning_transaction_type` (Enum: `EARN`, `TRANSFER`)
  - `reason` (String, Lob)
- **Cơ chế Transfer Earn & Khóa bi quan chống Double-spending:**
  - Chỉ PM phụ trách dự án mới có quyền chuyển điểm thưởng.
  - Sử dụng khóa bi quan (`findByIdForUpdate` với `LockModeType.PESSIMISTIC_WRITE`) trên bản ghi `EarningTransaction` nguồn để đảm bảo tính tuần tự, loại bỏ hoàn toàn khả năng 2 giao dịch chuyển điểm đồng thời làm âm số dư điểm khả dụng (`availablePoints = points gốc - tổng điểm đã transfer`).

### 4.11 TaskCostTransaction (`task_cost_transaction`)
- Bản ghi hạch toán chi phí điểm nội bộ phát sinh từ task:
  - `id` (Long, PK)
  - `task_id` (Long, FK -> `tasks.id`)
  - `executor_id` (Long, FK -> `project_member.id`)
  - `workdays` (BigDecimal, số ngày công quy đổi = tổng `TaskNote.workingHours / 8.0`)
  - `points` (BigDecimal, chi phí điểm tiêu thụ = `workdays * Allocation.resourceRateSnapshot`)
  - `reason` (String, Lob)
- **Tối ưu hóa hiệu năng:** Khi sinh chi phí cho nhân sự lúc Task hoàn thành, hệ thống truy vấn thông tin `Allocation` (lấy `resourceRateSnapshot`) một lần duy nhất trước vòng lặp tính toán ngày công, loại bỏ triệt để vấn đề N+1 query.

### 4.12 PhaseEvmSnapshot (`phase_evm_snapshots`)
- Lưu trữ snapshot bất biến của các chỉ số EVM tại thời điểm Phase hoàn thành (`status = DONE`):
  - `id` (Long, PK, Auto Increment)
  - `phase_id` (Long, 1-1 FK -> `phases.id`, Unique)
  - `captured_at` (LocalDateTime)
  - `bac` (BigDecimal, Budget at Completion của Phase)
  - `pv` (BigDecimal, Planned Value)
  - `ev` (BigDecimal, Earned Value)
  - `ac` (BigDecimal, Actual Cost)
- **Ý nghĩa:** Khi xem báo cáo phase qua `GET /api/evm/project/{projectId}/phases`, dữ liệu EVM của phase đã hoàn thành được lấy từ snapshot lịch sử thay vì tính toán động, bảo đảm tính bất biến của dữ liệu quá khứ.

### 4.13 Notification (`notifications`) & UserNotification (`user_notification`)
- **Notification (`notifications`):** Nội dung thông báo dùng chung hoặc được liên kết:
  - `id` (Long, PK, Auto Increment)
  - `content` (String, Not Null)
  - `link` (String, đường dẫn deep link điều hướng frontend, vd: `/projects/{projectId}?tab=tasks&taskId={taskId}`, `/projects/{projectId}?tab=earnings`)
  - `type` (Enum `NotificationType`: `INFO`, `WARNING`, `SUCCESS`, `ERROR`)
- **UserNotification (`user_notification`):** Bản ghi thông báo gửi tới từng người dùng cụ thể:
  - `id` (Long, PK, Auto Increment)
  - `sender_id` (Long, FK -> `users.id`, Not Null)
  - `receiver_id` (Long, FK -> `users.id`, Not Null)
  - `notification_id` (Long, FK -> `notifications.id`)
  - `is_read` (Boolean, cờ đánh dấu đã đọc, mặc định `false`)
  - `created_at` (LocalDateTime, thời điểm phát sinh thông báo)
- **Cơ chế tự động sinh thông báo & Deep link Routing:**
  - **Tạo nhật ký công việc (Task Note):** Tự động gửi thông báo `INFO` tới PM phụ trách dự án: `"{tên nhân viên} vừa tạo 1 task note trong task {tên task}"`, link: `/projects/{projectId}?tab=tasks&taskId={taskId}`.
  - **Giao task mới:** Tự động gửi thông báo `INFO` tới nhân viên nhận việc: `"Bạn vừa được giao task {tên task}"`, link: `/projects/{projectId}?tab=tasks&taskId={taskId}`.
  - **Loại bỏ khỏi task:** Tự động gửi thông báo `WARNING` tới nhân viên bị gỡ bỏ: `"Bạn đã được loại bỏ khỏi task {tên task}"`, link: `/projects/{projectId}?tab=tasks`.
  - **Hoàn thành task (Done):** Tự động gửi thông báo `SUCCESS` tới từng nhân sự nhận điểm thưởng: `"Bạn vừa nhận được {điểm} point từ task {tên task}"`, link: `/projects/{projectId}?tab=earnings`.
  - **Chuyển nhượng điểm thưởng (Transfer Earn):**
    - Gửi thông báo `INFO` tới nhân viên nhận điểm: `"Bạn vừa được chuyển cho {điểm} point của {người chuyển} từ task {tên task}"`, link: `/projects/{projectId}?tab=earnings&type=TRANSFER`.
    - Gửi thông báo `WARNING` tới nhân viên bị trừ điểm: `"Bạn vừa bị chuyển {điểm} point từ task {tên task} sang cho {người nhận}"`, link: `/projects/{projectId}?tab=earnings&type=TRANSFER`.
- **Nguyên tắc bảo mật & Tính bất biến:**
  - **Chống IDOR:** Người dùng chỉ có quyền truy xuất và thao tác trên các thông báo mà mình là người nhận (`receiver_id == currentUser.id`).
  - **Idempotency:** API đánh dấu đọc (`read`) kiểm tra trạng thái trước khi cập nhật, đảm bảo an toàn và tính nhất quán khi gọi nhiều lần.

---

## 5. Trạng thái nghiệp vụ (Business Enums)

### 5.1 ProjectStatus
- `DRAFT`: Dự án mới lập, đang soạn thảo (chỉ trạng thái này mới được xóa dự án).
- `IN_PROGRESS`: Dự án đang trong quá trình thực hiện.
- `ON_HOLD`: Dự án tạm ngưng.
- `DONE`: Dự án đã nghiệm thu hoàn thành (yêu cầu tất cả Phase phải `DONE`).
- `CANCELLED`: Dự án bị hủy bỏ.

### 5.2 PhaseStatus
- `PLANNED`: Giai đoạn đã lập kế hoạch.
- `IN_PROGRESS`: Giai đoạn đang diễn ra (yêu cầu Project phải `IN_PROGRESS`).
- `DONE`: Giai đoạn đã hoàn thành (yêu cầu tất cả Task phải `DONE` hoặc `CANCELED`; tự động tạo `PhaseEvmSnapshot`).

### 5.3 TaskStatus
- `TODO`: Công việc mới tạo, chờ thực hiện.
- `IN_PROGRESS`: Công việc đang được nhân viên tiến hành (yêu cầu Phase phải `IN_PROGRESS`).
- `DONE`: Công việc đã hoàn tất (kích hoạt tạo giao dịch Earning và Cost).
- `BLOCKED`: Công việc bị đình trệ/nghẽn.
- `CANCELED`: Công việc bị hủy.

### 5.4 TaskNoteStatus
- `PENDING_CONFIRMATION`: Chờ PM phê duyệt (nhân viên có thể sửa/xóa).
- `CONFIRMED`: Đã được PM phê duyệt (không thể sửa/xóa, đủ điều kiện để tính Task Cost).

### 5.5 EarningTransactionType
- `EARN`: Điểm thưởng phát sinh do hoàn thành Task.
- `TRANSFER`: Điểm thưởng chuyển nhượng giữa các nhân viên trong dự án.

### 5.6 ScheduleStatus (Đánh giá tiến độ dự án)
- `NO_DATA`: Chưa đủ dữ liệu SPI.
- `FAST`: Tiến độ vượt kế hoạch (`SPI > 1.0`).
- `ON_SCHEDULE`: Đúng tiến độ kế hoạch (`SPI == 1.0`).
- `SLOW`: Tiến độ chậm so với kế hoạch (`SPI < 1.0`).

### 5.7 NotificationType
- `INFO`: Thông báo thông tin (giao việc, có task note mới, nhận chuyển nhượng point).
- `WARNING`: Thông báo cảnh báo (bị gỡ khỏi task, bị chuyển trừ point).
- `SUCCESS`: Thông báo kết quả tích cực (hoàn tất task, nhận điểm thưởng Earning).
- `ERROR`: Thông báo lỗi phát sinh trong quy trình nghiệp vụ.

---

## 6. Quy tắc nghiệp vụ & Công thức tính toán chi tiết

### 6.1 Quy tắc tính ngày làm việc thực tế (`TimeHelper.workingDates`)
- **Tập ngày chuẩn:** Mặc định các ngày từ Thứ Hai đến Thứ Sáu nằm trong khoảng `[startAt, endAt]`.
- **Cộng dồn ngày làm bù (`makeUpDates`):** Chỉ chấp nhận các ngày rơi vào Thứ Bảy hoặc Chủ Nhật nằm trong khoảng thời gian của Phase.
- **Loại trừ ngày nghỉ (`offDates`):** Loại bỏ các ngày nghỉ rơi vào các ngày thường (Thứ Hai đến Thứ Sáu).
- Số ngày công chuẩn của Phase: `phaseWorkdays = workingDates(startAt, endAt, makeUpDates, offDates).size()`.
- Số ngày công lũy kế đến ngày $T$ (`elapsedWorkdays`): Số ngày làm việc tính từ `startAt` đến $\min(T, endAt)$.
- **Bảo đảm an toàn null:** Tất cả các thao tác tính toán ngày làm việc đều kiểm tra null-safety, trả về tập rỗng hoặc 0 ngày nếu bất kỳ tham số bắt buộc nào bị null.

### 6.2 Công thức tính điểm phân bổ (`Allocation.allocationPoint`)
$$\text{allocationPoint} = \frac{\text{phaseWorkdays} \times \text{resourceRateSnapshot} \times \text{allocationPercentage}}{100}$$
*(Làm tròn 2 chữ số thập phân, quy tắc `RoundingMode.HALF_UP`)*.

### 6.3 Thuật toán phát hiện quá tải nhân sự (`Allocation Overloaded Dates`)
- Mỗi khi tạo/sửa/xóa một bản ghi Allocation cho nhân viên $U$:
  1. Khóa bi quan toàn bộ bản ghi `project_member` của user $U$ (`lockAllProjectMembersByUserId`).
  2. Lấy toàn bộ các Allocation đang có của user $U$ trên tất cả các Phase trong hệ thống.
  3. Tính tập các ngày làm việc thực tế (`workingDates`) cho từng Allocation.
  4. Gom tổng tỷ lệ phân bổ phần trăm (`dailyTotals`) theo từng ngày lịch: $\text{dailyTotals}[\text{date}] = \sum \text{allocationPercentage}$.
  5. Nếu tại ngày bất kỳ, $\text{dailyTotals}[\text{date}] > 100\%$, ngày đó được coi là **ngày quá tải** và được lưu vào bảng `allocation_overloaded_dates` của Allocation tương ứng cùng giá trị `totalAllocationPercentage`.
- **Ý nghĩa:** Không chặn cứng việc lưu Allocation để hỗ trợ các trường hợp làm thêm giờ / phân bổ đặc biệt, nhưng minh bạch và lưu vết cảnh báo chính xác cho PM.

### 6.4 Tiến độ thực tế của Phase (`calculateProgress`)
$$\text{Progress} (\%) = \frac{\sum \text{EarningPoints}(\text{phaseId}, \text{EARN}) \times 100}{\sum \text{TaskPoints}(\text{phaseId})}$$
*(Nếu tổng điểm task bằng 0 thì tiến độ là 0%)*.

### 6.5 Quy trình hoàn tất Task & Sinh giao dịch tự động
Khi PM cập nhật trạng thái Task sang `DONE`:
1. **Kiểm tra tiền điều kiện:**
   - Task hiện tại phải đang ở trạng thái `IN_PROGRESS` (không thể chuyển trực tiếp từ `TODO` sang `DONE`).
   - Task phải có ít nhất một bản ghi phân công `TaskAssignment`.
   - Mỗi nhân sự trong `TaskAssignment` **bắt buộc phải có ít nhất một Task Note đã được PM duyệt** (`TaskNoteStatus.CONFIRMED`). Nếu thiếu, ném lỗi `TASK_NOTE_NOT_FOUND`.
2. **Tự động sinh giao dịch `EarningTransaction` (Điểm thu nhập):**
   - Với mỗi nhân viên tham gia:
     $$\text{earnedPoints} = \frac{\text{task.taskPoint} \times \text{assignment.contributionPercentage}}{100}$$
   - Tạo bản ghi `EarningTransaction` với `earningTransactionType = EARN`, lý do: `"Hoàn thành task: " + taskName`.
3. **Tự động sinh giao dịch `TaskCostTransaction` (Chi phí nội bộ tiêu thụ):**
   - Lấy `Allocation` của nhân viên đó trong Phase chứa task (bắt buộc phải tồn tại `resourceRateSnapshot`).
   - Tính tổng ngày công quy đổi từ các `TaskNote` đã duyệt:
     $$\text{dayFraction} = \frac{\text{workingHours}}{8.0}, \quad \text{workdays} = \sum \text{dayFraction}$$
     $$\text{costPoints} = \sum (\text{dayFraction} \times \text{resourceRateSnapshot})$$
   - Tạo bản ghi `TaskCostTransaction` với `workdays` và `points`, lý do: `"Chi phí hoàn thành task: " + taskName`.

### 6.6 Quy tắc chuyển nhượng điểm (`Transfer Earn`)
- Chỉ PM phụ trách dự án mới có quyền chuyển điểm thưởng.
- Giao dịch nguồn phải thuộc loại `EARN` của chính dự án đó.
- Nhân viên nhận chuyển nhượng phải là thành viên hợp lệ trong dự án và khác với nhân viên nguồn.
- Kiểm tra số dư: Số điểm chuyển phải $\le$ số điểm còn lại của giao dịch nguồn ($\text{points gốc} - \sum \text{points đã chuyển nhượng}$).
- Áp dụng khóa bi quan (`PESSIMISTIC_WRITE`) trên giao dịch nguồn để bảo đảm an toàn giao dịch tuyệt đối khi xử lý đồng thời.

### 6.7 Bộ công thức Quản trị giá trị thu được (EVM)
Áp dụng thang đo điểm số để tính toán cho từng Phase và toàn Dự án:
- **BAC (Budget at Completion):**
  - Của Phase: Tổng `allocationPoint` của toàn bộ các bản ghi phân bổ trong Phase.
  - Của Dự án: `project.projectBudget`.
- **PV (Planned Value - Giá trị kế hoạch):**
  - Tính tại thời điểm $T$:
    $$\text{PV}_{\text{phase}}(T) = \sum_{\text{alloc}} \left( \text{allocationPoint} \times \frac{\text{elapsedWorkdays}(T)}{\text{phaseWorkdays}} \right)$$
    *(Nếu $T < \text{startAt}$ thì $\text{PV} = 0$; nếu $T \ge \text{endAt}$ thì $\text{PV} = \text{allocationPoint}$)*.
  - $\text{PV}_{\text{project}}(T) = \sum \text{PV}_{\text{phase}}(T)$ trên tất cả các phase của dự án.
- **EV (Earned Value - Giá trị thu được):**
  - Tổng các điểm thưởng thực tế kiếm được từ giao dịch hoàn tất task:
    $$\text{EV} = \sum \text{EarningTransaction.points} \quad (\text{loại EARN})$$
- **AC (Actual Cost - Chi phí thực tế):**
  - Tổng chi phí điểm thực tế tiêu thụ từ các task:
    $$\text{AC} = \sum \text{TaskCostTransaction.points}$$
- **Các chỉ số dẫn xuất:**
  - Sai lệch chi phí: $\text{CV} = \text{EV} - \text{AC}$
  - Sai lệch tiến độ: $\text{SV} = \text{EV} - \text{PV}$
  - Chỉ số hiệu suất chi phí: $\text{CPI} = \frac{\text{EV}}{\text{AC}}$ *(scale 4, HALF_UP; null nếu $\text{AC} = 0$)*
  - Chỉ số hiệu suất tiến độ: $\text{SPI} = \frac{\text{EV}}{\text{PV}}$ *(scale 4, HALF_UP; null nếu $\text{PV} = 0$)*
  - Ước lượng chi phí khi hoàn thành: $\text{EAC} = \frac{\text{BAC}}{\text{CPI}}$
  - Chi phí ước lượng còn lại: $\text{ETC} = \text{EAC} - \text{AC}$
  - Biến thiên khi hoàn thành: $\text{VAC} = \text{BAC} - \text{EAC}$
  - Chỉ số hiệu suất cần đạt để hoàn thành: $\text{TCPI} = \frac{\text{BAC} - \text{EV}}{\text{BAC} - \text{AC}}$

### 6.8 Ma trận ràng buộc liên trạng thái (Cross-Status Validations)

| Cấp thực thể | Chuyển sang trạng thái | Tiền điều kiện bắt buộc | Lỗi ném ra nếu vi phạm |
| :--- | :--- | :--- | :--- |
| **Project** | `DONE` | Tất cả Phase trong Project phải có `status = DONE` | `HAS_PHASE_NOT_DONE` (1141) |
| **Project** | Xóa (`DELETE`) | Project phải ở `DRAFT` và không có ProjectMember hoặc Phase | `PROJECT_IN_PROGRESS` (1051) / `PROJECT_IN_USE` (1140) |
| **Phase** | `IN_PROGRESS` | Project chứa Phase phải có `projectStatus = IN_PROGRESS` | `PROJECT_NOT_IN_PROGRESS` (1021) |
| **Phase** | `DONE` | Phase hiện tại phải là `IN_PROGRESS` VÀ tất cả Task trong Phase phải là `DONE` hoặc `CANCELED` | `PHASE_NOT_IN_PROGRESS` (1139) / `HAS_TASK_NOT_DONE` (1137) |
| **Phase** | Xóa (`DELETE`) | Phase chưa có bản ghi Allocation hoặc Task nào | `PHASE_IN_USE` (1026) |
| **Task** | `IN_PROGRESS` | Phase chứa Task phải có `status = IN_PROGRESS` | `PHASE_NOT_IN_PROGRESS` (1139) |
| **Task** | `DONE` | Task hiện tại là `IN_PROGRESS`, có Assignment, tất cả assignee có Note CONFIRMED | `TASK_NOT_IN_PROGRESS` (1057) / `TASK_NOTE_NOT_FOUND` (1041) |
| **Task** | Cập nhật bất kỳ | Phase chứa Task chưa ở trạng thái `DONE` | `TASK_PHASE_DONE` (1028) |
| **Task** | Xóa (`DELETE`) | Task phải ở trạng thái `TODO` và chưa có bất kỳ Task Note nào | `TASK_IN_PROGRESS_CANNOT_DELETE` (1040) / `TASK_IN_USE` (1142) |
| **ProjectMember**| Xóa (`DELETE`) | Member chưa được phân công Task và chưa được phân bổ Allocation | `PROJECT_MEMBER_IN_USE` (1135) |

---

## 7. Danh mục API Endpoints hoàn chỉnh

### 7.1 Phân hệ Xác thực (`/api/auth`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/admin` | `CREATE_ACCOUNT` | Admin tạo tài khoản nhân viên mới |
| `POST` | `/api/auth/log-in` | Public | Đăng nhập tài khoản, trả về danh sách roles |
| `POST` | `/api/auth/select-role` | Public | Chọn role làm việc, phát hành JWT token |
| `POST` | `/api/auth/log-in-google` | Public | Đăng nhập qua mã authorization code của Google |
| `POST` | `/api/auth/select-role/google` | Public | Chọn role làm việc sau khi xác thực Google |
| `POST` | `/api/auth/forgot-password` | Public | Yêu cầu gửi email mã đổi mật khẩu qua Brevo |
| `POST` | `/api/auth/reset-password` | Public | Đặt lại mật khẩu mới bằng mã xác thực |
| `POST` | `/api/auth/refresh-token` | Public | Làm mới JWT token đang sử dụng |
| `POST` | `/api/auth/introspect` | Public | Kiểm tra tính hợp lệ của token |
| `PUT` | `/api/auth/change-password` | `CHANGE_PASSWORD` | Người dùng đổi mật khẩu khi đang đăng nhập |

### 7.2 Phân hệ Người dùng (`/api/users`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users` | `GET_USERS` | Danh sách người dùng (lọc theo role, department, keyword) |
| `GET` | `/api/users/statistics` | `GET_STATISTICS_USERS` | Thống kê số lượng người dùng theo từng Role |
| `GET` | `/api/users/{id}` | `GET_USER_BY_ID` | Xem chi tiết thông tin người dùng theo ID |
| `GET` | `/api/users/my-info` | `GET_MY_INFO` | Lấy thông tin cá nhân của user đang đăng nhập |
| `PUT` | `/api/users/update-avatar` | `UPDATE_AVATAR` | Upload ảnh đại diện multipart lên Cloudinary |
| `PUT` | `/api/users/admin/update-user` | `UPDATE_USER` | Admin cập nhật thông tin, role, rate của nhân sự |

### 7.3 Phân hệ Phòng ban (`/api/department`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/department` | Public | Lấy toàn bộ danh sách phòng ban |
| `GET` | `/api/department/{id}` | Public | Lấy thông tin chi tiết một phòng ban |
| `GET` | `/api/department/statistics` | `GET_STATISTICS_DEPARTMENTS` | Thống kê số lượng phòng ban active/hidden |
| `POST` | `/api/department` | `CREATE_DEPARTMENT` | Tạo phòng ban mới |
| `PUT` | `/api/department/{id}` | `UPDATE_DEPARTMENT` | Cập nhật tên hoặc ẩn/hiện phòng ban |
| `DELETE` | `/api/department/{id}` | `DELETE_DEPARTMENT` | Xóa phòng ban (ràng buộc không còn nhân sự) |

### 7.4 Phân hệ Dự án (`/api/project`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/project` | `CREATE_PROJECT` | Khởi tạo dự án mới |
| `PUT` | `/api/project/{id}` | `UPDATE_PROJECT` | Cập nhật dự án, đổi PM, đổi trạng thái (chặn DONE nếu còn phase chưa xong) |
| `DELETE` | `/api/project/{id}` | `DELETE_PROJECT` | Xóa dự án (chỉ khi ở trạng thái DRAFT và chưa có member/phase) |
| `GET` | `/api/project` | `GET_PROJECTS` | Lấy danh sách dự án (phân quyền dữ liệu theo vai trò) |
| `GET` | `/api/project/{id}` | `GET_PROJECT_BY_ID` | Xem chi tiết dự án (phân quyền trường dữ liệu) |

### 7.5 Phân hệ Thành viên dự án (`/api/project-member`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/project-member/project/{projectId}` | `GET_PROJECT_MEMBERS_BY_PROJECT` | Xem danh sách thành viên của dự án |
| `POST` | `/api/project-member` | `CREATE_PROJECT_MEMBER` | Thêm nhân viên vào dự án (chỉ nhận role EMPLOYEE) |
| `DELETE` | `/api/project-member/{id}` | `DELETE_PROJECT_MEMBER` | Xóa thành viên khỏi dự án (chỉ khi chưa có phân bổ và task) |

### 7.6 Phân hệ Giai đoạn (`/api/phase`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/phase` | `CREATE_PHASE` | Tạo phase mới (kèm makeUpDates, offDates) |
| `PUT` | `/api/phase/{id}` | `UPDATE_PHASE` | Cập nhật phase, chuyển trạng thái DONE (tạo snapshot EVM, kiểm tra all task DONE/CANCELED) |
| `DELETE` | `/api/phase/{id}` | `DELETE_PHASE` | Xóa phase (chỉ khi chưa có task và allocation) |
| `GET` | `/api/phase/project/{projectId}` | `GET_PHASES_BY_PROJECT` | Lấy danh sách phase theo dự án kèm % tiến độ |
| `GET` | `/api/phase/detail/{phaseId}` | `GET_PHASE_BY_ID` | Xem thông tin chi tiết một phase |

### 7.7 Phân hệ Phân bổ nhân sự (`/api/allocation`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/allocation` | `CREATE_ALLOCATION` | Tạo phân bổ nhân sự (hỗ trợ > 100%, tự động ghi nhận ngày quá tải) |
| `PUT` | `/api/allocation/{id}` | `UPDATE_ALLOCATION` | Cập nhật tỷ lệ phân bổ nhân sự |
| `DELETE` | `/api/allocation/{id}` | `DELETE_ALLOCATION` | Xóa bản ghi phân bổ nhân sự |
| `GET` | `/api/allocation/project/{projectId}` | `GET_ALLOCATIONS_BY_PROJECT` | Lấy danh sách phân bổ (kèm allocationPoint và overloadedDates) |
| `GET` | `/api/allocation/{id}` | `GET_ALLOCATIONS_BY_ID` | Xem chi tiết một bản ghi phân bổ |

### 7.8 Phân hệ Công việc (`/api/task`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/task` | `CREATE_TASK` | Tạo task mới và lồng danh sách phân công đóng góp |
| `PUT` | `/api/task/{id}` | `UPDATE_TASK` | Cập nhật task (nếu DONE: kích hoạt tạo Earning & Cost) |
| `DELETE` | `/api/task/{id}` | `DELETE_TASK` | Xóa task (chỉ khi task ở trạng thái TODO và chưa có task note) |
| `GET` | `/api/task/project/{projectId}` | `GET_TASKS_BY_PROJECT` | Danh sách task theo dự án (lọc theo phaseId, userId, keyword) |
| `GET` | `/api/task/pm/detail/{id}` | `GET_TASK_BY_ID` | Xem chi tiết thông tin task |
| `PATCH`| `/api/task/{id}/status` | `UPDATE_MY_TASK_STATUS_BY_ID` | Nhân viên chuyển trạng thái task từ TODO sang IN_PROGRESS |

### 7.9 Phân hệ Nhật ký công việc (`TaskNote`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/task/notes` | `CREATE_TASK_NOTE` | Nhân viên tạo nhật ký công việc (giờ làm 0..8, ngày làm) |
| `PUT` | `/api/task-note/{noteId}` | `UPDATE_TASK_NOTE` | Nhân viên sửa note (chỉ khi PENDING_CONFIRMATION) |
| `DELETE` | `/api/task-note/{noteId}` | `DELETE_TASK_NOTE` | Nhân viên xóa note (chỉ khi PENDING_CONFIRMATION) |
| `GET` | `/api/task/{taskId}/notes` | `GET_TASK_NOTES_BY_TASK` | Xem danh sách các note của một task |
| `PATCH`| `/api/task-note/{noteId}/confirm` | `CONFIRM_TASK_NOTES` | PM phê duyệt xác nhận task note |

### 7.10 Phân hệ Điểm thưởng & Chi phí (`/api/earning-transactions`, `/api/task-cost-transactions`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/earning-transactions/project/{projectId}` | `GET_EARNINGS_BY_PROJECT` | Xem danh sách giao dịch earn của dự án |
| `GET` | `/api/earning-transactions/project/{projectId}/{id}`| `GET_EARNINGS_BY_ID` | Xem chi tiết giao dịch earn |
| `POST` | `/api/earning-transactions/project/{projectId}/transfer`| `TRANSFER_EARN` | PM chuyển nhượng điểm earn (áp dụng khóa bi quan chống double-spending) |
| `GET` | `/api/task-cost-transactions/project/{projectId}`| `GET_TASK_COSTS_BY_PROJECT`| Xem danh sách chi phí point tiêu thụ của dự án |
| `GET` | `/api/task-cost-transactions/{id}` | `GET_TASK_COSTS_BY_ID` | Xem chi tiết một bản ghi chi phí task |

### 7.11 Phân hệ EVM Health (`/api/evm`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/evm/project/{projectId}` | `GET_PROJECT_EVM_HEALTH` | Báo cáo sức khỏe EVM toàn dự án (BAC, PV, EV, AC, CPI, SPI, EAC, ETC, VAC, TCPI) |
| `GET` | `/api/evm/project/{projectId}/phases` | `GET_PROJECT_PHASES_EVM` | Báo cáo các chỉ số EVM theo phase trong dự án |
| `GET` | `/api/evm/project/{projectId}/employees` | `GET_PROJECT_EMPLOYEES_EVM` | Báo cáo hiệu suất EVM theo nhân viên trong dự án |
| `GET` | `/api/evm/project/{projectId}/financial-summary` | `GET_PROJECT_EVM_FINANCIAL_SUMMARY` | Tổng hợp ngân sách và kết quả tài chính EVM; dữ liệu ngân sách khách hàng chỉ hiển thị cho ADMIN |
| `GET` | `/api/evm/project/{projectId}/export-csv` | `EXPORT_PROJECT_EVM_CSV` | Tải báo cáo EVM dự án ở định dạng CSV UTF-8 |

### 7.12 Phân hệ Báo cáo thống kê Dashboard (`/api/statistics`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/statistics/summary` | `GET_STATISTICS_SUMMARY` | Tổng số dự án, số user (Admin), số dự án quá hạn, cận hạn (7 ngày) |
| `GET` | `/api/statistics/projects/status-statistics` | `GET_PROJECT_STATUS_STATISTICS` | Thống kê số lượng và tỷ lệ % dự án theo trạng thái |
| `GET` | `/api/statistics/projects/overview` | `GET_PROJECT_OVERVIEW_STATISTICS`| Tổng số dự án và map số lượng theo trạng thái |
| `GET` | `/api/statistics/projects/ev-ac-statistics` | `GET_PROJECT_EV_AC_STATISTICS` | Thống kê đối sánh EV và AC theo từng dự án |
| `GET` | `/api/statistics/projects/in-progress` | `GET_IN_PROGRESS_PROJECT_STATISTICS` | Danh sách dự án đang chạy kèm SPI, CPI và ScheduleStatus (FAST, SLOW, ON_SCHEDULE) |
| `GET` | `/api/statistics/employee/summary` | `GET_EMPLOYEE_STATISTICS_SUMMARY` | Thống kê cá nhân nhân viên: tổng dự án, tổng point kiếm được, task đã xong |
| `GET` | `/api/statistics/employee/project-points` | `GET_EMPLOYEE_PROJECT_POINT_STATISTICS` | Thống kê điểm kiếm được phân bổ theo từng dự án của cá nhân |
| `GET` | `/api/statistics/employee/tasks` | `GET_EMPLOYEE_TASK_STATISTICS` | Danh sách task đang tiến hành hoặc sắp tới hạn của cá nhân |

### 7.13 Phân hệ Thông báo người dùng (`/api/user-notifications`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/user-notifications/receiver` | `GET_ALL_USER_NOTIFICATION` | Lấy tất cả thông báo của người dùng hiện tại (sắp xếp mới nhất trước) |
| `GET` | `/api/user-notifications/receiver/unread` | `GET_UNREAD_USER_NOTIFICATION` | Lấy danh sách thông báo chưa đọc của người dùng hiện tại |
| `PUT` | `/api/user-notifications/read/{id}` | `READ_NOTIFICATION` | Đánh dấu thông báo cụ thể là đã đọc (bảo mật kiểm tra người nhận) |
| `PUT` | `/api/user-notifications/read-all` | `READ_ALL_NOTIFICATION` | Đánh dấu tất cả thông báo chưa đọc của người dùng là đã đọc |
| `PUT` | `/api/user-notifications/read-all/{id}` | `READ_ALL_NOTIFICATION` | Đánh dấu tất cả thông báo của người dùng là đã đọc (hỗ trợ tương thích endpoint) |

### 7.14 Phân hệ Lịch làm việc nhân viên (`/api/employee`)
| Method | Endpoint | Quyền (Authority) | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/employee/work-schedule` | `GET_EMPLOYEE_WORK_SCHEDULE` | Trả về lịch từ thứ Hai đến Chủ Nhật cho tuần chứa `date` (ISO `yyyy-MM-dd`); nếu bỏ qua `date`, dùng ngày hiện tại. Mỗi ngày có cờ `isWorkingDay` và danh sách allocation đang áp dụng. |
| `GET` | `/api/employee/work-schedule/tasks` | `GET_EMPLOYEE_WORK_SCHEDULE_TASKS` | Trả về task được giao cho ngày `date` (bắt buộc, ISO `yyyy-MM-dd`); `phaseIds` là bộ lọc phase tùy chọn. Nếu thiếu phase hợp lệ hoặc không có task phù hợp, trả danh sách rỗng. |

---

## 8. Danh mục mã lỗi hệ thống (`AppError`)

Toàn bộ 143 mã lỗi chuẩn hóa của hệ thống:

| Mã lỗi | Hằng số lỗi | Thông báo chi tiết | HTTP Status |
| :--- | :--- | :--- | :--- |
| 1001 | `MISSING_TOKEN` | Thiếu mã truy cập | 401 Unauthorized |
| 1002 | `UPDATE_PROFILE_FAILED` | Cập nhật thông tin cá nhân thất bại | 400 Bad Request |
| 1003 | `INVALID_IMAGE_FORMAT` | Ảnh không đúng định dạng | 415 Unsupported Media |
| 1004 | `TOKEN_EXPIRED` | Mã truy cập đã hết hạn | 401 Unauthorized |
| 1005 | `INVALID_TOKEN` | Mã truy cập không hợp lệ | 401 Unauthorized |
| 1006 | `INVALID_SECRET_KEY_LENGTH`| Độ dài khóa bí mật không hợp lệ | 500 Internal Server |
| 1007 | `JOSE_PROCESSING_ERROR` | Lỗi xử lý JWT/JOSE | 500 Internal Server |
| 1008 | `USER_NOT_FOUND` | Không tìm thấy người dùng | 404 Not Found |
| 1009 | `EMAIL_ALREADY_EXISTS` | Email đã tồn tại | 409 Conflict |
| 1010 | `INVALID_VERIFICATION_CODE`| Mã xác thực không đúng | 400 Bad Request |
| 1011 | `GOOGLE_LOGIN_FAILED` | Đăng nhập bằng Google thất bại | 401 Unauthorized |
| 1012 | `ACCESS_DENIED` | Bạn không có quyền thực hiện chức năng này | 403 Forbidden |
| 1013 | `LOGIN_FAILED` | Đăng nhập thất bại | 401 Unauthorized |
| 1014 | `FAILED_TO_PARSE_DATA` | Không thể phân tích dữ liệu | 400 Bad Request |
| 1015 | `CANNOT_SEND_EMAIL` | Gửi email thất bại | 400 Bad Request |
| 1016 | `DEPARTMENT_NOT_FOUND` | Không tìm thấy phòng ban | 404 Not Found |
| 1017 | `ACCOUNT_NOT_FOUND` | Không tìm thấy tài khoản | 404 Not Found |
| 1018 | `INCORRECT_PASSWORD` | Mật khẩu không đúng | 401 Unauthorized |
| 1019 | `ROLE_NOT_FOUND` | Không tìm thấy vai trò | 404 Not Found |
| 1020 | `PROJECT_NOT_FOUND` | Không tìm thấy dự án | 404 Not Found |
| 1021 | `USER_NOT_PM` | Người dùng không phải là quản lý dự án | 400 Bad Request |
| 1022 | `PHASE_NOT_FOUND` | Không tìm thấy giai đoạn | 404 Not Found |
| 1023 | `INVALID_DATE_RANGE` | Ngày bắt đầu phải trước ngày kết thúc | 400 Bad Request |
| 1024 | `PHASE_OUT_OF_PROJECT_RANGE`| Giai đoạn phải nằm trong khoảng thời gian của dự án | 400 Bad Request |
| 1025 | `PHASE_ALREADY_DONE` | Giai đoạn đã hoàn thành và không thể thay đổi | 400 Bad Request |
| 1026 | `PHASE_IN_USE` | Không thể xóa giai đoạn hoặc chuyển giai đoạn sang dự án khác khi đang được sử dụng | 400 Bad Request |
| 1027 | `TASK_OUT_OF_PHASE_RANGE` | Thời gian thực hiện công việc phải nằm trong thời gian của giai đoạn | 400 Bad Request |
| 1028 | `TASK_PHASE_DONE` | Không thể thay đổi công việc thuộc giai đoạn đã hoàn thành | 400 Bad Request |
| 1029 | `PHASE_EVM_SNAPSHOT_NOT_FOUND`| Không tìm thấy bản ghi EVM của giai đoạn đã hoàn tất | 404 Not Found |
| 1030 | `PHASE_EVM_SNAPSHOT_ALREADY_EXISTS`| Bản ghi EVM của giai đoạn đã tồn tại | 409 Conflict |
| 1031 | `PROJECT_MEMBER_NOT_FOUND`| Không tìm thấy thành viên dự án | 404 Not Found |
| 1032 | `PROJECT_MEMBER_ALREADY_EXISTS`| Thành viên đã tồn tại trong dự án | 409 Conflict |
| 1033 | `PHASE_INVALID_SPECIAL_DATE`| Ngày nghỉ và ngày làm bù phải khác nhau và nằm trong thời gian của giai đoạn | 400 Bad Request |
| 1034 | `ALLOCATION_ALREADY_EXISTS`| Nhân viên đã được phân bổ vào giai đoạn này | 409 Conflict |
| 1035 | `MEMBER_NOT_FOUND` | Không tìm thấy nhân viên trong dự án | 404 Not Found |
| 1036 | `ALLOCATION_NOT_FOUND` | Không tìm thấy thông tin phân bổ | 404 Not Found |
| 1037 | `TOTAL_ALLOCATION_EXCEEDS_100`| Tổng tỷ lệ phân bổ cho nhân viên không được vượt quá 100% | 400 Bad Request |
| 1038 | `TASK_NOT_FOUND` | Không tìm thấy công việc | 404 Not Found |
| 1039 | `TASK_ASSIGNMENT_ALREADY_EXISTS`| Nhân viên đã được phân công vào công việc | 409 Conflict |
| 1040 | `TASK_IN_PROGRESS_CANNOT_DELETE`| Không thể xóa công việc đang được thực hiện | 400 Bad Request |
| 1041 | `TASK_NOTE_NOT_FOUND` | Không tìm thấy ghi chú công việc | 404 Not Found |
| 1042 | `TASK_NOTE_NOT_PENDING` | Chỉ có thể sửa hoặc xóa ghi chú công việc đang chờ xác nhận | 400 Bad Request |
| 1043 | `TASK_NOTE_WORKING_DATE_OUT_OF_RANGE`| Ngày làm việc phải nằm trong thời gian thực hiện công việc | 400 Bad Request |
| 1044 | `TASK_ALREADY_DONE` | Công việc đã hoàn thành và không thể chỉnh sửa | 400 Bad Request |
| 1045 | `ALLOCATION_RATE_NOT_FOUND`| Không tìm thấy bản ghi tỷ lệ nguồn lực của thông tin phân bổ | 400 Bad Request |
| 1046 | `TASK_HAS_NO_ASSIGNMENTS` | Công việc phải có nhân viên được phân công trước khi hoàn thành | 400 Bad Request |
| 1047 | `EARNING_TRANSACTION_NOT_FOUND`| Không tìm thấy giao dịch điểm thưởng gốc | 404 Not Found |
| 1048 | `INVALID_EARNING_TRANSFER`| Thông tin chuyển điểm thưởng không hợp lệ | 400 Bad Request |
| 1049 | `EARNING_BALANCE_NOT_ENOUGH`| Số điểm thưởng còn lại không đủ để chuyển | 400 Bad Request |
| 1050 | `ONLY_EMPLOYEE` | Chỉ có thể chọn nhân viên | 400 Bad Request |
| 1051 | `HAS_PHASE_OUT_OF_PROJECT` | Có phase nằm ngoài project | 400 Bad Request |
| 1052 | `PROJECT_ALREADY_DONE` | Dự án đã hoàn thành | 400 Bad Request |
| 1053 | `PROJECT_MISMATCH` | Dự án không trùng khớp | 400 Bad Request |
| 1054 | `EARNING_NOT_FOUND` | Không tìm thấy bản ghi giao dịch | 400 Bad Request |
| 1055 | `TASK_COST_TRANSACTION_NOT_FOUND`| Không tìm thấy giao dịch chi phí của công việc | 400 Bad Request |
| 1056 | `DEPARTMENT_NOT_EMPTY` | Phòng ban vẫn còn nhân viên | 400 Bad Request |
| 1057 | `TASK_NOT_IN_PROGRESS` | Công việc chưa được bắt đầu | 400 Bad Request |
| 1058 | `INVALID_TASK_STATUS_TRANSITION`| Nhân viên chỉ có thể bắt đầu công việc đang chờ | 400 Bad Request |
| 1059 | `USER_NOT_BELONG_TO_TASK` | Nhân viên không được phân công vào công việc này | 400 Bad Request |
| 1060 | `PERMISSIONS_NOT_MATCH` | Quyền được cung cấp không khớp | 400 Bad Request |
| 1061 | `EMAIL_REQUIRED` | Email không được để trống | 400 Bad Request |
| 1062 | `EMAIL_INVALID_FORMAT` | Email không đúng định dạng | 400 Bad Request |
| 1063 | `EMAIL_INVALID` | Email không hợp lệ | 400 Bad Request |
| 1064 | `EMAIL_EXAMPLE_FORMAT` | Email phải đúng định dạng, ví dụ: example@gmail.com | 400 Bad Request |
| 1065 | `PASSWORD_REQUIRED` | Mật khẩu không được để trống | 400 Bad Request |
| 1066 | `PASSWORD_FORMAT_INVALID` | Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt | 400 Bad Request |
| 1067 | `ROLE_LIST_REQUIRED` | Danh sách vai trò không được để trống | 400 Bad Request |
| 1068 | `USERNAME_REQUIRED` | Tên người dùng không được để trống | 400 Bad Request |
| 1069 | `USERNAME_LENGTH_INVALID` | Tên người dùng phải từ 3 đến 50 ký tự | 400 Bad Request |
| 1070 | `POSITION_REQUIRED` | Chức vụ không được để trống | 400 Bad Request |
| 1071 | `POSITION_MAX_LENGTH_EXCEEDED`| Chức vụ không được vượt quá 100 ký tự | 400 Bad Request |
| 1072 | `RESOURCE_RATE_REQUIRED` | Tỷ lệ nguồn lực không được để trống | 400 Bad Request |
| 1073 | `RESOURCE_RATE_BELOW_ZERO`| Tỷ lệ nguồn lực không được nhỏ hơn 0 | 400 Bad Request |
| 1074 | `RESOURCE_RATE_ABOVE_100` | Tỷ lệ nguồn lực không được lớn hơn 100 | 400 Bad Request |
| 1075 | `DEPARTMENT_ID_REQUIRED` | Mã phòng ban không được để trống | 400 Bad Request |
| 1076 | `DEPARTMENT_ID_MUST_BE_POSITIVE`| Mã phòng ban phải là số dương | 400 Bad Request |
| 1077 | `OLD_PASSWORD_REQUIRED` | Mật khẩu cũ không được để trống | 400 Bad Request |
| 1078 | `NEW_PASSWORD_REQUIRED` | Mật khẩu mới không được để trống | 400 Bad Request |
| 1079 | `PROJECT_ID_REQUIRED` | Mã dự án không được để trống | 400 Bad Request |
| 1080 | `USER_ID_REQUIRED` | Mã người dùng không được để trống | 400 Bad Request |
| 1081 | `START_DATE_REQUIRED` | Ngày bắt đầu không được để trống | 400 Bad Request |
| 1082 | `END_DATE_REQUIRED` | Ngày kết thúc không được để trống | 400 Bad Request |
| 1083 | `PHASE_NAME_REQUIRED` | Tên giai đoạn không được để trống | 400 Bad Request |
| 1084 | `PHASE_NAME_LENGTH_INVALID`| Tên giai đoạn phải có từ 2 đến 100 ký tự | 400 Bad Request |
| 1085 | `ALLOCATION_PHASE_ID_REQUIRED`| Mã giai đoạn không được để trống | 400 Bad Request |
| 1086 | `ALLOCATION_MEMBER_ID_REQUIRED`| Mã thành viên không được để trống | 400 Bad Request |
| 1087 | `ALLOCATION_PERCENTAGE_REQUIRED`| Tỷ lệ phân bổ không được để trống | 400 Bad Request |
| 1088 | `ALLOCATION_PERCENTAGE_MIN_INVALID`| Tỷ lệ phân bổ phải ít nhất là 1% | 400 Bad Request |
| 1089 | `ALLOCATION_PERCENTAGE_MAX_INVALID`| Tỷ lệ phân bổ không được vượt quá 100% | 400 Bad Request |
| 1090 | `PROJECT_NAME_REQUIRED` | Tên dự án không được để trống | 400 Bad Request |
| 1091 | `PROJECT_NAME_LENGTH_INVALID`| Tên dự án phải có từ 2 đến 100 ký tự | 400 Bad Request |
| 1092 | `PM_ID_REQUIRED` | Mã quản lý dự án không được để trống | 400 Bad Request |
| 1093 | `TASK_ID_REQUIRED` | Mã công việc không được để trống | 400 Bad Request |
| 1094 | `WORKING_DATE_REQUIRED` | Ngày làm việc không được để trống | 400 Bad Request |
| 1095 | `TASK_NOTE_CONTENT_REQUIRED`| Nội dung ghi chú công việc không được để trống | 400 Bad Request |
| 1096 | `WORKING_HOURS_REQUIRED` | Số giờ làm không được để trống | 400 Bad Request |
| 1097 | `WORKING_HOURS_MIN_INVALID`| Số giờ làm phải từ 0 đến 8 | 400 Bad Request |
| 1098 | `WORKING_HOURS_MAX_INVALID`| Số giờ làm không được quá 8 tiếng | 400 Bad Request |
| 1099 | `TASK_NAME_REQUIRED` | Tên công việc không được để trống | 400 Bad Request |
| 1100 | `CLIENT_BUDGET_REQUIRED` | Ngân sách khách hàng không được để trống | 400 Bad Request |
| 1101 | `PROJECT_BUDGET_REQUIRED`| Ngân sách dự án không được để trống | 400 Bad Request |
| 1102 | `TASK_STATUS_REQUIRED` | Trạng thái công việc không được để trống | 400 Bad Request |
| 1103 | `TASK_POINT_REQUIRED` | Điểm công việc không được để trống | 400 Bad Request |
| 1104 | `TASK_POINT_MUST_BE_POSITIVE`| Điểm công việc phải lớn hơn 0 | 400 Bad Request |
| 1105 | `TASK_ASSIGNMENTS_REQUIRED`| Danh sách phân công không được để trống | 400 Bad Request |
| 1106 | `PHASE_ID_REQUIRED` | Mã giai đoạn không được để trống | 400 Bad Request |
| 1107 | `EMPLOYEE_ID_REQUIRED` | Mã nhân viên không được để trống | 400 Bad Request |
| 1108 | `CONTRIBUTION_PERCENTAGE_REQUIRED`| Tỷ lệ đóng góp không được để trống | 400 Bad Request |
| 1109 | `CONTRIBUTION_PERCENTAGE_INVALID`| Tỷ lệ đóng góp phải từ 0 đến 100 | 400 Bad Request |
| 1110 | `PHASE_STATUS_REQUIRED` | Trạng thái giai đoạn không được để trống | 400 Bad Request |
| 1111 | `DEPARTMENT_HIDDEN_REQUIRED`| Trạng thái ẩn không được để trống | 400 Bad Request |
| 1112 | `PROJECT_STATUS_REQUIRED`| Trạng thái dự án không được để trống | 400 Bad Request |
| 1113 | `USER_ID_MUST_BE_POSITIVE`| Mã người dùng phải là số dương | 400 Bad Request |
| 1114 | `USERNAME_MAX_LENGTH_EXCEEDED`| Tên người dùng không được vượt quá 50 ký tự | 400 Bad Request |
| 1115 | `POSITION_MUST_NOT_BE_BLANK`| Chức vụ không được để trống | 400 Bad Request |
| 1116 | `RESOURCE_RATE_MUST_BE_POSITIVE`| Tỷ lệ nguồn lực phải lớn hơn 0 | 400 Bad Request |
| 1117 | `DEPARTMENT_ID_MUST_NOT_BE_NULL`| Mã phòng ban không được để trống | 400 Bad Request |
| 1118 | `DEPARTMENT_ID_MUST_BE_POSITIVE_EN`| Mã phòng ban phải là số dương | 400 Bad Request |
| 1119 | `ROLES_REQUIRED` | Danh sách vai trò không được để trống | 400 Bad Request |
| 1120 | `ROLE_REQUIRED` | Vai trò không được để trống | 400 Bad Request |
| 1121 | `SOURCE_TRANSACTION_ID_REQUIRED`| Mã giao dịch điểm thưởng gốc không được để trống | 400 Bad Request |
| 1122 | `TARGET_EMPLOYEE_ID_REQUIRED`| Mã nhân viên nhận không được để trống | 400 Bad Request |
| 1123 | `EARNING_POINTS_REQUIRED`| Số điểm thưởng cần chuyển không được để trống | 400 Bad Request |
| 1124 | `EARNING_POINTS_MUST_BE_POSITIVE`| Số điểm thưởng cần chuyển phải lớn hơn 0 | 400 Bad Request |
| 1125 | `FORGOT_PASSWORD_CODE_REQUIRED`| Mã khôi phục mật khẩu không được để trống | 400 Bad Request |
| 1126 | `CODE_REQUIRED` | Mã xác thực không được để trống | 400 Bad Request |
| 1127 | `USER_ID_MUST_NOT_BE_NULL`| Mã người dùng không được để trống | 400 Bad Request |
| 1128 | `USERNAME_MUST_NOT_BE_BLANK`| Tên người dùng không được để trống | 400 Bad Request |
| 1129 | `POSITION_MAX_LENGTH_EN` | Chức vụ không được vượt quá 100 ký tự | 400 Bad Request |
| 1130 | `RESOURCE_RATE_MUST_NOT_BE_NULL`| Tỷ lệ nguồn lực không được để trống | 400 Bad Request |
| 1131 | `HAS_TASK_OUT_OF_PHASE` | Có task nằm ngoài phase | 400 Bad Request |
| 1132 | `DEPARTMENT_NAME_REQUIRED`| Tên phòng ban không được để trống | 400 Bad Request |
| 1133 | `DEPARTMENT_NAME_LENGTH_INVALID`| Tên phòng ban phải từ 2 đến 50 ký tự | 400 Bad Request |
| 1134 | `ROLE_SELECTION_REQUIRED`| Vai trò không được để trống | 400 Bad Request |
| 1135 | `PROJECT_MEMBER_IN_USE` | Thành viên dự án đã được sử dụng | 400 Bad Request |
| 1136 | `HAS_TASK_NOTE_UNCONFIRMED`| Vẫn còn task note chưa xác nhận | 400 Bad Request |
| 1137 | `HAS_TASK_NOT_DONE` | Vẫn còn task chưa xong | 400 Bad Request |
| 1138 | `BUDGET_MUST_BE_POSITIVE` | Ngân sách không được âm | 400 Bad Request |
| 1139 | `PHASE_NOT_IN_PROGRESS` | Giai đoạn chưa được bắt đầu | 400 Bad Request |
| 1140 | `PROJECT_IN_USE` | Không thể xóa dự án đang có thành viên hoặc giai đoạn | 400 Bad Request |
| 1141 | `HAS_PHASE_NOT_DONE` | Vẫn còn giai đoạn chưa hoàn thành | 400 Bad Request |
| 1142 | `TASK_IN_USE` | Không thể xóa công việc đã phát sinh ghi chú hoặc chi phí | 400 Bad Request |
| 1143 | `NOTIFICATION_NOT_FOUND` | Không tìm thấy thông báo | 400 Bad Request |

---

## 9. Dữ liệu mồi hệ thống (`AppConfig Bootstrap`)
Khi hệ thống khởi chạy (`ApplicationRunner`), các dữ liệu danh mục được tự động kiểm tra và khởi tạo nếu chưa có:
- **Phòng ban mặc định:** `ADMIN`, `DEV`, `BA`, `QC`.
- **Vai trò mặc định:** `ADMIN`, `PM`, `EMPLOYEE` được gắn đầy đủ quyền theo Ma trận phân quyền tại Mục 3.2.
- **Tài khoản demo (khi bật cấu hình `app.bootstrap.demo-users.enabled=true`):**
  - `admin@gmail.com` (Roles: `ADMIN`, `PM`, `EMPLOYEE`, Department: `ADMIN`, Resource Rate: `2.0`)
  - `pm1@gmail.com` (Roles: `PM`, `EMPLOYEE`, Department: `DEV`, Resource Rate: `1.5`)
  - `employee1@gmail.com` (Roles: `EMPLOYEE`, Department: `DEV`, Resource Rate: `1.0`)
  - `employee2@gmail.com` (Roles: `EMPLOYEE`, Department: `DEV`, Resource Rate: `0.5`)
