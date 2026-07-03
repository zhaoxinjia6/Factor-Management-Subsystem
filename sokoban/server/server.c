/*
 * server.c - 推箱子多人游戏服务器
 *
 * 使用技术:
 *   - epoll I/O 多路复用 (边缘触发)
 *   - 非阻塞 socket
 *   - TCP IPv4
 *   - 文本行协议 (应用层)
 *
 * 编译: gcc -Wall -O2 -o sokoban_server server.c game.c levels.c client.c -lpthread
 * 运行: ./sokoban_server <端口号>
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <signal.h>
#include <stdarg.h>
#include <sys/epoll.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>

#include "game.h"
#include "client.h"
#include "levels.h"

/* ============ 配置 ============ */
#define DEFAULT_PORT     8888
#define MAX_EVENTS       1024
#define BACKLOG          128
#define MAX_CLIENTS      512
#define BUF_SIZE         BUFFER_SIZE
#define CMD_MAX          256

/* ============ 全局状态 ============ */
static int g_server_fd = -1;          /* 服务器 socket */
static int g_epoll_fd = -1;           /* epoll 实例 */
static volatile int g_running = 1;     /* 运行标志 */
static ClientManager g_clients;        /* 客户端管理器 */
static LevelManager g_levels;          /* 关卡管理器 */
static pthread_mutex_t g_mutex = PTHREAD_MUTEX_INITIALIZER;  /* 全局锁 */

/* ============ 工具函数 ============ */

/* 设置非阻塞 */
static int set_nonblocking(int fd)
{
    int flags = fcntl(fd, F_GETFL, 0);
    if (flags == -1) return -1;
    return fcntl(fd, F_SETFL, flags | O_NONBLOCK);
}

/* 发送响应给客户端 */
static int send_response(ClientNode *c, const char *fmt, ...)
{
    char buf[BUF_SIZE];
    va_list ap;
    int len;

    va_start(ap, fmt);
    len = vsnprintf(buf, sizeof(buf), fmt, ap);
    va_end(ap);

    if (len < 0 || len >= (int)sizeof(buf))
        return -1;

    /* 加 \r\n 结束 */
    int total = len + 2;
    if (total > BUF_SIZE) total = BUF_SIZE;

    pthread_mutex_lock(&g_mutex);
    if (c->wlen + total < BUFFER_SIZE) {
        memcpy(c->wbuf + c->wlen, buf, len);
        c->wlen += len;
        c->wbuf[c->wlen++] = '\r';
        c->wbuf[c->wlen++] = '\n';
    }
    pthread_mutex_unlock(&g_mutex);
    return 0;
}

/* 构建地图响应 */
static void send_map(ClientNode *c)
{
    char map_buf[BUF_SIZE];
    char stats_buf[256];

    game_render_map(&c->game, map_buf, sizeof(map_buf));
    game_get_stats(&c->game, stats_buf, sizeof(stats_buf));

    send_response(c, "MAP %dx%d\n%sSTATS %s",
        c->game.level->rows, c->game.level->cols,
        map_buf, stats_buf);
}

/* ============ 命令处理 ============ */

static void handle_login(ClientNode *c, const char *arg)
{
    char username[MAX_USERNAME];

    if (c->state != ST_LOGIN) {
        send_response(c, "ERR 已经登录");
        return;
    }

    /* 提取用户名（第一个单词） */
    sscanf(arg, "%31s", username);
    if (strlen(username) < 2) {
        send_response(c, "ERR 用户名至少2个字符");
        return;
    }

    pthread_mutex_lock(&g_mutex);
    /* 检查用户名是否已被使用 */
    if (client_manager_find_by_name(&g_clients, username)) {
        pthread_mutex_unlock(&g_mutex);
        send_response(c, "ERR 用户名已被使用");
        return;
    }

    strncpy(c->username, username, MAX_USERNAME - 1);
    c->state = ST_LOBBY;
    pthread_mutex_unlock(&g_mutex);

    send_response(c, "OK 欢迎, %s! 输入 HELP 查看命令列表", username);

    /* 广播用户上线 */
    {
        char broadcast[256];
        snprintf(broadcast, sizeof(broadcast), "CHAT *** %s 加入了游戏 ***", username);
        pthread_mutex_lock(&g_mutex);
        client_manager_broadcast(&g_clients, broadcast, c);
        pthread_mutex_unlock(&g_mutex);
    }
}

