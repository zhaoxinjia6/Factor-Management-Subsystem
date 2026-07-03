/*
 * client.c - 推箱子游戏客户端
 *
 * 纯终端文本交互，支持:
 *   - 彩色地图渲染
 *   - 单字符快捷键 (wasd 移动)
 *   - 非阻塞输入 (kbhit 方式)
 *   - 自动刷新显示
 *
 * 编译: gcc -Wall -O2 -o sokoban_client client.c
 * 运行: ./sokoban_client <服务器地址> <端口号>
 */

#define _POSIX_C_SOURCE 199309L

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <signal.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/time.h>
#include <termios.h>
#include <stdarg.h>
#include <time.h>

#define BUF_SIZE 8192
#define CMD_SIZE 256

/* ============ 全局 ============ */

static int g_sock = -1;
static volatile int g_running = 1;
static char g_username[64] = "";
static int g_logged_in = 0;
static int g_in_game = 0;      /* 是否正在显示地图 */
static int g_auto_refresh = 0; /* 游戏中自动显示地图 */

/* ============ 终端设置 ============ */

static struct termios g_old_term;

static void term_enter_raw(void)
{
    struct termios raw;
    tcgetattr(STDIN_FILENO, &g_old_term);
    raw = g_old_term;
    raw.c_lflag &= ~(ECHO | ICANON | ISIG);
    raw.c_cc[VMIN] = 0;
    raw.c_cc[VTIME] = 0;
    tcsetattr(STDIN_FILENO, TCSAFLUSH, &raw);
}

static void term_exit_raw(void)
{
    tcsetattr(STDIN_FILENO, TCSAFLUSH, &g_old_term);
}

/* 检测是否有按键 */
static int kbhit(void)
{
    char c;
    if (read(STDIN_FILENO, &c, 1) > 0) {
        /* 把字符放回（无法放回，通过全局保存） */
        return (unsigned char)c;
    }
    return 0;
}

/* ============ 颜色定义 ============ */

#define COLOR_RESET   "\033[0m"
#define COLOR_RED     "\033[31m"
#define COLOR_GREEN   "\033[32m"
#define COLOR_YELLOW  "\033[33m"
#define COLOR_BLUE    "\033[34m"
#define COLOR_MAGENTA "\033[35m"
#define COLOR_CYAN    "\033[36m"
#define COLOR_WHITE   "\033[37m"
#define COLOR_BOLD    "\033[1m"
#define COLOR_BG_RED  "\033[41m"
#define COLOR_BG_BLUE "\033[44m"
#define COLOR_BG_CYAN "\033[46m"
#define COLOR_BG_GREEN "\033[42m"
#define COLOR_BG_YELLOW "\033[43m"

/* 清屏 */
#define CLEAR_SCREEN "\033[2J\033[H"

/* ============ 网络通信 ============ */

/* 连接服务器 */
static int connect_server(const char *host, int port)
{
    struct sockaddr_in addr;
    struct hostent *he;

    g_sock = socket(AF_INET, SOCK_STREAM, 0);
    if (g_sock < 0) {
        perror("socket");
        return -1;
    }

    he = gethostbyname(host);
    if (!he) {
        fprintf(stderr, "无法解析主机: %s\n", host);
        close(g_sock);
        return -1;
    }

    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);
    memcpy(&addr.sin_addr, he->h_addr_list[0], he->h_length);

    if (connect(g_sock, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        perror("connect");
        close(g_sock);
        return -1;
    }

    return 0;
}

/* 发送命令 */
static void send_cmd(const char *fmt, ...)
{
    char buf[BUF_SIZE];
    va_list ap;
    int len;

    va_start(ap, fmt);
    len = vsnprintf(buf, sizeof(buf), fmt, ap);
    va_end(ap);

    if (len > 0) {
        buf[len] = '\n';
        write(g_sock, buf, len + 1);
    }
}

