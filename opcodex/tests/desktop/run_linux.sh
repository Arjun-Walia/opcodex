#!/usr/bin/env bash
# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
set -euo pipefail

# Run inside a disposable Xvfb server, never in the user's desktop session.
: "${DISPLAY:?Run this script through xvfb-run}"
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../../.." && pwd)"
wm_log="${TMPDIR:-/tmp}/opcodex-test-wm-$$.log"
openbox --sm-disable > "$wm_log" 2>&1 &
wm_pid=$!
trap 'kill "$wm_pid" 2>/dev/null || true' EXIT
ready=false
for ((i=0; i<40; i++)); do
    if ! kill -0 "$wm_pid" 2>/dev/null; then
        cat "$wm_log" >&2
        exit 1
    fi
    if [[ "$(LC_ALL=C xprop -root _NET_SUPPORTING_WM_CHECK)" == *"window id"* ]]; then
        ready=true
        break
    fi
    sleep .1
done
if [[ "$ready" != true ]]; then
    cat "$wm_log" >&2
    exit 1
fi
python3 "$repo_dir/opcodex/tests/desktop/check_linux.py" "$@"