static void handle_logout(ClientNode *c)
{
    if (c->state == ST_LOGIN) {
        send_response(c, "OK 再见");
        return;
    }

    {
        char broadcast[256];
        snprintf(broadcast, sizeof(broadcast), "CHAT *** %s 离开了游戏 ***", c->username);
        pthread_mutex_lock(&g_mutex);
        client_manager_broadcast(&g_clients, broadcast, c);
        pthread_mutex_unlock(&g_mutex);
    }

    c->state = ST_LOGIN;
    c->username[0] = '\0';
    send_response(c, "OK 已注销");
}

static void handle_list(ClientNode *c)
{
    char users[BUF_SIZE];
    pthread_mutex_lock(&g_mutex);
    client_manager_list_users(&g_clients, users, sizeof(users));
    pthread_mutex_unlock(&g_mutex);
    /* 直接发送，不用 send_response 包装 */
    char resp[BUF_SIZE + 64];
    snprintf(resp, sizeof(resp), "OK %s", users);
    send_response(c, "%s", resp);
}

static void handle_levels(ClientNode *c)
{
    char resp[BUF_SIZE];
    int pos = 0;
    int i;

    pos += snprintf(resp + pos, sizeof(resp) - pos,
        "OK 共 %d 个关卡:\n", g_levels.count);
    for (i = 0; i < g_levels.count; i++) {
        pos += snprintf(resp + pos, sizeof(resp) - pos,
            "  %2d. %s\n", g_levels.levels[i].id, g_levels.levels[i].name);
    }
    send_response(c, "%s", resp);
}

static void handle_play(ClientNode *c, const char *arg)
{
    int level_id;
    const Level *level;

    if (c->state == ST_LOGIN) {
        send_response(c, "ERR 请先登录");
        return;
    }

    if (sscanf(arg, "%d", &level_id) != 1) {
        send_response(c, "ERR 用法: PLAY <关卡号>");
        return;
    }

    pthread_mutex_lock(&g_mutex);
    level = level_manager_get(&g_levels, level_id);
    pthread_mutex_unlock(&g_mutex);

    if (!level) {
        send_response(c, "ERR 关卡 %d 不存在", level_id);
        return;
    }

    game_init(&c->game, level);
    c->state = ST_PLAYING;
    send_response(c, "OK 开始关卡: %s", level->name);
    send_map(c);
}

static void handle_move(ClientNode *c, const char *arg)
{
    Direction dir;
    int result;

    if (c->state != ST_PLAYING) {
        send_response(c, "ERR 未在游戏中，请先 PLAY <关卡号>");
        return;
    }

    dir = direction_from_char(arg[0]);
    if (dir == DIR_INVALID) {
        send_response(c, "ERR 方向无效 (w=上 s=下 a=左 d=右)");
        return;
    }

    pthread_mutex_lock(&g_mutex);
    result = game_move(&c->game, dir);
    pthread_mutex_unlock(&g_mutex);

    switch (result) {
        case 0:
            send_response(c, "OK 移动成功");
            send_map(c);
            break;
        case 1:
            send_response(c, "OK 推动箱子! 推箱次数: %d", c->game.push_count);
            send_map(c);
            break;
        case 2:
            send_response(c, "OK 恭喜通关! 移动: %d 次, 推箱: %d 次",
                c->game.move_count, c->game.push_count);
            send_map(c);
            c->state = ST_LOBBY;
            {
                char broadcast[256];
                snprintf(broadcast, sizeof(broadcast),
                    "CHAT *** %s 通关了 '%s'! (移动%d次) ***",
                    c->username, c->game.level->name, c->game.move_count);
                pthread_mutex_lock(&g_mutex);
                client_manager_broadcast(&g_clients, broadcast, c);
                pthread_mutex_unlock(&g_mutex);
            }
            break;
        case -1:
            send_response(c, "ERR 撞墙了!");
            break;
        case -2:
            send_response(c, "ERR 箱子被阻挡!");
            break;
        case -3:
            send_response(c, "ERR 游戏未开始");
            break;
    }
}

