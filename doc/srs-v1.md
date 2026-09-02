# SRS v1 - Project Pulse Backend

## 1. Muc tieu he thong
He thong cung cap API de quan ly du an theo mo hinh point, phuc vu:
- quan ly du an va giai doan du an
- phan bo nhan luc theo phan tram thoi gian
- ghi nhan tien do va dong gop cua nhan vien
- ghi nhan point thuc nhan cua nhan vien
- ho tro danh gia hieu suat nhan vien va suc khoe du an

He thong duoc thiet ke de han che viec hien thi luong truc tiep. `resourceRate` va cac giao dich point la du lieu noi bo phuc vu quy doi, kiem soat va danh gia.

## 2. Pham vi
Phien ban v1 tap trung vao:
- quan ly nguoi dung, phong ban, vai tro, quyen
- quan ly du an, phase, task, task note
- quan ly project member
- quan ly allocation cho phase
- quan ly task assignment nhu du lieu ghi nhan nhan vien tham gia task
- quan ly giao dich point gom earning transaction va task cost transaction

Ngoai pham vi v1:
- skill matrix
- audit log chi tiet
- timesheet/work log theo ngay
- health dashboard nang cao

## 3. Vai tro nguoi dung
### 3.1 Admin
- quan ly danh muc he thong
- duyet hoac tu choi ke hoach phan bo
- quan tri nguoi dung, vai tro, quyen

### 3.2 PM
- tao va quan ly project
- tao phase, task
- chon project member
- phan bo nhan luc trong phase
- theo doi tien do va hieu suat du an

### 3.3 Employee
- tham gia project theo phan cong
- cap nhat tien do task thong qua du lieu ghi nhan
- nhan point tu ket qua lam viec

## 4. Doi tuong nghiep vu chinh
### 4.1 User
Thong tin nhan vien hoac nguoi dung he thong gom:
- `userName`
- `position`
- `resourceRate`
- `avatarUrl`
- `hidden`
- `department`
- `roles`

### 4.2 Department
Quan ly phong ban cua user.

### 4.3 Role va Permission
Quan ly phan quyen o muc he thong:
- `Role` co danh sach `Permission`
- `User` co the thuoc nhieu `Role`

### 4.4 Authentication
Quan ly thong tin xac thuc:
- `email`
- `password`
- `forgotPasswordCode`
- lien ket 1-1 voi `User`

### 4.5 Project
Dai dien cho mot du an, gom:
- ten du an
- PM phu trach
- thoi gian bat dau/ket thuc
- budget khach hang
- budget noi bo
- mo ta
- trang thai du an

### 4.6 ProjectMember
Danh sach thanh vien cua du an theo tung user va trang thai:
- `PENDING`
- `APPROVED`
- `REJECTED`
- `REMOVED`

### 4.7 Phase
Dai dien cho mot giai doan trong project, gom:
- project cha
- thoi gian bat dau/ket thuc
- ten phase
- so workdays
- PM phu trach phase
- trang thai ke hoach phan bo

### 4.8 Allocation
Ghi nhan phan bo nhan vien cho phase:
- phase
- employee
- allocation percentage
- resource rate snapshot
- allocation point

`allocationPoint` la chi so point noi bo cua phan bo, duoc tinh dua tren thoi luong phase, resource rate snapshot va ty le allocation.

### 4.9 Task
Dai dien cho cong viec trong project, gom:
- task name
- thoi gian bat dau/ket thuc
- project
- task status
- task point
- danh sach assignment
- danh sach note

### 4.10 TaskAssignment
Ghi nhan viec mot user tham gia xu ly task va ket qua dong gop cuoi cung:
- task
- user
- contribution percentage

Trong version hien tai, `TaskAssignment` la bang ghi nhan ty le dong gop cuoi cung cua nhan vien cho task. Bang nay khong con giu `effortPercentage` hay `workedDays`.

### 4.11 TaskNote
Ghi chu trao doi lien quan den task:
- task
- author
- content
- workingHours
- createdAt
- updatedAt

`workingHours` la so gio lam thuc te cua nhan vien cho task trong ngay.

