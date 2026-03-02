# BananaGtn Android 睡眠锁机 App

这是一个可运行的 **Android (Kotlin + Compose)** 示例项目，目标是：
- 用户设置“必须睡觉时间”；
- 到点后进入锁机页；
- 若要解锁，必须走“计费解锁”；
- 支持支付宝 / 微信支付（当前仓库是 `FakePaymentGateway` 演示，正式版需接服务端）。

---

## 已实现（MVP Demo）

- 睡觉时间输入（示例）
- 语音来源设置入口（录制/上传示例按钮，约束说明为 <=60秒）
- 锁机状态模拟：`LOCKED`
- 解锁前计费决策（核心已落代码）
- 支付流程抽象：
  - `PaymentGateway` 接口
  - `FakePaymentGateway` 模拟支付成功
- 解锁成功后更新账本（次数、最近解锁时间）

> 注意：系统级“不可打开”在普通应用权限下无法 100% 保证。正式商用需 Device Owner + Kiosk/LockTask + 企业设备管理策略。

---

## 计费机制（完善版）

### 1）基础阶梯
按历史解锁次数（`totalUnlockCount`）计费：
- 第 1 次：0 元（免费，必须弹窗提醒“后续收费”）
- 第 2/3/4 次：1 元
- 第 5 次起：5 元

### 2）33 天未打开重置
- 当 `now - lastUnlockAt >= 33 天`，视为达成“长期坚持不打开”；
- 计费阶梯重置，再次解锁按“第 1 次免费”重新开始。

### 3）你的补充“每次缴费5元”如何兼容
你最后补充了“每次缴费5元，当满33天期间没有打开...”，与上面的阶梯价有冲突。建议产品化时支持两种策略可配置：
- `TIERED`：首免 + 1元阶梯 + 5元（当前代码默认）
- `FIXED_5`：除首免外，每次固定5元

本仓库先完成 `TIERED` 规则，`FIXED_5` 可在 `BillingPolicy` 中新增策略枚举快速扩展。

---

## 关键代码位置

- 计费策略：`app/src/main/java/com/bananagtn/sleeplock/billing/BillingPolicy.kt`
- 支付模型与网关：
  - `app/src/main/java/com/bananagtn/sleeplock/payment/PaymentModels.kt`
  - `app/src/main/java/com/bananagtn/sleeplock/payment/PaymentGateway.kt`
- 应用主界面（Demo 流程）：`app/src/main/java/com/bananagtn/sleeplock/ui/SleepLockApp.kt`
- 单元测试（计费规则）：`app/src/test/java/com/bananagtn/sleeplock/billing/BillingPolicyTest.kt`

---

## 正式版还需要你继续接入

1. **锁机能力**：
   - Device Owner（企业设备）
   - Lock Task / Kiosk
   - 紧急呼叫白名单

2. **语音能力**：
   - MediaRecorder 录制并硬限制 60 秒
   - 音频时长校验（上传）

3. **支付闭环（必须服务端）**：
   - 服务端创建订单
   - 支付宝/微信回调验签
   - 服务端落账后下发“可解锁 token”
   - 客户端拿 token 才能进入临时解锁窗口

4. **反作弊与风控**：
   - 防重放订单号
   - 防篡改本地次数（以服务端账本为准）
   - Root / 调试检测（可选）

---

## 运行说明

> 需要本地 Android Studio（JDK17 + Android SDK）

1. 打开项目根目录
2. Sync Gradle
3. 运行 `app` 到真机/模拟器

---

## 单元测试覆盖点

`BillingPolicyTest` 已覆盖：
- 首次免费
- 第 2~4 次 1 元
- 第 5 次及以后 5 元
- 33 天未解锁后重置


## 直接生成 APK（你本地可直接测试）

在项目根目录执行：

```bash
bash scripts/build_apk.sh
```

成功后 APK 路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```

如果你本地第一次编译失败，请检查：
- JDK 使用 17；
- Android Studio 已安装 Android SDK（API 34）；
- 网络可访问 Google Maven（`dl.google.com`），否则 Android Gradle Plugin/依赖无法下载。


## 发布版 APK（可直接安装测试）

### 1) 先生成签名文件（只需一次）

```bash
keytool -genkeypair   -v   -storetype PKCS12   -keystore banana-release.jks   -alias banana   -keyalg RSA   -keysize 2048   -validity 10000
```

### 2) 设置签名环境变量

```bash
export KEYSTORE_PATH="$PWD/banana-release.jks"
export KEYSTORE_PASSWORD="你的store密码"
export KEY_ALIAS="banana"
export KEY_PASSWORD="你的key密码"
```

### 3) 一键打包 release APK

```bash
bash scripts/build_release_apk.sh
```

成功后产物：

```text
app/build/outputs/apk/release/app-release.apk
```

### 4) 安装到手机

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

> 脚本会在构建后自动删除 `app/signing.properties`，避免签名密码残留在仓库。