static void handle_quit(ClientNode *c)
{
    if (c->state == ST_PLAYING) {
        c->state = ST_LOBBY;
        send_response(c, "OK 已退出当前游戏");
    } else {
        send_response(c, "ERR 当前没有进行中的游戏");
    }
}

static void handle_chat(ClientNode *c, const char *arg)
{
    if (c->state == ST_LOGIN) {
        send_response(c, "ERR 请先登录");
        return;
    }
    if (strlen(arg) == 0) {
        send_response(c, "ERR 聊天消息不能为空");
        return;
    }

    char msg[512];
    snprintf(msg, sizeof(msg), "CHAT [%s] %s", c->username, arg);

    pthread_mutex_lock(&g_mutex);
    client_manager_broadcast(&g_clients, msg, NULL);
    pthread_mutex_unlock(&g_mutex);
}

static void handle_help(ClientNode *c)
{
    const char *help_msg =
        "OK 可用命令:\n"
        "  LOGIN <用户名>    - 登录\n"
        "  LOGOUT           - 注销\n"
        "  LIST             - 查看在线用户\n"
        "  LEVELS           - 查看关卡列表\n"
        "  PLAY <关卡号>    - 开始游戏\n"
        "  MOVE <方向>      - 移动 (w上 s下 a左 d右)\n"
        "  QUIT             - 退出当前游戏\n"
        "  CHAT <消息>      - 聊天\n"
        "  STATS            - 查看当前游戏统计\n"
        "  HELP             - 显示此帮助\n"
        "  WHOAMI           - 查看当前用户";
    send_response(c, "%s", help_msg);
}

static void handle_stats(ClientNode *c)
{
    if (c->state != ST_PLAYING) {
        send_response(c, "ERR 未在游戏中");
        return;
    }
    char stats[256];
    game_get_stats(&c->game, stats, sizeof(stats));
    send_response(c, "OK %s", stats);
}

static void handle_whoami(ClientNode *c)
{
    if (c->state == ST_LOGIN)
        send_response(c, "OK 未登录");
    else
        send_response(c, "OK %s [%s]",
            c->username,
            c->state == ST_PLAYING ? "游戏中" : "大厅");
}

/* ============ 请求解析与分发 ============ */

static void process_command(ClientNode *c, const char *line)
{
    char cmd[CMD_MAX];
    const char *arg;

    /* 跳过空白 */
    while (*line == ' ' || *line == '\t') line++;

    /* 空行忽略 */
    if (*line == '\0' || *line == '\r' || *line == '\n')
        return;

    /* 提取命令和参数 */
    int i = 0;
    while (line[i] && line[i] != ' ' && line[i] != '\t' && i < CMD_MAX - 1) {
        cmd[i] = line[i];
        i++;
    }
    cmd[i] = '\0';

    arg = line + i;
    while (*arg == ' ' || *arg == '\t') arg++;

    /* 命令分发 */
    if (strcasecmp(cmd, "LOGIN") == 0)     handle_login(c, arg);
    else if (strcasecmp(cmd, "LOGOUT") == 0)   handle_logout(c);
    else if (strcasecmp(cmd, "LIST") == 0)     handle_list(c);
    else if (strcasecmp(cmd, "LEVELS") == 0)   handle_levels(c);
    else if (strcasecmp(cmd, "PLAY") == 0)     handle_play(c, arg);
    else if (strcasecmp(cmd, "MOVE") == 0)     handle_move(c, arg);
    else if (strcasecmp(cmd, "QUIT") == 0)     handle_quit(c);
    else if (strcasecmp(cmd, "CHAT") == 0)     handle_chat(c, arg);
    else if (strcasecmp(cmd, "HELP") == 0)     handle_help(c);
    else if (strcasecmp(cmd, "STATS") == 0)    handle_stats(c);
    else if (strcasecmp(cmd, "WHOAMI") == 0)   handle_whoami(c);
    else if (strcasecmp(cmd, "EXIT") == 0) {
        send_response(c, "OK 再见!");
        /* 关闭连接由主循环处理 */
    } else {
        send_response(c, "ERR 未知命令 '%s', 输入 HELP 查看可用命令", cmd);
    }
}

