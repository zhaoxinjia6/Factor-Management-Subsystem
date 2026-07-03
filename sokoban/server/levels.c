/*
 * levels.c - 关卡数据定义
 * 包含 10 个精心设计的推箱子关卡，难度从易到难
 * 地图字符: #墙 .目标 $箱子 @玩家 _空格  +玩家在目标上 *箱子在目标上
 * 地图中空格用 '_' 表示不可达区域（方便文本表示）
 */

#include <string.h>
#include "levels.h"

static void _init_level_1(Level *lvl);
static void _init_level_2(Level *lvl);
static void _init_level_3(Level *lvl);
static void _init_level_4(Level *lvl);
static void _init_level_5(Level *lvl);
static void _init_level_6(Level *lvl);
static void _init_level_7(Level *lvl);
static void _init_level_8(Level *lvl);
static void _init_level_9(Level *lvl);
static void _init_level_10(Level *lvl);

void level_manager_init(LevelManager *mgr)
{
    mgr->count = 0;

    _init_level_1(&mgr->levels[mgr->count++]);
    _init_level_2(&mgr->levels[mgr->count++]);
    _init_level_3(&mgr->levels[mgr->count++]);
    _init_level_4(&mgr->levels[mgr->count++]);
    _init_level_5(&mgr->levels[mgr->count++]);
    _init_level_6(&mgr->levels[mgr->count++]);
    _init_level_7(&mgr->levels[mgr->count++]);
    _init_level_8(&mgr->levels[mgr->count++]);
    _init_level_9(&mgr->levels[mgr->count++]);
    _init_level_10(&mgr->levels[mgr->count++]);
}

const Level *level_manager_get(const LevelManager *mgr, int id)
{
    if (id < 1 || id > mgr->count)
        return NULL;
    return &mgr->levels[id - 1];
}

int level_manager_count(const LevelManager *mgr)
{
    return mgr->count;
}

/* ---- 关卡1：入门 ---- */
/*    ###
 *    #@#
 *    # $#
 *    # .#
 *    ###     */