/* 从服务器读取一行 */
static int read_line(char *buf, int maxlen)
{
    static char rbuf[BUF_SIZE];
    static int rlen = 0;
    int i;

    /* 先从缓冲区找换行符 */
    for (i = 0; i < rlen; i++) {
        if (rbuf[i] == '\n') {
            int linelen = i;
            if (linelen > 0 && rbuf[linelen - 1] == '\r')
                linelen--;

            int cpylen = linelen < maxlen - 1 ? linelen : maxlen - 1;
            memcpy(buf, rbuf, cpylen);
            buf[cpylen] = '\0';

            /* 移除已处理的数据 */
            rlen -= (i + 1);
            memmove(rbuf, rbuf + i + 1, rlen);
            return 1;
        }
    }

    /* 从 socket 读取更多数据 */
    if (rlen < BUF_SIZE) {
        int n = (int)read(g_sock, rbuf + rlen, BUF_SIZE - rlen);
        if (n > 0) {
            rlen += n;
            /* 重试查找换行符 */
            for (i = rlen - n; i < rlen; i++) {
                if (rbuf[i] == '\n') {
                    int linelen = i;
                    if (linelen > 0 && rbuf[linelen - 1] == '\r')
                        linelen--;

                    int cpylen = linelen < maxlen - 1 ? linelen : maxlen - 1;
                    memcpy(buf, rbuf, cpylen);
                    buf[cpylen] = '\0';

                    rlen -= (i + 1);
                    memmove(rbuf, rbuf + i + 1, rlen);
                    return 1;
                }
            }
        } else if (n == 0) {
            return -1;  /* 连接关闭 */
        } else if (n < 0 && errno != EAGAIN && errno != EWOULDBLOCK) {
            return -1;
        }
    }

    return 0;  /* 没有完整行 */
}

/* ============ 地图渲染 ============ */

static void render_map(const char *map_data)
{
    const char *p = map_data;
    printf("\n" COLOR_BOLD "┌──────────────────────┐" COLOR_RESET "\n");

    while (*p) {
        if (*p == '\n') {
            printf(COLOR_BOLD "│" COLOR_RESET "\n");
            p++;
            continue;
        }
        if (p == map_data || *(p - 1) == '\n')
            printf(COLOR_BOLD "│" COLOR_RESET);

        switch (*p) {
            case '#':
                printf(COLOR_BG_BLUE "  " COLOR_RESET);
                break;
            case '@':
                printf(COLOR_BG_GREEN COLOR_BOLD "@@" COLOR_RESET);
                break;
            case '+':
                printf(COLOR_BG_YELLOW COLOR_BOLD "@@" COLOR_RESET);
                break;
            case '$':
                printf(COLOR_BG_CYAN COLOR_BOLD "$$" COLOR_RESET);
                break;
            case '*':
                printf(COLOR_BG_GREEN COLOR_BOLD "$$" COLOR_RESET);
                break;
            case '.':
                printf(COLOR_BG_YELLOW ".." COLOR_RESET);
                break;
            case ' ':
                printf("  ");
                break;
            default:
                printf("%c ", *p);
                break;
        }
        p++;
    }
    printf(COLOR_BOLD "└──────────────────────┘" COLOR_RESET "\n");
}

/* ============ 命令响应处理 ============ */

static void process_response(const char *line)
{
    /* 忽略空行 */
    if (strlen(line) == 0) return;

    /* 检测响应类型 */
    if (strncmp(line, "OK ", 3) == 0) {
        printf(COLOR_GREEN "%s" COLOR_RESET "\n", line + 3);
    } else if (strncmp(line, "ERR ", 4) == 0) {
        printf(COLOR_RED "[错误] %s" COLOR_RESET "\n", line + 4);
    } else if (strncmp(line, "CHAT ", 5) == 0) {
        printf(COLOR_CYAN "%s" COLOR_RESET "\n", line + 5);
    } else if (strncmp(line, "NOTICE ", 7) == 0) {
        printf(COLOR_YELLOW "[通知] %s" COLOR_RESET "\n", line + 7);
    } else if (strncmp(line, "MAP ", 4) == 0) {
        /* MAP 响应包含地图数据，接下来两行是尺寸和地图 */
        /* 实际地图数据在下一行开始 */
        g_in_game = 1;
        g_auto_refresh = 1;
    } else if (strncmp(line, "STATS ", 6) == 0) {
        printf(COLOR_MAGENTA "%s" COLOR_RESET "\n", line + 6);
    } else {
        printf("%s\n", line);
    }
}

