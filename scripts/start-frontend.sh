#!/usr/bin/env bash
# 启动 Vue2 + Element-UI 前端。首次运行会安装依赖。
set -euo pipefail
cd "$(dirname "$0")/../frontend"
if [ ! -d node_modules ]; then
  npm install
fi
exec npm run dev
