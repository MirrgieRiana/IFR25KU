---
name: pixiv-novel-extraction
description: Pixiv小説のAPIにアクセスするスクリプトを提供する。
---

# Pixiv小説APIスキル

## 小説の検索

```shell
xa '
  @USE("https://raw.githubusercontent.com/MirrgieRiana/IFR25KU/refs/heads/main/site/pixiv-novel-extraction/pixiv.xa1")
  search("ずんだもん"; 1).body.novel.data() >> JSONL["  "]
'
```

## 小説データの取得

```shell
xa '
  @USE("https://raw.githubusercontent.com/MirrgieRiana/IFR25KU/refs/heads/main/site/pixiv-novel-extraction/pixiv.xa1")
  get("21278784").body.content
'
```
