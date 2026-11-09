#!/usr/bin/env bash
#
# review-diff.sh — 为 AI 代码评审生成干净的 unified diff。
#
# 默认范围是「工作区相对 HEAD 的改动」（已暂存 + 未暂存 + 未跟踪文件），
# 这正是本地让 Claude 评审时最常用的范围。提交前想看某个分支的完整改动，
# 用 --base；评审线上 PR 用 --pr。
#
# 兼容 macOS 自带的 bash 3.2，不依赖 GNU 工具。
#
set -Eeuo pipefail

usage() {
    cat <<'USAGE'
用法: scripts/review-diff.sh [选项] [REF]

范围（默认与 HEAD 比较，含未跟踪文件）:
  (无参数)            已暂存 + 未暂存 + 未跟踪文件，相对 HEAD
  --staged            仅已暂存（git diff --cached）
  --base REF          与 merge-base(HEAD, REF) 比较，含工作区改动与未跟踪文件
  --pr NUMBER         通过 gh CLI 拉取 PR 的 diff（要求已安装并登录 gh）
  --path PATH         只评审指定路径，可重复；路径相对仓库根目录

输出:
  -o, --output FILE   写入文件（默认写到 stdout）
  --stat              只输出 diffstat
  --name-only         只输出变更文件名
  --no-untracked      不包含未跟踪文件
  --no-header         不输出元信息头
  -h, --help          显示本帮助

自动忽略: target/、.idea/、logs/、tmp/、*.class、*.jar、*.log、.DS_Store

退出码:
  0  正常（没有改动也返回 0）
  1  参数或环境错误
USAGE
}

die() {
    printf 'review-diff: %s\n' "$*" >&2
    exit 1
}

warn() {
    printf 'review-diff: %s\n' "$*" >&2
}

# ---------------------------------------------------------------- 参数解析

MODE=worktree
BASE_REF=""
PR_NUMBER=""
OUTPUT=""
STAT=0
NAME_ONLY=0
INCLUDE_UNTRACKED=1
WITH_HEADER=1
PATHS=()

needs_value() {
    [ "$1" -ge 2 ] || die "选项 ${2:-} 需要一个参数（--help 查看用法）"
}

while [ $# -gt 0 ]; do
    case "$1" in
        --staged)
            MODE=staged
            ;;
        --base)
            needs_value $# "$1"
            BASE_REF=$2
            MODE=base
            shift
            ;;
        --pr)
            needs_value $# "$1"
            PR_NUMBER=$2
            MODE=pr
            shift
            ;;
        --path)
            needs_value $# "$1"
            PATHS[${#PATHS[@]}]=$2
            shift
            ;;
        -o|--output)
            needs_value $# "$1"
            OUTPUT=$2
            shift
            ;;
        --stat)
            STAT=1
            ;;
        --name-only)
            NAME_ONLY=1
            ;;
        --no-untracked)
            INCLUDE_UNTRACKED=0
            ;;
        --no-header)
            WITH_HEADER=0
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        --)
            shift
            while [ $# -gt 0 ]; do
                PATHS[${#PATHS[@]}]=$1
                shift
            done
            break
            ;;
        -*)
            die "未知选项: $1（--help 查看用法）"
            ;;
        *)
            [ -z "$BASE_REF" ] || die "只能指定一个 REF"
            BASE_REF=$1
            MODE=base
            ;;
    esac
    shift
done

if [ "$STAT" -eq 1 ] && [ "$NAME_ONLY" -eq 1 ]; then
    die "--stat 与 --name-only 不能同时使用"
fi

command -v git >/dev/null 2>&1 || die "未找到 git"
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || die "当前目录不在 git 仓库内"

ROOT=$(git rev-parse --show-toplevel)
cd "$ROOT"

BRANCH=$(git symbolic-ref --short -q HEAD || git rev-parse --short HEAD)
HAS_HEAD=0
git rev-parse --verify --quiet HEAD >/dev/null && HAS_HEAD=1 || true

case "$MODE" in
    base)
        [ -n "$BASE_REF" ] || die "--base 需要一个 ref"
        ;;
    pr)
        command -v gh >/dev/null 2>&1 || die "--pr 需要安装 gh CLI（https://cli.github.com）"
        [ -n "$PR_NUMBER" ] || die "--pr 需要一个 PR 编号"
        [ "${#PATHS[@]}" -eq 0 ] || warn "--pr 模式下忽略 --path 过滤"
        ;;
esac

# ---------------------------------------------------------------- 公共参数

DIFF_OPTS=(--no-color --no-ext-diff --no-renames --unified=3)

# 评审时不需要看的产物 / IDE / 日志文件。
EXCLUDES=(
    ':(exclude,glob)target/**'
    ':(exclude,glob).idea/**'
    ':(exclude,glob)logs/**'
    ':(exclude,glob)tmp/**'
    ':(exclude,glob)**/*.class'
    ':(exclude,glob)**/*.jar'
    ':(exclude,glob)**/*.log'
    ':(exclude,glob).DS_Store'
    ':(exclude,glob)**/.DS_Store'
)

# 默认路径 . = 仓库根目录下的全部文件；随后追加排除规则。
if [ "${#PATHS[@]}" -eq 0 ]; then
    PATHSPEC=(. "${EXCLUDES[@]}")
else
    PATHSPEC=("${PATHS[@]}" "${EXCLUDES[@]}")
fi

# 输出模式（--stat / --name-only）与完整 patch 互斥。
if [ "$NAME_ONLY" -eq 1 ]; then
    MODE_OPTS=(--name-only)