### 4.12 EarningTransaction
Giao dich point thuc nhan cua nhan vien:
- task lien quan
- employee
- referenceTransaction
- points
- earningTransactionType
- reason

`EarningTransaction` la nguon ghi nhan point thuc nhan va co the tham chieu sang transaction truoc do. `points` duoc tinh tu `Task.taskPoint` va `TaskAssignment.contributionPercentage`.

### 4.13 TaskCostTransaction
Giao dich point chi phi noi bo theo task:
- task
- executor
- points
- workdays
- reason

`TaskCostTransaction` phan anh chi phi point phat sinh tu thoi gian lam viec thuc te cua task. `workdays` duoc suy ra tu tong `TaskNote.workingHours / 8`. `points` duoc tinh theo cong thuc noi bo tu `workdays` va `Allocation.resourceRateSnapshot`.

## 5. Trang thai nghiep vu
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

## 6. Quy tac nghiep vu chinh
1. Mot `Project` co mot PM phu trach.
2. Mot `Project` co the co nhieu `Phase`.
3. Mot `Phase` thuoc dung mot `Project`.
4. Mot `Task` thuoc dung mot `Project`.
5. Mot `Task` co the co nhieu `TaskAssignment`.
6. Mot user chi duoc co mot `TaskAssignment` cho cung mot task.
7. `TaskAssignment` dung de ghi nhan ty le dong gop cuoi cung cua nhan vien tren task.
8. `TaskNote.workingHours` la so gio lam thuc te cua nhan vien cho task trong ngay.
9. `Allocation` dung de phan bo mot user vao mot `Phase` theo phan tram thoi gian.
10. Tong allocation cua mot user trong cung khoang thoi gian can duoc kiem tra de tranh vuot muc cho phep, tru truong hop duoc phep overcommit.
11. `Allocation.resourceRateSnapshot` la gia tri snapshot cua `User.resourceRate` tai thoi diem phan bo va khong thay doi trong suot phase.
12. `Allocation.allocationPoint = Phase.workdays * Allocation.resourceRateSnapshot * Allocation.allocationPercentage / 100`
13. `EarningTransaction.points = Task.taskPoint * TaskAssignment.contributionPercentage / 100`
14. `TaskCostTransaction.workdays = sum(TaskNote.workingHours) / 8`
15. `TaskCostTransaction.points = TaskCostTransaction.workdays * Allocation.resourceRateSnapshot`
16. `EarningTransaction` co the tham chieu sang transaction truoc do qua `referenceTransaction`.
17. `ProjectMember` la danh sach thanh vien duoc xem xet hoac tham gia project theo trang thai.
18. `hidden` tren `User` va `Department` phuc vu an du lieu nhung khong xoa vat ly.

## 7. Yeu cau du lieu
- ID cua cac bang la tu tang.
- Cac bang quan he phai dam bao khoa ngoai dung huong.
- Nhung truong trang thai dung enum luu dang chuoi.
- `task_point`, `points`, `client_budget`, `project_budget`, `resource_rate`, `resource_rate_snapshot`, `allocation_point` phai dung kieu so chinh xac, khong dung so thuc co sai so.
- `project_member`, `task_assignment` nen co rang buoc tranh trung cap theo cap khoa nghiep vu.
- Cac cong thuc noi bo phai duoc ap dung nhat quan trong service layer.

## 8. Bao cao va danh gia
He thong can ho tro cac bao cao co ban:
- tien do du an theo project, phase, task
- muc do phan bo nhan luc theo phase
- hieu suat nhan vien dua tren assignment va earning transaction
- tong point theo user, project, task
- trang thai du an va trang thai task

## 9. Huong mo rong
Cac phan co the bo sung o phien ban sau:
- `ProjectHealthSnapshot`
- `ProjectRisk`
- `AuditLog`
- `PointAdjustmentLog`
- `Skill`, `UserSkill`, `SkillRequirement`
- `WorkLog` hoac `Timesheet`

## 10. Ghi chu trien khai
Tai lieu nay mo ta v1 theo structure hien tai cua codebase. Khi nghiep vu thay doi, SRS can duoc cap nhat dong bo voi entity, constant va rule kiem tra o service layer.
