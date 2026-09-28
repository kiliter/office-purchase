#!/usr/bin/env bash
# 论文使用 Spring Boot 2.7。优先 JDK 17，其次 JDK 8。当前机器默认 JDK 21 不在该版本官方支持范围内。
set -euo pipefail
cd "$(dirname "$0")/.."
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  if /usr/libexec/java_home -v 17 >/dev/null 2>&1; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
  elif /usr/libexec/java_home -v 1.8 >/dev/null 2>&1; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 1.8)"
  fi
  export PATH="$JAVA_HOME/bin:$PATH"
fi
echo "使用 JAVA_HOME=${JAVA_HOME:-未设置}"
cd backend
if [ "${1:-mysql}" = "h2" ]; then
  exec mvn spring-boot:run -Dspring-boot.run.profiles=h2
fi
if [ -z "${DB_PASSWORD:-}" ]; then
  echo "请先设置数据库密码，例如：export DB_PASSWORD=你的本地密码"
  exit 1
fi
exec mvn spring-boot:run
