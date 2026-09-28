---
title: 検索
description: IFR25KUの公式サイトのページの全文検索
layout: one-column
sidebar: false
toc: false
---

<link href="{{ '/pagefind/pagefind-component-ui.css' | relative_url }}" rel="stylesheet">
<script src="{{ '/pagefind/pagefind-component-ui.js' | relative_url }}" type="module"></script>

<div class="content-wrap">
  <pagefind-input></pagefind-input>
  <pagefind-summary></pagefind-summary>
  <pagefind-results></pagefind-results>
</div>

<script type="module">
  const instance = window.PagefindComponents.getInstanceManager().getInstance("default");
  instance.on("search", (term) => {
    const url = new URL(location.href);
    if (term) {
      url.searchParams.set("q", term);
    } else {
      url.searchParams.delete("q");
    }
    history.replaceState(history.state, "", url);
  });
  const query = new URLSearchParams(location.search).get("q");
  if (query) {
    instance.triggerSearch(query);
  }
</script>
