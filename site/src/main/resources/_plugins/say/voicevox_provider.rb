# frozen_string_literal: true

# =============================================================================
# say/voicevox_provider.rb — VOICEVOX キャラクター Provider
# =============================================================================
#
# VOICEVOX キャラクターの立ち絵レイヤー PNG を重ね合わせて、顔画像を生成するのだ～🌱
# extracted/ 内の presets.json と layers.json を読み込んで、パーツ単位で
# レイヤーを選んで合成するのだ～🌱
#
# ## 使用例
#
#   {% say zundamon23 %}のだ！{% endsay %}
#   {% say zundamon23:口=ほあー %}のだ！{% endsay %}
#   {% say zundamon23:口=ほあー:眉=困り眉2 %}のだ！{% endsay %}
#
# ## パラメータ
#
#   パーツ名（"眉"、"目"、"口" 等）をキーにして、選択肢のラベルを値として指定するのだ～🌱
#   radio パーツは、指定しないと "default": true の要素が使われるのだ～🌱
#   checkbox パーツは、指定しないと何も選ばれなくて、カンマ区切りで複数指定できるのだ～🌱
#   "color" は、枠線色の上書きなのだ～🌱（16進数カラーコード）
#            指定しないと、_data/voicevox.yml で定義された既定の色を使うのだ～🌱
#
# =============================================================================

require "json"
require "yaml"

module Say
  class VoicevoxProvider
    # extracted/ ディレクトリのパスとスラグを受け取って、
    # presets.json と layers.json を読み込んで初期化するのだ～🌱
    def initialize(slug, data_dir, color)
      @slug = slug
      @color = color
      presets_data = JSON.parse(File.read(File.join(data_dir, "presets.json")))
      layers_data = JSON.parse(File.read(File.join(data_dir, "layers.json")))
      @layers_tree = layers_data["layers"]
      @preset = presets_data.first
    end

    # プリセット名からパラメータハッシュへのマッピングを返すのだ～🌱
    # VOICEVOX Provider ではプリセットを使わないから、空のハッシュを返すのだ～🌱
    def presets
      {}
    end

    # 吹き出し枠線に使うキャラクター色を返すのだ～🌱
    def color(params)
      params["color"] || @color
    end

    # 吹き出しのトゲを表示するか否かを返すのだ～🌱
    def tail?
      true
    end

    # 解決済みパラメータから、顔部分の HTML を生成して返すのだ～🌱
    # 各パーツのレイヤー PNG を、absolute 配置で重ね合わせるのだ～🌱
    def resolve(params, context = nil)
      baseurl = context ? (context.registers[:site].config["baseurl"] || "") : ""
      face_base = "#{baseurl}/assets/images/voicevox/extracted/#{@slug}/face"

      layer_ids = collect_layer_ids(params)
      border_color = params["color"] || @color

      imgs = layer_ids.map { |id| %(<img src="#{face_base}/#{id}.png" alt="" />) }
      border_svg = %(<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="47" fill="none" stroke="#{border_color}" stroke-width="6"/></svg>)
      %(<div class="say-voicevox">#{imgs.join}#{border_svg}</div>)
    end

    private

    # パラメータに基づいて、有効なレイヤー ID のリストを集めるのだ～🌱
    def collect_layer_ids(params)
      ids = []

      # ベースレイヤー（衣装の基本構成）
      @preset["base"].each do |path|
        id = resolve_layer_path(path)
        ids << id if id
      end

      # パーツレイヤー
      @preset["parts"].each do |part|
        ids.concat(collect_part_layer_ids(part, params[part["name"]]))
      end

      ids
    end

    # 1 つのパーツについて、有効なレイヤー ID を集めるのだ～🌱
    def collect_part_layer_ids(part, selection)
      ids = []

      if part["mode"] == "radio"
        # radio: 1 つ選択（未指定時は default 要素）
        element = find_element(part, selection) || default_element(part)
        resolve_element_layers(element, ids) if element
      elsif selection
        # checkbox: 指定されたもののみ有効
        selection.split(",").each do |label|
          element = find_element(part, label.strip)
          resolve_element_layers(element, ids) if element
        end
      end

      ids
    end

    # elements 配列から、ラベルが一致する要素を探すのだ～🌱
    def find_element(part, label)
      return nil unless label
      part["elements"].find { |e| e.is_a?(Hash) && e["label"] == label }
    end

    # elements 配列から "default": true の要素を探すのだ～🌱
    # 見つからないときは、先頭のハッシュ要素を返すのだ～🌱
    def default_element(part)
      part["elements"].find { |e| e.is_a?(Hash) && e["default"] } ||
        part["elements"].find { |e| e.is_a?(Hash) }
    end

    # element の layers パスを解決して、ID リストへ足すのだ～🌱
    def resolve_element_layers(element, ids)
      element["layers"].each do |path|
        id = resolve_layer_path(path)
        ids << id if id
      end
    end

    # レイヤーパス（例: "!口/*ほあー"）を layers.json のツリーから辿って、
    # 対応するリーフの ID を返すのだ～🌱
    # 見つからないときは nil なのだ～🌱
    def resolve_layer_path(path)
      segments = path.split("/")
      current_children = @layers_tree
      segments.each_with_index do |segment, i|
        node = current_children.find { |n| n["name"] == segment }
        return nil unless node
        if i == segments.length - 1
          return node["id"]
        end
        return nil unless node["type"] == "group"
        current_children = node["children"]
      end
      nil
    end
  end
end

# _data/voicevox.yml に定義されたキャラクターを登録するのだ～🌱
# _plugins/say/ の 2 階層上が Jekyll ソースルート
source_root = File.expand_path("../..", __dir__)
voicevox_yml = File.join(source_root, "_data/voicevox.yml")
voicevox_base = File.join(source_root, "assets/images/voicevox/extracted")

raise "VOICEVOX: _data/voicevox.yml not found" unless File.exist?(voicevox_yml)

YAML.safe_load(File.read(voicevox_yml))&.each do |slug, config|
  data_dir = File.join(voicevox_base, slug)
  raise "VOICEVOX: extracted directory not found: #{slug}" unless File.directory?(data_dir)
  raise "VOICEVOX: presets.json not found: #{slug}" unless File.exist?(File.join(data_dir, "presets.json"))
  color = config&.dig("color")
  raise "VOICEVOX: color not defined for #{slug}" unless color
  Say.register_character(slug, Say::VoicevoxProvider.new(slug, data_dir, color))
end
