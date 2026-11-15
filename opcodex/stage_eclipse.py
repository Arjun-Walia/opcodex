"""Stage the two Eclipse archives from the pinned upstream binary distribution."""

# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import argparse
import hashlib
import shutil
import tempfile
from pathlib import Path
from urllib.request import urlopen
from zipfile import ZipFile

RELEASE_ROOT = "ghidra_12.1.4_PUBLIC"
RELEASE_URL = ("https://github.com/NationalSecurityAgency/ghidra/releases/download/"
               "Ghidra_12.1.4_build/ghidra_12.1.4_PUBLIC_20260921.zip")
RELEASE_SHA256 = "ddac49f903da9d5bac833e5cc79395098b9c33cfd3279be5f31bd00387d2d4db"
EXTENSIONS = {
    "GhidraDev/GhidraDev-5.0.1.zip":
        "4cf212d8ca9522d213e1539196ffbcf9a9751fcc0419750e366dc6f884f0bb46",
    "GhidraSleighEditor/GhidraSleighEditor-1.0.2.zip":
        "a7926c1ec18a3d50fc987f099d36f75791978317ccee7bfb6def3401fae94a28",
}


def digest(path):
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


def stage(destination, archive):
    if all((destination / name).is_file() and digest(destination / name) == expected
           for name, expected in EXTENSIONS.items()):
        return

    archive.parent.mkdir(parents=True, exist_ok=True)
    if not archive.is_file():
        with tempfile.TemporaryDirectory(dir=archive.parent) as directory:
            downloaded = Path(directory) / archive.name
            with urlopen(RELEASE_URL, timeout=60) as response, downloaded.open("wb") as output:
                shutil.copyfileobj(response, output)
            if digest(downloaded) != RELEASE_SHA256:
                raise ValueError("Upstream binary archive checksum mismatch")
            downloaded.replace(archive)

    if digest(archive) != RELEASE_SHA256:
        raise ValueError("Upstream binary archive checksum mismatch")

    with ZipFile(archive) as source:
        for name, expected in EXTENSIONS.items():
            payload = source.read(f"{RELEASE_ROOT}/Extensions/Eclipse/{name}")
            if hashlib.sha256(payload).hexdigest() != expected:
                raise ValueError(f"Eclipse extension checksum mismatch: {name}")
            target = destination / name
            target.parent.mkdir(parents=True, exist_ok=True)
            with tempfile.TemporaryDirectory(dir=target.parent) as directory:
                pending = Path(directory) / target.name
                pending.write_bytes(payload)
                pending.replace(target)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--destination", type=Path, required=True)
    parser.add_argument("--archive", type=Path, required=True)
    args = parser.parse_args()
    stage(args.destination, args.archive)
