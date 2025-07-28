# FX:ID 总结文档

这个文档总结了项目中所有 FXML 文件的 FX:ID 及其用途说明。

## auth.fxml - 认证界面

该文件包含应用程序的登录/注册界面组件。

| FX:ID | 组件类型 | 用途说明 |
|-------|----------|----------|
| `btnLogin` | Button | 登录按钮 - 用户点击进入登录流程 |
| `btnRegister` | Button | 注册按钮 - 用户点击进入注册流程 |
| `btnGuest` | Button | 访客模式按钮 - 允许用户以访客身份继续使用应用 |

## menu.fxml - 菜单栏界面

该文件包含应用程序的主菜单栏组件。

| FX:ID | 组件类型 | 用途说明 |
|-------|----------|----------|
| `menuFile` | Menu | 文件菜单 - 包含应用程序基本操作 |
| `menuItemAbout` | MenuItem | 关于菜单项 - 显示应用程序信息 |
| `menuItemQuit` | MenuItem | 退出菜单项 - 退出应用程序 |
| `menuAccount` | Menu | 账户菜单 - 包含用户账户相关操作 |
| `menuItemLogin` | MenuItem | 登录菜单项 - 跳转到登录页面 |
| `menuItemRegister` | MenuItem | 注册菜单项 - 跳转到注册页面 |
| `menuItemLogout` | MenuItem | 登出菜单项 - 用户登出操作 |
| `menuItemProfile` | MenuItem | 个人资料菜单项 - 查看/编辑用户资料 |

## tabs.fxml - 标签页界面

该文件包含应用程序的主要功能标签页。

| FX:ID | 组件类型 | 用途说明 |
|-------|----------|----------|
| `tabPaneMain` | VBox | 主标签页容器 - 包含所有功能标签页的根容器 |
| `tabHome` | Tab | 首页标签 - 显示应用程序主页内容 |
| `tabAnnouncements` | Tab | 公告标签 - 显示社区公告和通知 |
| `tabFoodSharing` | Tab | 食物分享标签 - 食物分享功能页面 |
| `tabFoodInventory` | Tab | 食物库存标签 - 食物库存管理页面 |
| `tabDiscussionForum` | Tab | 讨论论坛标签 - 社区讨论交流页面 |
| `tabEvents` | Tab | 活动标签 - 社区活动信息页面 |

## login.fxml - 登录界面

该文件包含用户登录界面的组件。

| FX:ID | 组件类型 | 用途说明 |
|-------|----------|----------|
| `loginUser` | TextField | 用户名或邮箱输入框 |
| `loginPassword` | PasswordField | 密码输入框 |
| `btnLogin` | Button | 登录按钮 - 用户点击进入登录流程 |
| `btnRegister` | Button | 注册按钮 - 跳转到注册页面 |
| `btnBack` | Button | 返回按钮 - 返回到上一个界面 |

## register.fxml - 注册界面

该文件包含用户注册界面的组件。

| FX:ID | 组件类型 | 用途说明 |
|-------|----------|----------|
| `registerUser` | TextField | 用户名输入框 |
| `registerName` | TextField | 用户全名输入框 |
| `registerContact` | TextField | 联系信息输入框 |
| `registerEmail` | TextField | 邮箱输入框 |
| `registerPassword` | PasswordField | 密码输入框 |
| `registerPassword1` | PasswordField | 重复密码输入框 |
| `btnLogin` | Button | 发送验证邮件按钮 |
| `btnRegister` | Button | 注册按钮 - 提交注册信息 |
| `btnBack` | Button | 返回按钮 - 返回到上一个界面 |
| `lblUsernameStatus` | Text | 用户名状态提示 |
| `lblEmailStatus` | Text | 邮箱状态提示 |
| `lblPasswordStatus` | Text | 密码状态提示 |
| `lblPasswordRepeatStatus` | Text | 重复密码状态提示 |

## 使用说明

### FX:ID 的作用
- **组件标识**: FX:ID 为 FXML 中的 UI 组件提供唯一标识符
- **控制器绑定**: 在 JavaFX 控制器类中，可以使用 `@FXML` 注解和相同的变量名来引用这些组件
- **事件处理**: 通过 FX:ID 可以为组件添加事件监听器和处理方法

### 命名规范
项目中的 FX:ID 遵循以下命名规范：
- **按钮**: `btn` + 功能描述 (如 `btnLogin`, `btnRegister`)
- **菜单**: `menu` + 功能描述 (如 `menuFile`, `menuAccount`)
- **菜单项**: `menuItem` + 功能描述 (如 `menuItemLogin`, `menuItemAbout`)
- **标签页**: `tab` + 功能描述 (如 `tabHome`, `tabEvents`)
- **容器**: 功能描述 + 类型 (如 `tabPaneMain`)

### 开发建议
1. 确保在控制器类中声明对应的 `@FXML` 字段
2. 为每个交互式组件添加相应的事件处理方法
3. 保持命名的一致性和描述性
4. 定期更新此文档以反映新增或修改的组件

---

*最后更新: 2025年7月28日*
