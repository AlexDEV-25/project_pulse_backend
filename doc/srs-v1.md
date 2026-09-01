# SRS v1 - Project Pulse Backend

## 1. Mục tiêu hệ thống
Hệ thống cung cấp API để quản lý dự án theo mô hình point, phục vụ:
- quản lý dự án và giai đoạn dự án
- phân bổ nhân lực theo phần trăm thời gian
- ghi nhận tiến độ và đóng góp của nhân viên
- ghi nhận point thực nhận của nhân viên
- hỗ trợ đánh giá hiệu suất nhân viên và sức khỏe dự án

Hệ thống được thiết kế để hạn chế việc hiển thị lương trực tiếp. `resourceRate` và các giao dịch point là dữ liệu nội bộ phục vụ quy đổi, kiểm soát và đánh giá.

## 2. Phạm vi
Phiên bản v1 tập trung vào:
- quản lý người dùng, phòng ban, vai trò, quyền
- quản lý dự án, phase, task, task note
- quản lý project member
- quản lý allocation cho phase
- quản lý task assignment như dữ liệu ghi nhận nhân viên tham gia task
- quản lý giao dịch point gồm earning transaction và task cost transaction

Ngoài phạm vi v1:
- skill matrix
- audit log chi tiết
- timesheet/work log theo ngày
- health dashboard nâng cao

## 3. Vai trò người dùng
### 3.1 Admin
- quản lý danh mục hệ thống
- duyệt hoặc từ chối kế hoạch phân bổ
- quản trị người dùng, vai trò, quyền

### 3.2 PM
- tạo và quản lý project
- tạo phase, task
- chọn project member
- phân bổ nhân lực trong phase
- theo dõi tiến độ và hiệu suất dự án

### 3.3 Employee
- tham gia project theo phân công
- cập nhật tiến độ task thông qua dữ liệu ghi nhận
- nhận point từ kết quả làm việc

## 4. Đối tượng nghiệp vụ chính
### 4.1 User
Thông tin nhân viên hoặc người dùng hệ thống gồm:
- `userName`
- `position`
- `resourceRate`
- `avatarUrl`
- `hidden`
- `department`
- `roles`

### 4.2 Department
Quản lý phòng ban của user.

### 4.3 Role và Permission
Quản lý phân quyền ở mức hệ thống:
- `Role` có danh sách `Permission`
- `User` có thể thuộc nhiều `Role`

### 4.4 Authentication
Quản lý thông tin xác thực:
- `email`
- `password`
- `forgotPasswordCode`
- liên kết 1-1 với `User`

### 4.5 Project
Đại diện cho một dự án, gồm:
- tên dự án
- PM phụ trách
- thời gian bắt đầu/kết thúc
- budget khách hàng
- budget nội bộ
- mô tả
- trạng thái dự án

### 4.6 ProjectMember
Danh sách thành viên của dự án theo từng user và trạng thái:
- `PENDING`
- `APPROVED`
- `REJECTED`
- `REMOVED`

### 4.7 Phase
Đại diện cho một giai đoạn trong project, gồm:
- project cha
- thời gian bắt đầu/kết thúc
- tên phase
- số workdays
- PM phụ trách phase
- trạng thái kế hoạch phân bổ

### 4.8 Allocation
Ghi nhận phân bổ nhân viên cho phase:
- phase
- employee
- allocation percentage
- resource rate snapshot

### 4.9 Task
Đại diện cho công việc trong project, gồm:
- task name
- thời gian bắt đầu/kết thúc
- project
- task status
- task point
- danh sách assignment
- danh sách note

### 4.10 TaskAssignment
Ghi nhận việc một user tham gia xử lý task và kết quả đóng góp:
- task
- user
- effort percentage
- contribution percentage
- worked days

### 4.11 TaskNote
Ghi chú trao đổi liên quan đến task:
- task
- author
- content
- createdAt
- updatedAt

