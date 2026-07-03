/*
 * game.c - 推箱子游戏逻辑实现
 */

#include <string.h>
#include <stdio.h>
#include "game.h"

Direction direction_from_char(char c)
{
    switch (c) {
        case 'w': case 'W': case 'A': case 0x41/*上箭头*/: return DIR_UP;
        case 's': case 'S': case 'B': case 0x42/*下箭头*/: return DIR_DOWN;
        case 'a': case 'A': case 'D': case 0x44/*左箭头*/: return DIR_LEFT;
        case 'd': case 'D': case 'C': case 0x43/*右箭头*/: return DIR_RIGHT;
        default: return DIR_INVALID;
    }
}

/* ---- 地图辅助函数 ---- */

static int _is_walkable(char cell)
{
    /* 玩家可以站在空地、目标点、空箱子位不行 */
    return cell == ' ' || cell == '.' || cell == '+' || cell == '@';
}

static int _is_box(char cell)
{
    return cell == '$' || cell == '*';
}

static int _is_goal(char cell)
{
    return cell == '.' || cell == '+' || cell == '*';
}

void game_init(GameSession *gs, const Level *level)
{
    int r, c;
    gs->level = level;
    gs->push_count = 0;
    gs->move_count = 0;
    gs->status = GAME_PLAYING;

    /* 复制地图并找到玩家位置 */
    for (r = 0; r < level->rows; r++) {
        for (c = 0; c < level->cols; c++) {
            gs->map[r][c] = level->map[r][c];
            if (gs->map[r][c] == '@' || gs->map[r][c] == '+') {
                gs->player_row = r;
                gs->player_col = c;
            }
        }
    }
}

int game_move(GameSession *gs, Direction dir)
{
    int dr = 0, dc = 0;
    int new_r, new_c;
    int box_r, box_c;

    if (gs->status != GAME_PLAYING)
        return -3;

    switch (dir) {
        case DIR_UP:    dr = -1; dc = 0;  break;
        case DIR_DOWN:  dr =  1; dc = 0;  break;
        case DIR_LEFT:  dr =  0; dc = -1; break;
        case DIR_RIGHT: dr =  0; dc =  1; break;
        default:        return -1;
    }

    new_r = gs->player_row + dr;
    new_c = gs->player_col + dc;

    /* 检查边界 */
    if (new_r < 0 || new_r >= gs->level->rows ||
        new_c < 0 || new_c >= gs->level->cols)
        return -1;

    /* 如果目标位置是墙 */
    if (gs->map[new_r][new_c] == '#')
        return -1;

    /* 如果目标位置是箱子 */
    if (_is_box(gs->map[new_r][new_c])) {
        box_r = new_r + dr;
        box_c = new_c + dc;

        /* 箱子后面不能是墙、边界或另一个箱子 */
        if (box_r < 0 || box_r >= gs->level->rows ||
            box_c < 0 || box_c >= gs->level->cols)
            return -2;

        if (gs->map[box_r][box_c] == '#' || _is_box(gs->map[box_r][box_c]))
            return -2;

        /* 推动箱子 */
        /* 箱子原来位置变为玩家 */
        gs->map[gs->player_row][gs->player_col] =
            _is_goal(gs->map[gs->player_row][gs->player_col]) ? '.' : ' ';

        /* 箱子新位置 */
        if (gs->map[box_r][box_c] == '.')
            gs->map[box_r][box_c] = '*';
        else
            gs->map[box_r][box_c] = '$';

        /* 玩家新位置 */
        if (_is_goal(gs->map[new_r][new_c]))
            gs->map[new_r][new_c] = '+';
        else
            gs->map[new_r][new_c] = '@';

        gs->player_row = new_r;
        gs->player_col = new_c;
        gs->push_count++;
        gs->move_count++;

        /* 检查胜利条件：所有目标点上都有箱子(*) */
    {
        int i, r, c;
        int all_done = 1;
        for (i = 0; i < gs->level->target_count; i++) {
            r = gs->level->target_positions[i][0];
            c = gs->level->target_positions[i][1];
            if (gs->map[r][c] != '*') {
                all_done = 0;
                break;
            }
        }
        if (all_done) {
            gs->status = GAME_WON;
            return 2;
        }
    }

        return 1;  /* 推了箱子 */
    }

    /* 目标位置是空地或目标点 */
    if (gs->map[new_r][new_c] == ' ' || gs->map[new_r][new_c] == '.') {
        /* 原位置 */
        gs->map[gs->player_row][gs->player_col] =
            _is_goal(gs->map[gs->player_row][gs->player_col]) ? '.' : ' ';

        /* 新位置 */
        gs->map[new_r][new_c] =
            (gs->map[new_r][new_c] == '.') ? '+' : '@';

        gs->player_row = new_r;
        gs->player_col = new_c;
        gs->move_count++;
        return 0;
    }

    return -1;  /* 不应该到达此处 */
}

GameStatus game_status(const GameSession *gs)
{
    return gs->status;
}

void game_render_map(const GameSession *gs, char *out, size_t out_size)
{
    int r, c;
    size_t pos = 0;

    for (r = 0; r < gs->level->rows; r++) {
        for (c = 0; c < gs->level->cols; c++) {
            if (pos < out_size - 1)
                out[pos++] = gs->map[r][c];
        }
        if (pos < out_size - 1)
            out[pos++] = '\n';
    }
    out[pos] = '\0';
}

void game_get_stats(const GameSession *gs, char *out, size_t out_size)
{
    snprintf(out, out_size,
        "关卡: %s | 移动: %d | 推箱: %d | 状态: %s",
        gs->level ? gs->level->name : "无",
        gs->move_count,
        gs->push_count,
        gs->status == GAME_WON ? "已通关!" :
        gs->status == GAME_PLAYING ? "进行中" : "未开始");
}