/* 处理一行完整的数据 */
static void handle_client_data(ClientNode *c)
{
    char *p, *line;

    p = c->rbuf;
    while (p < c->rbuf + c->rlen) {
        /* 查找行结束符 */
        line = p;
        while (p < c->rbuf + c->rlen && *p != '\n') p++;

        if (p >= c->rbuf + c->rlen) {
            /* 不完整的行，保留 */
            int remaining = c->rlen - (int)(line - c->rbuf);
            if (remaining > 0 && line > c->rbuf) {
                memmove(c->rbuf, line, remaining);
                c->rlen = remaining;
            }
            return;
        }

        /* 完整的一行 */
        *p = '\0';  /* 替换换行符为结束符 */

        pthread_mutex_lock(&g_mutex);
        process_command(c, line);
        pthread_mutex_unlock(&g_mutex);

        p++;  /* 跳过 \n */
    }

    c->rlen = 0;
}

/* ============ 网络 I/O 处理 ============ */

/* 处理新连接 */
static void handle_accept(void)
{
    struct sockaddr_in addr;
    socklen_t addrlen = sizeof(addr);
    int client_fd;
    struct epoll_event ev;

    client_fd = accept(g_server_fd, (struct sockaddr *)&addr, &addrlen);
    if (client_fd < 0) {
        if (errno != EAGAIN && errno != EWOULDBLOCK)
            perror("accept");
        return;
    }

    /* 设置为非阻塞 */
    if (set_nonblocking(client_fd) < 0) {
        close(client_fd);
        return;
    }

    /* 注册到 epoll */
    ev.events = EPOLLIN | EPOLLET | EPOLLRDHUP;
    ev.data.fd = client_fd;
    if (epoll_ctl(g_epoll_fd, EPOLL_CTL_ADD, client_fd, &ev) < 0) {
        perror("epoll_ctl add client");
        close(client_fd);
        return;
    }

    pthread_mutex_lock(&g_mutex);
    ClientNode *c = client_manager_add(&g_clients, client_fd, &addr);
    pthread_mutex_unlock(&g_mutex);

    if (!c) {
        close(client_fd);
        return;
    }

    char ip[INET_ADDRSTRLEN];
    inet_ntop(AF_INET, &addr.sin_addr, ip, sizeof(ip));
    printf("[连接] %s:%d (fd=%d)\n", ip, ntohs(addr.sin_port), client_fd);

    /* 发送欢迎信息 */
    send_response(c, "OK 欢迎来到推箱子服务器! 输入 HELP 查看命令, LOGIN <用户名> 登录");
}

/* 读取客户端数据 */
static void handle_read(ClientNode *c)
{
    int n;
    char tmp[BUF_SIZE];

    while (1) {
        n = (int)read(c->fd, tmp, sizeof(tmp) - 1);
        if (n > 0) {
            tmp[n] = '\0';
            pthread_mutex_lock(&g_mutex);
            if (c->rlen + n < BUFFER_SIZE) {
                memcpy(c->rbuf + c->rlen, tmp, n);
                c->rlen += n;
            }
            pthread_mutex_unlock(&g_mutex);
        } else if (n == 0) {
            /* 连接关闭 */
            return;
        } else {
            if (errno == EAGAIN || errno == EWOULDBLOCK)
                break;  /* 数据读完 */
            return;     /* 错误 */
        }
    }

    /* 处理收到的数据 */
    pthread_mutex_lock(&g_mutex);
    handle_client_data(c);
    pthread_mutex_unlock(&g_mutex);
}

/* 向客户端写数据 */
static void handle_write(ClientNode *c)
{
    if (c->wpos >= c->wlen) {
        c->wpos = 0;
        c->wlen = 0;
        return;
    }

    int n = (int)write(c->fd, c->wbuf + c->wpos, c->wlen - c->wpos);
    if (n > 0) {
        c->wpos += n;
        if (c->wpos >= c->wlen) {
            c->wpos = 0;
            c->wlen = 0;
        }
    } else if (n < 0) {
        if (errno != EAGAIN && errno != EWOULDBLOCK) {
            /* 写错误，关闭连接 */
        }
    }
}

