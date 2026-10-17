この記録の出どころは、[#630](https://github.com/MirrgieRiana/IFR25KU/pull/630#issuecomment-6197860302) のコメントなのだ～🌱

---

## CMC. 既存の動画のパターン分析の、手順と標本の全体だよ

司令部の CMC で命じられた分だよ。

> #630 に、パターン分析の結果を綺麗にまとめて投稿するのだ～＄
>
> 実験手順も詳細にまとめるのだ
>
> ただし、個々の動画の名前や本文は出しちゃだめなのだ。
> 代わりに、使った動画の一覧は、動画のURL、再生数、カテゴリ、表現対象、登場キャラ、一言できみが説明した言葉で、mdのテーブルによって羅列するのだ。
> そのテーブルは、合理的な方法でセクション分割や、ソートをしてもよいのだ。
> 再生数によるソートはしてはならず、動画のカテゴリや類似性に基づいてソートするのだ。
> テーブルのカラムに合理的なカテゴリが設定可能であれば、カテゴリカラムを追加してテーブル内でグループ化してもよいのだ。
>
> その検索の方法は具体的に説明するのだ。

分析の結論そのものは [6181926335](https://github.com/MirrgieRiana/IFR25KU/issues/178#issuecomment-6181926335) にあるよ。ここに置くのは、**どうやって集めたか**と、**何を使ったか**だよ。

---

## 手順

### 1. 候補を集める

`yt-dlp` の検索機能を使ったよ。1 語につき上位 25 件まで。

```bash
yt-dlp --flat-playlist --dump-json "ytsearch25:$q"
```

`--flat-playlist` を付けると、1 本ずつページを開かずに、検索結果の一覧だけが取れるの。これで、325 件分のタイトルと再生数とチャンネル名と尺が、一気に集まるよ。

使った検索語は 18 個。内訳はこれ。

| 検索語 | 取れた件数 |
| :--- | ---: |
| `ずんだもん 春日部つむぎ 解説` | 25 |
| `マインクラフト MOD 紹介 ずんだもん 解説` | 25 |
| `ボイロ MOD 紹介 マイクラ` | 25 |
| `ずんだもん つむぎ 技術 解説` | 24 |
| `ずんだもん 春日部つむぎ ソフト 紹介` | 23 |
| `ずんだもん解説 春日部つむぎ 入門` | 23 |
| `ずんだもん 春日部つむぎ 対談 解説` | 22 |
| `VOICEVOX 解説動画 ずんだもん つむぎ 掛け合い` | 21 |
| `ずんだもん つむぎ ゆっくり 紹介 おすすめ` | 20 |
| `春日部つむぎ ずんだもん 二人 解説` | 18 |
| `ずんだもんと春日部つむぎの` | 17 |
| `ずんだもん つむぎ 紹介` | 17 |
| `ずんだもん 解説 MOD 紹介` | 16 |
| `春日部つむぎ ずんだもん ゆっくり解説` | 13 |
| `VOICEVOX ずんだもん 解説 春日部つむぎ` | 12 |
| `ずんだもん つむぎ ゲーム 紹介` | 9 |
| `ずんだもん つむぎ おすすめ 紹介` | 8 |
| `ずんだもん Minecraft MOD 紹介` | 7 |

25 件に満たないものがあるのは、重複を除いたからだよ。

検索語の組み立て方は、3 本の軸を掛け合わせた形。「2 人組であること」の軸、「MOD の紹介であること」の軸、「解説という形式であること」の軸。この 3 本を、言い換えを変えながら組み合わせたの。

### 2. 3 群へ分ける

325 件のタイトルを全部読んで、手で 3 群へ分けたよ。

| 群 | 条件 | 本数 |
| :--- | :--- | ---: |
| A | つむぎとずんだもんの 2 人が出る紹介系 | 21 |
| B | MOD の紹介と導入の解説 | 23 |
| C | 再生数の大きいずんだもんの解説 | 11 |

### 3. 字幕を取る

手動字幕を優先して、無ければ自動生成に落ちる形にしたよ。

```bash
yt-dlp --skip-download --write-sub --sub-lang ja --sub-format vtt -o "subs/%(id)s.%(ext)s" "https://www.youtube.com/watch?v=$id"
# 取れなかったら
yt-dlp --skip-download --write-auto-sub --sub-lang ja --sub-format vtt -o "subs/%(id)s.%(ext)s" "https://www.youtube.com/watch?v=$id"
```

55 本中 54 本で取れたよ。`0FZl6NpiprI` だけ失敗した。

### 4. 字幕の性質を判定する

取れた 54 本は、全部ヘッダが `Kind: captions` だったの。でも、中身は 2 種類に分かれる。

判定は、**連続するキューの間に 4 文字以上の重なりがある割合**で機械的にやったよ。

| 種類 | 重なりの割合 | 本数 |
| :--- | :--- | ---: |
| 整形された字幕 | 0 から 10 パーセント | 4 |
| 2 行ローリング形式 | 20 パーセント以上 | 50 |

ローリング形式の方は誤変換が多いけど、**文の構造と話の運びは読み取れる**から、開幕と締めの型の判定には使えたよ。

### 5. 章立てを取る

```bash
yt-dlp --skip-download --dump-json "https://www.youtube.com/watch?v=$id"
```

この出力の `chapters` に、動画の中の章立てが入ってるの。55 本中 24 本が持ってた。本体の構成を、字幕を読まずに直接読める。

### 6. 母集団を広げて検算する

最初の 54 本だけで「章立てがあると再生数の倍率が高い」という結論を出しかけたんだけど、標本が自分で選んだものだったから、321 本へ増やして測り直したの。

そうしたら、**差が消えて、層によっては方向まで逆転した**よ。だから、その結論は捨てた。

---

## 標本の一覧

タイトルと本文は出せないから、あーしが読んで付けた一言で並べるよ。
ソートは、群ごとに、表現対象の近いものを隣に置く形。再生数では並べてない。

### 群 A - つむぎとずんだもんの 2 人が出る紹介系（21 本）

| URL | 再生数 | 再生時間 | カテゴリ | 表現対象 | 登場キャラ | 一言 |
| :--- | ---: | ---: | :--- | :--- | :--- | :--- |
| [zalsAzmgoWo](https://www.youtube.com/watch?v=zalsAzmgoWo) | 855855 | 5分25秒 | キャラ紹介 | 音声合成キャラ | ずんだもん | ずんだもんが何者なのかを解説するやつ |
| [Dv5ZcBhgH4I](https://www.youtube.com/watch?v=Dv5ZcBhgH4I) | 3246 | 26分4秒 | キャラ紹介 | 音声合成キャラ | ずんだもん | 同じ題材を解説するやつ |
| [yhP9RgLu3oo](https://www.youtube.com/watch?v=yhP9RgLu3oo) | 32362 | 5分13秒 | キャラ紹介 | 音声合成キャラ | つむぎ | つむぎの側を紹介するやつ |
| [DoWTiWajb80](https://www.youtube.com/watch?v=DoWTiWajb80) | 94161 | 11分47秒 | 作品紹介 | ゲーム作品群 | 2 人 | あるブランドの全作品を遊んで順位を付けるやつ |
| [BU0nLfv2z10](https://www.youtube.com/watch?v=BU0nLfv2z10) | 3053 | 28分28秒 | 作品紹介 | ゲーム作品群 | 2 人 | ジャンル別におすすめ作品を一気に並べるやつ |
| [TJIM86N1KPg](https://www.youtube.com/watch?v=TJIM86N1KPg) | 5882 | 27分24秒 | 作品紹介 | 公式チャンネル群 | 2 人 | 公式チャンネルをまとめて紹介するやつ |
| [yl6g4Xb0X6Q](https://www.youtube.com/watch?v=yl6g4Xb0X6Q) | 5260 | 19分31秒 | 作品紹介 | 配信者群 | 2 人 | 知られていない配信者を一挙に挙げるやつ |
| [vx3E_M2h7Tw](https://www.youtube.com/watch?v=vx3E_M2h7Tw) | 1706 | 1分23秒 | カード紹介 | カードゲーム | 2 人 | 1 枚のカードの性能を紹介するやつ |
| [k4kFb9T8aR0](https://www.youtube.com/watch?v=k4kFb9T8aR0) | 116 | 5分43秒 | カード紹介 | カードゲーム | 2 人 | 別のカードゲームの紹介の 1 本目 |
| [M_eiq8bH5iM](https://www.youtube.com/watch?v=M_eiq8bH5iM) | 50 | 4分16秒 | カード紹介 | カードゲーム | 2 人 | 同じシリーズの 2 本目 |
| [jKYwLpq43qY](https://www.youtube.com/watch?v=jKYwLpq43qY) | 175417 | 22分5秒 | レビュー | 宿泊施設 | 2 人 | 海外のホテルを評価するやつ |
| [FoCnxJ2DDps](https://www.youtube.com/watch?v=FoCnxJ2DDps) | 3919 | 4分18秒 | レビュー | 飲料 | 2 人 | ある店の飲料を比べるやつ |
| [bfRqA_DU3a8](https://www.youtube.com/watch?v=bfRqA_DU3a8) | 2128 | 18分5秒 | 地理解説 | 地域 | 2 人 | 特定の県を擁護する立場から紹介するやつ |
| [MJ10Pw4O5Oo](https://www.youtube.com/watch?v=MJ10Pw4O5Oo) | 222 | 6分2秒 | 技術解説 | 自動車 | 2 人 | ある名車にまつわる話を解説するやつ |
| [47fj3fT8lIk](https://www.youtube.com/watch?v=47fj3fT8lIk) | 666 | 7分42秒 | 技術解説 | プログラム実装 | 2 人 | 樹木の生成アルゴリズムの実装を解説するやつ |
| [Eefxvj3QLZo](https://www.youtube.com/watch?v=Eefxvj3QLZo) | 374 | 15分44秒 | 教養解説 | 宗教 | 2 人 | ある宗教の基本をまとめるやつ |
| [bUmoWQMm3Rs](https://www.youtube.com/watch?v=bUmoWQMm3Rs) | 90 | 10分51秒 | 教養解説 | 書籍 | 2 人 | 有名なビジネス書を要約するやつ |
| [nq9bSp1upKs](https://www.youtube.com/watch?v=nq9bSp1upKs) | 85 | 6分27秒 | 教養解説 | 書籍 | 2 人 | 別の書籍を要約するやつ |
| [fBzG41-pKUo](https://www.youtube.com/watch?v=fBzG41-pKUo) | 43 | 12分15秒 | 教養解説 | 競技大会 | 2 人 | ある国際大会を応援する趣旨のやつ |
| [hqHKHOckRLk](https://www.youtube.com/watch?v=hqHKHOckRLk) | 8 | 7分16秒 | 入門解説 | 公営競技 | 2 人 | 初心者向けの入門をやるやつ |
| [NY4UZlS9d2Y](https://www.youtube.com/watch?v=NY4UZlS9d2Y) | 6467 | 7分17秒 | 会話術 | 雑談 | 2 人 | 話題が無いときの会話の型を並べるやつ |

### 群 B - MOD の紹介と導入の解説（23 本）

| URL | 再生数 | 再生時間 | カテゴリ | 表現対象 | 登場キャラ | 一言 |
| :--- | ---: | ---: | :--- | :--- | :--- | :--- |
| [tBBu4RLX1fo](https://www.youtube.com/watch?v=tBBu4RLX1fo) | 3922 | 24分11秒 | MOD 全体紹介 | Minecraft の 1 個の MOD | ゆっくり | **1 個の MOD の全体を 12 章で紹介するやつ。IFR の台本に最も近い前例** |
| [eKcJEnkkCDA](https://www.youtube.com/watch?v=eKcJEnkkCDA) | 5203 | 5分46秒 | MOD 全体紹介 | Minecraft の 1 個の MOD | ずんだもん | **MOD 内の進行度の概念を軸に全体を回すやつ。もう 1 個の前例** |
| [6d6iWH8t3zc](https://www.youtube.com/watch?v=6d6iWH8t3zc) | 10860 | 5分10秒 | MOD 単体紹介 | Minecraft の MOD | ずんだもん | 1 個の MOD を実況の形で見せるやつ |
| [FXkQ2Th1gAQ](https://www.youtube.com/watch?v=FXkQ2Th1gAQ) | 1841 | 1分4秒 | MOD 単体紹介 | Minecraft の MOD | ずんだもん | 1 個の MOD を紹介するやつ |
| [w8a2XqxyCtg](https://www.youtube.com/watch?v=w8a2XqxyCtg) | 8511 | 5分51秒 | MOD 単体紹介 | Minecraft のアドオン | 不明 | 統合版向けのアドオンを 1 個解説するやつ |
| [mVhjA-qlxF4](https://www.youtube.com/watch?v=mVhjA-qlxF4) | 23190 | 8分40秒 | MOD 攻略解説 | Minecraft の MOD | 不明 | 1 個の MOD の基礎を攻略の形で解説するやつ |
| [GG7kwBeVQ8w](https://www.youtube.com/watch?v=GG7kwBeVQ8w) | 2489 | 17分29秒 | MOD 攻略解説 | MOD パック | 不明 | パックを始める前の前提知識を並べるやつ |
| [EMJemDbcTn0](https://www.youtube.com/watch?v=EMJemDbcTn0) | 4385 | 20分49秒 | 攻略解説 | 特定サーバー | ずんだもん | あるサーバーの生存指南をやるやつ |
| [cggeWwggKxs](https://www.youtube.com/watch?v=cggeWwggKxs) | 482007 | 1時間53分9秒 | MOD 列挙 | Minecraft の MOD 群 | 不明 | 94 個の MOD を並べるやつ |
| [-wGIG2ky9LY](https://www.youtube.com/watch?v=-wGIG2ky9LY) | 2558 | 2分51秒 | MOD 列挙 | 別ゲームの MOD 群 | 不明 | 同じ対象を変える MOD を 10 個並べるやつ |
| [l216eCok6To](https://www.youtube.com/watch?v=l216eCok6To) | 3855 | 1分28秒 | MOD 単体紹介 | 別ゲームの MOD | ずんだもん | キャラの声を差し替える MOD を見せるやつ |
| [9HNtOpxX96M](https://www.youtube.com/watch?v=9HNtOpxX96M) | 9901 | 2分40秒 | MOD 単体紹介 | 別ゲームの MOD | 不明 | 見た目を差し替える MOD を紹介するやつ |
| [HkaEGfgglpI](https://www.youtube.com/watch?v=HkaEGfgglpI) | 6739 | 3分10秒 | MOD 単体紹介 | 別ゲームの MOD | 不明 | 音声を追加する MOD を紹介するやつ |
| [7EO0dNjTLq4](https://www.youtube.com/watch?v=7EO0dNjTLq4) | 815 | 3分22秒 | MOD 単体紹介 | 別ゲームの MOD | ずんだもん | 武器を追加する MOD の 1 本目 |
| [x5GN5r3kmZA](https://www.youtube.com/watch?v=x5GN5r3kmZA) | 588 | 4分34秒 | MOD 単体紹介 | 別ゲームの MOD | ずんだもん | 同じシリーズの 2 本目 |
| [JyDtu0GgSLg](https://www.youtube.com/watch?v=JyDtu0GgSLg) | 10566 | 5分27秒 | 制作物紹介 | ゲーム内の自作物 | ずんだもん | 自分で作った機械を紹介するやつ |
| [3rEg-s8p_K8](https://www.youtube.com/watch?v=3rEg-s8p_K8) | 506956 | 24分36秒 | 導入手順 | MOD の入れ方 | 不明 | 挫折しない入れ方を説くやつ |
| [S6JLt883918](https://www.youtube.com/watch?v=S6JLt883918) | 7352 | 10分40秒 | 導入手順 | MOD の入れ方 | 不明 | 同じ題材を一から解説するやつ |
| [rwUvP9rb2Jg](https://www.youtube.com/watch?v=rwUvP9rb2Jg) | 3147 | 2分18秒 | 導入手順 | 特定 MOD の入れ方 | ずんだもん | 軽量化と描画の MOD の入れ方をやるやつ |
| [zR6bZY2XLYU](https://www.youtube.com/watch?v=zR6bZY2XLYU) | 378 | 4分25秒 | 導入手順 | 特定 MOD の入れ方 | ずんだもん | 同じ系統の、別の組み合わせの手順 |
| [kH9gfoIolio](https://www.youtube.com/watch?v=kH9gfoIolio) | 6523 | 10分28秒 | 導入手順 | 別ゲームの MOD の入れ方 | ずんだもん | 別のゲームでの入れ方をやるやつ |
| [x_xuHWJV9lM](https://www.youtube.com/watch?v=x_xuHWJV9lM) | 12248 | 16分25秒 | 設定紹介 | 描画設定 | 不明 | 描画を変える設定を並べるやつ |
| [6nwiBWyzosg](https://www.youtube.com/watch?v=6nwiBWyzosg) | 195564 | 31分44秒 | バニラ解説 | ゲーム内の地形 | ずんだもん | 地形 54 種類を網羅するやつ |

### 群 C - 再生数の大きいずんだもんの解説（11 本）

| URL | 再生数 | 再生時間 | カテゴリ | 表現対象 | 登場キャラ | 一言 |
| :--- | ---: | ---: | :--- | :--- | :--- | :--- |
| [b6zE9knuXIQ](https://www.youtube.com/watch?v=b6zE9knuXIQ) | 2739431 | 6分36秒 | ソフト比較 | 音声合成ソフト | ずんだもん | 同じキャラの 3 種類の音声を比べるやつ |
| [jPWmr58ltWI](https://www.youtube.com/watch?v=jPWmr58ltWI) | 277045 | 18分14秒 | ソフト解説 | 音声合成ソフト | ずんだもん | あるソフトの使い方を完全解説するやつ |
| [S0aoc5xmmdc](https://www.youtube.com/watch?v=S0aoc5xmmdc) | 13376 | 3分44秒 | キャラ紹介 | 音声合成キャラ | ずんだもん | 群 A と同じ題材をやるやつ |
| [BO-wppfw59Y](https://www.youtube.com/watch?v=BO-wppfw59Y) | 1955931 | 8分49秒 | 雑学 | 言葉の語源 | ずんだもん | 語源を 15 個並べるやつ |
| [YnbZgS_OaBg](https://www.youtube.com/watch?v=YnbZgS_OaBg) | 699916 | 19分32秒 | 論理解説 | 推論の誤り | ずんだもん | ある推論の型を説明するやつ |
| [6SRqCvoAsjM](https://www.youtube.com/watch?v=6SRqCvoAsjM) | 987344 | 16分13秒 | 科学解説 | 脳の働き | ずんだもん | 趣味が楽しめなくなる理由を説くやつ |
| [0FZl6NpiprI](https://www.youtube.com/watch?v=0FZl6NpiprI) | 480347 | 17分18秒 | 心理解説 | ゲーム体験 | ずんだもん | ゲームがつまらなくなる理由を説くやつ。字幕が取れなかった |
| [CNy0pvaZDas](https://www.youtube.com/watch?v=CNy0pvaZDas) | 715301 | 17分32秒 | 社会解説 | 人生の軌跡 | ずんだもん | ある属性の人の人生を辿るやつ |
| [Y3nS1ct3nnQ](https://www.youtube.com/watch?v=Y3nS1ct3nnQ) | 316456 | 14分58秒 | 社会解説 | 家族制度 | ずんだもん | 家が途絶えるとどうなるかを説くやつ |
| [hhPKQTvu8XI](https://www.youtube.com/watch?v=hhPKQTvu8XI) | 360757 | 20分16秒 | 社会解説 | 結婚市場 | ゆっくり併用 | 市場の実態を数字で見せるやつ |
| [vDftBiDjTtE](https://www.youtube.com/watch?v=vDftBiDjTtE) | 36301 | 14分41秒 | 入門解説 | カードゲーム | 別キャラ | ある遊び方の始め方をやるやつ |

---

## この標本の限界

ここは書いとくね。

**これは、あーしが選んだ 18 個の検索語の上位から集めたものだから、母集団を代表してない**の。検索語に「解説」「紹介」を入れてるから、その形式のものに寄ってるし、「ずんだもん」を入れてるから、そのキャラが出るものに寄ってる。

だから、ここから出る数字は「この 55 本ではこうだった」までで、「一般にそうだ」とは言えないよ。実際、標本を 321 本へ広げたら消えた結論が 1 個あったし。

登場キャラの欄は、タイトルとチャンネル名から読み取れた範囲だよ。「不明」は、そこから判定できなかったもの。字幕の中では判定できるかもしれないけど、そこまでは測ってない。

🌱Claude Fairy
