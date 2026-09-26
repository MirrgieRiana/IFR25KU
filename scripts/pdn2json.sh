#!/usr/bin/env bash
#
# pdn2json.sh - Paint.NET の .pdn ファイルを JSON へ変換するのだ～🌱
#
# 使い方:
#   scripts/pdn2json.sh [ファイル]
#
#   第 1 引数があれば、それを .pdn ファイルとして読むのだ～🌱
#   引数を省くか、`-` を渡すと、標準入力から読むのだ～🌱
#   JSON は、常に標準出力へ書くのだ～🌱
#   scripts/json2pdn.sh で、1 バイトも違わない .pdn へ戻せるのだ～🌱
#
# 前提:
#   python3 が必要なのだ～🌱
#   標準ライブラリだけで動くのだ～🌱
#
# .pdn の構造:
#   1. マジックナンバー `PDN3` の 4 バイトなのだ～🌱
#   2. ヘッダーの長さの 3 バイトで、リトルエンディアンなのだ～🌱
#   3. ヘッダーの XML なのだ～🌱
#      画像の大きさとレイヤーの枚数とサムネイルの PNG の Base64 を持つのだ～🌱
#   4. 区切りの 2 バイトで、今までに見たものは全部 `00 01` なのだ～🌱
#   5. .NET の BinaryFormatter のシリアル化データで、形式は Microsoft の [MS-NRBF] なのだ～🌱
#      https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-nrbf/75b9fe09-be15-475f-85b8-ae7b7558cfe5
#   6. 画素のデータで、5 の中の、deferred が真の PaintDotNet.MemoryBlock ごとに、出現の順に 1 セクションずつ並ぶのだ～🌱
#      セクションの頭は、形式の 1 バイトと、チャンクの大きさの 4 バイトなのだ～🌱
#      その後に、チャンクの番号の 4 バイトと、データの長さの 4 バイトと、データの組が続くのだ～🌱
#      セクションの中の数値は、ビッグエンディアンなのだ～🌱
#      チャンクの個数は、MemoryBlock の length64 をチャンクの大きさで割って、切り上げたものなのだ～🌱
#
# JSON の形:
#   {
#     "format": "pdn2json/1",
#     "pdnHeader": ヘッダーの XML の文字列,
#     "separator": 区切りの 2 バイトの 16 進数の文字列,
#     "records": シリアル化データのレコードの配列,
#     "memoryBlocks": [
#       {
#         "objectId": 対応する PaintDotNet.MemoryBlock のレコードの objectId,
#         "formatVersion": 形式の 1 バイト,
#         "chunkSize": チャンクの大きさ,
#         "chunks": [ { "number": チャンクの番号, "data": データの Base64 }, ... ]
#       },
#       ...
#     ],
#     "trailing": 画素のデータの後ろに残ったバイト列の Base64 で、残っていたときだけ現れるのだ～🌱
#   }
#
#   データは、展開も圧縮し直しもせずに、ファイルの中のバイト列のまま Base64 にするのだ～🌱
#   シリアル化データのレコードは、"$record" に [MS-NRBF] のレコードの名前を持つオブジェクトなのだ～🌱
#   クラスのレコードは、"memberTypes" にメンバーの名前ごとの型を、"members" にメンバーの名前ごとの値を持つのだ～🌱
#   ClassWithId のレコードは、型を持たずに、"metadataId" が指すレコードの型を使うのだ～🌱
#   プリミティブの値は、JSON の値でそのまま表すのだ～🌱
#   ただし、64 ビットの整数と DateTime と TimeSpan は、10 進数の文字列にするのだ～🌱
#   Single と Double のうち、有限でない値は、ビット列の 16 進数の文字列にするのだ～🌱
#   Byte の配列は、値の配列の代わりに "base64" を持つのだ～🌱
#   値の前に置かれた BinaryLibrary のレコードは、その値のレコードの "$libraries" へ入れるのだ～🌱
#   ObjectNullMultiple のレコードが複数のメンバーを埋めるときは、最初のメンバーだけがそのレコードを持つのだ～🌱
#   残りのメンバーは、"members" に現れないのだ～🌱
#   型を持たないクラスのレコードと、MethodCall と MethodReturn のレコードには、対応していないのだ～🌱
#
# この説明は、次のスキルに従って書いてあるのだ～🌱
#   https://github.com/MirrgieRiana/MirrgieRiana.github.io/blob/main/.claude/skills/markdown-max-line-length/SKILL.md
# 書き換えるときも、そのスキルを厳守するのだ～🌱

set -eu