/* 关闭客户端连接 */
static void close_client(int fd)
{
    printf("[断开] fd=%d\n", fd);

    pthread_mutex_lock(&g_mutex);
    /* 广播离开消息 */
    ClientNode *c = client_manager_find(&g_clients, fd);
    if (c && c->state != ST_LOGIN) {
        char msg[256];
        snprintf(msg, sizeof(msg), "CHAT *** %s 断开了连接 ***", c->username);
        client_manager_broadcast(&g_clients, msg, c);
    }
    client_manager_remove(&g_clients, fd);
    pthread_mutex_unlock(&g_mutex);

    epoll_ctl(g_epoll_fd, EPOLL_CTL_DEL, fd, NULL);
    close(fd);
}

/* ============ 信号处理 ============ */

static void signal_handler(int sig)
{
    if (sig == SIGINT || sig == SIGTERM) {
        printf("\n[收到信号 %d] 正在关闭服务器...\n", sig);
        g_running = 0;
    }
}

static void setup_signal_handlers(void)
{
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = signal_handler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
    sigaction(SIGINT, &sa, NULL);
    sigaction(SIGTERM, &sa, NULL);

    /* 忽略 SIGPIPE，防止写已关闭连接时进程退出 */
    sa.sa_handler = SIG_IGN;
    sigaction(SIGPIPE, &sa, NULL);
}

/* ============ 统计打印线程 ============ */

static void *stats_thread(void *arg)
{
    (void)arg;
    while (g_running) {
        sleep(60);
        if (g_running) {
            pthread_mutex_lock(&g_mutex);
            printf("[状态] 在线: %d 客户端\n", g_clients.count);
            pthread_mutex_unlock(&g_mutex);
        }
    }
    return NULL;
}

/* ============ EPOLL 事件循环（主线程） ============ */

static void event_loop(void)
{
    struct epoll_event events[MAX_EVENTS];
    int nfds, i;

    printf("[服务器] 开始事件循环\n");

    while (g_running) {
        nfds = epoll_wait(g_epoll_fd, events, MAX_EVENTS, 1000);
        if (nfds < 0) {
            if (errno == EINTR) continue;
            perror("epoll_wait");
            break;
        }

        for (i = 0; i < nfds; i++) {
            int fd = events[i].data.fd;

            /* 处理客户端挂起/错误 */
            if (events[i].events & (EPOLLRDHUP | EPOLLHUP | EPOLLERR)) {
                close_client(fd);
                continue;
            }

            /* 新连接 */
            if (fd == g_server_fd) {
                handle_accept();
                continue;
            }

            /* 可读事件 */
            if (events[i].events & EPOLLIN) {
                pthread_mutex_lock(&g_mutex);
                ClientNode *c = client_manager_find(&g_clients, fd);
                pthread_mutex_unlock(&g_mutex);

                if (c) {
                    handle_read(c);
                    /* 检查连接是否在该处理中关闭 */
                    pthread_mutex_lock(&g_mutex);
                    if (!client_manager_find(&g_clients, fd)) {
                        pthread_mutex_unlock(&g_mutex);
                        continue;
                    }
                    pthread_mutex_unlock(&g_mutex);

                    /* 注册写事件（如果有数据要发） */
                    pthread_mutex_lock(&g_mutex);
                    c = client_manager_find(&g_clients, fd);
                    if (c && c->wlen > 0) {
                        struct epoll_event ev;
                        ev.events = EPOLLIN | EPOLLOUT | EPOLLET | EPOLLRDHUP;
                        ev.data.fd = fd;
                        epoll_ctl(g_epoll_fd, EPOLL_CTL_MOD, fd, &ev);
                    }
                    pthread_mutex_unlock(&g_mutex);
                }
            }

            /* 可写事件 */
            if (events[i].events & EPOLLOUT) {
                pthread_mutex_lock(&g_mutex);
                ClientNode *c = client_manager_find(&g_clients, fd);
                pthread_mutex_unlock(&g_mutex);

                if (c) {
                    handle_write(c);

                    /* 写完数据后，改回只读模式 */
                    pthread_mutex_lock(&g_mutex);
                    if (c->wlen == 0) {
                        struct epoll_event ev;
                        ev.events = EPOLLIN | EPOLLET | EPOLLRDHUP;
                        ev.data.fd = fd;
                        epoll_ctl(g_epoll_fd, EPOLL_CTL_MOD, fd, &ev);
                    }
                    pthread_mutex_unlock(&g_mutex);
                } else {
                    /* 客户端已不存在，关闭 */
                    epoll_ctl(g_epoll_fd, EPOLL_CTL_DEL, fd, NULL);
                    close(fd);
                }
            }
        }
    }
}

