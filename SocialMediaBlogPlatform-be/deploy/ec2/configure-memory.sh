#!/usr/bin/env bash
set -euo pipefail

SWAP_FILE="${SWAP_FILE:-/swapfile}"
SWAP_SIZE="${SWAP_SIZE:-2G}"

if [[ "${EUID}" -ne 0 ]]; then
  echo "Run this script with sudo." >&2
  exit 1
fi

if [[ ! -f "${SWAP_FILE}" ]]; then
  fallocate -l "${SWAP_SIZE}" "${SWAP_FILE}"
  chmod 600 "${SWAP_FILE}"
  mkswap "${SWAP_FILE}"
fi

if ! swapon --show=NAME --noheadings | grep -Fxq "${SWAP_FILE}"; then
  swapon "${SWAP_FILE}"
fi

if ! grep -Fq "${SWAP_FILE} none swap sw 0 0" /etc/fstab; then
  printf '%s none swap sw 0 0\n' "${SWAP_FILE}" >> /etc/fstab
fi

echo "Memory configuration:"
free -h
swapon --show