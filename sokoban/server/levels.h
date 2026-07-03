/*
 * levels.h - 推箱子游戏关卡定义
 * 每个关卡包含：名称、地图行数、列数、地图数据
 * 地图字符: #墙 .目标 $箱子 @玩家  空格地板  +玩家在目标上  *箱子在目标上
 */

#ifndef LEVELS_H
#define LEVELS_H

#include <stddef.h>

#define MAX_LEVELS 20
#define MAX_ROWS   20
#define MAX_COLS   20
#define MAX_LEVEL_NAME 64

typedef struct {
    int id;
    char name[MAX_LEVEL_NAME];
    int rows;
    int cols;
    char map[MAX_ROWS][MAX_COLS];
    /* 目标位置（用于胜利判定），存储所有目标点 */
    int target_count;
    int target_positions[MAX_ROWS * MAX_COLS][2];
} Level;

/* 关卡管理器 */
typedef struct {
    Level levels[MAX_LEVELS];
    int count;
} LevelManager;

/* 初始化所有关卡 */
void level_manager_init(LevelManager *mgr);

/* 获取关卡 */
const Level *level_manager_get(const LevelManager *mgr, int id);

/* 获取关卡数量 */
int level_manager_count(const LevelManager *mgr);

#endif /* LEVELS_H */