### 4.12 EarningTransaction
Giao dịch point thực nhận của nhân viên:
- task liên quan
- employee
- referenceTransaction
- points
- earningTransactionType
- reason

### 4.13 TaskCostTransaction
Giao dịch point chi phí nội bộ theo task:
- task
- executor
- points
- workdays
- reason

## 5. Trạng thái nghiệp vụ
### 5.1 ProjectStatus
- `DRAFT`
- `PLANNING`
- `ACTIVE`
- `ON_HOLD`
- `COMPLETED`
- `CANCELLED`
- `FAILED`

### 5.2 TaskStatus
- `TODO`
- `IN_PROGRESS`
- `IN_REVIEW`
- `DONE`
- `BLOCKED`
- `CANCELED`

### 5.3 AllocationPlanStatus
- `PENDING`
- `APPROVED`
- `REJECTED`
- `CANCELLED`
- `PENDING_CHANGE`

### 5.4 ProjectMemberStatus
- `PENDING`
- `APPROVED`
- `REJECTED`
- `REMOVED`

### 5.5 EarningTransactionType
- `EARN`
- `TRANSFER`
- `REVERSAL`

## 6. Quy tắc nghiệp vụ chính
1. Một `Project` có một PM phụ trách.
2. Một `Project` có thể có nhiều `Phase`.
3. Một `Phase` thuộc đúng một `Project`.
4. Một `Task` thuộc đúng một `Project`.
5. Một `Task` có thể có nhiều `TaskAssignment`.
6. Một user chỉ được có một `TaskAssignment` cho cùng một task.
7. `TaskAssignment` dùng để ghi nhận mức tham gia, đóng góp và số ngày làm thực tế của nhân viên trên task.
8. `Allocation` dùng để phân bổ một user vào một `Phase` theo phần trăm thời gian.
9. Tổng allocation của một user trong cùng khoảng thời gian cần được kiểm tra để tránh vượt mức cho phép, trừ trường hợp được phép overcommit.
10. `EarningTransaction` là nguồn ghi nhận point thực nhận và có thể tham chiếu sang transaction trước đó.
11. `TaskCostTransaction` là nguồn ghi nhận chi phí point và workdays theo task.
12. `ProjectMember` là danh sách thành viên được xem xét hoặc tham gia project theo trạng thái.
13. `hidden` trên `User` và `Department` phục vụ ẩn dữ liệu nhưng không xóa vật lý.

## 7. Yêu cầu dữ liệu
- ID của các bảng là tự tăng.
- Các bảng quan hệ phải đảm bảo khóa ngoại đúng hướng.
- Những trường trạng thái dùng enum lưu dạng chuỗi.
- `task_point`, `points`, `client_budget`, `project_budget`, `resource_rate`, `resource_rate_snapshot` phải dùng kiểu số chính xác, không dùng số thực có sai số.
- `project_member`, `task_assignment` nên có ràng buộc tránh trùng cặp theo cặp khóa nghiệp vụ.

## 8. Báo cáo và đánh giá
Hệ thống cần hỗ trợ các báo cáo cơ bản:
- tiến độ dự án theo project, phase, task
- mức độ phân bổ nhân lực theo phase
- hiệu suất nhân viên dựa trên assignment và earning transaction
- tổng point theo user, project, task
- trạng thái dự án và trạng thái task

## 9. Hướng mở rộng
Các phần có thể bổ sung ở phiên bản sau:
- `ProjectHealthSnapshot`
- `ProjectRisk`
- `AuditLog`
- `PointAdjustmentLog`
- `Skill`, `UserSkill`, `SkillRequirement`
- `WorkLog` hoặc `Timesheet`

## 10. Ghi chú triển khai
Tài liệu này mô tả v1 theo structure hiện tại của codebase. Khi nghiệp vụ thay đổi, SRS cần được cập nhật đồng bộ với entity, constant và rule kiểm tra ở service layer.