/* 处理地图数据块 */
static int handle_map_section(void)
{
    char line[BUF_SIZE];
    int ret;

    /* 读取地图尺寸行, e.g. "MAP 10x8" */
    ret = read_line(line, sizeof(line));
    if (ret <= 0) return ret;

    int rows = 0, cols = 0;
    sscanf(line, " MAP %dx%d", &rows, &cols);
    if (rows > 0 && cols > 0) {
        /* 地图数据是固定行数 */
        char map_buf[BUF_SIZE];
        int map_len = 0;

        printf(CLEAR_SCREEN COLOR_BOLD "=== 推箱子游戏 ===\n" COLOR_RESET);
        printf(COLOR_CYAN "用户名: %s | 方向: w上 s下 a左 d右 | q=退出游戏\n" COLOR_RESET, g_username);
        printf(COLOR_YELLOW "图例: " COLOR_BG_BLUE "  " COLOR_RESET "墙 "
               COLOR_BG_GREEN "@@" COLOR_RESET "玩家 "
               COLOR_BG_CYAN "$$" COLOR_RESET "箱子 "
               COLOR_BG_YELLOW ".." COLOR_RESET "目标 "
               COLOR_BG_GREEN "$$" COLOR_RESET "归位\n");

        for (int r = 0; r < rows; r++) {
            ret = read_line(line, sizeof(line));
            if (ret <= 0) return ret;
            /* 去掉前导空格 */
            const char *trimmed = line;
            while (*trimmed == ' ') trimmed++;
            map_len += snprintf(map_buf + map_len, sizeof(map_buf) - map_len, "%s\n", trimmed);
        }

        render_map(map_buf);

        /* 读取 STATS 行 */
        ret = read_line(line, sizeof(line));
        if (ret > 0) {
            printf(COLOR_MAGENTA "%s" COLOR_RESET "\n", line);
        }
    }

    return 1;
}

/* ============ 显示提示符 ============ */

static void show_prompt(void)
{
    if (g_logged_in) {
        printf(COLOR_GREEN "\n[%s] > " COLOR_RESET, g_username);
    } else {
        printf(COLOR_CYAN "\n[未登录] > " COLOR_RESET);
    }
    fflush(stdout);
}

/* ============ 自动刷新游戏画面 ============ */

static void request_map(void)
{
    send_cmd("STATS");
    /* 这会导致服务器发送 STATS，但没有地图 */
    /* 更好的方法：重新 PLAY 同一关卡，但会重置游戏 */
    /* 实际上我们想刷新显示，所以发送 MOVE 同方向不动？ */
    /* 在游戏中，每走一步服务器会自动发地图，所以不需要额外刷新 */
}

/* ============ 主交互循环 ============ */

