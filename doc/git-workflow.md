    QUY TRÌNH LÀM VIỆC VỚI BRANCH TRONG DỰ ÁN

1. Trước khi bắt đầu một task mới

- Chuyển về branch cha:

git switch main (hoặc branch cha của task)

- Lấy code mới nhất:

git pull --ff-only

- Tạo branch mới cho task:

git switch -c feat/ten-task

2. Trong quá trình làm task

- Kiểm tra trạng thái:

git status

- Xem branch hiện tại:

git branch --show-current

- Sau khi hoàn thành một phần công việc:

git add .
git commit -m "feat/fix: mo ta thay doi"

- Push branch lên remote:

git push -u origin feat/ten-task

- Những lần push sau chỉ cần:

git push

3. Nếu cần chuyển sang branch khác

- Nếu code đã commit:

git switch ten-branch-khac

- Nếu code chưa commit và muốn lưu tạm:

git stash

git switch ten-branch-khac

- Khi quay lại branch cũ:

git switch feat/ten-task

git stash pop

4. Trước khi hoàn thành task

- Lấy thông tin mới nhất từ remote:

git fetch origin

- Cập nhật code mới từ branch cha vào branch đang làm:

git rebase origin/main (hoặc origin/branch-cha)

- Nếu có conflict:
    + Sửa conflict
    + git add .
    + git rebase --continue

- Nếu muốn hủy rebase:

git rebase --abort

- Sau khi rebase thành công, chạy test lại.

- Push code:

git push

- Nếu branch đã từng push trước khi rebase:

git push --force-with-lease

5. Khi task hoàn thành

- Đảm bảo:

[ ] Code chạy được
[ ] Test liên quan chạy được
[ ] Không còn code debug
[ ] Không còn code/field cũ
[ ] Đã cập nhật branch cha mới nhất
[ ] Đã resolve conflict
[ ] Đã push code mới nhất

- Tạo Pull Request / Merge Request:

Source:
feat/ten-task

Target:
feat/main (hoặc branch cha)

6. Sau khi task đã được merge

- Chuyển về branch cha:

git switch main (hoặc branch cha của task)

- Lấy code mới nhất:

git pull --ff-only

- Nếu bắt đầu task mới thì tạo branch mới từ đây:

git switch -c feat/task-moi

NGUYÊN TẮC

- Một task hoặc một nhóm thay đổi độc lập = một branch.
- Luôn tạo branch mới từ branch cha mới nhất.
- Không làm nhiều task không liên quan trên cùng một branch.
- Trước khi merge, luôn cập nhật code mới nhất từ branch cha.
- Ưu tiên dùng rebase để cập nhật branch task.
- Sau rebase, dùng git push --force-with-lease, không dùng git push --force.
- Không merge trực tiếp vào main nếu quy trình dự án yêu cầu merge qua branch feature cha.

LUỒNG LÀM VIỆC

Branch cha
↓
git pull --ff-only
↓
Tạo branch task
↓
Code
↓
Commit
↓
Push
↓
Rebase branch cha mới nhất
↓
Test lại
↓
Push
↓
Tạo PR/MR vào branch cha
↓
Merge
↓
Cập nhật branch cha
↓
Tạo branch cho task tiếp theo