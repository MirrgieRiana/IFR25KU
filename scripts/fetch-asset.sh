#!/usr/bin/env bash

set -eu

cd -- "$(dirname -- "$0")/.."

script="$0" ./xarpite/xa -q '
  getb := url -> EXECB("curl", "-sL", url)
  get := url -> url >> getb >> UTF8D
  mkdirs := file -> EXEC("mkdir", "-p", file)

  entryPointUrl := "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
  version := "1.21.1"
  assetPaths := [
    "minecraft/lang/en_us.json"
    "minecraft/lang/ja_jp.json"
  ]

  ARGS.$# == 0 || :
    ERRL << "Usage: $(ENV.script)"
    EXIT << 1

  entryPointData := entryPointUrl >> get >> JSOND
  versionData := entryPointData | _.versions() >> FILTER[_ -> _.id == version] >> SINGLE | _.url >> get >> JSOND
  assetsData := versionData | _.assetIndex.url >> get >> JSOND
  assetPaths() | assetPath => :
    assetData := assetsData | _.objects.(assetPath).hash | "https://resources.download.minecraft.net/$(_::take(2))/$_" >> getb
    mkdirs << "./unpackedAssets" RESOLVE assetPath RESOLVE ".."
    WRITEB["./unpackedAssets" RESOLVE assetPath] << assetData
' "$@"
