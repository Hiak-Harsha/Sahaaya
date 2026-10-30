#!/usr/bin/env python3
"""Download the recommended small offline models for H5 (see MODELS.md).

Run ONCE while online:
    pip install huggingface_hub
    python download_models.py                 # default set (~1 GB)
    python download_models.py --all           # also the optional models (~2.7 GB total)
    python download_models.py --dir ./models  # choose the cache folder (default ./models)

After this, your solution must work offline. Nothing runs at import time.
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

# (repo_id, pinned revision, allow_patterns or None for the whole repo, default?)
MODELS = [
    ("Qwen/Qwen2.5-0.5B-Instruct-GGUF", "9217f5db79a29953eb74d5343926648285ec7e67",
     ["qwen2.5-0.5b-instruct-q4_k_m.gguf", "LICENSE", "README.md"], True),
    ("intfloat/multilingual-e5-small", "614241f622f53c4eeff9890bdc4f31cfecc418b3",
     ["*.json", "model.safetensors", "sentencepiece.bpe.model", "1_Pooling/*", "README.md"], True),
    ("Qwen/Qwen2.5-1.5B-Instruct-GGUF", "91cad51170dc346986eccefdc2dd33a9da36ead9",
     ["qwen2.5-1.5b-instruct-q4_k_m.gguf", "LICENSE", "README.md"], False),
    ("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2", "e8f8c211226b894fcb81acc59f3b34ba3efd5f42",
     ["*.json", "model.safetensors", "sentencepiece.bpe.model", "1_Pooling/*", "README.md"], False),
    ("openai/whisper-tiny", "169d4a4341b33bc18d8881c4b69c2e104e1cc0af",
     ["*.json", "model.safetensors", "*.txt", "README.md"], False),
]


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--dir", default="models")
    ap.add_argument("--all", action="store_true", help="also download the optional models")
    args = ap.parse_args()
    try:
        from huggingface_hub import snapshot_download
    except ImportError:
        print("Please run:  pip install huggingface_hub   (then re-run this script)")
        return 1
    out = Path(args.dir)
    out.mkdir(parents=True, exist_ok=True)
    for repo, rev, patterns, default in MODELS:
        if not default and not args.all:
            continue
        target = out / repo.replace("/", "__")
        print(f"-> {repo} @ {rev[:10]} into {target}")
        try:
            snapshot_download(repo_id=repo, revision=rev, allow_patterns=patterns, local_dir=str(target))
        except Exception as e:  # noqa: BLE001
            print(f"   FAILED: {e}\n   (Check your internet connection, or ask the organisers for the local mirror.)")
            return 1
    print("Done. Your models are cached; switch the network off and test your offline path.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