static void interaction_loop(void)
{
    char cmd[CMD_SIZE];
    char line[BUF_SIZE];
    int ret;
    int pending_cmd = 0;  /* 0=无, 1=有命令待发 */
    char next_cmd[CMD_SIZE];

    term_enter_raw();

    /* 先读取欢迎信息 */
    fd_set readfds;
    struct timeval tv;
    FD_ZERO(&readfds);
    FD_SET(g_sock, &readfds);
    tv.tv_sec = 0;
    tv.tv_usec = 100000;  /* 100ms */

    while (select(g_sock + 1, &readfds, NULL, NULL, &tv) > 0) {
        ret = read_line(line, sizeof(line));
        if (ret > 0) {
            process_response(line);
            if (strstr(line, "MAP ") == line) {
                handle_map_section();
            }
        } else if (ret < 0) {
            printf(COLOR_RED "\n[连接断开]\n" COLOR_RESET);
            goto done;
        }

        FD_ZERO(&readfds);
        FD_SET(g_sock, &readfds);
        tv.tv_sec = 0;
        tv.tv_usec = 100000;
    }

    show_prompt();

    while (g_running) {
        struct timeval tv;
        fd_set readfds;
        int maxfd;

        FD_ZERO(&readfds);
        FD_SET(g_sock, &readfds);
        FD_SET(STDIN_FILENO, &readfds);
        maxfd = (g_sock > STDIN_FILENO ? g_sock : STDIN_FILENO) + 1;

        tv.tv_sec = 0;
        tv.tv_usec = 100000;  /* 100ms 超时，允许刷新 */

        ret = select(maxfd, &readfds, NULL, NULL, &tv);
        if (ret < 0) {
            if (errno == EINTR) continue;
            break;
        }

        /* 处理服务器响应 */
        if (FD_ISSET(g_sock, &readfds)) {
            while (1) {
                ret = read_line(line, sizeof(line));
                if (ret > 0) {
                    if (strncmp(line, "MAP ", 4) == 0) {
                        /* 收到地图，新地图替换当前画面 */
                        handle_map_section();
                        /* 如果在游戏中，重新显示提示符 */
                        show_prompt();
                    } else {
                        process_response(line);
                    }
                } else if (ret < 0) {
                    printf(COLOR_RED "\n[连接断开]\n" COLOR_RESET);
                    g_running = 0;
                    break;
                } else {
                    break;  /* 没有更多数据 */
                }
            }
        }

        /* 处理键盘输入 */
        if (FD_ISSET(STDIN_FILENO, &readfds)) {
            char ch;
            int n = (int)read(STDIN_FILENO, &ch, 1);
            if (n > 0) {
                if (g_in_game && (ch == 'w' || ch == 'W' ||
                                  ch == 'a' || ch == 'A' ||
                                  ch == 's' || ch == 'S' ||
                                  ch == 'd' || ch == 'D')) {
                    /* 游戏中的方向键短命令 */
                    char dir[2] = {ch, '\0'};
                    if (ch >= 'A' && ch <= 'Z')
                        dir[0] = ch + 32;
                    send_cmd("MOVE %c", ch);
                } else if (g_in_game && ch == 'q') {
                    send_cmd("QUIT");
                    g_in_game = 0;
                    g_auto_refresh = 0;
                    printf(CLEAR_SCREEN);
                } else if (ch == '\n') {
                    /* 发送累积的命令 */
                    cmd[pending_cmd] = '\0';
                    if (pending_cmd > 0) {
                        printf("\n");
                        send_cmd("%s", cmd);
                        pending_cmd = 0;
                        /* 等待100ms让服务器响应 */
                        usleep(50000);
                    }
                    show_prompt();
                } else if (ch == 27) {  /* ESC */
                    g_in_game = 0;
                    g_auto_refresh = 0;
                    printf(CLEAR_SCREEN);
                    show_prompt();
                } else if (ch == 127 || ch == 8) {  /* Backspace */
                    if (pending_cmd > 0) {
                        pending_cmd--;
                        printf("\b \b");
                        fflush(stdout);
                    }
                } else if (ch >= 32 && ch < 127) {
                    if (pending_cmd < CMD_SIZE - 1) {
                        cmd[pending_cmd++] = ch;
                        putchar(ch);
                        fflush(stdout);
                    }
                }
            }
        }
    }

done:
    term_exit_raw();
}

/* ============ 信号处理 ============ */

static void sigint_handler(int sig)
{
    (void)sig;
    g_running = 0;
}

/* ============ 主函数 ============ */

int main(int argc, char *argv[])
{
    const char *host = "127.0.0.1";
    int port = 8888;

    printf(COLOR_BOLD CLEAR_SCREEN);
    printf("========================================\n");
    printf("    推箱子多人游戏客户端 v1.0\n");
    printf("========================================\n");

    if (argc > 1) host = argv[1];
    if (argc > 2) port = atoi(argv[2]);

    printf("连接 %s:%d ...\n", host, port);

    if (connect_server(host, port) < 0) {
        fprintf(stderr, "连接服务器失败\n");
        return 1;
    }

    printf("已连接!\n\n");

    /* 设置信号处理 */
    signal(SIGINT, sigint_handler);
    signal(SIGTERM, sigint_handler);

    printf("使用说明:\n");
    printf("  输入命令后按 Enter 发送\n");
    printf("  游戏中按 w/a/s/d 直接移动, q 退出游戏\n");
    printf("  ESC 清除画面返回输入模式\n");
    printf("  首次请登录: LOGIN 你的用户名\n\n");

    interaction_loop();

    if (g_sock >= 0) {
        close(g_sock);
    }

    printf("\n感谢游玩!\n");
    return 0;
}
