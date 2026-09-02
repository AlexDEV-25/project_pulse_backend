# Branch Plan

Chia task theo từng nhánh riêng như sau:

## A

- CRUD department: `feat/a-department-crud`

+ tạo phòng ban, sửa phòng ban, xóa phòng ban, xem danh sách phòng ban

- CRUD authentication, user: `feat/a-auth-user-crud`

+ tạo tài khoản, đăng nhập, sửa thông tin người dùng, xóa người dùng, xem danh sách người dùng, ẩn/hiện người dùng

- CRUD earning transaction: `feat/a-earning-transaction-crud`

+ tạo giao dịch thu nhập, xem danh sách giao dịch thu nhập, transfer giao dịch thu nhập, reserve giao dịch thu nhập

- CRUD task note: `feat/a-task-note-crud`

+ tạo ghi chú công việc, sửa ghi chú công việc (của mình), xóa ghi chú công việc (của mình), xem danh sách ghi chú công
  việc

- CRUD task assignment: `feat/a-task-assignment-crud`

+ tạo phân công công việc, sửa phân công công việc, xóa phân công công việc, xem danh sách phân công công việc

- CRUD task cost transaction: `feat/a-task-cost-transaction-crud`

+ tạo giao dịch chi phí công việc, xem danh sách giao dịch chi phí công việc

## B

- CRUD project: `feat/b-project-crud`

+ tạo dự án, sửa dự án, xóa dự án, xem danh sách dự án

- CRUD phase: `feat/b-phase-crud`

+ tạo giai đoạn, sửa giai đoạn, xóa giai đoạn, xem danh sách giai đoạn

- CRUD allocation: `feat/b-allocation-crud`

+ tạo phân bổ nhân lực, sửa phân bổ nhân lực, xóa phân bổ nhân lực, xem danh sách phân bổ nhân lực

- CRUD project member: `feat/b-project-member-crud`

+ thêm thành viên dự án, xóa thành viên dự án, xem danh sách thành viên dự án

- CRUD task: `feat/b-task-crud`

+ tạo công việc, sửa công việc, xóa công việc, xem danh sách công việc

## Ghi chú

- Mỗi nhánh nên bám đúng một nhóm chức năng để dễ review và tránh xung đột.
- Nếu muốn đồng bộ theo quy ước chung, có thể đổi toàn bộ sang dạng `feature/...` hoặc `feat/...`.
- Tạm thời chưa cần phân quyền mọi endpoint đều để public và thêm vào trong file SecurityConfig 
