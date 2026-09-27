#!/usr/bin/env bash
#
# json2pdn.sh - scripts/pdn2json.sh が書いた JSON を、Paint.NET の .pdn ファイルへ戻すのだ～🌱
#
# 使い方:
#   scripts/json2pdn.sh [ファイル]
#   scripts/json2pdn.sh -h
#   scripts/json2pdn.sh --help
#
#   第 1 引数があれば、それを JSON ファイルとして読むのだ～🌱
#   引数を省くか、`-` を渡すと、標準入力から読むのだ～🌱
#   .pdn は、常に標準出力へ書くのだ～🌱
#
# 終了コード:
#   0 は、変換できたときなのだ～🌱
#   1 は、読むファイルが無いときと、JSON がこのスクリプトの検証に通らなかったときなのだ～🌱
#   このときは、理由を標準エラー出力へ書くのだ～🌱
#   それ以外の失敗では、Python のトレースバックがそのまま出て、終了コードも 1 になるのだ～🌱
#   2 は、引数が 2 個以上あるときと、-h と --help のときで、使い方を標準エラー出力へ書くのだ～🌱
#
# 前提:
#   python3 が必要なのだ～🌱
#   標準ライブラリだけで動くのだ～🌱
#
# JSON の形と、.pdn の構造は、scripts/pdn2json.sh の冒頭に書いてあるのだ～🌱
#
# JSON を書き換えてから戻すときは、中身の整合を取るのは書き換えた側の役目なのだ～🌱
# このスクリプトが検証するのは、主に次のものなのだ～🌱
#   - JSON の一番外側と、その直下の値の型と、format の値なのだ～🌱
#   - separator の長さと、records の最後のレコードなのだ～🌱
#   - クラスのレコードのメンバーと、その型と値の対応なのだ～🌱
#   - MemoryBlock のレコードと、画素のセクションの対応なのだ～🌱
#   - 書き込む数値が、真偽値ではなく、それぞれのバイト数に収まることなのだ～🌱
#
# この説明は、次のスキルに従って書いてあるのだ～🌱
#   https://github.com/MirrgieRiana/MirrgieRiana.github.io/blob/main/.claude/skills/markdown-max-line-length/SKILL.md
# 書き換えるときも、そのスキルを厳守するのだ～🌱

set -eu

