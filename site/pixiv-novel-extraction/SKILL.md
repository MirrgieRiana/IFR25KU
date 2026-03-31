---
name: pixiv-novel-extraction
description: Pixiv小説のAPIにアクセスするスクリプトを提供する。
---

# Pixiv小説APIスキル

## 小説の検索

```shell
xa '
  search := q, p -> (
    SLEEP << 1000
    FETCH("https://www.pixiv.net/ajax/search/novels/$(URL(q))?word=$(URL(q))&order=date_d&mode=safe&p=$p&csw=0&s_mode=s_tag&gs=0&ai_type=1&lang=ja").$*
  )
  1 .. 7 | p => search("ずんだもん"; p).body.novel.data() >> JSONL["  "]
'
```

## 小説データの取得

```shell
xa '
  get := id -> (
    SLEEP << 1000
    FETCH("https://www.pixiv.net/ajax/novel/$id?lang=ja").$*
  )
  get("21278784").body.content
' | head -n 10
```
