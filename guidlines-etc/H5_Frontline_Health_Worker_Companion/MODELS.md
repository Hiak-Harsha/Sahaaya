# H5 — Recommended small offline models

The problem statement asks for "a small offline-capable language model and embedding model, pre-cached". The models below are our recommendations, not requirements.

- You may use any open model that runs **on a CPU, offline**.
- Whatever model you use, you must report its footprint in `submission/H5_footprint.json`.

We checked every model ID, licence, revision and file size on the Hugging Face model API on **2026-10-21**.

## How to download them (once, while you have internet)

Run the two steps below to cache (save) the models on your laptop.

Run these commands from your problem folder, one at a time. On Mac/Linux, type `python3` instead of `python`.

**Step 1.** Install the download library.

```
pip install huggingface_hub
```

If `pip` is not found, type `python -m pip install huggingface_hub` instead (Mac/Linux: `python3 -m pip install huggingface_hub`).

**Step 2.** Download the models.

```
python download_models.py
```

By default this downloads the two default models (about 1 GB) into a folder named `models`. To also download the optional models (about 2.7 GB in total), type `python download_models.py --all` instead.

After this, your assistant must work with the network switched off. Organisers may also provide a local mirror at the venue.

## The models

Some words used in the table:

- **Generator:** the language model that writes the answer text.
- **Embeddings:** a model that turns text into a list of numbers, so you can search for the sections closest in meaning to a question.
- **GGUF:** a single-file model format that llama.cpp (a program that runs models on a CPU) can load.
- **q4_k_m / quantised:** the model's numbers are stored with fewer bits, so the file is smaller.
- **ONNX:** another portable model file format.

| Role | Model (Hugging Face ID) | Licence | Files we pin | Size on disk | Notes |
|---|---|---|---|---|---|
| **Generator (default)** | `Qwen/Qwen2.5-0.5B-Instruct-GGUF` | Apache-2.0 | `qwen2.5-0.5b-instruct-q4_k_m.gguf` | ~491 MB | ~0.49 B parameters, 32k context. Runs with llama.cpp / `llama-cpp-python` on any laptop CPU. Phone-class footprint. |
| Generator (bigger, optional) | `Qwen/Qwen2.5-1.5B-Instruct-GGUF` | Apache-2.0 | `qwen2.5-1.5b-instruct-q4_k_m.gguf` | ~1.1 GB | Better wording, slower. Still CPU-only. |
| **Embeddings (default)** | `intfloat/multilingual-e5-small` | MIT | full repo (PyTorch/safetensors) | ~471 MB (`model.safetensors`) | 12 layers, 384-dim, max 512 tokens, XLM-R tokenizer (~100 languages incl. Hindi, Bengali, Marathi, Telugu). **Prefix inputs with `query: ` / `passage: `.** The repo also has quantised ONNX files (e.g. `onnx/model_qint8_avx512_vnni.onnx`, ~118 MB, x86 only). |
| Embeddings (alternative) | `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` | Apache-2.0 | full repo | ~471 MB (`model.safetensors`) | Has quantised ONNX for ARM (`onnx/model_qint8_arm64.onnx`, ~118 MB), which is closer to a phone. |
| Speech (stretch, optional) | `openai/whisper-tiny` | Apache-2.0 | full repo | ~151 MB (`model.safetensors`) | Multilingual speech-to-text, for the voice-input stretch goal. |

Pinned revisions (commit hashes as of 2026-10-21) are in `download_models.py`. A pinned revision is one fixed version of the model, so everyone downloads the same files.

## Tips for a phone-sized footprint

- **Retrieval does most of the work.** The protocol corpus is small (about 100 sections). BM25 (keyword search) plus embeddings over the sections is plenty, with the top 1–3 sections passed to a 0.5 B model. Pre-compute the section embeddings once and store them in a file.
- **Escalation must not depend on the model.** The problem statement requires a hard-coded danger-sign layer that runs *before* any model reasoning. A small LLM can then phrase the answer. It must never decide on its own whether a danger sign is present.
- **Measure, don't guess.** Report these in `H5_footprint.json`: model size on disk, peak RAM (e.g. `resource.getrusage` on Linux/macOS or `psutil`), and median latency (response time) per question on a CPU.
- **Licences.** All models above allow commercial and non-commercial use under their licences. Keep their licence files if you redistribute them.
- **Hindi/Hinglish.** Qwen2.5 is multilingual but is not specially tuned for Hinglish. Keep numbers and section IDs copied exactly from the retrieved text. Never let the model invent a dose.