program=$(cat <<'EOF'
import base64
import json
import math
import os
import re
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
        raise PdnError(f"unsupported {what} {key!r}")
    return table[key]


class Encoder:
    def __init__(self):
        self.output = bytearray()
        self.classes = {}
        self.memory_blocks = []

    def pack(self, struct_format, value):
        # 真偽値は Python では整数としても書けてしまうから、先に弾くのだ～🌱
        if isinstance(value, bool):
            raise PdnError(f"expected a number, but got {value!r}")
        try:
            self.output += struct.pack(struct_format, value)
        except struct.error as e:
            raise PdnError(f"cannot write value {value!r}: {e}")

    def byte(self, value):
        self.pack("<B", value)

    def int32(self, value):
        self.pack("<i", value)

    def length_prefixed_string(self, value):
        if not isinstance(value, str):
            raise PdnError(f"expected a string, but got {value!r}")
        data = value.encode("utf-8")
        # 長さは、下位から 7 ビットずつ書いて、続きがあるバイトは最上位のビットを立てるのだ～🌱
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
                raise PdnError(f"expected a Boolean value, but got {value!r}")
            self.byte(1 if value else 0)
        elif primitive_type == "Char":
            if not isinstance(value, str) or len(value) != 1:
                raise PdnError(f"expected a string of a single Char, but got {value!r}")
            self.output += value.encode("utf-8")
        elif primitive_type in ("Decimal", "String"):
            self.length_prefixed_string(value)
        elif primitive_type == "Null":
            if value is not None:
                raise PdnError(f"expected null for Null, but got {value!r}")
        elif primitive_type in STRING_INTEGER_TYPES:
            if not isinstance(value, str):
                raise PdnError(f"expected a decimal string for {primitive_type}, but got {value!r}")
            self.pack(FIXED_FORMATS[primitive_type], int(value))
        elif primitive_type in FLOAT_BITS_FORMATS and isinstance(value, str):
            if not re.fullmatch(r"0x[0-9a-fA-F]+", value):
                raise PdnError(f"expected a number or a 0x-prefixed hexadecimal string for {primitive_type}, but got {value!r}")
            bits = int(value, 16)
            size = struct.calcsize(FIXED_FORMATS[primitive_type])
            if bits >= 1 << size * 8 or math.isfinite(struct.unpack(FIXED_FORMATS[primitive_type], bits.to_bytes(size, "little"))[0]):
                raise PdnError(f"expected the bit pattern of a non-finite {primitive_type}, but got {value!r}")
            self.pack(FLOAT_BITS_FORMATS[primitive_type], bits)
        elif primitive_type in FIXED_FORMATS:
            if isinstance(value, bool) or not isinstance(value, (int, float)):
                raise PdnError(f"expected a number for {primitive_type}, but got {value!r}")
            self.pack(FIXED_FORMATS[primitive_type], value)
        else:
            raise PdnError(f"unsupported primitive type {primitive_type!r}")

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
            self.byte(lookup(PRIMITIVE_TYPE_CODES, member_type["primitiveType"], "primitive type"))
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
            raise PdnError(f"class {class_name} has no members named {', '.join(sorted(unknown))}")
        skip = 0
        for name, member_type in zip(names, types):
            if skip > 0:
                if name in values:
                    raise PdnError(f"member {name} of class {class_name} must be absent because the preceding ObjectNullMultiple covers it")
                skip -= 1
                continue
            if name not in values:
                raise PdnError(f"member {name} of class {class_name} is missing")
            value = values[name]
            if member_type["binaryType"] == "Primitive":
                self.primitive(member_type["primitiveType"], value)
                continue
            if not isinstance(value, dict):
                raise PdnError(f"expected a record for member {name} of class {class_name}, but got {value!r}")
            self.record_with_libraries(value)
            if value["$record"] in NULL_MULTIPLE_RECORDS:
                skip = value["count"] - 1
        if skip > 0:
            raise PdnError(f"ObjectNullMultiple record in class {class_name} covers {skip} more members than remain")

    def items(self, length, items):
        covered = 0
        for item in items:
            self.record_with_libraries(item)
            covered += item["count"] if item["$record"] in NULL_MULTIPLE_RECORDS else 1
        if covered != length:
            raise PdnError(f"array items cover {covered} elements, which does not match the length {length}")

    def object_members(self, object_id, class_name, names, types, values):
        self.members(class_name, names, types, values)
        if class_name == MEMORY_BLOCK_CLASS and values.get("deferred") is True:
            self.memory_blocks.append((object_id, int(values["length64"])))

    def record(self, record):
        record_type = record["$record"]
        self.byte(lookup(RECORD_CODES, record_type, "record type"))
        if record_type == "SerializedStreamHeader":
            self.int32(record["rootId"])
            self.int32(record["headerId"])
            self.int32(record["majorVersion"])
            self.int32(record["minorVersion"])
        elif record_type == "ClassWithId":
            self.int32(record["objectId"])
            self.int32(record["metadataId"])
            if record["metadataId"] not in self.classes:
                raise PdnError(f"ClassWithId record refers to record {record['metadataId']}, which has not appeared yet")
            class_name, names, types = self.classes[record["metadataId"]]
            self.object_members(record["objectId"], class_name, names, types, record["members"])
        elif record_type in ("SystemClassWithMembersAndTypes", "ClassWithMembersAndTypes"):
            entries = record["memberTypes"]
            names = [entry["key"] for entry in entries]
            types = [entry["type"] for entry in entries]
            if len(set(names)) != len(names):
                raise PdnError(f"memberTypes of record {record['objectId']} has duplicate member names")
            self.int32(record["objectId"])
            self.length_prefixed_string(record["className"])
            self.int32(len(names))
            for name in names:
                self.length_prefixed_string(name)
            for member_type in types:
                self.byte(lookup(BINARY_TYPE_CODES, member_type["binaryType"], "binary type"))
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
            self.byte(lookup(PRIMITIVE_TYPE_CODES, record["primitiveType"], "primitive type"))
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
                raise PdnError(f"{record_type} record must cover at least 1 member, but got {record['count']}")
            if record_type == "ObjectNullMultiple256":
                self.byte(record["count"])
            else:
                self.int32(record["count"])
        elif record_type == "ArraySinglePrimitive":
            self.int32(record["objectId"])
            length_position = len(self.output)
            self.int32(0)
            self.byte(lookup(PRIMITIVE_TYPE_CODES, record["primitiveType"], "primitive type"))
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
        self.byte(lookup(BINARY_ARRAY_TYPE_CODES, array_type, "binary array type"))
        self.int32(len(lengths))
        for length in lengths:
            self.int32(length)
        if array_type.endswith("Offset"):
            lower_bounds = record["lowerBounds"]
            if len(lower_bounds) != len(lengths):
                raise PdnError(f"number of lowerBounds of array {record['objectId']} does not match its rank")
            for lower_bound in lower_bounds:
                self.int32(lower_bound)
        item_type = record["itemType"]
        self.byte(lookup(BINARY_TYPE_CODES, item_type["binaryType"], "binary type"))
        self.additional_info(item_type)
        length = math.prod(lengths)
        if item_type["binaryType"] == "Primitive":
            if self.primitive_array(item_type["primitiveType"], record) != length:
                raise PdnError(f"number of values of array {record['objectId']} does not match the length {length}")
        else:
            self.items(length, record["items"])


def check_type(document, key, value_type, what):
    if not isinstance(document.get(key), value_type):
        raise PdnError(f"{key} must be {what}")


def encode(document):
    if not isinstance(document, dict):
        raise PdnError("the top level of the JSON must be an object")
    if document.get("format") != "pdn2json/1":
        raise PdnError("not a JSON written by pdn2json.sh: format must be pdn2json/1")
    check_type(document, "pdnHeader", str, "a string")
    check_type(document, "separator", str, "a string")
    check_type(document, "records", list, "an array")
    check_type(document, "memoryBlocks", list, "an array")
    if len(document["separator"]) != 4:
        raise PdnError("separator must be a hexadecimal string of 2 bytes")
    if not all(isinstance(record, dict) for record in document["records"]):
        raise PdnError("records must contain only objects")
    if not all(isinstance(memory_block, dict) for memory_block in document["memoryBlocks"]):
        raise PdnError("memoryBlocks must contain only objects")
    output = bytearray(b"PDN3")
    header = document["pdnHeader"].encode("utf-8")
    if len(header) >= 1 << 24:
        raise PdnError("pdnHeader is too long for a 3-byte length")
    output += len(header).to_bytes(3, "little")
    output += header
    output += bytes.fromhex(document["separator"])

    encoder = Encoder()
    records = document["records"]
    if not records or records[-1].get("$record") != "MessageEnd":
        raise PdnError("the last record must be MessageEnd")
    for record in records:
        encoder.record_with_libraries(record)
    output += encoder.output

    memory_blocks = document["memoryBlocks"]
    expected_ids = [object_id for object_id, _ in encoder.memory_blocks]
    actual_ids = [memory_block["objectId"] for memory_block in memory_blocks]
    if actual_ids != expected_ids:
        raise PdnError(f"objectId order of memoryBlocks {actual_ids} does not match the order of MemoryBlock records {expected_ids}")
    sections = Encoder()
    for memory_block, (object_id, length) in zip(memory_blocks, encoder.memory_blocks):
        chunk_size = memory_block["chunkSize"]
        chunks = memory_block["chunks"]
        if length < 0:
            raise PdnError(f"length64 of MemoryBlock {object_id} is negative: {length}")
        if chunk_size <= 0 or len(chunks) != -(-length // chunk_size):
            raise PdnError(f"number of chunks of MemoryBlock {object_id} does not match the number determined by length64 and chunkSize")
        sections.pack(">B", memory_block["formatVersion"])
        sections.pack(">I", chunk_size)
        for chunk in chunks:
            data = base64.b64decode(chunk["data"], validate=True)
            sections.pack(">I", chunk["number"])
            sections.pack(">I", len(data))
            sections.output += data
    output += sections.output

    if "trailing" in document:
        output += base64.b64decode(document["trailing"], validate=True)
    return bytes(output)


def check_file(path):
    if path == "-":
        return
    if not os.path.exists(path):
        raise PdnError(f"file not found: {path}")
    if not os.path.isfile(path):
        raise PdnError(f"not a regular file: {path}")


def main(script_name, args):
    if len(args) > 1 or (args and args[0] in ("-h", "--help")):
        print(f"usage: {script_name} [FILE]", file=sys.stderr)
        return 2
    path = args[0] if args else "-"
    try:
        check_file(path)
        if path == "-":
            document = json.load(sys.stdin)
        else:
            with open(path, encoding="utf-8") as file:
                document = json.load(file)
        data = encode(document)
    except PdnError as e:
        print(f"{script_name}: {e}", file=sys.stderr)
        return 1
    sys.stdout.buffer.write(data)
    return 0


sys.exit(main(sys.argv[1], sys.argv[2:]))
EOF
)

exec python3 -c "$program" "$0" "$@"
