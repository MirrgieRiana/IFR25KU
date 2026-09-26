#!/usr/bin/env bash
#
# json2pdn.sh - scripts/pdn2json.sh が書いた JSON を、Paint.NET の .pdn ファイルへ戻すのだ～🌱
#
# 使い方:
#   scripts/json2pdn.sh [ファイル]
#
#   第 1 引数があれば、それを JSON ファイルとして読むのだ～🌱
#   引数を省くか、`-` を渡すと、標準入力から読むのだ～🌱
#   .pdn は、常に標準出力へ書くのだ～🌱
#
# 前提:
#   python3 が必要なのだ～🌱 標準ライブラリだけで動くのだ～🌱
#
# JSON の形と、.pdn の並びは、scripts/pdn2json.sh の冒頭に書いてあるのだ～🌱
#
# JSON を書き換えてから戻すときは、中身の整合を取るのは書き換えた側の役目なのだ～🌱
# このスクリプトは、JSON の形と、MemoryBlock の記録と画素の区画の対応だけを確かめるのだ～🌱

set -eu

program=$(cat <<'EOF'
import base64
import json
import math
import struct
import sys

RECORD_CODES = {
    "SerializedStreamHeader": 0,
    "ClassWithId": 1,
    "SystemClassWithMembersAndTypes": 4,
    "ClassWithMembersAndTypes": 5,
    "BinaryObjectString": 6,
    "BinaryArray": 7,
    "MemberPrimitiveTyped": 8,
    "MemberReference": 9,
    "ObjectNull": 10,
    "MessageEnd": 11,
    "BinaryLibrary": 12,
    "ObjectNullMultiple256": 13,
    "ObjectNullMultiple": 14,
    "ArraySinglePrimitive": 15,
    "ArraySingleObject": 16,
    "ArraySingleString": 17,
}
BINARY_TYPE_CODES = {name: code for code, name in enumerate(["Primitive", "String", "Object", "SystemClass", "Class", "ObjectArray", "StringArray", "PrimitiveArray"])}
PRIMITIVE_TYPE_CODES = {
    "Boolean": 1,
    "Byte": 2,
    "Char": 3,
    "Decimal": 5,
    "Double": 6,
    "Int16": 7,
    "Int32": 8,
    "Int64": 9,
    "SByte": 10,
    "Single": 11,
    "TimeSpan": 12,
    "DateTime": 13,
    "UInt16": 14,
    "UInt32": 15,
    "UInt64": 16,
    "Null": 17,
    "String": 18,
}
BINARY_ARRAY_TYPE_CODES = {name: code for code, name in enumerate(["Single", "Jagged", "Rectangular", "SingleOffset", "JaggedOffset", "RectangularOffset"])}
FIXED_FORMATS = {
    "Byte": "<B",
    "SByte": "<b",
    "Int16": "<h",
    "UInt16": "<H",
    "Int32": "<i",
    "UInt32": "<I",
    "Int64": "<q",
    "UInt64": "<Q",
    "Single": "<f",
    "Double": "<d",
    "TimeSpan": "<q",
    "DateTime": "<Q",
}
STRING_INTEGER_TYPES = {"Int64", "UInt64", "TimeSpan", "DateTime"}
FLOAT_BITS_FORMATS = {"Single": "<I", "Double": "<Q"}
NULL_MULTIPLE_RECORDS = {"ObjectNullMultiple256", "ObjectNullMultiple"}
MEMORY_BLOCK_CLASS = "PaintDotNet.MemoryBlock"


class PdnError(Exception):
    pass


def lookup(table, key, what):
    if key not in table:
        raise PdnError(f"未知の{what} {key!r} なのだ")
    return table[key]


class Encoder:
    def __init__(self):
        self.output = bytearray()
        self.classes = {}
        self.memory_blocks = []

    def pack(self, struct_format, value):
        try:
            self.output += struct.pack(struct_format, value)
        except struct.error as e:
            raise PdnError(f"値 {value!r} を書けないのだ: {e}")

    def byte(self, value):
        self.pack("<B", value)

    def int32(self, value):
        self.pack("<i", value)

    def length_prefixed_string(self, value):
        if not isinstance(value, str):
            raise PdnError(f"文字列のはずの値が {value!r} なのだ")
        data = value.encode("utf-8")
        length = len(data)
        while True:
            if length < 0x80:
                self.byte(length)
                break
            self.byte(length & 0x7F | 0x80)
            length >>= 7
        self.output += data

    def primitive(self, primitive_type, value):
        if primitive_type == "Boolean":
            if not isinstance(value, bool):
                raise PdnError(f"Boolean のはずの値が {value!r} なのだ")
            self.byte(1 if value else 0)
        elif primitive_type == "Char":
            if not isinstance(value, str) or len(value) != 1:
                raise PdnError(f"Char のはずの値が {value!r} なのだ")
            self.output += value.encode("utf-8")
        elif primitive_type in ("Decimal", "String"):
            self.length_prefixed_string(value)
        elif primitive_type == "Null":
            if value is not None:
                raise PdnError(f"Null のはずの値が {value!r} なのだ")
        elif primitive_type in STRING_INTEGER_TYPES:
            if not isinstance(value, str):
                raise PdnError(f"{primitive_type} は 10 進数の文字列のはずなのに {value!r} なのだ")
            self.pack(FIXED_FORMATS[primitive_type], int(value))
        elif primitive_type in FLOAT_BITS_FORMATS and isinstance(value, str):
            self.pack(FLOAT_BITS_FORMATS[primitive_type], int(value, 16))
        elif primitive_type in FIXED_FORMATS:
            if isinstance(value, bool) or not isinstance(value, (int, float)):
                raise PdnError(f"{primitive_type} のはずの値が {value!r} なのだ")
            self.pack(FIXED_FORMATS[primitive_type], value)
        else:
            raise PdnError(f"未知のプリミティブ型 {primitive_type!r} なのだ")

    def primitive_array(self, primitive_type, record):
        if primitive_type == "Byte":
            data = base64.b64decode(record["base64"], validate=True)
            self.output += data
            return len(data)
        values = record["values"]
        for value in values:
            self.primitive(primitive_type, value)
        return len(values)

    def additional_info(self, member_type):
        binary_type = member_type["binaryType"]
        if binary_type in ("Primitive", "PrimitiveArray"):
            self.byte(lookup(PRIMITIVE_TYPE_CODES, member_type["primitiveType"], "プリミティブ型"))
        elif binary_type == "SystemClass":
            self.length_prefixed_string(member_type["className"])
        elif binary_type == "Class":
            self.length_prefixed_string(member_type["className"])
            self.int32(member_type["libraryId"])

    def record_with_libraries(self, record):
        for library in record.get("$libraries", []):
            self.byte(RECORD_CODES["BinaryLibrary"])
            self.int32(library["libraryId"])
            self.length_prefixed_string(library["libraryName"])
        self.record(record)

    def members(self, class_name, names, types, values):
        unknown = set(values) - set(names)
        if unknown:
            raise PdnError(f"クラス {class_name} に無いメンバー {sorted(unknown)} があるのだ")
        skip = 0
        for name, member_type in zip(names, types):
            if skip > 0:
                if name in values:
                    raise PdnError(f"クラス {class_name} のメンバー {name} は、前の ObjectNullMultiple が埋めているはずなのだ")
                skip -= 1
                continue
            if name not in values:
                raise PdnError(f"クラス {class_name} のメンバー {name} が無いのだ")
            value = values[name]
            if member_type["binaryType"] == "Primitive":
                self.primitive(member_type["primitiveType"], value)
                continue
            if not isinstance(value, dict):
                raise PdnError(f"クラス {class_name} のメンバー {name} は、記録のはずなのに {value!r} なのだ")
            self.record_with_libraries(value)
            if value["$record"] in NULL_MULTIPLE_RECORDS:
                skip = value["count"] - 1
        if skip > 0:
            raise PdnError(f"クラス {class_name} の ObjectNullMultiple の記録が、メンバーの数を {skip} 個超えているのだ")

    def items(self, length, items):
        covered = 0
        for item in items:
            self.record_with_libraries(item)
            covered += item["count"] if item["$record"] in NULL_MULTIPLE_RECORDS else 1
        if covered != length:
            raise PdnError(f"配列の要素が {covered} 個分あって、長さの {length} と合わないのだ")

    def object_members(self, object_id, class_name, names, types, values):
        self.members(class_name, names, types, values)
        if class_name == MEMORY_BLOCK_CLASS and values.get("deferred") is True:
            self.memory_blocks.append((object_id, int(values["length64"])))

    def record(self, record):
        record_type = record["$record"]
        self.byte(lookup(RECORD_CODES, record_type, "記録の種類"))
        if record_type == "SerializedStreamHeader":
            self.int32(record["rootId"])
            self.int32(record["headerId"])
            self.int32(record["majorVersion"])
            self.int32(record["minorVersion"])
        elif record_type == "ClassWithId":
            self.int32(record["objectId"])
            self.int32(record["metadataId"])
            if record["metadataId"] not in self.classes:
                raise PdnError(f"ClassWithId の記録が、まだ現れていない {record['metadataId']} 番の記録を指しているのだ")
            class_name, names, types = self.classes[record["metadataId"]]
            self.object_members(record["objectId"], class_name, names, types, record["members"])
        elif record_type in ("SystemClassWithMembersAndTypes", "ClassWithMembersAndTypes"):
            names = list(record["memberTypes"])
            types = list(record["memberTypes"].values())
            self.int32(record["objectId"])
            self.length_prefixed_string(record["className"])
            self.int32(len(names))
            for name in names:
                self.length_prefixed_string(name)
            for member_type in types:
                self.byte(lookup(BINARY_TYPE_CODES, member_type["binaryType"], "型の種類"))
            for member_type in types:
                self.additional_info(member_type)
            if record_type == "ClassWithMembersAndTypes":
                self.int32(record["libraryId"])
            self.classes[record["objectId"]] = (record["className"], names, types)
            self.object_members(record["objectId"], record["className"], names, types, record["members"])
        elif record_type == "BinaryObjectString":
            self.int32(record["objectId"])
            self.length_prefixed_string(record["value"])
        elif record_type == "MemberPrimitiveTyped":
            self.byte(lookup(PRIMITIVE_TYPE_CODES, record["primitiveType"], "プリミティブ型"))
            self.primitive(record["primitiveType"], record["value"])
        elif record_type == "MemberReference":
            self.int32(record["idRef"])
        elif record_type in ("ObjectNull", "MessageEnd"):
            pass
        elif record_type == "BinaryLibrary":
            self.int32(record["libraryId"])
            self.length_prefixed_string(record["libraryName"])
        elif record_type in NULL_MULTIPLE_RECORDS:
            if record["count"] < 1:
                raise PdnError(f"{record_type} の記録の個数が {record['count']} なのだ")
            if record_type == "ObjectNullMultiple256":
                self.byte(record["count"])
            else:
                self.int32(record["count"])
        elif record_type == "ArraySinglePrimitive":
            self.int32(record["objectId"])
            length_position = len(self.output)
            self.int32(0)
            self.byte(lookup(PRIMITIVE_TYPE_CODES, record["primitiveType"], "プリミティブ型"))
            length = self.primitive_array(record["primitiveType"], record)
            self.output[length_position:length_position + 4] = struct.pack("<i", length)
        elif record_type in ("ArraySingleObject", "ArraySingleString"):
            self.int32(record["objectId"])
            self.int32(record["length"])
            self.items(record["length"], record["items"])
        elif record_type == "BinaryArray":
            self.binary_array(record)

    def binary_array(self, record):
        array_type = record["binaryArrayType"]
        lengths = record["lengths"]
        self.int32(record["objectId"])
        self.byte(lookup(BINARY_ARRAY_TYPE_CODES, array_type, "配列の種類"))
        self.int32(len(lengths))
        for length in lengths:
            self.int32(length)
        if array_type.endswith("Offset"):
            lower_bounds = record["lowerBounds"]
            if len(lower_bounds) != len(lengths):
                raise PdnError(f"{record['objectId']} 番の配列の lowerBounds の数が、次元の数と合わないのだ")
            for lower_bound in lower_bounds:
                self.int32(lower_bound)
        item_type = record["itemType"]
        self.byte(lookup(BINARY_TYPE_CODES, item_type["binaryType"], "型の種類"))
        self.additional_info(item_type)
        length = math.prod(lengths)
        if item_type["binaryType"] == "Primitive":
            if self.primitive_array(item_type["primitiveType"], record) != length:
                raise PdnError(f"{record['objectId']} 番の配列の値の数が、長さの {length} と合わないのだ")
        else:
            self.items(length, record["items"])


def encode(document):
    if document.get("format") != "pdn2json/1":
        raise PdnError("format が pdn2json/1 ではないから、pdn2json.sh の JSON ではないのだ")
    output = bytearray(b"PDN3")
    header = document["pdnHeader"].encode("utf-8")
    if len(header) >= 1 << 24:
        raise PdnError("ヘッダーの XML が、3 バイトで表せる長さを超えているのだ")
    output += len(header).to_bytes(3, "little")
    output += header
    output += bytes.fromhex(document["separator"])

    encoder = Encoder()
    records = document["records"]
    if not records or records[-1]["$record"] != "MessageEnd":
        raise PdnError("records の最後が MessageEnd の記録ではないのだ")
    for record in records:
        encoder.record_with_libraries(record)
    output += encoder.output

    memory_blocks = document["memoryBlocks"]
    expected_ids = [object_id for object_id, _ in encoder.memory_blocks]
    actual_ids = [memory_block["objectId"] for memory_block in memory_blocks]
    if actual_ids != expected_ids:
        raise PdnError(f"memoryBlocks の objectId の並び {actual_ids} が、記録の中の MemoryBlock の並び {expected_ids} と合わないのだ")
    for memory_block, (object_id, length) in zip(memory_blocks, encoder.memory_blocks):
        chunk_size = memory_block["chunkSize"]
        chunks = memory_block["chunks"]
        if chunk_size <= 0 or len(chunks) != -(-length // chunk_size):
            raise PdnError(f"{object_id} 番の MemoryBlock の塊の数が、length64 と chunkSize から決まる数と合わないのだ")
        output += struct.pack(">B", memory_block["formatVersion"])
        output += struct.pack(">I", chunk_size)
        for chunk in chunks:
            data = base64.b64decode(chunk["data"], validate=True)
            output += struct.pack(">I", chunk["number"])
            output += struct.pack(">I", len(data))
            output += data

    if "trailing" in document:
        output += base64.b64decode(document["trailing"], validate=True)
    return bytes(output)


def main(script_name, args):
    if len(args) > 1 or args[:1] in (["-h"], ["--help"]):
        print(f"使い方: {script_name} [ファイル]", file=sys.stderr)
        return 2
    path = args[0] if args else "-"
    try:
        if path == "-":
            document = json.load(sys.stdin)
        else:
            with open(path, encoding="utf-8") as file:
                document = json.load(file)
        data = encode(document)
    except KeyError as e:
        print(f"{script_name}: JSON に {e} が無いのだ", file=sys.stderr)
        return 1
    except (OSError, ValueError, TypeError, PdnError) as e:
        print(f"{script_name}: {e}", file=sys.stderr)
        return 1
    sys.stdout.buffer.write(data)
    return 0


sys.exit(main(sys.argv[1], sys.argv[2:]))
EOF
)

exec python3 -c "$program" "$0" "$@"