static void _init_level_1(Level *lvl)
{
    const char *raw[] = {
        "____###_",
        "____#.#_",
        "____# $#",
        "### # .#",
        "#@# #  #",
        "#  $   #",
        "#  # ###",
        "### ##__"
    };
    int r, c;
    lvl->id = 1;
    strncpy(lvl->name, "初识推箱", MAX_LEVEL_NAME);
    lvl->rows = 8;
    lvl->cols = 8;
    lvl->target_count = 0;

    for (r = 0; r < lvl->rows; r++) {
        for (c = 0; c < lvl->cols; c++) {
            char ch = raw[r][c];
            if (ch == '_')
                lvl->map[r][c] = ' ';
            else if (ch == '.') {
                lvl->map[r][c] = '.';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '+') {
                lvl->map[r][c] = '+';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else {
                lvl->map[r][c] = ch;
            }
        }
    }
}

/* ---- 关卡2：双箱入门 ---- */
/*  ####
 *  # .#
 *  #  ###
 *  #*@  #
 *  #  $ #
 *  #  ###
 *  ####   */
static void _init_level_2(Level *lvl)
{
    const char *raw[] = {
        "_____###_",
        "____##.##",
        "____# $ #",
        "___## $ #",
        "___#  @ #",
        "___#  *###",
        "___#  .__",
        "___######"
    };
    int r, c;
    lvl->id = 2;
    strncpy(lvl->name, "双箱初试", MAX_LEVEL_NAME);
    lvl->rows = 8;
    lvl->cols = 10;
    lvl->target_count = 0;

    for (r = 0; r < lvl->rows; r++) {
        for (c = 0; c < lvl->cols; c++) {
            char ch = raw[r][c];
            if (ch == '_') {
                lvl->map[r][c] = ' ';
            } else if (ch == '.') {
                lvl->map[r][c] = '.';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '+') {
                lvl->map[r][c] = '+';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '*') {
                lvl->map[r][c] = '*';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else {
                lvl->map[r][c] = ch;
            }
        }
    }
}

/* ---- 关卡3：简单转折 ---- */
/*  #####
 *  #   #
 *  # $ #
 *  ### $##
 *    # @ #
 *    # . #
 *    #   #
 *    #####   */
static void _init_level_3(Level *lvl)
{
    const char *raw[] = {
        "___#####_",
        "___#   #_",
        "___#$  #_",
        "_###  $##",
        "_#  @  #",
        "_# . . #",
        "_#     #",
        "_#######"
    };
    int r, c;
    lvl->id = 3;
    strncpy(lvl->name, "转折之道", MAX_LEVEL_NAME);
    lvl->rows = 8;
    lvl->cols = 9;
    lvl->target_count = 0;

    for (r = 0; r < lvl->rows; r++) {
        for (c = 0; c < lvl->cols; c++) {
            char ch = raw[r][c];
            if (ch == '_') {
                lvl->map[r][c] = ' ';
            } else if (ch == '.') {
                lvl->map[r][c] = '.';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '+') {
                lvl->map[r][c] = '+';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '*') {
                lvl->map[r][c] = '*';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else {
                lvl->map[r][c] = ch;
            }
        }
    }
}

/* ---- 关卡4 ---- */
/*    #####
 *    #   #
 *    # $ #
 *  ### $ ##
 *  #  $  @#
 *  # #  # #
 *  # .  . #
 *  #  .   #
 *  #########  */
static void _init_level_4(Level *lvl)
{
    const char *raw[] = {
        "____#####",
        "____#   #",
        "____#$  #",
        "_### $ ##",
        "_#  $  @#",
    "# # ##  #",
        "_# .  . #",
        "_#  .   #",
        "_########"
    };
    int r, c;
    lvl->id = 4;
    strncpy(lvl->name, "三角布局", MAX_LEVEL_NAME);
    lvl->rows = 9;
    lvl->cols = 9;
    lvl->target_count = 0;

    for (r = 0; r < lvl->rows; r++) {
        for (c = 0; c < lvl->cols; c++) {
            char ch = raw[r][c];
            if (ch == '_') {
                lvl->map[r][c] = ' ';
            } else if (ch == '.') {
                lvl->map[r][c] = '.';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '+') {
                lvl->map[r][c] = '+';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '*') {
                lvl->map[r][c] = '*';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else {
                lvl->map[r][c] = ch;
            }
        }
    }
}

/* ---- 关卡5 ---- */
/*  ########
 *  #      #
 *  # $  $ #
 *  # $  $ #
 *  # @ ## #
 *  ##   . #
 *   #  .. #
 *   ######   */
static void _init_level_5(Level *lvl)
{
    const char *raw[] = {
        "__########",
        "__#      #",
        "__# $$ $ #",
        "__# $$ $ #",
        "__# @ ## #",
        "__##   . #",
        "__#   .. #",
        "__########"
    };
    int r, c;
    lvl->id = 5;
    strncpy(lvl->name, "四箱阵列", MAX_LEVEL_NAME);
    lvl->rows = 8;
    lvl->cols = 10;
    lvl->target_count = 0;

    for (r = 0; r < lvl->rows; r++) {
        for (c = 0; c < lvl->cols; c++) {
            char ch = raw[r][c];
            if (ch == '_') {
                lvl->map[r][c] = ' ';
            } else if (ch == '.') {
                lvl->map[r][c] = '.';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '+') {
                lvl->map[r][c] = '+';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '*') {
                lvl->map[r][c] = '*';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else {
                lvl->map[r][c] = ch;
            }
        }
    }
}

/* ---- 关卡6 ---- */
/*   ######
 *   #    #
 *   # $  #
 *   # $  #
 * ## $  ##
 * #  @   #
 * # .  # #
 * # .  # #
 * #  .   #
 * ######## */
static void _init_level_6(Level *lvl)
{
    const char *raw[] = {
        "___######",
        "___#    #",
        "___# $  #",
        "___# $  #",
        "_## $  ##",
        "_#  @   #",
    "# .  # #",
        "# .  # #",
        "_#  .   #",
        "_########"
    };
    int r, c;
    lvl->id = 6;
    strncpy(lvl->name, "迷宫回廊", MAX_LEVEL_NAME);
    lvl->rows = 10;
    lvl->cols = 9;
    lvl->target_count = 0;

    for (r = 0; r < lvl->rows; r++) {
        for (c = 0; c < lvl->cols; c++) {
            char ch = raw[r][c];
            if (ch == '_') {
                lvl->map[r][c] = ' ';
            } else if (ch == '.') {
                lvl->map[r][c] = '.';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '+') {
                lvl->map[r][c] = '+';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '*') {
                lvl->map[r][c] = '*';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else {
                lvl->map[r][c] = ch;
            }
        }
    }
}

/* ---- 关卡7 ---- */
/*  ########
 *  #      #
 *  # $  $ #
 *  # $  $ #
 *  #   $  #
 *  # @$   #
 *  #  #   #
 *  # .  . #
 *  # .  . #
 *  ######## */
static void _init_level_7(Level *lvl)
{
    const char *raw[] = {
        "__########",
        "__#      #",
        "__# $  $ #",
        "__# $  $ #",
        "__#   $  #",
        "__# @$   #",
        "__#  #   #",
        "__# .  . #",
        "__# .  . #",
        "__########"
    };
    int r, c;
    lvl->id = 7;
    strncpy(lvl->name, "五箱挑战", MAX_LEVEL_NAME);
    lvl->rows = 10;
    lvl->cols = 10;
    lvl->target_count = 0;

    for (r = 0; r < lvl->rows; r++) {
        for (c = 0; c < lvl->cols; c++) {
            char ch = raw[r][c];
            if (ch == '_') {
                lvl->map[r][c] = ' ';
            } else if (ch == '.') {
                lvl->map[r][c] = '.';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '+') {
                lvl->map[r][c] = '+';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '*') {
                lvl->map[r][c] = '*';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else {
                lvl->map[r][c] = ch;
            }
        }
    }
}

/* ---- 关卡8 ---- */
/*   ######
 *   #    #
 *   # $  #
 * ### $ ###
 * #  $ $  #
 * # @  $  #
 * ### $ ###
 *   # .  #
 *   # .. #
 *   ######  */
static void _init_level_8(Level *lvl)
{
    const char *raw[] = {
        "___######",
        "___#    #",
        "___# $  #",
        "_### $ ###",
        "_#  $ $  #",
    "# @  $  #",
        "_### $ ###",
        "___# .  #",
        "___# .. #",
        "___######"
    };
    int r, c;
    lvl->id = 8;
    strncpy(lvl->name, "回字形阵", MAX_LEVEL_NAME);
    lvl->rows = 10;
    lvl->cols = 10;
    lvl->target_count = 0;

    for (r = 0; r < lvl->rows; r++) {
        for (c = 0; c < lvl->cols; c++) {
            char ch = raw[r][c];
            if (ch == '_') {
                lvl->map[r][c] = ' ';
            } else if (ch == '.') {
                lvl->map[r][c] = '.';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '+') {
                lvl->map[r][c] = '+';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '*') {
                lvl->map[r][c] = '*';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else {
                lvl->map[r][c] = ch;
            }
        }
    }
}

/* ---- 关卡9 ---- */
/*  ########
 *  #  #  .#
 *  #  #  .#
 *  #  $  .#
 *  #  $   #
 *  #  $  @#
 *  #  # $ #
 *  #  #  .#
 *  #  #  .#
 *  ######## */
static void _init_level_9(Level *lvl)
{
    const char *raw[] = {
        "__########",
        "__#  #  .#",
        "__#  #  .#",
        "__#  $  .#",
        "__#  $   #",
        "__#  $  @#",
    "#  # $ #",
        "__#  #  .#",
        "__#  #  .#",
        "__########"
    };
    int r, c;
    lvl->id = 9;
    strncpy(lvl->name, "长路漫漫", MAX_LEVEL_NAME);
    lvl->rows = 10;
    lvl->cols = 10;
    lvl->target_count = 0;

    for (r = 0; r < lvl->rows; r++) {
        for (c = 0; c < lvl->cols; c++) {
            char ch = raw[r][c];
            if (ch == '_') {
                lvl->map[r][c] = ' ';
            } else if (ch == '.') {
                lvl->map[r][c] = '.';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '+') {
                lvl->map[r][c] = '+';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '*') {
                lvl->map[r][c] = '*';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else {
                lvl->map[r][c] = ch;
            }
        }
    }
}

/* ---- 关卡10 ---- */
/*  ##########
 *  #  @     #
 *  #  $ $ $ #
 *  #  $ $ $ #
 *  #  $ $ $ #
 *  #   #    #
 *  # .  .   #
 *  # .  .   #
 *  ########## */
static void _init_level_10(Level *lvl)
{
    const char *raw[] = {
        "__##########",
        "__#  @     #",
        "__#  $ $ $ #",
        "__#  $ $ $ #",
        "__#  $ $ $ #",
        "__#   #    #",
        "__# .  .   #",
        "__# .  .   #",
        "__##########"
    };
    int r, c;
    lvl->id = 10;
    strncpy(lvl->name, "九箱终章", MAX_LEVEL_NAME);
    lvl->rows = 9;
    lvl->cols = 12;
    lvl->target_count = 0;

    for (r = 0; r < lvl->rows; r++) {
        for (c = 0; c < lvl->cols; c++) {
            char ch = raw[r][c];
            if (ch == '_') {
                lvl->map[r][c] = ' ';
            } else if (ch == '.') {
                lvl->map[r][c] = '.';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '+') {
                lvl->map[r][c] = '+';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else if (ch == '*') {
                lvl->map[r][c] = '*';
                lvl->target_positions[lvl->target_count][0] = r;
                lvl->target_positions[lvl->target_count][1] = c;
                lvl->target_count++;
            } else {
                lvl->map[r][c] = ch;
            }
        }
    }
}