/* ============ 启动服务器 ============ */

static int start_server(int port)
{
    struct sockaddr_in addr;
    int optval = 1;

    g_server_fd = socket(AF_INET, SOCK_STREAM | SOCK_NONBLOCK, 0);
    if (g_server_fd < 0) {
        perror("socket");
        return -1;
    }

    /* 地址复用 */
    if (setsockopt(g_server_fd, SOL_SOCKET, SO_REUSEADDR,
                   &optval, sizeof(optval)) < 0) {
        perror("setsockopt SO_REUSEADDR");
        close(g_server_fd);
        return -1;
    }

    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_port = htons(port);

    if (bind(g_server_fd, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        perror("bind");
        close(g_server_fd);
        return -1;
    }

    if (listen(g_server_fd, BACKLOG) < 0) {
        perror("listen");
        close(g_server_fd);
        return -1;
    }

    return 0;
}

/* ============ 主函数 ============ */

int main(int argc, char *argv[])
{
    int port = DEFAULT_PORT;
    pthread_t stats_tid;

    if (argc > 1) {
        port = atoi(argv[1]);
        if (port <= 0 || port > 65535) {
            fprintf(stderr, "端口号无效: %s\n", argv[1]);
            return 1;
        }
    }

    printf("====================================\n");
    printf("  推箱子多人游戏服务器 v1.0\n");
    printf("====================================\n");

    /* 初始化关卡 */
    level_manager_init(&g_levels);
    printf("[关卡] 已加载 %d 个关卡\n", g_levels.count);

    /* 初始化客户端管理器 */
    client_manager_init(&g_clients);

    /* 启动服务器 */
    if (start_server(port) < 0) {
        fprintf(stderr, "服务器启动失败\n");
        return 1;
    }
    printf("[网络] 服务器监听 0.0.0.0:%d\n", port);

    /* 创建 epoll */
    g_epoll_fd = epoll_create1(0);
    if (g_epoll_fd < 0) {
        perror("epoll_create1");
        close(g_server_fd);
        return 1;
    }

    /* 注册服务器 socket 到 epoll */
    struct epoll_event ev;
    ev.events = EPOLLIN | EPOLLET;
    ev.data.fd = g_server_fd;
    if (epoll_ctl(g_epoll_fd, EPOLL_CTL_ADD, g_server_fd, &ev) < 0) {
        perror("epoll_ctl add server");
        close(g_server_fd);
        close(g_epoll_fd);
        return 1;
    }

    /* 设置信号处理 */
    setup_signal_handlers();

    /* 启动统计线程 */
    pthread_create(&stats_tid, NULL, stats_thread, NULL);

    printf("[状态] 服务器运行中...\n");
    printf("[命令] 按 Ctrl+C 停止服务器\n\n");

    /* 主事件循环 */
    event_loop();

    /* 清理 */
    printf("\n[关闭] 正在关闭服务器...\n");

    g_running = 0;
    pthread_join(stats_tid, NULL);

    close(g_epoll_fd);
    close(g_server_fd);

    pthread_mutex_lock(&g_mutex);
    /* 向所有客户端发送关闭消息 */
    ClientNode *p = g_clients.head;
    while (p) {
        const char *msg = "NOTICE 服务器正在关闭...\r\n";
        write(p->fd, msg, strlen(msg));
        p = p->next;
    }
    client_manager_destroy(&g_clients);
    pthread_mutex_unlock(&g_mutex);

    pthread_mutex_destroy(&g_mutex);

    printf("[关闭] 服务器已停止\n");
    return 0;
}
