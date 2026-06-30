# Git推送到GitHub操作指南

## 📋 前提条件

1. 已安装Git（下载地址：https://git-scm.com/）
2. 拥有GitHub账号
3. 已在GitHub上创建仓库

---

## 🚀 首次推送（全新仓库）

### 步骤1：配置Git用户信息

```bash
# 配置你的GitHub用户名
git config --global user.name "YourGitHubUsername"

# 配置你的邮箱
git config --global user.email "your.email@example.com"
```

### 步骤2：初始化Git仓库

```bash
# 进入项目根目录
cd F:\AIGC

# 初始化Git仓库
git init
```

### 步骤3：添加文件到暂存区

```bash
# 添加所有文件
git add .

# 或者添加指定文件
git add README.md
git add XiaoBan/
```

### 步骤4：提交到本地仓库

```bash
# 提交并添加说明信息
git commit -m "feat: 首次提交项目代码"

# 多行提交信息
git commit -m "feat: 项目初始版本

- 添加了前端Android代码
- 添加了后端Spring Boot代码
- 完成基础功能开发"
```

### 步骤5：关联远程仓库

```bash
# 添加远程仓库（将URL替换为你的仓库地址）
git remote add origin https://github.com/YourUsername/YourRepo.git

# 查看远程仓库
git remote -v
```

### 步骤6：推送到GitHub

```bash
# 重命名分支为main（GitHub默认分支）
git branch -M main

# 推送到远程仓库
git push -u origin main
```

---

## 🔄 日常更新推送

当你修改了代码后，按以下步骤推送：

### 1. 查看修改状态

```bash
# 查看哪些文件被修改了
git status

# 查看具体修改内容
git diff
```

### 2. 添加修改的文件

```bash
# 添加所有修改
git add .

# 或添加指定文件
git add path/to/file.java
```

### 3. 提交修改

```bash
# 提交并说明修改内容
git commit -m "fix: 修复了登录bug"
```

### 4. 推送到GitHub

```bash
# 推送到main分支
git push origin main

# 或简写（如果已设置upstream）
git push
```

---

## 📝 提交信息规范

推荐使用以下格式：

```
<type>(<scope>): <subject>

<body>
```

**类型（type）：**
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具链更新

**示例：**
```bash
git commit -m "feat(auth): 添加用户登录功能"
git commit -m "fix(chat): 修复语音识别失败的问题"
git commit -m "docs: 更新README安装说明"
```

---

## 🔧 常用Git命令

### 查看状态和历史

```bash
# 查看当前状态
git status

# 查看提交历史
git log

# 查看简洁的提交历史
git log --oneline

# 查看最近5条提交
git log --oneline -5
```

### 分支操作

```bash
# 查看所有分支
git branch -a

# 创建新分支
git branch feature-new

# 切换分支
git checkout feature-new

# 创建并切换到新分支
git checkout -b feature-new

# 合并分支
git merge feature-new

# 删除分支
git branch -d feature-new
```

### 撤销操作

```bash
# 撤销工作区的修改
git checkout -- file.txt

# 撤销暂存区的文件（保留工作区修改）
git reset HEAD file.txt

# 撤销最后一次提交（保留修改）
git reset --soft HEAD^

# 撤销最后一次提交（删除修改）
git reset --hard HEAD^
```

### 远程操作

```bash
# 查看远程仓库
git remote -v

# 添加远程仓库
git remote add origin https://github.com/user/repo.git

# 修改远程仓库地址
git remote set-url origin https://github.com/user/new-repo.git

# 拉取远程更新
git pull origin main

# 推送到远程
git push origin main
```

---

## 🛡️ .gitignore文件

在项目根目录创建`.gitignore`文件，指定不需要提交的文件：

```gitignore
# IDE配置
.idea/
.vscode/
*.iml

# 编译产物
build/
target/
*.class

# 本地配置
local.properties
.env

# 日志文件
*.log

# 操作系统文件
.DS_Store
Thumbs.db

# Claude AI配置
.claude/
```

---

## ⚠️ 常见问题

### 问题1：push被拒绝

```bash
error: failed to push some refs to 'https://github.com/...'
```

**解决方法：**
```bash
# 先拉取远程更新
git pull origin main

# 解决冲突后再推送
git push origin main
```

### 问题2：忘记添加文件就提交了

```bash
# 添加遗漏的文件
git add forgotten-file.txt

# 修改最后一次提交
git commit --amend --no-edit
```

### 问题3：想要强制覆盖远程仓库

```bash
# 强制推送（谨慎使用！会覆盖远程历史）
git push -f origin main
```

### 问题4：提交了不该提交的文件

```bash
# 从Git中删除但保留本地文件
git rm --cached file.txt

# 提交删除操作
git commit -m "chore: 移除敏感文件"

# 推送
git push origin main
```

---

## 🔐 安全提示

### 推送前检查清单

- [ ] `.gitignore`已配置，排除敏感文件
- [ ] 没有提交密码、API密钥等敏感信息
- [ ] 没有提交编译产物（build/, target/）
- [ ] 没有提交IDE配置文件
- [ ] 提交信息清晰明确

### 检查命令

```bash
# 查看即将提交的文件
git status

# 查看文件内容是否包含敏感信息
git diff

# 查看暂存区的文件
git diff --cached
```

---

## 📚 快速参考

### 完整推送流程（新项目）

```bash
# 1. 初始化
git init
git config user.name "YourName"
git config user.email "your@email.com"

# 2. 添加文件
git add .

# 3. 提交
git commit -m "feat: 初始提交"

# 4. 关联远程仓库
git remote add origin https://github.com/user/repo.git

# 5. 推送
git branch -M main
git push -u origin main
```

### 日常更新流程

```bash
# 1. 查看修改
git status

# 2. 添加修改
git add .

# 3. 提交
git commit -m "fix: 修复bug"

# 4. 推送
git push
```

---

## 🔗 有用的资源

- **Git官方文档**: https://git-scm.com/doc
- **GitHub文档**: https://docs.github.com/
- **Git简明指南**: https://rogerdudler.github.io/git-guide/index.zh.html
- **Learn Git Branching**: https://learngitbranching.js.org/?locale=zh_CN

---

## 💡 小技巧

### 1. 设置Git别名

```bash
# 设置常用命令的别名
git config --global alias.st status
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit

# 使用别名
git st  # 等同于 git status
git ci -m "message"  # 等同于 git commit -m "message"
```

### 2. 查看漂亮的提交历史

```bash
git log --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit
```

### 3. 临时保存工作

```bash
# 保存当前工作
git stash

# 查看保存的工作
git stash list

# 恢复保存的工作
git stash pop
```

---

<div align="center">

**掌握Git，轻松管理代码版本！** 🚀

</div>
