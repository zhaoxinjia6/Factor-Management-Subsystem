/*
 * game.h - 推箱子游戏逻辑引擎
 * 负责：游戏状态管理、移动逻辑、胜利判定
 */

#ifndef GAME_H
#define GAME_H

#include "levels.h"

/* 游戏状态 */
typedef enum {
    GAME_PLAYING,
    GAME_WON,
    GAME_IDLE
} GameStatus;

/* 游戏会话 */
typedef struct {
    const Level *level;        /* 当前关卡 */
    char map[MAX_ROWS][MAX_COLS];  /* 当前地图状态（可修改） */
    int player_row;            /* 玩家当前行 */
    int player_col;            /* 玩家当前列 */
    int push_count;            /* 推箱子次数 */
    int move_count;            /* 移动次数 */
    GameStatus status;         /* 游戏状态 */
} GameSession;

/* 方向 */
typedef enum {
    DIR_UP,
    DIR_DOWN,
    DIR_LEFT,
    DIR_RIGHT,
    DIR_INVALID
} Direction;

/* 将字符转换为方向 */
Direction direction_from_char(char c);

/* 初始化游戏会话 */
void game_init(GameSession *gs, const Level *level);

/* 执行移动操作，返回结果代码 */
/*  0 - 成功移动      1 - 推动箱子成功   2 - 游戏胜利 */
/* -1 - 撞墙        -2 - 箱子被阻挡   -3 - 未开始游戏 */
int game_move(GameSession *gs, Direction dir);

/* 获取游戏状态 */
GameStatus game_status(const GameSession *gs);

/* 将当前地图渲染为字符串（供网络传输） */
void game_render_map(const GameSession *gs, char *out, size_t out_size);

/* 获取统计信息 */
void game_get_stats(const GameSession *gs, char *out, size_t out_size);

#endif /* GAME_H */