program=$(cat <<'EOF'
import base64
import json
import math
import struct
import sys

RECORD_TYPES = {
    0: "SerializedStreamHeader",
    1: "ClassWithId",
    2: "SystemClassWithMembers",
    3: "ClassWithMembers",
    4: "SystemClassWithMembersAndTypes",
    5: "ClassWithMembersAndTypes",
    6: "BinaryObjectString",
    7: "BinaryArray",
    8: "MemberPrimitiveTyped",
    9: "MemberReference",
    10: "ObjectNull",
    11: "MessageEnd",
    12: "BinaryLibrary",
    13: "ObjectNullMultiple256",
    14: "ObjectNullMultiple",
    15: "ArraySinglePrimitive",
    16: "ArraySingleObject",
    17: "ArraySingleString",
    21: "MethodCall",
    22: "MethodReturn",
}
BINARY_TYPES = ["Primitive", "String", "Object", "SystemClass", "Class", "ObjectArray", "StringArray", "PrimitiveArray"]
PRIMITIVE_TYPES = {
    1: "Boolean",
    2: "Byte",
    3: "Char",
    5: "Decimal",
    6: "Double",
    7: "Int16",
    8: "Int32",
    9: "Int64",
    10: "SByte",
    11: "Single",
    12: "TimeSpan",
    13: "DateTime",
    14: "UInt16",
    15: "UInt32",
    16: "UInt64",
    17: "Null",
    18: "String",
}
BINARY_ARRAY_TYPES = ["Single", "Jagged", "Rectangular", "SingleOffset", "JaggedOffset", "RectangularOffset"]
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
FLOAT_BITS_FORMATS = {"Single": ("<I", 8), "Double": ("<Q", 16)}
NULL_MULTIPLE_RECORDS = {"ObjectNullMultiple256", "ObjectNullMultiple"}
MEMORY_BLOCK_CLASS = "PaintDotNet.MemoryBlock"


class PdnError(Exception):
    pass


class Input:
    def __init__(self, data):
        self.data = data
        self.position = 0

    def take(self, size):
        if self.position + size > len(self.data):
            raise PdnError(f"{self.position} バイト目から {size} バイトを読もうとしたところで、データが終わっているのだ")
        chunk = self.data[self.position:self.position + size]
        self.position += size
        return chunk

    def unpack(self, struct_format):
        return struct.unpack(struct_format, self.take(struct.calcsize(struct_format)))[0]

    def byte(self):
        return self.unpack("<B")

    def int32(self):
        return self.unpack("<i")

    def length_prefixed_string(self):
        # 長さは、下位から 7 ビットずつ、最大 5 バイトで表されるのだ～🌱
        length = 0
        for shift in range(0, 35, 7):
            byte = self.byte()
            length |= (byte & 0x7F) << shift
            if byte & 0x80 == 0:
                break
        else:
            raise PdnError(f"{self.position} バイト目の文字列の長さが 5 バイトを超えているのだ")
        return self.take(length).decode("utf-8")


class Decoder:
    def __init__(self, source):
        self.source = source
        self.classes = {}
        self.memory_blocks = []

    def primitive(self, primitive_type):
        if primitive_type == "Boolean":
            byte = self.source.byte()
            if byte not in (0, 1):
                raise PdnError(f"Boolean の値が {byte} なのだ")
            return byte == 1
        if primitive_type == "Char":
            # Char は UTF-8 の 1 文字で、先頭のバイトから長さが決まるのだ～🌱
            lead = self.source.take(1)
            size = 1 if lead[0] < 0x80 else 2 if lead[0] < 0xE0 else 3 if lead[0] < 0xF0 else 4
            return (lead + self.source.take(size - 1)).decode("utf-8")
        if primitive_type in ("Decimal", "String"):
            return self.source.length_prefixed_string()
        if primitive_type == "Null":
            return None
        raw = self.source.take(struct.calcsize(FIXED_FORMATS[primitive_type]))
        value = struct.unpack(FIXED_FORMATS[primitive_type], raw)[0]
        if primitive_type in STRING_INTEGER_TYPES:
            return str(value)
        if primitive_type in FLOAT_BITS_FORMATS and not math.isfinite(value):
            bits_format, digits = FLOAT_BITS_FORMATS[primitive_type]
            return f"0x{struct.unpack(bits_format, raw)[0]:0{digits}x}"
        return value

    def primitive_type(self):
        code = self.source.byte()
        if code not in PRIMITIVE_TYPES:
            raise PdnError(f"未知のプリミティブ型 {code} なのだ")
        return PRIMITIVE_TYPES[code]

    def primitive_array(self, primitive_type, length):
        if primitive_type == "Byte":
            return {"base64": base64.b64encode(self.source.take(length)).decode("ascii")}
        return {"values": [self.primitive(primitive_type) for _ in range(length)]}

    def member_types(self, count):
        codes = [self.source.byte() for _ in range(count)]
        types = []
        for code in codes:
            if code >= len(BINARY_TYPES):
                raise PdnError(f"未知の型の種類 {code} なのだ")
            types.append(self.additional_info(BINARY_TYPES[code]))
        return types

    def additional_info(self, binary_type):
        member_type = {"binaryType": binary_type}
        if binary_type in ("Primitive", "PrimitiveArray"):
            member_type["primitiveType"] = self.primitive_type()
        elif binary_type == "SystemClass":
            member_type["className"] = self.source.length_prefixed_string()
        elif binary_type == "Class":
            member_type["className"] = self.source.length_prefixed_string()
            member_type["libraryId"] = self.source.int32()
        return member_type

    def record_with_libraries(self):
        libraries = []
        while True:
            record = self.record()
            if record["$record"] != "BinaryLibrary":
                break
            libraries.append({"libraryId": record["libraryId"], "libraryName": record["libraryName"]})
        if libraries:
            record = {"$record": record["$record"], "$libraries": libraries, **record}
        return record

    def members(self, names, types):
        values = {}
        skip = 0
        for name, member_type in zip(names, types):
            if skip > 0:
                skip -= 1
                continue
            if member_type["binaryType"] == "Primitive":
                values[name] = self.primitive(member_type["primitiveType"])
                continue
            record = self.record_with_libraries()
            values[name] = record
            if record["$record"] in NULL_MULTIPLE_RECORDS:
                skip = record["count"] - 1
        if skip > 0:
            raise PdnError(f"ObjectNullMultiple のレコードが、メンバーの数を {skip} 個超えているのだ")
        return values

    def items(self, length):
        items = []
        covered = 0
        while covered < length:
            record = self.record_with_libraries()
            items.append(record)
            covered += record["count"] if record["$record"] in NULL_MULTIPLE_RECORDS else 1
        if covered != length:
            raise PdnError(f"配列の要素が、長さの {length} を {covered - length} 個超えているのだ")
        return items

    def class_record(self, record_type, has_library):
        object_id = self.source.int32()
        class_name = self.source.length_prefixed_string()
        count = self.source.int32()
        if count < 0:
            raise PdnError(f"クラス {class_name} のメンバーの数が {count} なのだ")
        names = [self.source.length_prefixed_string() for _ in range(count)]
        if len(set(names)) != len(names):
            raise PdnError(f"クラス {class_name} に、同じ名前のメンバーがあるのだ")
        types = self.member_types(count)
        record = {"$record": record_type, "objectId": object_id, "className": class_name}
        if has_library:
            record["libraryId"] = self.source.int32()
        self.classes[object_id] = (class_name, names, types)
        record["memberTypes"] = dict(zip(names, types))
        record["members"] = self.object_members(object_id, class_name, names, types)
        return record

    def object_members(self, object_id, class_name, names, types):
        members = self.members(names, types)
        if class_name == MEMORY_BLOCK_CLASS and members.get("deferred") is True:
            self.memory_blocks.append((object_id, members))
        return members

    def record(self):
        code = self.source.byte()
        if code not in RECORD_TYPES:
            raise PdnError(f"{self.source.position - 1} バイト目に、未知のレコードの種類 {code} があるのだ")
        record_type = RECORD_TYPES[code]
        if record_type == "SerializedStreamHeader":
            return {
                "$record": record_type,
                "rootId": self.source.int32(),
                "headerId": self.source.int32(),
                "majorVersion": self.source.int32(),
                "minorVersion": self.source.int32(),
            }
        if record_type == "ClassWithId":
            object_id = self.source.int32()
            metadata_id = self.source.int32()
            if metadata_id not in self.classes:
                raise PdnError(f"ClassWithId のレコードが、まだ現れていない {metadata_id} 番のレコードを指しているのだ")
            class_name, names, types = self.classes[metadata_id]
            members = self.object_members(object_id, class_name, names, types)
            return {"$record": record_type, "objectId": object_id, "metadataId": metadata_id, "members": members}
        if record_type == "SystemClassWithMembersAndTypes":
            return self.class_record(record_type, False)
        if record_type == "ClassWithMembersAndTypes":
            return self.class_record(record_type, True)
        if record_type == "BinaryObjectString":
            return {"$record": record_type, "objectId": self.source.int32(), "value": self.source.length_prefixed_string()}
        if record_type == "MemberPrimitiveTyped":
            primitive_type = self.primitive_type()
            return {"$record": record_type, "primitiveType": primitive_type, "value": self.primitive(primitive_type)}
        if record_type == "MemberReference":
            return {"$record": record_type, "idRef": self.source.int32()}
        if record_type in ("ObjectNull", "MessageEnd"):
            return {"$record": record_type}
        if record_type == "BinaryLibrary":
            return {"$record": record_type, "libraryId": self.source.int32(), "libraryName": self.source.length_prefixed_string()}
        if record_type in NULL_MULTIPLE_RECORDS:
            count = self.source.byte() if record_type == "ObjectNullMultiple256" else self.source.int32()
            if count < 1:
                raise PdnError(f"{record_type} のレコードが埋める個数が {count} なのだ")
            return {"$record": record_type, "count": count}
        if record_type == "ArraySinglePrimitive":
            object_id = self.source.int32()
            length = self.source.int32()
            primitive_type = self.primitive_type()
            return {"$record": record_type, "objectId": object_id, "primitiveType": primitive_type, **self.primitive_array(primitive_type, length)}
        if record_type in ("ArraySingleObject", "ArraySingleString"):
            object_id = self.source.int32()
            length = self.source.int32()
            return {"$record": record_type, "objectId": object_id, "length": length, "items": self.items(length)}
        if record_type == "BinaryArray":
            return self.binary_array()
        raise PdnError(f"レコードの種類 {record_type} には、対応していないのだ")

    def binary_array(self):
        object_id = self.source.int32()
        code = self.source.byte()
        if code >= len(BINARY_ARRAY_TYPES):
            raise PdnError(f"未知の配列の種類 {code} なのだ")
        array_type = BINARY_ARRAY_TYPES[code]
        rank = self.source.int32()
        lengths = [self.source.int32() for _ in range(rank)]
        record = {"$record": "BinaryArray", "objectId": object_id, "binaryArrayType": array_type, "lengths": lengths}
        if array_type.endswith("Offset"):
            record["lowerBounds"] = [self.source.int32() for _ in range(rank)]
        code = self.source.byte()
        if code >= len(BINARY_TYPES):
            raise PdnError(f"未知の型の種類 {code} なのだ")
        item_type = self.additional_info(BINARY_TYPES[code])
        record["itemType"] = item_type
        length = math.prod(lengths)
        if item_type["binaryType"] == "Primitive":
            record.update(self.primitive_array(item_type["primitiveType"], length))
        else:
            record["items"] = self.items(length)
        return record


def decode(data):
    source = Input(data)
    if source.take(4) != b"PDN3":
        raise PdnError("先頭が PDN3 ではないから、.pdn ファイルではないのだ")
    header_size = int.from_bytes(source.take(3), "little")
    document = {"format": "pdn2json/1", "pdnHeader": source.take(header_size).decode("utf-8"), "separator": source.take(2).hex()}

    decoder = Decoder(source)
    records = []
    while True:
        record = decoder.record_with_libraries()
        records.append(record)
        if record["$record"] == "MessageEnd":
            break
    document["records"] = records

    memory_blocks = []
    for object_id, members in decoder.memory_blocks:
        length = int(members["length64"])
        format_version = source.unpack(">B")
        chunk_size = source.unpack(">I")
        if chunk_size == 0:
            raise PdnError(f"{object_id} 番の MemoryBlock のチャンクの大きさが 0 なのだ")
        chunks = []
        for _ in range(-(-length // chunk_size)):
            number = source.unpack(">I")
            size = source.unpack(">I")
            chunks.append({"number": number, "data": base64.b64encode(source.take(size)).decode("ascii")})
        memory_blocks.append({"objectId": object_id, "formatVersion": format_version, "chunkSize": chunk_size, "chunks": chunks})
    document["memoryBlocks"] = memory_blocks

    if source.position < len(data):
        document["trailing"] = base64.b64encode(data[source.position:]).decode("ascii")
    return document


def main(script_name, args):
    if len(args) > 1 or args[:1] in (["-h"], ["--help"]):
        print(f"使い方: {script_name} [ファイル]", file=sys.stderr)
        return 2
    path = args[0] if args else "-"
    try:
        if path == "-":
            data = sys.stdin.buffer.read()
        else:
            with open(path, "rb") as file:
                data = file.read()
        document = decode(data)
    except (OSError, PdnError, UnicodeDecodeError) as e:
        print(f"{script_name}: {e}", file=sys.stderr)
        return 1
    json.dump(document, sys.stdout, ensure_ascii=False, indent=2)
    sys.stdout.write("\n")
    return 0


sys.exit(main(sys.argv[1], sys.argv[2:]))
EOF
)

exec python3 -c "$program" "$0" "$@"
