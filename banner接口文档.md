# 轮播图/公告接口文档

## 接口说明

轮播图和公告已合并为统一接口，通过 banner 表统一管理。

- `content` 为空时 → 纯图片轮播，点击跳转 `linkUrl`
- `content` 有值时 → 公告卡片，点击进入公告详情页渲染富文本

---

## 获取列表

- **接口地址：** `GET /api/banner`
- **是否需要鉴权：** 否
- **返回格式：**

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "title": "系统维护通知",
      "imageUrl": "https://example.com/banner.png",
      "linkUrl": "",
      "content": "<p style=\"text-align:center\"><strong>重要通知</strong></p><p>2025年4月25日凌晨2:00-6:00进行系统维护，届时服务暂停。</p>",
      "type": "info",
      "enabled": true,
      "sortOrder": 0,
      "createdAt": "2025-04-23 10:00:00",
      "updatedAt": "2025-04-23 10:00:00"
    },
    {
      "id": 2,
      "title": "新功能上线",
      "imageUrl": "https://example.com/new.png",
      "linkUrl": "https://example.com/detail",
      "content": "",
      "type": "info",
      "enabled": true,
      "sortOrder": 1,
      "createdAt": "2025-04-22 10:00:00",
      "updatedAt": "2025-04-22 10:00:00"
    }
  ]
}
```

---

## 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | number | ID |
| `title` | string | 标题 |
| `imageUrl` | string | 图片地址 |
| `linkUrl` | string | 跳转链接（content 为空时使用） |
| `content` | string | 公告内容，HTML 富文本（为空表示纯图片轮播） |
| `type` | string | 公告类型：`info`（一般通知）/ `warning`（警告）/ `error`（紧急） |
| `enabled` | boolean | 是否启用（固定返回 true，接口只返回启用的数据） |
| `sortOrder` | number | 排序值，越小越靠前 |
| `createdAt` | string | 创建时间 |
| `updatedAt` | string | 更新时间 |

---

## 前端适配逻辑

```javascript
// 伪代码
for (const item of bannerList) {
  if (item.content) {
    // 有 content → 公告卡片，点击跳转公告详情页，用 rich-text 渲染 content
    showNoticeCard(item)
  } else {
    // 无 content → 纯图片轮播，点击跳转 item.linkUrl
    showBannerImage(item)
  }
}
```

### type 字段使用建议

| type | 建议样式 |
|------|---------|
| `info` | 默认样式 / 蓝色标签 |
| `warning` | 橙色/黄色标签 |
| `error` | 红色标签 |

### content 富文本渲染

使用微信小程序 `rich-text` 组件：

```html
<rich-text nodes="{{item.content}}"></rich-text>
```

支持标签：`<p>` `<strong>` `<b>` `<em>` `<i>` `<u>` `<span>` `<img>` `<br>` `<h1>`~`<h6>` `<ul>` `<ol>` `<li>`

支持内联样式：`text-align` `font-weight` `color` `font-size` `background-color` `width` `line-height`

---

## 与旧接口的变化

| 变化点 | 旧 | 新 |
|--------|----|----|
| 新增字段 | - | `content`（富文本）、`type`（类型） |
| 新增字段 | - | `enabled`（布尔值，由 status 计算） |
| 排序规则 | sort_order 升序 | sort_order 升序 + created_at 倒序 |

其他字段（`id`、`title`、`imageUrl`、`linkUrl`）无变化，**原有轮播图功能不受影响**。