elif [ "$STAT" -eq 1 ]; then
    MODE_OPTS=(--stat)
else
    MODE_OPTS=("${DIFF_OPTS[@]}")
fi

resolve_base() {
    git rev-parse --verify --quiet "$1^{commit}" >/dev/null 2>&1 \
        || die "无效的 git ref: $1"
    git merge-base HEAD "$1" 2>/dev/null || git rev-parse "$1^{commit}"
}

# ---------------------------------------------------------------- diff 输出

# 已跟踪文件（含暂存、未暂存、已提交，取决于 MODE）。
emit_tracked() {
    case "$MODE" in
        staged)
            git -c color.ui=false -c core.quotepath=false \
                diff --cached "${MODE_OPTS[@]}" -- "${PATHSPEC[@]}"
            ;;
        worktree)
            if [ "$HAS_HEAD" -eq 1 ]; then
                git -c color.ui=false -c core.quotepath=false \
                    diff "${MODE_OPTS[@]}" HEAD -- "${PATHSPEC[@]}"
            else
                warn "仓库还没有任何提交，跳过已跟踪文件的 diff"
            fi
            ;;
        base)
            local merge_base
            merge_base=$(resolve_base "$BASE_REF")
            git -c color.ui=false -c core.quotepath=false \
                diff "${MODE_OPTS[@]}" "$merge_base" -- "${PATHSPEC[@]}"
            ;;
    esac
}

# 未跟踪文件：git diff 默认看不到，用 --no-index 逐个补上。
emit_untracked() {
    [ "$INCLUDE_UNTRACKED" -eq 1 ] || return 0
    # PR / staged 模式不掺入工作区里的未跟踪文件。
    case "$MODE" in
        pr|staged) return 0 ;;
    esac

    local file
    # -z / read -d '' 保证空格与换行符都不会拆坏文件名。
    while IFS= read -r -d '' file; do
        [ -n "$file" ] || continue
        if [ "$NAME_ONLY" -eq 1 ]; then
            printf '%s\n' "$file"
        elif [ "$STAT" -eq 1 ]; then
            git -c color.ui=false -c core.quotepath=false \
                diff --no-index --stat -- /dev/null "$file" 2>/dev/null || true
        else
            git -c color.ui=false -c core.quotepath=false \
                diff --no-index "${DIFF_OPTS[@]}" -- /dev/null "$file" 2>/dev/null || true
        fi
    done < <(git -c core.quotepath=false ls-files --others --exclude-standard -z \
        -- "${PATHSPEC[@]}")
}

emit_pr() {
    case "$MODE" in
        pr)
            if [ "$NAME_ONLY" -eq 1 ]; then
                gh pr diff "$PR_NUMBER" --name-only
            elif [ "$STAT" -eq 1 ]; then
                gh pr diff "$PR_NUMBER" --patch --color never | git apply --stat
            else
                gh pr diff "$PR_NUMBER" --patch --color never
            fi
            ;;
    esac
}

# ---------------------------------------------------------------- 元信息头

print_header() {
    local scope="工作区 vs HEAD（含未跟踪文件）"
    case "$MODE" in
        staged) scope="已暂存 vs HEAD" ;;
        base)   scope="工作区 vs $(resolve_base "$BASE_REF")（base: $BASE_REF）" ;;
        pr)     scope="PR #$PR_NUMBER" ;;
    esac

    printf '# ===== AI REVIEW DIFF =====\n'
    printf '# branch: %s\n' "$BRANCH"
    printf '# scope: %s\n' "$scope"
    printf '# paths: %s\n' "${PATHS[*]:-<repo root>}"
    printf '# generated: %s\n' "$(date -u '+%Y-%m-%dT%H:%M:%SZ')"

    if [ "$MODE" != "pr" ] && [ "$MODE" != "staged" ] && [ "$INCLUDE_UNTRACKED" -eq 1 ]; then
        local untracked_count
        untracked_count=$(git ls-files --others --exclude-standard -- "${PATHSPEC[@]}" 2>/dev/null | wc -l | tr -d ' ')
        printf '# untracked: %s\n' "$untracked_count"
    fi

    printf '# ==========================\n'
}

# ---------------------------------------------------------------- 主流程

WORK_DIR=$(mktemp -d "${TMPDIR:-/tmp}/review-diff.XXXXXX")
trap 'rm -rf "$WORK_DIR"' EXIT

DIFF_FILE="$WORK_DIR/diff.txt"

if [ "$MODE" = "pr" ]; then
    emit_pr >"$DIFF_FILE"
else
    {
        emit_tracked
        emit_untracked
    } >"$DIFF_FILE"
fi

if [ ! -s "$DIFF_FILE" ]; then
    if [ -n "$OUTPUT" ]; then
        mkdir -p "$(dirname "$OUTPUT")"
        printf '# 没有可评审的改动\n' >"$OUTPUT"
    else
        printf '# 没有可评审的改动\n'
    fi
    exit 0
fi

FINAL_FILE="$WORK_DIR/final.txt"
if [ "$NAME_ONLY" -eq 0 ] && [ "$WITH_HEADER" -eq 1 ]; then
    {
        print_header
        cat "$DIFF_FILE"
    } >"$FINAL_FILE"
else
    cp "$DIFF_FILE" "$FINAL_FILE"
fi

if [ -n "$OUTPUT" ]; then
    mkdir -p "$(dirname "$OUTPUT")"
    cp "$FINAL_FILE" "$OUTPUT"
    line_count=$(wc -l <"$OUTPUT" | tr -d ' ')
    warn "已写入 $OUTPUT（$line_count 行）"
else
    cat "$FINAL_FILE"
fi
